package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyCapabilities;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyProfile;
import com.magmaguy.elitemobs.api.mind.EliteMindModule;
import com.magmaguy.elitemobs.api.mind.EliteMindPowerLoadout;
import com.magmaguy.elitemobs.api.mind.EliteMindProgram;
import com.magmaguy.elitemobs.api.mind.EliteMindService;
import com.magmaguy.elitemobs.api.mind.EliteMindSnapshot;
import com.magmaguy.elitemobs.api.mind.EliteMindSpawnRequest;
import com.magmaguy.elitemobs.api.mind.EliteMindTransferPolicy;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnContext;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnProvider;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnReplacement;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnSuppression;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyCapabilities;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyLocomotion;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyProfile;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyRehydration;
import com.magmaguy.elitemobs.magmacore.ai.MindHandle;
import com.magmaguy.elitemobs.magmacore.ai.MindHost;
import com.magmaguy.elitemobs.magmacore.ai.MindProgram;
import com.magmaguy.elitemobs.magmacore.ai.MobBody;
import com.magmaguy.elitemobs.magmacore.ai.StateTransfer;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.plugin.Plugin;

final class EliteMindServiceImpl implements EliteMindService, Listener {
   private final EliteMindCatalog catalog = new EliteMindCatalog();
   private final EliteNaturalSpawnProviderRegistry naturalSpawnProviders = new EliteNaturalSpawnProviderRegistry();
   private final MindHost mindHost;
   private final EliteLuaPowerServiceImpl luaPowerService;
   private final EliteMindBodyCapabilities bodyCapabilities;
   private boolean closed;

   EliteMindServiceImpl(MindHost mindHost, EliteLuaPowerServiceImpl luaPowerService) {
      this.mindHost = Objects.requireNonNull(mindHost, "mindHost");
      this.luaPowerService = Objects.requireNonNull(luaPowerService, "luaPowerService");
      this.bodyCapabilities = toPublicCapabilities(mindHost.bodyCapabilities());

      try {
         this.catalog.loadBehaviors(MetadataHandler.PLUGIN);
      } catch (RuntimeException var4) {
         this.catalog.clear();
         throw var4;
      }
   }

   LivingEntity spawnBehaviorBody(CustomBossEntity actor, Location location, String filename, Consumer<LivingEntity> configure) {
      this.requireAvailable();
      requireServerThread();
      CustomBossesConfigFields fields = actor.getCustomBossesConfigFields();
      EliteMobProperties properties = EliteMobProperties.getPluginData(fields.getEntityType());
      EliteMindCatalog.Entry entry = this.catalog.requireBehavior(filename);
      MindBodyProfile profile = new MindBodyProfile(
         MindBodyLocomotion.valueOf(properties.getLocomotion().name()), fields.getScale(), true, fields.getEntityType().getKey().toString()
      );
      MindHandle handle = this.mindHost.open(actor.getEliteUUID(), entry.instantiate());
      boolean configured = false;

      LivingEntity var12;
      try {
         MobBody body = this.mindHost.spawnBody(location, profile, preparedBody -> {
            CrashFix.persistentTracker(preparedBody.entity());
            actor.setEliteMindBinding(new EliteMindBinding(entry, handle, preparedBody));
            configure.accept(preparedBody.entity());
         });
         configured = true;
         var12 = body.entity();
      } finally {
         if (!configured) {
            clearProgramQuietly(actor, "failed configured behavior spawn");
            handle.close();
         }
      }

      return var12;
   }

   EliteEntity spawnBehaviorElite(Location location, EntityType type, int level, SpawnReason reason, Set<PowersConfigFields> explicitPowers) {
      EliteMobProperties properties = EliteMobProperties.getPluginData(type);
      EliteMindCatalog.Entry entry = this.catalog.requireBehavior(properties.getBehavior());
      EliteMindPowerLoadout loadout = explicitPowers == null ? EliteMindPowerLoadout.randomizedPowers() : EliteMindPowerLoadout.exact(List.of());
      return this.spawnResolved(
         MetadataHandler.PLUGIN,
         entry,
         location,
         level,
         false,
         EliteMindBodyProfile.forCarrier(type.getKey(), properties.getLocomotion()),
         loadout,
         reason,
         actor -> {
            if (reason == SpawnReason.CUSTOM) {
               actor.setNaturalEntity(true);
               actor.setMaxHealth();
            }

            if (explicitPowers != null) {
               actor.applyPowers(new HashSet<>(explicitPowers));
            }
         }
      );
   }

   void validateBehavior(String filename) {
      this.catalog.requireBehavior(filename);
   }

   static void detachBehavior(EliteEntity actor) {
      clearProgramQuietly(actor, "behavior body disposal");
   }

   @Override
   public EliteMindModule registerLuaModule(Plugin owner, NamespacedKey key, long revision, String source) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(owner, "owner");
      if (!owner.isEnabled()) {
         throw new IllegalStateException("Mind owner plugin is not enabled");
      } else {
         return this.catalog.registerModule(owner, key, revision, source);
      }
   }

   @Override
   public EliteMindProgram registerLuaProgram(Plugin owner, NamespacedKey key, long revision, String source) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(owner, "owner");
      if (!owner.isEnabled()) {
         throw new IllegalStateException("Mind owner plugin is not enabled");
      } else {
         return this.catalog.register(owner, key, revision, source);
      }
   }

   @Override
   public void unregisterOwner(Plugin owner) {
      this.requireAvailable();
      requireServerThread();
      this.unregisterOwnerInternal(Objects.requireNonNull(owner, "owner"));
   }

   @Override
   public EliteMindBodyCapabilities bodyCapabilities() {
      this.requireAvailable();
      return this.bodyCapabilities;
   }

   @Override
   public EliteEntity spawn(EliteMindSpawnRequest request) {
      return this.spawn(request, SpawnReason.CUSTOM);
   }

   private EliteEntity spawn(EliteMindSpawnRequest request, SpawnReason spawnReason) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(request, "request");
      Objects.requireNonNull(spawnReason, "spawnReason");
      if (!request.owner().isEnabled()) {
         throw new IllegalStateException("Mind owner plugin is not enabled");
      } else {
         EliteMindCatalog.Entry entry = this.catalog.require(request.programKey());
         if (entry.owner() != request.owner()) {
            throw new IllegalArgumentException("Plugin " + request.owner().getName() + " does not own mind program " + request.programKey());
         } else {
            return this.spawnResolved(
               request.owner(),
               entry,
               request.location(),
               request.level(),
               request.persistent(),
               request.bodyProfile(),
               request.powerLoadout(),
               spawnReason,
               ignored -> {}
            );
         }
      }
   }

   EliteEntity spawnInternal(InternalMindActorSpawnRequest request) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(request, "request");
      if (!request.systemOwner().isEnabled()) {
         throw new IllegalStateException("Mind owner plugin is not enabled");
      } else {
         NamespacedKey key = NamespacedKey.fromString(request.program().identifier());
         if (key == null) {
            throw new IllegalArgumentException("Internal Mind program key is invalid");
         } else {
            EliteMindProgramEntry entry = new EliteMindServiceImpl.InternalProgramEntry(
               request.systemOwner(), new EliteMindProgram(key, request.program().revision()), request.program()
            );
            return request.customBoss() != null
               ? this.spawnConfigured(request, entry)
               : this.spawnResolved(
                  request.systemOwner(),
                  entry,
                  request.location(),
                  request.level(),
                  false,
                  request.bodyProfile(),
                  EliteMindPowerLoadout.exact(List.of()),
                  SpawnReason.CUSTOM,
                  eliteEntity -> {
                     eliteEntity.addCustomData(new NamespacedKey(request.systemOwner(), "native_mind_player_owner"), request.playerOwnerId());
                     request.initializer().accept(eliteEntity);
                  }
               );
         }
      }
   }

   private EliteEntity spawnResolved(
      Plugin owner,
      EliteMindProgramEntry entry,
      Location location,
      int level,
      boolean persistent,
      EliteMindBodyProfile bodyProfile,
      EliteMindPowerLoadout powerLoadout,
      SpawnReason spawnReason,
      Consumer<EliteEntity> initializer
   ) {
      MobBody body = null;
      EliteEntity eliteEntity = null;
      MindHandle handle = null;
      EliteMindBinding binding = null;
      boolean committed = false;

      EliteEntity var15;
      try {
         this.bodyCapabilities.validate(bodyProfile);
         body = this.mindHost.spawnBody(location, toNativeProfile(bodyProfile));
         CrashFix.persistentTracker(body.entity());
         eliteEntity = new EliteEntity();
         eliteEntity.prepareMindActor(body.entity(), level, spawnReason, powerLoadout.randomized());
         if (!powerLoadout.randomized()) {
            this.luaPowerService.setPreparedPowers(owner, eliteEntity, powerLoadout.powerKeys());
         }

         eliteEntity.setPersistent(persistent);
         initializer.accept(eliteEntity);
         handle = this.mindHost.open(eliteEntity.getEliteUUID(), entry.instantiate());
         binding = new EliteMindBinding(entry, handle, body);
         eliteEntity.setEliteMindBinding(binding);
         if (!eliteEntity.commitPreparedMindSpawn()) {
            throw new IllegalStateException("EliteMobSpawnEvent rejected the native mind actor");
         }

         committed = true;
         var15 = eliteEntity;
      } finally {
         if (!committed) {
            rollbackSpawn(eliteEntity, body, handle, binding);
         }
      }

      return var15;
   }

   private EliteEntity spawnConfigured(InternalMindActorSpawnRequest request, EliteMindProgramEntry entry) {
      CustomBossesConfigFields config = request.customBoss();
      if (!config.isEnabled()) {
         throw new IllegalArgumentException("Custom boss is disabled: " + config.getFilename());
      } else if (!config.getEntityType().getKey().equals(request.bodyProfile().carrierType())) {
         throw new IllegalArgumentException("Native body must match custom boss entityType");
      } else {
         this.bodyCapabilities.validate(request.bodyProfile());
         CustomBossEntity actor = new CustomBossEntity(config);
         actor.setLevel(config.getLevel() > 0 ? config.getLevel() : request.level());
         actor.setPersistent(false);
         actor.addCustomData(new NamespacedKey(request.systemOwner(), "native_mind_player_owner"), request.playerOwnerId());
         MobBody[] body = new MobBody[1];
         MindHandle[] handle = new MindHandle[1];
         EliteMindBinding[] binding = new EliteMindBinding[1];
         boolean committed = false;

         CustomBossEntity var9;
         try {
            actor.spawnWithBody(request.location(), configure -> {
               handle[0] = this.mindHost.open(actor.getEliteUUID(), entry.instantiate());
               body[0] = this.mindHost.spawnBody(request.location(), toNativeProfile(request.bodyProfile()), preparedBody -> {
                  CrashFix.persistentTracker(preparedBody.entity());
                  binding[0] = new EliteMindBinding(entry, handle[0], preparedBody);
                  actor.setEliteMindBinding(binding[0]);
                  configure.accept(preparedBody.entity());
               });
               return body[0].entity();
            });
            if (!actor.exists() || EntityTracker.getEliteMobEntities().get(actor.getEliteUUID()) != actor) {
               throw new IllegalStateException("Custom boss spawn was rejected: " + config.getFilename());
            }

            request.initializer().accept(actor);
            committed = true;
            var9 = actor;
         } finally {
            if (!committed) {
               try {
                  actor.remove(RemovalReason.ENTITY_REPLACEMENT);
               } finally {
                  rollbackSpawn(actor, body[0], handle[0], binding[0]);
               }
            }
         }

         return var9;
      }
   }

   @Override
   public void registerNaturalSpawnProvider(Plugin owner, EliteNaturalSpawnProvider provider) {
      this.requireAvailable();
      requireServerThread();
      this.naturalSpawnProviders.register(owner, provider);
   }

   boolean hasNaturalSpawnProviders() {
      return !this.closed && !this.naturalSpawnProviders.isEmpty();
   }

   boolean replaceNaturalSpawn(CreatureSpawnEvent event, int suggestedLevel) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(event, "event");
      EliteNaturalSpawnContext context = new EliteNaturalSpawnContext(event.getEntity(), event.getSpawnReason(), suggestedLevel);
      EliteNaturalSpawnProviderRegistry.Selection selection = this.naturalSpawnProviders.select(context).orElse(null);
      if (selection == null) {
         return false;
      } else if (selection.claim() instanceof EliteNaturalSpawnSuppression) {
         return true;
      } else {
         EliteNaturalSpawnReplacement replacement = (EliteNaturalSpawnReplacement)selection.claim();
         EliteMindSpawnRequest requested = replacement.request();
         if (requested.owner() != selection.owner()) {
            throw new IllegalArgumentException("Natural-spawn claim owner does not own its Mind request");
         } else if (!requested.bodyProfile().carrierType().equals(event.getEntityType().getKey())) {
            throw new IllegalArgumentException(
               "Natural-spawn replacement carrier " + requested.bodyProfile().carrierType() + " does not match source " + event.getEntityType().getKey()
            );
         } else {
            EliteMindSpawnRequest atSource = new EliteMindSpawnRequest(
               requested.owner(),
               requested.programKey(),
               event.getLocation(),
               requested.level(),
               requested.persistent(),
               requested.bodyProfile(),
               requested.powerLoadout()
            );
            EliteEntity actor = this.spawn(atSource, event.getSpawnReason());

            try {
               replacement.afterSpawn().accept(actor);
               return true;
            } catch (Error | RuntimeException var12) {
               try {
                  this.clearProgram(actor);
               } catch (RuntimeException var11) {
                  var12.addSuppressed(var11);
               }

               throw var12;
            }
         }
      }
   }

   @Override
   public EliteMindSnapshot setProgram(EliteEntity eliteEntity, NamespacedKey key, EliteMindTransferPolicy transferPolicy) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(eliteEntity, "eliteEntity");
      EliteMindCatalog.Entry entry = this.catalog.require(key);
      EliteMindBinding binding = eliteEntity.getEliteMindBinding();
      if (binding == null) {
         throw new IllegalStateException("Elite actor is not backed by a MagmaCore native mind body");
      } else {
         binding.replace(entry, toNativeTransfer(transferPolicy));
         return binding.snapshot(eliteEntity);
      }
   }

   @Override
   public EliteMindSnapshot setPaused(EliteEntity eliteEntity, boolean paused) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(eliteEntity, "eliteEntity");
      EliteMindBinding binding = eliteEntity.getEliteMindBinding();
      if (binding == null) {
         throw new IllegalStateException("Elite actor is not backed by a MagmaCore native mind body");
      } else {
         EliteMindSnapshot before = binding.snapshot(eliteEntity);
         boolean previouslyPaused = binding.isExplicitlyPaused();
         if (previouslyPaused == paused) {
            return before;
         } else {
            binding.setPaused(paused);

            try {
               this.luaPowerService.setMindActorPowersPaused(eliteEntity, paused);
            } catch (RuntimeException var9) {
               try {
                  binding.setPaused(previouslyPaused);
               } catch (RuntimeException var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            return binding.snapshot(eliteEntity);
         }
      }
   }

   @Override
   public boolean clearProgram(EliteEntity eliteEntity) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(eliteEntity, "eliteEntity");
      if (eliteEntity.getEliteMindBinding() == null) {
         return false;
      } else {
         try {
            eliteEntity.terminateMindActor(RemovalReason.OTHER);
         } finally {
            clearProgramQuietly(eliteEntity, "explicit program clear");
         }

         return true;
      }
   }

   @Override
   public Optional<EliteMindSnapshot> inspect(EliteEntity eliteEntity) {
      this.requireAvailable();
      requireServerThread();
      Objects.requireNonNull(eliteEntity, "eliteEntity");
      EliteMindBinding binding = eliteEntity.getEliteMindBinding();
      return binding == null ? Optional.empty() : Optional.of(binding.snapshot(eliteEntity));
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteMobRemove(EliteMobRemoveEvent event) {
      clearProgramQuietly(event.getEliteMobEntity(), "removed elite");
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPluginDisable(PluginDisableEvent event) {
      if (!this.closed && event.getPlugin() != MetadataHandler.PLUGIN) {
         this.unregisterOwnerInternal(event.getPlugin());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntitiesLoad(EntitiesLoadEvent event) {
      if (!this.closed) {
         for (Entity entity : event.getEntities()) {
            if (entity instanceof LivingEntity livingEntity) {
               this.rehydrate(livingEntity);
            }
         }
      }
   }

   void shutdown() {
      if (!this.closed) {
         requireServerThread();
         this.closed = true;

         for (EliteEntity eliteEntity : new ArrayList<>(EntityTracker.getEliteMobEntities().values())) {
            clearProgramQuietly(eliteEntity, "EliteMobs shutdown");
         }

         this.catalog.clear();
         this.naturalSpawnProviders.clear();
      }
   }

   void reconcileLoadedBodies() {
      this.requireAvailable();
      requireServerThread();

      for (World world : Bukkit.getWorlds()) {
         for (LivingEntity livingEntity : world.getLivingEntities()) {
            this.rehydrate(livingEntity);
         }
      }
   }

   boolean suspendForChunkUnload(EliteEntity eliteEntity, LivingEntity removedBody) {
      EliteMindBinding binding = eliteEntity.getEliteMindBinding();
      if (this.closed || binding == null) {
         return false;
      } else if (removedBody != null && !removedBody.isDead() && removedBody.isPersistent()) {
         boolean suspended = eliteEntity.suspendMindBodyForChunkUnload(removedBody);
         if (suspended) {
            binding.suspendBody();
            this.luaPowerService.suspendMindActorPowers(eliteEntity);
         }

         return suspended;
      } else {
         return false;
      }
   }

   private void rehydrate(LivingEntity loadedCarrier) {
      Optional<MindBodyRehydration> recovered;
      try {
         recovered = this.mindHost.rehydrateBody(loadedCarrier);
      } catch (RuntimeException var15) {
         Logger.warn("Failed to recover a native mind carrier: " + var15.getMessage());
         return;
      }

      if (!recovered.isEmpty()) {
         MindBodyRehydration rehydration = recovered.get();
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntities().get(rehydration.logicalOwner());
         if (eliteEntity != null && eliteEntity.getEliteMindBinding() != null) {
            try {
               eliteEntity.getEliteMindBinding().attachBody(rehydration.body());
               eliteEntity.reattachMindBody(rehydration.body().entity());
               this.luaPowerService.resumeMindActorPowers(eliteEntity);
            } catch (RuntimeException var14) {
               RuntimeException exception = var14;
               this.luaPowerService.suspendMindActorPowers(eliteEntity);

               try {
                  eliteEntity.terminateMindActor(RemovalReason.ENTITY_REPLACEMENT);
               } catch (RuntimeException var12) {
                  exception.addSuppressed(var12);
               } finally {
                  clearProgramQuietly(eliteEntity, "failed native mind reattachment");
               }

               rehydration.body().entity().remove();
               Logger.warn("Failed to reattach native mind body for elite " + eliteEntity.getEliteUUID() + ": " + var14.getMessage());
            }
         } else {
            rehydration.body().entity().remove();
         }
      }
   }

   private void unregisterOwnerInternal(Plugin owner) {
      this.catalog.unregisterOwner(owner);
      this.naturalSpawnProviders.unregister(owner);

      for (EliteEntity eliteEntity : new ArrayList<>(EntityTracker.getEliteMobEntities().values())) {
         EliteMindBinding binding = eliteEntity.getEliteMindBinding();
         if (binding != null && binding.isOwnedBy(owner)) {
            removeOwnedActor(eliteEntity);
         }
      }
   }

   private static void removeOwnedActor(EliteEntity eliteEntity) {
      try {
         eliteEntity.terminateMindActor(RemovalReason.OTHER);
      } catch (RuntimeException var5) {
         Logger.warn("Failed to remove native mind actor during owner plugin disable: " + var5.getMessage());
      } finally {
         clearProgramQuietly(eliteEntity, "owner plugin disable");
      }
   }

   private static boolean clearProgramInternal(EliteEntity eliteEntity) {
      EliteMindBinding binding = eliteEntity.getEliteMindBinding();
      if (binding == null) {
         return false;
      } else {
         eliteEntity.setEliteMindBinding(null);
         binding.close();
         return true;
      }
   }

   private static void clearProgramQuietly(EliteEntity eliteEntity, String context) {
      try {
         clearProgramInternal(eliteEntity);
      } catch (RuntimeException var3) {
         Logger.warn("Failed to clear native mind binding during " + context + ": " + var3.getMessage());
      }
   }

   private static void rollbackSpawn(EliteEntity eliteEntity, MobBody body, MindHandle handle, EliteMindBinding binding) {
      boolean published = eliteEntity != null && EntityTracker.getEliteMobEntities().get(eliteEntity.getEliteUUID()) == eliteEntity;
      if (published) {
         try {
            eliteEntity.terminateMindActor(RemovalReason.ENTITY_REPLACEMENT);
         } catch (RuntimeException var17) {
            Logger.warn("Failed to remove a rejected native mind actor: " + var17.getMessage());
         } finally {
            clearProgramQuietly(eliteEntity, "native mind spawn rollback");
         }
      } else if (eliteEntity != null && eliteEntity.getEliteMindBinding() != null) {
         clearProgramQuietly(eliteEntity, "native mind spawn rollback");
      } else if (binding != null) {
         try {
            binding.close();
         } catch (RuntimeException var16) {
            Logger.warn("Failed to close a rejected native mind binding: " + var16.getMessage());
         }
      } else if (handle != null) {
         try {
            handle.close();
         } catch (RuntimeException var15) {
            Logger.warn("Failed to close a rejected native mind session: " + var15.getMessage());
         }
      }

      if (!published && eliteEntity != null) {
         try {
            eliteEntity.rollbackPreparedMindSpawn();
         } catch (RuntimeException var14) {
            Logger.warn("Failed to roll back a prepared native mind actor: " + var14.getMessage());
         }
      }

      if (body != null && body.entity().isValid()) {
         try {
            body.entity().remove();
         } catch (RuntimeException var13) {
            Logger.warn("Failed to remove a rejected native mind body: " + var13.getMessage());
         }
      }
   }

   private static StateTransfer toNativeTransfer(EliteMindTransferPolicy policy) {
      Objects.requireNonNull(policy, "transferPolicy");

      return switch (policy) {
         case NONE -> StateTransfer.NONE;
         case COMPATIBLE -> StateTransfer.COMPATIBLE;
         case PERSISTENT_ONLY -> StateTransfer.PERSISTENT_ONLY;
      };
   }

   private static MindBodyProfile toNativeProfile(EliteMindBodyProfile profile) {
      Objects.requireNonNull(profile, "profile");

      return new MindBodyProfile(switch (profile.locomotion()) {
         case GROUNDED -> MindBodyLocomotion.GROUNDED;
         case FLYING -> MindBodyLocomotion.FLYING;
         case AQUATIC -> MindBodyLocomotion.AQUATIC;
         case AMPHIBIOUS -> MindBodyLocomotion.AMPHIBIOUS;
         case STATIONARY -> MindBodyLocomotion.STATIONARY;
      }, profile.uniformScale(), profile.entityCollidable(), profile.carrierType().toString());
   }

   private static EliteMindBodyCapabilities toPublicCapabilities(MindBodyCapabilities capabilities) {
      Objects.requireNonNull(capabilities, "capabilities");
      return new EliteMindBodyCapabilities(
         toPublicLocomotion(capabilities.supportedLocomotions()),
         toPublicLocomotion(capabilities.pathfindingLocomotions()),
         capabilities.uniformScale(),
         capabilities.minimumUniformScale(),
         capabilities.maximumUniformScale(),
         capabilities.independentDimensions(),
         capabilities.entityCollisionToggle(),
         capabilities.blockCollisionToggle()
      );
   }

   private static Set<EliteMindBodyLocomotion> toPublicLocomotion(Set<MindBodyLocomotion> locomotion) {
      EnumSet<EliteMindBodyLocomotion> mapped = EnumSet.noneOf(EliteMindBodyLocomotion.class);

      for (MindBodyLocomotion value : locomotion) {
         mapped.add(switch (value) {
            case GROUNDED -> EliteMindBodyLocomotion.GROUNDED;
            case FLYING -> EliteMindBodyLocomotion.FLYING;
            case AQUATIC -> EliteMindBodyLocomotion.AQUATIC;
            case AMPHIBIOUS -> EliteMindBodyLocomotion.AMPHIBIOUS;
            case STATIONARY -> EliteMindBodyLocomotion.STATIONARY;
         });
      }

      return mapped;
   }

   private void requireAvailable() {
      if (this.closed) {
         throw new IllegalStateException("EliteMobs mind service is shut down");
      }
   }

   private static void requireServerThread() {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("EliteMobs mind operations must run on the server thread");
      }
   }

   private record InternalProgramEntry(Plugin owner, EliteMindProgram descriptor, MindProgram program) implements EliteMindProgramEntry {
      private InternalProgramEntry(Plugin owner, EliteMindProgram descriptor, MindProgram program) {
         Objects.requireNonNull(owner, "owner");
         Objects.requireNonNull(descriptor, "descriptor");
         Objects.requireNonNull(program, "program");
         this.owner = owner;
         this.descriptor = descriptor;
         this.program = program;
      }

      @Override
      public MindProgram instantiate() {
         return this.program;
      }
   }
}
