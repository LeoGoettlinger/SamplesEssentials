package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityMechanic;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.damage.AdvancedDamageScaling;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyProfile;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.mobconstructor.InternalMindActorSpawnRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class ClassMinionManager implements Listener, AutoCloseable {
   private static final int CORPSE_UPTIME_TICKS = 600;
   private static final double CORPSE_ELIGIBILITY_RANGE_SQUARED = 1024.0;
   private static final double CORPSE_TARGET_RANGE = 24.0;
   private static final double OWNER_MAX_DISTANCE_SQUARED = 1600.0;
   private static final double AUTONOMOUS_TARGET_RANGE_SQUARED = 256.0;
   private static final int AUTONOMOUS_TARGET_TICKS = 30;
   private static final int RETALIATION_TICKS = 160;
   private static final int TICK_PERIOD = 2;
   private static final int CORPSE_PARTICLE_PERIOD = 10;
   private static final int MAX_CORPSE_PARTICLE_MARKERS_PER_PULSE = 32;
   private final Plugin plugin;
   private final ClassMinionManager.OwnerProfileResolver ownerProfiles;
   private final ClassMinionManager.MinionDamageEvaluator damageEvaluator;
   private final ClassMinionManager.MinionImpactEffectApplier impactEffects;
   private final ClassMinionManager.MinionRuntimeObserver runtimeObserver;
   private final ClassMinionIdentity minionIdentity;
   private final AbyssalGateManager gates;
   private final CorpseOpportunityLedger corpseLedger = new CorpseOpportunityLedger();
   private final Map<UUID, ClassMinionManager.CorpseMarker> corpses = new LinkedHashMap<>();
   private final Map<UUID, ClassMinionManager.MinionInstance> minionsByEntity = new HashMap<>();
   private final Map<UUID, ClassMinionManager.MinionInstance> minionsByElite = new HashMap<>();
   private final Map<UUID, ClassMinionRoster<ClassMinionManager.MinionInstance>> rosters = new HashMap<>();
   private final BukkitTask task;
   private long nextCorpseParticleTick;
   private long nextAutonomousTargetTick;
   private int sequence;
   private boolean closed;

   public ClassMinionManager(
      Plugin plugin,
      ClassMinionManager.OwnerProfileResolver ownerProfiles,
      ClassMinionManager.MinionDamageEvaluator damageEvaluator,
      ClassMinionManager.MinionImpactEffectApplier impactEffects,
      ClassMinionManager.MinionRuntimeObserver runtimeObserver
   ) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.ownerProfiles = Objects.requireNonNull(ownerProfiles, "ownerProfiles");
      this.damageEvaluator = Objects.requireNonNull(damageEvaluator, "damageEvaluator");
      this.impactEffects = Objects.requireNonNull(impactEffects, "impactEffects");
      this.runtimeObserver = Objects.requireNonNull(runtimeObserver, "runtimeObserver");
      this.minionIdentity = new ClassMinionIdentity(plugin);
      this.gates = new AbyssalGateManager(plugin);
      Bukkit.getPluginManager().registerEvents(this, plugin);
      this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 2L);
   }

   public ClassMinionManager.SummonResult summon(Player owner, FixedAbilitySpec spec, int effectiveLevel, Location aimedLocation) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(spec, "spec");
      Objects.requireNonNull(aimedLocation, "aimedLocation");
      if (!this.closed && Bukkit.isPrimaryThread() && owner.isOnline() && !owner.isDead()) {
         ClassMinionTheme theme = ClassMinionTheme.forAbility(spec.id()).orElse(null);
         if (theme == null) {
            return ClassMinionManager.SummonResult.failure(ClassMinionManager.Status.RUNTIME_UNAVAILABLE);
         } else {
            ClassMinionBalanceContract balance = ClassMinionBalanceContract.from(spec, theme, effectiveLevel);
            long now = MonotonicTickClock.currentTick();
            ClassMinionManager.CorpseMarker selectedCorpse = null;
            if (spec.executionTraits().mechanics().contains(AbilityMechanic.REQUIRES_CORPSE)) {
               selectedCorpse = this.selectCorpse(owner, now).orElse(null);
               if (selectedCorpse == null) {
                  return ClassMinionManager.SummonResult.failure(ClassMinionManager.Status.NO_CORPSE);
               }

               aimedLocation = selectedCorpse.location();
            }

            AbyssalGateManager.GateHandle gate = null;
            if (theme == ClassMinionTheme.NETHER) {
               gate = this.gates.place(owner, aimedLocation, balance.uptimeTicks()).orElse(null);
               if (gate == null) {
                  return ClassMinionManager.SummonResult.failure(ClassMinionManager.Status.UNSAFE_DESTINATION);
               }
            }

            List<ClassMinionManager.SpawnPlan> plans = new ArrayList<>(balance.summonCount());

            for (int index = 0; index < balance.summonCount(); index++) {
               ClassMinionTheme.Carrier carrier = theme.carrierFor(this.sequence + index);
               Location spawn = this.findSpawn(aimedLocation, carrier, index).orElse(null);
               if (spawn == null) {
                  this.gates.remove(gate);
                  return ClassMinionManager.SummonResult.failure(ClassMinionManager.Status.UNSAFE_DESTINATION);
               }

               plans.add(new ClassMinionManager.SpawnPlan(carrier, spawn));
            }

            UUID ownerId = owner.getUniqueId();
            List<ClassMinionManager.MinionInstance> spawned = new ArrayList<>(plans.size());

            try {
               for (ClassMinionManager.SpawnPlan plan : plans) {
                  ClassMinionControlState control = new ClassMinionControlState(ownerId, balance.attackPeriodTicks());
                  EliteMindBodyProfile bodyProfile = EliteMindBodyProfile.forCarrier(plan.carrier().entityType().getKey(), plan.carrier().locomotion());
                  EliteEntity actor = EliteMindServiceModule.spawnInternal(
                        new InternalMindActorSpawnRequest(
                           this.plugin,
                           ownerId,
                           plan.location(),
                           Math.max(1, effectiveLevel),
                           bodyProfile,
                           ClassMinionMindProgramFactory.create(this.plugin, theme, control),
                           elite -> initializeActor(elite, balance)
                        )
                     )
                     .orElseThrow(() -> new IllegalStateException("Native Mind runtime is unavailable"));
                  LivingEntity body = actor.getLivingEntity();
                  if (body == null || !body.isValid()) {
                     EliteMindServiceModule.clearInternal(actor);
                     throw new IllegalStateException("Native Mind body was not committed");
                  }

                  this.minionIdentity.mark(body);
                  ClassMinionManager.MinionInstance instance = new ClassMinionManager.MinionInstance(
                     actor, body.getUniqueId(), ownerId, spec, effectiveLevel, theme, balance, control, expiresAt(now, balance.uptimeTicks())
                  );
                  spawned.add(instance);
               }
            } catch (RuntimeException var21) {
               for (ClassMinionManager.MinionInstance instance : spawned) {
                  this.removeMinion(instance, RemovalReason.OTHER);
               }

               this.gates.remove(gate);
               Logger.warn("Could not create class servant for " + owner.getName() + ": " + var21.getMessage());
               return ClassMinionManager.SummonResult.failure(ClassMinionManager.Status.RUNTIME_UNAVAILABLE);
            }

            if (selectedCorpse != null && !this.corpseLedger.consume(selectedCorpse.id(), ownerId, now)) {
               for (ClassMinionManager.MinionInstance instance : spawned) {
                  this.removeMinion(instance, RemovalReason.OTHER);
               }

               this.gates.remove(gate);
               return ClassMinionManager.SummonResult.failure(ClassMinionManager.Status.NO_CORPSE);
            } else {
               if (selectedCorpse != null && !this.corpseLedger.contains(selectedCorpse.id())) {
                  this.removeCorpse(selectedCorpse.id());
               }

               this.sequence = this.sequence + spawned.size();

               for (ClassMinionManager.MinionInstance instance : spawned) {
                  this.register(instance);
               }

               ClassMinionRoster<ClassMinionManager.MinionInstance> roster = this.rosters
                  .computeIfAbsent(ownerId, ignored -> new ClassMinionRoster<>(balance.ownerCap()));

               for (ClassMinionManager.MinionInstance evicted : roster.admitAll(spawned)) {
                  this.removeMinion(evicted, RemovalReason.OTHER);
               }

               Location presentationOrigin = spawned.getFirst().body().getLocation();
               presentationOrigin.getWorld().spawnParticle(summonParticle(theme), presentationOrigin.clone().add(0.0, 0.8, 0.0), 24, 0.65, 0.55, 0.65, 0.04);
               presentationOrigin.getWorld().playSound(presentationOrigin, summonSound(theme), 1.0F, summonPitch(theme));
               return ClassMinionManager.SummonResult.success(spawned.stream().map(ClassMinionManager.MinionInstance::body).toList(), presentationOrigin);
            }
         }
      } else {
         return ClassMinionManager.SummonResult.failure(ClassMinionManager.Status.RUNTIME_UNAVAILABLE);
      }
   }

   public void deactivate(Player owner) {
      if (owner != null) {
         this.deactivate(owner.getUniqueId());
      }
   }

   public void deactivate(UUID ownerId) {
      if (ownerId != null) {
         ClassMinionRoster<ClassMinionManager.MinionInstance> roster = this.rosters.remove(ownerId);
         if (roster != null) {
            for (ClassMinionManager.MinionInstance minion : roster.drain()) {
               this.removeMinion(minion, RemovalReason.OTHER);
            }
         }

         this.gates.removeOwner(ownerId);

         for (UUID corpseId : this.corpseLedger.removePlayer(ownerId)) {
            this.removeCorpse(corpseId);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEliteDeath(EliteMobDeathEvent event) {
      EliteEntity dead = event.getEliteEntity();
      if (dead != null && !this.minionsByElite.containsKey(dead.getEliteUUID())) {
         Location deathLocation = event.getEntity() == null ? null : event.getEntity().getLocation();
         if (deathLocation != null && deathLocation.getWorld() != null) {
            Set<UUID> eligible = new LinkedHashSet<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
               if (player.getWorld().equals(deathLocation.getWorld()) && !(player.getLocation().distanceSquared(deathLocation) > 1024.0)) {
                  ClassMinionManager.OwnerProfile profile = this.ownerProfiles.resolve(player).orElse(null);
                  if (profile != null && isNecromancerForm(profile.activeFormId())) {
                     eligible.add(player.getUniqueId());
                  }
               }
            }

            if (!eligible.isEmpty()) {
               UUID corpseId = UUID.randomUUID();
               long now = MonotonicTickClock.currentTick();
               long expiresAt = expiresAt(now, 600);
               Location markerLocation = deathLocation.clone().add(0.0, 0.35, 0.0);
               TextDisplay display = (TextDisplay)markerLocation.getWorld().spawn(markerLocation, TextDisplay.class, text -> {
                  text.setText(ChatColorConverter.convert("&5&lCorpse"));
                  text.setBillboard(Billboard.CENTER);
                  text.setShadowed(true);
                  text.setPersistent(false);
                  text.setSeeThrough(false);
                  text.setViewRange(24.0F);
               });
               EntityTracker.registerVisualEffects(display);
               this.corpseLedger.offer(corpseId, eligible, expiresAt);
               this.corpses.put(corpseId, new ClassMinionManager.CorpseMarker(corpseId, deathLocation, display, expiresAt));
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerAttacksMinion(EntityDamageByEntityEvent event) {
      if (this.minionsByEntity.containsKey(event.getEntity().getUniqueId()) && source(event.getDamager()) instanceof Player) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onMinionDamage(EntityDamageByEntityEvent event) {
      LivingEntity source = source(event.getDamager());
      if (source != null) {
         ClassMinionManager.MinionInstance minion = this.minionsByEntity.get(source.getUniqueId());
         if (minion != null) {
            EliteEntity target = EntityTracker.getEliteMobEntity(event.getEntity());
            double damage = target == null
               ? 4.0 * minion.balance().scaledDamageMultiplierPerHit()
               : AdvancedDamageScaling.classAbility(target, minion.effectiveLevel(), minion.balance().damageMultiplierPerHit());
            Player owner = Bukkit.getPlayer(minion.ownerId());
            if (target != null && owner != null && owner.isOnline() && !owner.isDead()) {
               damage = this.damageEvaluator.evaluate(owner, target, damage, CombatDamageContext.ClassAbilityDamageDomain.SINGLE_TARGET_SUMMON);
            }

            if (Double.isFinite(damage) && !(damage <= 0.0)) {
               event.setDamage(damage);
            } else {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onCombatCommand(EntityDamageByEntityEvent event) {
      LivingEntity attacker = source(event.getDamager());
      if (attacker != null && !(attacker instanceof Player) && !this.isMinion(attacker)) {
         ClassMinionManager.MinionInstance struckMinion = this.minionsByEntity.get(event.getEntity().getUniqueId());
         if (struckMinion != null) {
            struckMinion.control().commandTarget(attacker, 160);
         } else {
            if (event.getEntity() instanceof Player owner) {
               ClassMinionRoster<ClassMinionManager.MinionInstance> roster = this.rosters.get(owner.getUniqueId());
               if (roster == null) {
                  return;
               }

               for (ClassMinionManager.MinionInstance minion : roster.entries()) {
                  minion.control().commandTarget(attacker, 160);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onMinionImpact(EntityDamageByEntityEvent event) {
      LivingEntity attacker = source(event.getDamager());
      ClassMinionManager.MinionInstance minion = attacker == null ? null : this.minionsByEntity.get(attacker.getUniqueId());
      if (minion != null && event.getEntity() instanceof LivingEntity target && !(event.getFinalDamage() <= 0.0)) {
         Player owner = Bukkit.getPlayer(minion.ownerId());
         if (owner != null && owner.isOnline() && !owner.isDead()) {
            EliteEntity targetElite = EntityTracker.getEliteMobEntity(target);
            if (targetElite != null) {
               targetElite.addDamager(owner, event.getFinalDamage());
            }

            FixedAbilitySpec spec = minion.spec();
            this.runtimeObserver.applied(owner.getUniqueId(), target.getUniqueId(), spec.id(), AbilityEffect.DAMAGE, event.getFinalDamage());
            ClassMinionImpactPlan impactPlan = ClassMinionImpactPlan.from(spec);
            if (impactPlan.lifestealFraction() > 0.0) {
               double healed = healOwner(owner, event.getFinalDamage() * impactPlan.lifestealFraction() * this.damageEvaluator.healingDoneMultiplier(owner));
               if (healed > 0.0) {
                  this.runtimeObserver.applied(owner.getUniqueId(), owner.getUniqueId(), spec.id(), AbilityEffect.LIFESTEAL, healed);
               }
            }

            this.impactEffects.apply(owner, spec, target, minion.effectiveLevel());
            Location impact = target.getLocation().add(0.0, target.getHeight() * 0.5, 0.0);
            impact.getWorld().spawnParticle(summonParticle(minion.theme()), impact, 6, 0.22, 0.22, 0.22, 0.025);
            impact.getWorld().playSound(impact, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.55F, 0.8F);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onMinionDeath(EntityDeathEvent event) {
      if (this.isMinion(event.getEntity())) {
         event.getDrops().clear();
         event.setDroppedExp(0);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onEntitiesLoad(EntitiesLoadEvent event) {
      Iterator var2 = event.getEntities().iterator();

      while (true) {
         Entity entity;
         while (true) {
            if (!var2.hasNext()) {
               return;
            }

            entity = (Entity)var2.next();
            if (this.taggedMinion(entity)) {
               EliteEntity actor = EntityTracker.getEliteMobEntity(entity);
               if (actor == null) {
                  break;
               }

               try {
                  if (EliteMindServiceModule.clearInternal(actor)) {
                     continue;
                  }
               } catch (RuntimeException var6) {
               }
               break;
            }
         }

         entity.remove();
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteRemove(EliteMobRemoveEvent event) {
      ClassMinionManager.MinionInstance minion = this.minionsByElite.get(event.getEliteMobEntity().getEliteUUID());
      if (minion != null) {
         this.forget(minion);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPickup(EntityPickupItemEvent event) {
      if (this.isMinion(event.getEntity())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBreed(EntityBreedEvent event) {
      if (this.isMinion(event.getMother()) || this.isMinion(event.getFather())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onLoveMode(EntityEnterLoveModeEvent event) {
      if (this.isMinion(event.getEntity())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onChangeBlock(EntityChangeBlockEvent event) {
      if (this.isMinion(event.getEntity())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onExplosion(EntityExplodeEvent event) {
      if (this.isMinion(event.getEntity())) {
         event.blockList().clear();
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPortal(EntityPortalEvent event) {
      if (this.isMinion(event.getEntity())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onChunkUnload(ChunkUnloadEvent event) {
      for (Entity entity : event.getChunk().getEntities()) {
         ClassMinionManager.MinionInstance minion = this.minionsByEntity.get(entity.getUniqueId());
         if (minion != null) {
            this.removeMinion(minion, RemovalReason.OTHER);
         }
      }

      for (UUID markerId : this.corpses
         .values()
         .stream()
         .filter(marker -> marker.location().getWorld().equals(event.getWorld()))
         .filter(marker -> marker.location().getBlockX() >> 4 == event.getChunk().getX())
         .filter(marker -> marker.location().getBlockZ() >> 4 == event.getChunk().getZ())
         .map(ClassMinionManager.CorpseMarker::id)
         .toList()) {
         this.removeCorpse(markerId);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      for (ClassMinionManager.MinionInstance minion : new ArrayList<>(this.minionsByEntity.values())) {
         LivingEntity body = minion.body();
         if (body != null && body.getWorld().equals(event.getWorld())) {
            this.removeMinion(minion, RemovalReason.OTHER);
         }
      }

      for (UUID markerId : this.corpses
         .values()
         .stream()
         .filter(marker -> marker.location().getWorld().equals(event.getWorld()))
         .map(ClassMinionManager.CorpseMarker::id)
         .toList()) {
         this.removeCorpse(markerId);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      this.deactivate(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDeath(PlayerDeathEvent event) {
      this.deactivate(event.getEntity());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onWorldChange(PlayerChangedWorldEvent event) {
      this.deactivate(event.getPlayer());
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.task.cancel();

         for (ClassMinionManager.MinionInstance minion : new ArrayList<>(this.minionsByEntity.values())) {
            this.removeMinion(minion, RemovalReason.SHUTDOWN);
         }

         for (UUID corpseId : new ArrayList<>(this.corpses.keySet())) {
            this.removeCorpse(corpseId);
         }

         this.rosters.clear();
         this.gates.close();
         HandlerList.unregisterAll(this);
      }
   }

   private void tick() {
      if (!this.closed) {
         long now = MonotonicTickClock.currentTick();
         this.gates.tick(now);

         for (UUID corpseId : this.corpseLedger.expire(now)) {
            this.removeCorpse(corpseId);
         }

         if (now >= this.nextCorpseParticleTick) {
            this.nextCorpseParticleTick = now + 10L;
            this.corpses.values().stream().limit(32L).forEach(this::renderCorpse);
         }

         boolean acquireTargets = now >= this.nextAutonomousTargetTick;
         if (acquireTargets) {
            this.nextAutonomousTargetTick = now + 20L;
         }

         Map<UUID, Optional<ClassMinionManager.OwnerProfile>> profiles = new HashMap<>();

         for (ClassMinionManager.MinionInstance minion : new ArrayList<>(this.minionsByEntity.values())) {
            Player owner = Bukkit.getPlayer(minion.ownerId());
            LivingEntity body = minion.body();
            if (now < minion.expiresAtTick()
               && owner != null
               && owner.isOnline()
               && !owner.isDead()
               && body != null
               && body.isValid()
               && !body.isDead()
               && body.getWorld().equals(owner.getWorld())
               && !(body.getLocation().distanceSquared(owner.getLocation()) > 1600.0)) {
               ClassMinionManager.OwnerProfile profile = profiles.computeIfAbsent(minion.ownerId(), ignored -> this.ownerProfiles.resolve(owner)).orElse(null);
               if (profile == null || !minion.spec().id().equals(profile.activeFormId() + ".signature")) {
                  this.removeMinion(minion, RemovalReason.OTHER);
               } else if (acquireTargets && minion.control().target(body).isEmpty()) {
                  this.nearestEliteTarget(owner, body).ifPresent(target -> minion.control().commandTarget(target, 30));
               }
            } else {
               this.removeMinion(minion, RemovalReason.OTHER);
            }
         }
      }
   }

   private Optional<ClassMinionManager.CorpseMarker> selectCorpse(Player owner, long currentTick) {
      Location eye = owner.getEyeLocation();
      Vector direction = eye.getDirection();
      List<CorpseTargetSelector.Candidate> candidates = this.corpses
         .values()
         .stream()
         .filter(marker -> marker.location().getWorld().equals(owner.getWorld()))
         .filter(marker -> this.corpseLedger.availableTo(marker.id(), owner.getUniqueId(), currentTick))
         .map(marker -> new CorpseTargetSelector.Candidate(marker.id(), point(marker.location())))
         .toList();
      UUID selected = CorpseTargetSelector.select(
            point(eye), new CorpseTargetSelector.Point(direction.getX(), direction.getY(), direction.getZ()), 24.0, candidates
         )
         .orElse(null);
      return Optional.ofNullable(selected == null ? null : this.corpses.get(selected));
   }

   private Optional<LivingEntity> nearestEliteTarget(Player owner, LivingEntity body) {
      return body.getNearbyEntities(16.0, 12.0, 16.0)
         .stream()
         .filter(LivingEntity.class::isInstance)
         .map(LivingEntity.class::cast)
         .filter(candidate -> !(candidate instanceof Player))
         .filter(candidate -> !this.isMinion(candidate))
         .filter(candidate -> EntityTracker.getEliteMobEntity(candidate) != null)
         .filter(candidate -> candidate.getWorld().equals(owner.getWorld()))
         .filter(candidate -> candidate.getLocation().distanceSquared(owner.getLocation()) <= 256.0)
         .min(Comparator.comparingDouble(candidate -> candidate.getLocation().distanceSquared(body.getLocation())));
   }

   private void register(ClassMinionManager.MinionInstance minion) {
      this.minionsByEntity.put(minion.entityId(), minion);
      this.minionsByElite.put(minion.actor().getEliteUUID(), minion);
   }

   private void removeMinion(ClassMinionManager.MinionInstance minion, RemovalReason reason) {
      if (minion != null) {
         if (this.minionsByElite.get(minion.actor().getEliteUUID()) != minion && !this.minionsByEntity.containsKey(minion.entityId())) {
            try {
               EliteMindServiceModule.clearInternal(minion.actor());
            } catch (RuntimeException var5) {
            }
         } else {
            this.forget(minion);

            try {
               if (!EliteMindServiceModule.clearInternal(minion.actor())) {
                  minion.actor().remove(reason);
               }
            } catch (RuntimeException var6) {
               LivingEntity body = minion.body();
               if (body != null && body.isValid()) {
                  body.remove();
               }

               Logger.warn("Could not cleanly remove class servant: " + var6.getMessage());
            }
         }
      }
   }

   private void forget(ClassMinionManager.MinionInstance minion) {
      boolean tracked = this.minionsByEntity.remove(minion.entityId(), minion);
      tracked |= this.minionsByElite.remove(minion.actor().getEliteUUID(), minion);
      ClassMinionRoster<ClassMinionManager.MinionInstance> roster = this.rosters.get(minion.ownerId());
      if (roster != null) {
         roster.remove(minion);
         if (roster.entries().isEmpty()) {
            this.rosters.remove(minion.ownerId());
         }
      }

      if (tracked) {
         this.runtimeObserver.removed(minion.ownerId(), minion.entityId(), minion.spec().id());
      }
   }

   private void removeCorpse(UUID corpseId) {
      this.corpseLedger.discard(corpseId);
      ClassMinionManager.CorpseMarker marker = this.corpses.remove(corpseId);
      if (marker != null && marker.display().isValid()) {
         marker.display().remove();
      }
   }

   private void renderCorpse(ClassMinionManager.CorpseMarker marker) {
      World world = marker.location().getWorld();
      if (world != null && world.isChunkLoaded(marker.location().getBlockX() >> 4, marker.location().getBlockZ() >> 4)) {
         world.spawnParticle(Particle.SOUL, marker.location().clone().add(0.0, 0.5, 0.0), 4, 0.25, 0.35, 0.25, 0.01);
      }
   }

   private static void initializeActor(EliteEntity actor, ClassMinionBalanceContract balance) {
      actor.setPersistent(false);
      actor.setEliteLoot(false);
      actor.setVanillaLoot(false);
      actor.setRandomLoot(false);
      double existing = Math.max(1.0, actor.getMaxHealth());
      actor.setHealthMultiplier(balance.maxHealth() / existing);
      actor.setMaxHealth();
      actor.resetMaxHealth();
      actor.clearDamagers();
      LivingEntity body = actor.getLivingEntity();
      if (body != null) {
         body.setPersistent(false);
         body.setRemoveWhenFarAway(false);
         body.setCanPickupItems(false);
         body.setPortalCooldown(Integer.MAX_VALUE);
         body.setCustomNameVisible(false);
         AttributeManager.setAttribute(body, "generic_attack_damage", 1.0);
         EntityEquipment equipment = body.getEquipment();
         if (equipment != null) {
            equipment.clear();
            equipment.setItemInMainHandDropChance(0.0F);
            equipment.setItemInOffHandDropChance(0.0F);
            equipment.setHelmetDropChance(0.0F);
            equipment.setChestplateDropChance(0.0F);
            equipment.setLeggingsDropChance(0.0F);
            equipment.setBootsDropChance(0.0F);
         }

         if (body instanceof Ageable ageable) {
            ageable.setAdult();
            ageable.setAgeLock(true);
         }

         if (body instanceof Animals animals) {
            animals.setBreed(false);
         }
      }
   }

   private static double healOwner(Player owner, double requested) {
      if (Double.isFinite(requested) && !(requested <= 0.0)) {
         double maximum = owner.getAttribute(Attribute.MAX_HEALTH) == null
            ? Math.max(1.0, owner.getHealth())
            : owner.getAttribute(Attribute.MAX_HEALTH).getValue();
         double amount = Math.min(requested, Math.max(0.0, maximum - owner.getHealth()));
         if (amount <= 0.0) {
            return 0.0;
         } else {
            double before = owner.getHealth();
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(owner, amount, RegainReason.CUSTOM);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && event.getAmount() > 0.0) {
               owner.setHealth(Math.min(maximum, owner.getHealth() + event.getAmount()));
            }

            return Math.max(0.0, owner.getHealth() - before);
         }
      } else {
         return 0.0;
      }
   }

   private Optional<Location> findSpawn(Location aimed, ClassMinionTheme.Carrier carrier, int ordinal) {
      World world = aimed.getWorld();
      if (world == null) {
         return Optional.empty();
      } else {
         int[][] offsets = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {2, 0}, {-2, 0}};

         for (int step = 0; step < offsets.length; step++) {
            int[] offset = offsets[Math.floorMod(step + ordinal * 2, offsets.length)];
            int x = aimed.getBlockX() + offset[0];
            int z = aimed.getBlockZ() + offset[1];
            if (world.isChunkLoaded(x >> 4, z >> 4)) {
               for (int yOffset = 1; yOffset >= -2; yOffset--) {
                  int y = aimed.getBlockY() + yOffset;
                  Location candidate = new Location(world, x + 0.5, y, z + 0.5, aimed.getYaw(), 0.0F);
                  if (validSpawn(candidate, carrier)) {
                     return Optional.of(candidate);
                  }
               }
            }
         }

         return Optional.empty();
      }
   }

   private static boolean validSpawn(Location location, ClassMinionTheme.Carrier carrier) {
      if (!location.getBlock().isPassable() || !location.clone().add(0.0, 1.0, 0.0).getBlock().isPassable()) {
         return false;
      } else {
         return carrier.locomotion() == EliteMindBodyLocomotion.FLYING ? true : location.clone().add(0.0, -1.0, 0.0).getBlock().getType().isSolid();
      }
   }

   private boolean isMinion(Entity entity) {
      return entity != null && (this.minionsByEntity.containsKey(entity.getUniqueId()) || this.taggedMinion(entity));
   }

   private boolean taggedMinion(Entity entity) {
      return this.minionIdentity.matches(entity);
   }

   private static LivingEntity source(Entity damager) {
      if (damager instanceof LivingEntity living) {
         return living;
      } else if (damager instanceof Projectile projectile) {
         return projectile.getShooter() instanceof LivingEntity living ? living : null;
      } else {
         return null;
      }
   }

   private static boolean isNecromancerForm(String formId) {
      return "necromancer".equals(formId) || "lich".equals(formId) || "plaguebringer".equals(formId);
   }

   private static long expiresAt(long now, int durationTicks) {
      return Long.MAX_VALUE - now < durationTicks ? Long.MAX_VALUE : now + durationTicks;
   }

   private static CorpseTargetSelector.Point point(Location location) {
      return new CorpseTargetSelector.Point(location.getX(), location.getY(), location.getZ());
   }

   private static Particle summonParticle(ClassMinionTheme theme) {
      return switch (theme) {
         case UNDEAD -> Particle.SOUL;
         case ANIMAL -> Particle.HAPPY_VILLAGER;
         case NETHER -> Particle.SOUL_FIRE_FLAME;
         case SPIRIT -> Particle.END_ROD;
      };
   }

   private static Sound summonSound(ClassMinionTheme theme) {
      return switch (theme) {
         case UNDEAD -> Sound.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED;
         case ANIMAL -> Sound.ENTITY_WOLF_AMBIENT;
         case NETHER -> Sound.BLOCK_PORTAL_TRIGGER;
         case SPIRIT -> Sound.ENTITY_VEX_AMBIENT;
      };
   }

   private static float summonPitch(ClassMinionTheme theme) {
      return theme == ClassMinionTheme.NETHER ? 0.65F : 1.15F;
   }

   private record CorpseMarker(UUID id, Location location, TextDisplay display, long expiresAtTick) {
      private CorpseMarker(UUID id, Location location, TextDisplay display, long expiresAtTick) {
         location = location.clone();
         this.id = id;
         this.location = location;
         this.display = display;
         this.expiresAtTick = expiresAtTick;
      }

      public Location location() {
         return this.location.clone();
      }
   }

   @FunctionalInterface
   public interface MinionDamageEvaluator {
      ClassMinionManager.MinionDamageEvaluator NEUTRAL = (owner, target, rawDamage, domain) -> rawDamage;

      double evaluate(Player var1, EliteEntity var2, double var3, CombatDamageContext.ClassAbilityDamageDomain var5);

      default double healingDoneMultiplier(Player owner) {
         return 1.0;
      }
   }

   @FunctionalInterface
   public interface MinionImpactEffectApplier {
      ClassMinionManager.MinionImpactEffectApplier NONE = (owner, spec, target, effectiveLevel) -> {};

      void apply(Player var1, FixedAbilitySpec var2, LivingEntity var3, int var4);
   }

   private record MinionInstance(
      EliteEntity actor,
      UUID entityId,
      UUID ownerId,
      FixedAbilitySpec spec,
      int effectiveLevel,
      ClassMinionTheme theme,
      ClassMinionBalanceContract balance,
      ClassMinionControlState control,
      long expiresAtTick
   ) {
      LivingEntity body() {
         return this.actor.getLivingEntity();
      }
   }

   @FunctionalInterface
   public interface MinionRuntimeObserver {
      ClassMinionManager.MinionRuntimeObserver NONE = (casterId, targetId, abilityId, effect, amount) -> {};

      void applied(UUID var1, UUID var2, String var3, AbilityEffect var4, double var5);

      default void removed(UUID casterId, UUID minionId, String abilityId) {
      }
   }

   public record OwnerProfile(String activeFormId, int effectiveLevel) {
      public OwnerProfile(String activeFormId, int effectiveLevel) {
         Objects.requireNonNull(activeFormId, "activeFormId");
         if (effectiveLevel < 1) {
            throw new IllegalArgumentException("effectiveLevel must be positive");
         } else {
            this.activeFormId = activeFormId;
            this.effectiveLevel = effectiveLevel;
         }
      }
   }

   @FunctionalInterface
   public interface OwnerProfileResolver {
      Optional<ClassMinionManager.OwnerProfile> resolve(Player var1);
   }

   private record SpawnPlan(ClassMinionTheme.Carrier carrier, Location location) {
      private SpawnPlan(ClassMinionTheme.Carrier carrier, Location location) {
         location = location.clone();
         this.carrier = carrier;
         this.location = location;
      }

      public Location location() {
         return this.location.clone();
      }
   }

   public static enum Status {
      SUCCESS,
      NO_CORPSE,
      UNSAFE_DESTINATION,
      RUNTIME_UNAVAILABLE;
   }

   public record SummonResult(ClassMinionManager.Status status, List<LivingEntity> minions, Location origin) {
      public SummonResult(ClassMinionManager.Status status, List<LivingEntity> minions, Location origin) {
         Objects.requireNonNull(status, "status");
         minions = List.copyOf(minions);
         origin = origin == null ? null : origin.clone();
         if (status == ClassMinionManager.Status.SUCCESS != (!minions.isEmpty() && origin != null)) {
            throw new IllegalArgumentException("Successful summons need physical minions and an origin");
         } else {
            this.status = status;
            this.minions = minions;
            this.origin = origin;
         }
      }

      static ClassMinionManager.SummonResult success(List<LivingEntity> minions, Location origin) {
         return new ClassMinionManager.SummonResult(ClassMinionManager.Status.SUCCESS, minions, origin);
      }

      static ClassMinionManager.SummonResult failure(ClassMinionManager.Status status) {
         if (status == ClassMinionManager.Status.SUCCESS) {
            throw new IllegalArgumentException("Failure cannot be successful");
         } else {
            return new ClassMinionManager.SummonResult(status, List.of(), null);
         }
      }

      public Location origin() {
         return this.origin == null ? null : this.origin.clone();
      }
   }
}
