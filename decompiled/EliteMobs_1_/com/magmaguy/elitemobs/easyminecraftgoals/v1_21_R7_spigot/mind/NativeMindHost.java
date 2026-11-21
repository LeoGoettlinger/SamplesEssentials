package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.mind;

import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.TransientMovementOverride;
import com.magmaguy.elitemobs.magmacore.ai.MindActionContext;
import com.magmaguy.elitemobs.magmacore.ai.MindActionRequest;
import com.magmaguy.elitemobs.magmacore.ai.MindActionResult;
import com.magmaguy.elitemobs.magmacore.ai.MindActionSink;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyCapabilities;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyLocomotion;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyProfile;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyRehydration;
import com.magmaguy.elitemobs.magmacore.ai.MindFailure;
import com.magmaguy.elitemobs.magmacore.ai.MindFailureListener;
import com.magmaguy.elitemobs.magmacore.ai.MindHandle;
import com.magmaguy.elitemobs.magmacore.ai.MindHost;
import com.magmaguy.elitemobs.magmacore.ai.MindPersistentState;
import com.magmaguy.elitemobs.magmacore.ai.MindProgram;
import com.magmaguy.elitemobs.magmacore.ai.MobBody;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeRanged;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public final class NativeMindHost implements MindHost {
   private final String hostIdentity;
   private final MindFailureListener failureListener;
   private final MindActionSink actionSink;
   private final MindBodyCapabilities bodyCapabilities;
   private final Map<UUID, NativeMindSession> sessions = new LinkedHashMap<>();
   private final NativeMindServerBudget serverBudget = new NativeMindServerBudget();
   private final NativeMindEnvironment environment;
   private boolean closed;

   public NativeMindHost(NamespacedKey hostIdentity, MindFailureListener failureListener) {
      this(hostIdentity, failureListener, MindActionSink.rejecting());
   }

   public NativeMindHost(NamespacedKey hostIdentity, MindFailureListener failureListener, MindActionSink actionSink) {
      this.hostIdentity = Objects.requireNonNull(hostIdentity, "hostIdentity").toString();
      this.failureListener = Objects.requireNonNull(failureListener, "failureListener");
      this.actionSink = Objects.requireNonNull(actionSink, "actionSink");
      if (GenericAttributes.A.a() instanceof AttributeRanged scale) {
         this.bodyCapabilities = new MindBodyCapabilities(
            EnumSet.allOf(MindBodyLocomotion.class),
            EnumSet.of(MindBodyLocomotion.GROUNDED, MindBodyLocomotion.FLYING, MindBodyLocomotion.AQUATIC, MindBodyLocomotion.AMPHIBIOUS),
            true,
            scale.d(),
            scale.e(),
            false,
            true,
            false
         );
         this.environment = new NativeMindEnvironment(this.hostIdentity);
      } else {
         throw new IllegalStateException("Minecraft's scale attribute no longer exposes numeric bounds");
      }
   }

   @Override
   public MindHandle open(UUID logicalOwner, MindProgram program, MindPersistentState restoredState) {
      requirePrimaryThread();
      this.requireOpen();
      Objects.requireNonNull(logicalOwner, "logicalOwner");
      Objects.requireNonNull(program, "program");
      Objects.requireNonNull(restoredState, "restoredState");
      if (this.sessions.containsKey(logicalOwner)) {
         throw new IllegalStateException("Logical mind session is already open: " + logicalOwner);
      } else {
         program.claimForBinding();
         NativeMindSession session = new NativeMindSession(this, logicalOwner, program, restoredState, this.serverBudget);
         this.sessions.put(logicalOwner, session);
         return session;
      }
   }

   @Override
   public MindBodyCapabilities bodyCapabilities() {
      return this.bodyCapabilities;
   }

   @Override
   public MobBody spawnBody(Location location, MindBodyProfile profile, Consumer<MobBody> preparation) {
      requirePrimaryThread();
      this.requireOpen();
      Objects.requireNonNull(location, "location");
      this.bodyCapabilities.validate(profile);
      Objects.requireNonNull(preparation, "preparation");
      if (location.getWorld() == null) {
         throw new IllegalArgumentException("Native mind body requires a world");
      } else {
         WorldServer level = NativeMindVersion.getServerLevel(location);
         EntityTypes<? extends EntityInsentient> carrierType = resolveCarrierType(profile.carrierType());
         Entity created = carrierType.a(level, EntitySpawnReason.n);
         if (created instanceof EntityInsentient mob && mob.ay() == carrierType) {
            NativeMindVersion.position(mob, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            mob.gz();
            LivingEntity bukkitEntity = null;
            boolean published = false;
            NativeMindBody body = null;

            NativeMindBody var11;
            try {
               body = new NativeMindBody(this, mob, profile);
               bukkitEntity = body.entity();
               bukkitEntity.setCollidable(profile.entityCollidable());
               MindCarrierState.markBody(bukkitEntity, this.hostIdentity, profile);
               body.beginPreparation();
               preparation.accept(body);
               body.endPreparation();
               if (mob.eh() || !mob.cb()) {
                  throw new IllegalStateException("Mind body was removed during preparation");
               }

               if (!level.addFreshEntity(mob, SpawnReason.CUSTOM)) {
                  throw new IllegalStateException("Minecraft rejected the native mind body spawn");
               }

               published = true;
               var11 = body;
            } finally {
               if (!published) {
                  try {
                     if (body != null) {
                        body.abortPreparation();
                     }
                  } finally {
                     if (bukkitEntity != null) {
                        MindCarrierState.clear(bukkitEntity, this.hostIdentity);
                     }

                     mob.aC();
                  }
               }
            }

            return var11;
         } else {
            if (created != null) {
               created.aC();
            }

            throw new IllegalStateException("Minecraft did not create the requested native Mind carrier: " + profile.carrierType());
         }
      }
   }

   @Override
   public void tick() {
      requirePrimaryThread();
      this.requireOpen();

      for (NativeMindSession session : this.serverBudget.fairOrder(new ArrayList<>(this.sessions.values()))) {
         session.tickBody();
      }
   }

   public static boolean isMindCarrier(org.bukkit.entity.Entity entity) {
      Objects.requireNonNull(entity, "entity");
      return entity instanceof LivingEntity livingEntity && MindCarrierState.isMarked(livingEntity);
   }

   public Optional<TransientMovementOverride> beginFlee(LivingEntity body, Location threatLocation, double speedModifier) {
      requirePrimaryThread();
      this.requireOpen();
      Objects.requireNonNull(body, "body");
      Objects.requireNonNull(threatLocation, "threatLocation");

      for (NativeMindSession session : this.sessions.values()) {
         if (session.hasBody(body.getUniqueId())) {
            return session.beginFlee(threatLocation, speedModifier);
         }
      }

      return Optional.empty();
   }

   public Optional<PathfindingHandle> openPathfinding(LivingEntity body) {
      requirePrimaryThread();
      this.requireOpen();

      for (NativeMindSession session : this.sessions.values()) {
         if (session.hasBody(body.getUniqueId())) {
            return session.openPathfinding();
         }
      }

      return Optional.empty();
   }

   @Override
   public Optional<MindBodyRehydration> rehydrateBody(LivingEntity carrier) {
      requirePrimaryThread();
      this.requireOpen();
      Objects.requireNonNull(carrier, "carrier");
      Optional<MindCarrierState.Stored> stored = MindCarrierState.read(carrier, this.hostIdentity);
      if (stored.isEmpty()) {
         return Optional.empty();
      } else {
         MindCarrierState.Stored marker = stored.get();
         this.bodyCapabilities.validate(marker.profile());
         EntityLiving loaded = NativeMindVersion.getNMSLivingEntity(carrier);
         if (!(loaded.ao() instanceof WorldServer)) {
            throw new IllegalArgumentException("Mind carrier is not in a server level");
         } else if (loaded instanceof EntityInsentient mob) {
            EntityTypes carrierType = resolveCarrierType(marker.profile().carrierType());
            if (mob.ay() != carrierType) {
               throw new IllegalStateException("MagmaCore mind carrier type disagrees with its marker");
            } else {
               NativeMindSession session = this.sessions.get(marker.logicalOwner());
               if (!mob.eh() && session != null && session.hasBody(mob)) {
                  return Optional.empty();
               } else {
                  NativeMindBody body = new NativeMindBody(this, mob, marker.profile());
                  carrier.setCollidable(marker.profile().entityCollidable());
                  return Optional.of(
                     new MindBodyRehydration(
                        body, marker.profile(), marker.logicalOwner(), marker.programIdentifier(), marker.programRevision(), marker.persistentState()
                     )
                  );
               }
            }
         } else {
            throw new IllegalStateException("Stored MagmaCore mind carrier is not a native Mob");
         }
      }
   }

   String hostIdentity() {
      return this.hostIdentity;
   }

   void sessionClosed(NativeMindSession session) {
      this.sessions.remove(session.logicalOwner(), session);
   }

   void report(MindFailure failure) {
      try {
         this.failureListener.onFailure(failure);
      } catch (RuntimeException var3) {
         Bukkit.getLogger().warning("MagmaCore mind failure listener threw " + var3.getClass().getSimpleName());
      }
   }

   MindActionResult requestAction(NativeMindSession session, NativeMindBody body, long gameTick, long generation, MindActionRequest request) {
      requirePrimaryThread();
      Objects.requireNonNull(request, "request");
      MindActionResult result = this.actionSink.request(new MindActionContext(session.logicalOwner(), body, gameTick, generation), request);
      return Objects.requireNonNull(result, "Mind action sink result");
   }

   public void shutdown() {
      requirePrimaryThread();
      if (!this.closed) {
         this.closed = true;

         for (NativeMindSession session : new ArrayList<>(this.sessions.values())) {
            session.closeForShutdown();
         }

         this.sessions.clear();
         this.environment.close();
      }
   }

   private void requireOpen() {
      if (this.closed) {
         throw new IllegalStateException("Native mind host is closed");
      }
   }

   static void requirePrimaryThread() {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Native mind operations require the server thread");
      }
   }

   private static EntityTypes<? extends EntityInsentient> resolveCarrierType(String carrierKey) {
      NamespacedKey bukkitKey = NamespacedKey.fromString(carrierKey);
      EntityType bukkitType = bukkitKey == null ? null : (EntityType)Registry.ENTITY_TYPE.get(bukkitKey);
      if (bukkitType != null && bukkitType.isSpawnable() && bukkitType.getEntityClass() != null && Mob.class.isAssignableFrom(bukkitType.getEntityClass())) {
         EntityTypes<?> entityType = NativeMindVersion.entityType(carrierKey);
         if (entityType == null) {
            throw new IllegalArgumentException("Unknown Minecraft Mind carrier type: " + carrierKey);
         } else {
            return (EntityTypes<? extends EntityInsentient>)entityType;
         }
      } else {
         throw new IllegalArgumentException("Mind carrier is not a spawnable Mob type: " + carrierKey);
      }
   }
}
