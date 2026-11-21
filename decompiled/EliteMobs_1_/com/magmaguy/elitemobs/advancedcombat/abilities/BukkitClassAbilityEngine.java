package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.advancedcombat.constructs.ClassAbilityConstructRuntime;
import com.magmaguy.elitemobs.advancedcombat.constructs.ClassConstructVisualRegistry;
import com.magmaguy.elitemobs.advancedcombat.minions.ClassMinionManager;
import com.magmaguy.elitemobs.advancedcombat.presentation.BukkitClassAbilityPresentation;
import com.magmaguy.elitemobs.instanced.InstancePlayerMovement;
import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpactRequest;
import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpacts;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class BukkitClassAbilityEngine implements ClassAbilityEngine {
   private static final double LEAP_MINIMUM_HORIZONTAL_VELOCITY = 0.8;
   private static final double LEAP_VERTICAL_VELOCITY = 0.9;
   private static final int LEAP_LANDING_TIMEOUT_TICKS = 80;
   private static final double PROJECTILE_BLOCKS_PER_TICK = 2.5;
   private final Plugin plugin;
   private final FixedAbilityRegistry registry;
   private final AbilitySemantics semantics;
   private final AbilityTargeting targeting;
   private final TimedCombatModifiers modifiers;
   private final AbilityStateRuntime states;
   private final AbilityEffects effects;
   private final FrenzyRuntime frenzy;
   private final ClassAbilityProjectileCarrier projectileCarriers;
   private final DivineSteedManager divineSteeds;
   private final GuardianFlightManager guardianFlights;
   private final ClassMinionManager classMinions;
   private final BukkitBlinkPotionFlash blinkPotionFlash;
   private final ClassAbilityConstructRuntime constructs;
   private final BukkitClassAbilityPresentation presentations = new BukkitClassAbilityPresentation();
   private final AutoCloseable controlAttributionLease;
   private final Map<AbilityFamily, BukkitClassAbilityEngine.AbilityExecutor> executors = new EnumMap<>(AbilityFamily.class);
   private final Map<UUID, Set<BukkitTask>> tasksByCaster = new HashMap<>();
   private final Map<UUID, BukkitClassAbilityEngine.SanctuaryAnchor> sanctuaryAnchors = new HashMap<>();
   private boolean closed;

   public BukkitClassAbilityEngine(
      Plugin plugin,
      FixedAbilityRegistry registry,
      final AbilitySemantics semantics,
      ClassMinionManager.OwnerProfileResolver ownerProfiles,
      ClassMinionManager.MinionDamageEvaluator minionDamageEvaluator,
      ClassAbilityDamage.Delivery damageDelivery
   ) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.semantics = Objects.requireNonNull(semantics, "semantics");
      this.registry = Objects.requireNonNull(registry, "registry");
      this.targeting = new AbilityTargeting(semantics);
      this.modifiers = new TimedCombatModifiers(plugin, semantics);
      ClassAbilityDamage damage = new ClassAbilityDamage(damageDelivery);
      this.states = new AbilityStateRuntime(plugin, semantics, registry, damage);
      this.effects = new AbilityEffects(semantics, this.modifiers, this.states, damage);
      this.controlAttributionLease = ClassControlAttribution.install(
         (caster, target) -> this.states.hasOwnedTaunt(caster, target) || this.effects.controlledBy(caster, target)
      );
      this.frenzy = new FrenzyRuntime(plugin);
      this.projectileCarriers = new ClassAbilityProjectileCarrier(plugin);
      this.divineSteeds = new DivineSteedManager(plugin);
      this.guardianFlights = new GuardianFlightManager(plugin);
      this.classMinions = new ClassMinionManager(
         plugin,
         ownerProfiles,
         minionDamageEvaluator,
         this.effects::applyMinionImpact,
         new ClassMinionManager.MinionRuntimeObserver() {
            @Override
            public void applied(UUID casterId, UUID targetId, String abilityId, AbilityEffect effect, double amount) {
               semantics.observe(
                  AbilityRuntimeObservation.effect(
                     effect == AbilityEffect.LIFESTEAL ? AbilityRuntimeObservation.Kind.HEAL : AbilityRuntimeObservation.Kind.DAMAGE,
                     casterId,
                     targetId,
                     abilityId,
                     amount,
                     0,
                     effect
                  )
               );
            }

            @Override
            public void removed(UUID casterId, UUID minionId, String abilityId) {
               semantics.observe(
                  new AbilityRuntimeObservation(AbilityRuntimeObservation.Kind.SUMMON_CLEARED, casterId, minionId, abilityId, 1.0, 0, 1, Set.of(), Set.of())
               );
            }
         }
      );
      this.blinkPotionFlash = new BukkitBlinkPotionFlash(plugin);
      this.constructs = new ClassAbilityConstructRuntime(
         plugin,
         new ClassAbilityConstructRuntime.LifecycleObserver() {
            @Override
            public void spawned(UUID casterId, String abilityId, int durationTicks, ClassConstructVisualRegistry.AnchorMode anchorMode) {
               semantics.observe(
                  new AbilityRuntimeObservation(
                     AbilityRuntimeObservation.Kind.CONSTRUCT_SPAWNED,
                     casterId,
                     casterId,
                     abilityId,
                     1.0,
                     durationTicks,
                     1,
                     Set.of(),
                     BukkitClassAbilityEngine.constructMechanics(anchorMode)
                  )
               );
            }

            @Override
            public void cleared(UUID casterId, String abilityId, ClassConstructVisualRegistry.AnchorMode anchorMode) {
               semantics.observe(
                  new AbilityRuntimeObservation(
                     AbilityRuntimeObservation.Kind.CONSTRUCT_CLEARED,
                     casterId,
                     casterId,
                     abilityId,
                     1.0,
                     0,
                     1,
                     Set.of(),
                     BukkitClassAbilityEngine.constructMechanics(anchorMode)
                  )
               );
            }
         }
      );
      this.executors.put(AbilityFamily.MOUNTED_CHARGE, this::executeMountedCharge);
      this.executors.put(AbilityFamily.BALLISTIC_LEAP, this::executeLeap);
      this.executors.put(AbilityFamily.SAFE_DASH, this::executeDash);
      this.executors.put(AbilityFamily.SAFE_BLINK, this::executeBlink);
      this.executors.put(AbilityFamily.ALLY_FLIGHT, this::executeAllyFlight);
      this.executors.put(AbilityFamily.INSTANT, this::executeInstant);
      this.executors.put(AbilityFamily.PROJECTILE, this::executeProjectile);
      this.executors.put(AbilityFamily.SUMMON, this::executeSummon);
      this.executors.put(AbilityFamily.ZONE, this::executeZone);
      if (this.executors.size() != AbilityFamily.values().length) {
         throw new IllegalStateException("Every ability family requires an executor");
      }
   }

   @Override
   public AbilityResult execute(Player player, ClassLineage lineage, AbilitySlot slot, int effectiveLevel) {
      Objects.requireNonNull(player, "player");
      Objects.requireNonNull(lineage, "lineage");
      Objects.requireNonNull(slot, "slot");
      AbilityDefinition ability = abilityFor(lineage, slot);
      if (this.closed) {
         return AbilityResult.failure(ability.id(), AbilityFailureReason.ENGINE_CLOSED);
      } else if (!Bukkit.isPrimaryThread()) {
         return AbilityResult.failure(ability.id(), AbilityFailureReason.WRONG_THREAD);
      } else if (player.isOnline() && player.isValid() && !player.isDead()) {
         if (!lineage.activeForm().band().containsEffectiveLevel(effectiveLevel)) {
            return AbilityResult.failure(ability.id(), AbilityFailureReason.INVALID_LEVEL);
         } else {
            FixedAbilitySpec spec;
            try {
               spec = this.registry.require(ability.id());
            } catch (IllegalArgumentException var10) {
               return AbilityResult.failure(ability.id(), AbilityFailureReason.ABILITY_NOT_REGISTERED);
            }

            BukkitClassAbilityEngine.AbilityExecutor executor = this.executors.get(spec.family());
            BukkitClassAbilityPresentation.Session presentation = this.presentations.begin(player, lineage, spec);
            AbilityResult result = executor.execute(player, spec, effectiveLevel, presentation);
            if (result.successful()) {
               presentation.commit();
            }

            return result;
         }
      } else {
         return AbilityResult.failure(ability.id(), AbilityFailureReason.INVALID_PLAYER);
      }
   }

   public FixedAbilityRegistry registry() {
      return this.registry;
   }

   private AbilityResult executeInstant(Player player, FixedAbilitySpec spec, int effectiveLevel, BukkitClassAbilityPresentation.Session presentation) {
      AbilityTargeting.TargetSelection selection = this.targeting.select(player, spec);
      if (!hasApplicableTarget(spec, selection)) {
         return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
      } else if (!this.states.canActivate(player, spec, selection)) {
         return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
      } else {
         this.presentConstruct(player, spec, selection.origin());
         if (spec.executionTraits().mechanics().contains(AbilityMechanic.CHAINING_CAST)) {
            this.launchEffectChain(player, spec, selection, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
         } else {
            AbilityContribution initial = this.applyAndPresent(presentation, player, spec, selection, effectiveLevel);
            if (spec.executionTraits().mechanics().contains(AbilityMechanic.MISSING_HEALTH_SCALING)) {
               double levelScale = AbilityLevelScaling.multiplier(effectiveLevel);
               this.frenzy
                  .activate(
                     player,
                     Math.max(0.0, spec.tuning().modifierMultiplier() - 1.0) * levelScale,
                     0.4 * levelScale,
                     FrenzyScalingPolicy.durationTicks(spec.tuning(), effectiveLevel),
                     spec.id()
                  );
            }

            if (spec.executionTraits().mechanics().contains(AbilityMechanic.FOLLOW_CASTER_FIELD)) {
               this.launchFollowingPulses(player, spec, effectiveLevel, presentation);
               return AbilityResult.scheduled(spec.id(), initial);
            } else {
               return AbilityResult.success(spec.id(), initial);
            }
         }
      }
   }

   private void launchEffectChain(
      final Player caster,
      final FixedAbilitySpec spec,
      AbilityTargeting.TargetSelection selection,
      final int effectiveLevel,
      final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      final List<LivingEntity> enemies = new ArrayList<>(selection.enemies());
      final List<Player> allies = new ArrayList<>(selection.allies());
      if (spec.executionTraits().mechanics().contains(AbilityMechanic.LOWEST_HEALTH_FIRST)) {
         allies.sort(Comparator.comparingDouble(BukkitClassAbilityEngine::healthFraction));
      }

      final List<Long> allyLifecycleTokens = allies.stream().map(this.states::lifecycleToken).toList();
      final int count = Math.max(enemies.size(), allies.size());
      if (count != 0) {
         BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
               private int index;

               @Override
               protected void tick() {
                  if (BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken) && this.index < count) {
                     LivingEntity enemy = this.index < enemies.size() ? enemies.get(this.index) : null;
                     Player ally = this.index < allies.size() ? allies.get(this.index) : null;
                     long allyLifecycleToken = this.index < allyLifecycleTokens.size() ? allyLifecycleTokens.get(this.index) : Long.MIN_VALUE;
                     List<LivingEntity> currentEnemies = enemy != null && BukkitClassAbilityEngine.this.semantics.canTargetEnemy(caster, enemy, spec)
                        ? List.of(enemy)
                        : List.of();
                     List<Player> currentAllies = BukkitClassAbilityEngine.this.isCurrentChainAlly(caster, ally, allyLifecycleToken)
                        ? List.of(ally)
                        : List.of();
                     this.index++;
                     if (!currentEnemies.isEmpty() || !currentAllies.isEmpty()) {
                        Location origin = !currentEnemies.isEmpty() ? currentEnemies.get(0).getLocation() : currentAllies.get(0).getLocation();
                        AbilityContribution contribution = BukkitClassAbilityEngine.this.applyAndPresent(
                           presentation, caster, spec, new AbilityTargeting.TargetSelection(currentEnemies, currentAllies, origin), effectiveLevel
                        );
                        if (contribution.isMeaningful()) {
                           BukkitClassAbilityEngine.this.semantics.recordContribution(caster, spec.id(), contribution);
                        }

                        origin.getWorld().spawnParticle(Particle.END_ROD, origin, 8, 0.25, 0.3, 0.25, 0.02);
                     }
                  } else {
                     BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                  }
               }
            })
            .runTaskTimer(this.plugin, 1L, 3L);
         this.track(caster.getUniqueId(), task);
      }
   }

   private boolean isCurrentChainAlly(Player caster, Player candidate, long lifecycleToken) {
      if (candidate == null
         || !this.states.isCurrentLifecycle(candidate, lifecycleToken)
         || !candidate.isOnline()
         || !candidate.isValid()
         || candidate.isDead()
         || !ClassAbilityEligibility.isEligible(candidate)
         || !candidate.getWorld().equals(caster.getWorld())) {
         return false;
      } else {
         return candidate.getUniqueId().equals(caster.getUniqueId())
            ? true
            : this.semantics.alliesOf(caster).stream().filter(Objects::nonNull).anyMatch(current -> current.getUniqueId().equals(candidate.getUniqueId()));
      }
   }

   private void launchFollowingPulses(
      final Player caster, final FixedAbilitySpec spec, final int effectiveLevel, final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      final int pulses = Math.max(2, Math.min(5, spec.tuning().durationTicks() / 20));
      BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
            private int completed;

            @Override
            protected void tick() {
               if (BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken) && this.completed < pulses) {
                  Location origin = caster.getLocation();
                  AbilityTargeting.TargetSelection current = BukkitClassAbilityEngine.this.targeting.select(caster, spec, origin);
                  AbilityContribution contribution = BukkitClassAbilityEngine.this.applyPeriodicAndPresent(presentation, caster, spec, current, effectiveLevel);
                  if (contribution.isMeaningful()) {
                     BukkitClassAbilityEngine.this.semantics.recordContribution(caster, spec.id(), contribution);
                  }

                  origin.getWorld()
                     .spawnParticle(
                        Particle.SWEEP_ATTACK, origin, 8, Math.max(1.0, spec.tuning().radius() * 0.5), 0.4, Math.max(1.0, spec.tuning().radius() * 0.5), 0.03
                     );
                  this.completed++;
               } else {
                  BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
               }
            }
         })
         .runTaskTimer(this.plugin, 20L, 20L);
      this.track(caster.getUniqueId(), task);
   }

   private AbilityResult executeProjectile(Player player, FixedAbilitySpec spec, int effectiveLevel, BukkitClassAbilityPresentation.Session presentation) {
      Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
      AbilityTargeting.TargetSelection selection;
      if (mechanics.contains(AbilityMechanic.PIERCING_CAST)) {
         List<LivingEntity> intercepts = this.targeting.piercingEnemies(player, spec);
         Location origin = intercepts.isEmpty() ? player.getLocation() : intercepts.get(intercepts.size() - 1).getLocation();
         selection = new AbilityTargeting.TargetSelection(intercepts, List.of(), origin);
      } else {
         selection = this.targeting.select(player, spec);
      }

      List<LivingEntity> visibleEnemies = selection.enemies()
         .stream()
         .filter(enemy -> clearSegment(player.getEyeLocation(), enemy.getLocation().add(0.0, enemy.getHeight() * 0.55, 0.0)))
         .toList();
      selection = new AbilityTargeting.TargetSelection(visibleEnemies, selection.allies(), selection.origin());
      if (!this.states.canActivate(player, spec, selection)) {
         return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
      } else {
         long windUp = mechanics.contains(AbilityMechanic.WIND_UP) ? 15L : 0L;
         if (windUp > 0L) {
            player.getWorld().spawnParticle(Particle.ENCHANT, player.getEyeLocation(), 24, 0.35, 0.35, 0.35, 0.03);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.7F, 1.5F);
         }

         if (selection.enemies().isEmpty()) {
            return this.launchUnaimedProjectile(player, spec, effectiveLevel, windUp, presentation);
         } else {
            if (mechanics.contains(AbilityMechanic.CHAINING_CAST)) {
               this.launchChain(player, spec, selection.enemies(), effectiveLevel, presentation);
            } else if (mechanics.contains(AbilityMechanic.PIERCING_CAST)) {
               List<LivingEntity> pierced = selection.enemies();
               this.launchProjectile(
                  player,
                  spec,
                  pierced.get(pierced.size() - 1),
                  new AbilityTargeting.TargetSelection(pierced, selection.allies(), selection.origin()),
                  effectiveLevel,
                  windUp,
                  presentation
               );
            } else if (spec.target() == AbilityTarget.AIMED_ENEMY && spec.tuning().projectileCount() > 1) {
               LivingEntity target = selection.enemies().get(0);

               for (int index = 0; index < spec.tuning().projectileCount(); index++) {
                  this.launchProjectile(
                     player,
                     spec,
                     target,
                     new AbilityTargeting.TargetSelection(List.of(target), selection.allies(), target.getLocation()),
                     effectiveLevel,
                     windUp + index * 2L,
                     presentation
                  );
               }
            } else {
               List<LivingEntity> targets = selection.enemies().stream().limit(spec.tuning().projectileCount()).toList();

               for (int index = 0; index < targets.size(); index++) {
                  LivingEntity target = targets.get(index);
                  this.launchProjectile(
                     player,
                     spec,
                     target,
                     new AbilityTargeting.TargetSelection(List.of(target), selection.allies(), target.getLocation()),
                     effectiveLevel,
                     windUp + index * 2L,
                     presentation
                  );
               }
            }

            player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation(), Math.max(4, spec.tuning().projectileCount() * 3), 0.3, 0.3, 0.3, 0.35);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.8F, 1.15F);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
         }
      }
   }

   private AbilityResult executeZone(
      final Player player, final FixedAbilitySpec spec, final int effectiveLevel, final BukkitClassAbilityPresentation.Session presentation
   ) {
      final Location origin = spec.target() == AbilityTarget.AIMED_LOCATION
         ? this.targeting.aimedLocation(player, spec.tuning().range())
         : player.getLocation();
      if (origin.getWorld() != null && origin.getWorld().isChunkLoaded(origin.getBlockX() >> 4, origin.getBlockZ() >> 4)) {
         this.presentConstruct(player, spec, origin);
         int totalDuration = spec.effects().contains(AbilityEffect.HEAL)
            ? AbilityHealingPolicy.durationTicks(spec.tuning().durationTicks(), AbilityHealingType.PERIODIC, this.semantics.mechanicModifiers(player))
            : spec.tuning().durationTicks();
         if (spec.executionTraits().establishesMobilityAnchor()) {
            this.sanctuaryAnchors.put(player.getUniqueId(), new BukkitClassAbilityEngine.SanctuaryAnchor(origin, expiresAt(totalDuration)));
         }

         origin.getWorld().spawnParticle(Particle.ENCHANT, origin, 32, spec.tuning().radius() * 0.5, 0.3, spec.tuning().radius() * 0.5, 0.03);
         if (spec.executionTraits().mechanics().contains(AbilityMechanic.CLUSTER_PROJECTILE)) {
            this.launchClusterProjectile(player, spec, origin, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
         } else if (spec.executionTraits().mechanics().contains(AbilityMechanic.EXPANDING_PULSES)) {
            this.launchExpandingPulses(player, spec, origin, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
         } else if (spec.executionTraits().mechanics().contains(AbilityMechanic.PROJECTILE_BOMBARDMENT)) {
            this.launchProjectileBombardment(player, spec, origin, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
         } else if (spec.executionTraits().mechanics().contains(AbilityMechanic.WIND_UP)) {
            this.launchWoundUpZone(player, spec, origin, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
         } else {
            AbilityTargeting.TargetSelection initialSelection = this.targeting.select(player, spec, origin);
            AbilityContribution initial = this.applyPeriodicAndPresent(presentation, player, spec, initialSelection, effectiveLevel);
            if (spec.tuning().repetitions() <= 1) {
               return AbilityResult.success(spec.id(), initial);
            } else {
               int period = Math.max(1, totalDuration / spec.tuning().repetitions());
               final long casterLifecycleToken = this.states.lifecycleToken(player);
               BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(player.getUniqueId(), spec.id()) {
                     private int repetitions = 1;

                     @Override
                     protected void tick() {
                        if (BukkitClassAbilityEngine.this.currentCast(player, casterLifecycleToken) && this.repetitions < spec.tuning().repetitions()) {
                           Location pulseOrigin = spec.executionTraits().mechanics().contains(AbilityMechanic.FOLLOW_CASTER_FIELD)
                              ? player.getLocation()
                              : origin;
                           AbilityTargeting.TargetSelection selection = BukkitClassAbilityEngine.this.targeting.select(player, spec, pulseOrigin);
                           AbilityContribution delayed = BukkitClassAbilityEngine.this.applyPeriodicAndPresent(
                              presentation, player, spec, selection, effectiveLevel
                           );
                           if (delayed.isMeaningful()) {
                              BukkitClassAbilityEngine.this.semantics.recordContribution(player, spec.id(), delayed);
                           }

                           if (pulseOrigin.getWorld() != null) {
                              pulseOrigin.getWorld()
                                 .spawnParticle(Particle.ENCHANT, pulseOrigin, 18, spec.tuning().radius() * 0.45, 0.25, spec.tuning().radius() * 0.45, 0.02);
                           }

                           this.repetitions++;
                        } else {
                           BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
                        }
                     }
                  })
                  .runTaskTimer(this.plugin, period, period);
               this.track(player.getUniqueId(), task);
               return AbilityResult.scheduled(spec.id(), initial);
            }
         }
      } else {
         return AbilityResult.failure(spec.id(), AbilityFailureReason.UNSAFE_DESTINATION);
      }
   }

   private void launchExpandingPulses(
      final Player caster,
      final FixedAbilitySpec spec,
      final Location origin,
      final int effectiveLevel,
      final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      final ExpandingPulsePlan plan = ExpandingPulsePlan.create(spec.tuning().radius(), spec.tuning().repetitions(), spec.tuning().durationTicks());
      final Set<UUID> struck = new HashSet<>();
      BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
            private int age;
            private int nextPulse;

            @Override
            protected void tick() {
               if (BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken)
                  && caster.getWorld().equals(origin.getWorld())
                  && this.nextPulse < plan.pulses().size()) {
                  while (this.nextPulse < plan.pulses().size() && plan.pulses().get(this.nextPulse).delayTicks() <= this.age) {
                     ExpandingPulsePlan.Pulse pulse = plan.pulses().get(this.nextPulse++);
                     AbilityTargeting.TargetSelection candidates = BukkitClassAbilityEngine.this.targeting.select(caster, spec, origin);
                     List<LivingEntity> newlyReached = candidates.enemies()
                        .stream()
                        .filter(enemy -> pulse.contains(BukkitClassAbilityEngine.horizontalDistance(origin, enemy.getLocation())))
                        .filter(enemy -> struck.add(enemy.getUniqueId()))
                        .toList();
                     AbilityContribution contribution = BukkitClassAbilityEngine.this.applyPeriodicAndPresent(
                        presentation, caster, spec, new AbilityTargeting.TargetSelection(newlyReached, List.of(), origin), effectiveLevel
                     );
                     if (contribution.isMeaningful()) {
                        BukkitClassAbilityEngine.this.semantics.recordContribution(caster, spec.id(), contribution);
                     }

                     BukkitClassAbilityEngine.renderExpandingRing(origin, pulse);
                  }

                  this.age++;
               } else {
                  BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
               }
            }
         })
         .runTaskTimer(this.plugin, 1L, 1L);
      this.track(caster.getUniqueId(), task);
   }

   private static void renderExpandingRing(Location origin, ExpandingPulsePlan.Pulse pulse) {
      if (origin.getWorld() != null) {
         int samples = Math.max(18, (int)Math.ceil(pulse.outerRadiusInclusive() * 5.0));

         for (int sample = 0; sample < samples; sample++) {
            double angle = (Math.PI * 2) * sample / samples;
            Location point = origin.clone().add(Math.cos(angle) * pulse.outerRadiusInclusive(), 0.12, Math.sin(angle) * pulse.outerRadiusInclusive());
            origin.getWorld().spawnParticle(Particle.DUST_PLUME, point, 1, 0.04, 0.035, 0.04, 0.005);
         }

         origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 0.55F, 0.72F + pulse.index() * 0.13F);
      }
   }

   private void launchProjectileBombardment(
      final Player caster,
      final FixedAbilitySpec spec,
      final Location origin,
      final int effectiveLevel,
      final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      int fallTicks = Math.max(4, Math.min(8, spec.tuning().durationTicks() / 10));
      long spreadSeed = caster.getUniqueId().getMostSignificantBits()
         ^ caster.getUniqueId().getLeastSignificantBits()
         ^ Double.doubleToLongBits(origin.getX())
         ^ Long.rotateLeft(Double.doubleToLongBits(origin.getZ()), 23)
         ^ System.nanoTime();
      final ProjectileBombardmentPlan plan = ProjectileBombardmentPlan.create(
         spec.tuning().radius(), spec.tuning().durationTicks(), spec.tuning().repetitions(), spec.tuning().projectileCount(), fallTicks, spreadSeed
      );
      final int finalImpactTick = plan.impacts().get(plan.impacts().size() - 1).impactTick();
      BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
            private int age;
            private int firstActive;
            private int nextLaunch;

            @Override
            protected void tick() {
               if (BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken)
                  && caster.getWorld().equals(origin.getWorld())
                  && origin.getWorld() != null
                  && origin.getWorld().isChunkLoaded(origin.getBlockX() >> 4, origin.getBlockZ() >> 4)) {
                  while (this.nextLaunch < plan.impacts().size() && plan.impacts().get(this.nextLaunch).launchTick() <= this.age) {
                     this.nextLaunch++;
                  }

                  while (this.firstActive < this.nextLaunch && plan.impacts().get(this.firstActive).impactTick() < this.age) {
                     this.firstActive++;
                  }

                  for (int index = this.firstActive; index < this.nextLaunch; index++) {
                     ProjectileBombardmentPlan.Impact impact = plan.impacts().get(index);
                     Location destination = BukkitClassAbilityEngine.bombardmentImpactLocation(origin, impact);
                     if (this.age == impact.impactTick()) {
                        BukkitClassAbilityEngine.this.applyBombardmentImpact(caster, spec, destination, plan.impactRadius(), effectiveLevel, presentation);
                     } else {
                        BukkitClassAbilityEngine.renderBombardmentProjectile(destination, impact, this.age);
                     }
                  }

                  if (this.age++ >= finalImpactTick) {
                     BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                  }
               } else {
                  BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
               }
            }
         })
         .runTaskTimer(this.plugin, 1L, 1L);
      this.track(caster.getUniqueId(), task);
   }

   private void observeProjectileImpact(Player caster, FixedAbilitySpec spec, int selectedEnemies) {
      this.semantics
         .observe(
            new AbilityRuntimeObservation(
               AbilityRuntimeObservation.Kind.PROJECTILE_IMPACT,
               caster.getUniqueId(),
               null,
               spec.id(),
               selectedEnemies,
               0,
               1,
               spec.effects(),
               spec.executionTraits().mechanics()
            )
         );
   }

   private void applyBombardmentImpact(
      Player caster, FixedAbilitySpec spec, Location impact, double impactRadius, int effectiveLevel, BukkitClassAbilityPresentation.Session presentation
   ) {
      List<LivingEntity> target = this.targeting.enemiesNear(caster, impact, impactRadius, spec).stream().limit(1L).toList();
      this.observeProjectileImpact(caster, spec, target.size());
      if (!target.isEmpty()) {
         AbilityContribution contribution = this.applyProjectileAndPresent(
            presentation, caster, spec, new AbilityTargeting.TargetSelection(target, List.of(), impact), effectiveLevel
         );
         if (contribution.isMeaningful()) {
            this.semantics.recordContribution(caster, spec.id(), contribution);
         }
      }

      impact.getWorld().spawnParticle(Particle.CRIT, impact, 8, 0.18, 0.12, 0.18, 0.04);
      impact.getWorld().spawnParticle(Particle.DUST_PLUME, impact, 5, 0.22, 0.08, 0.22, 0.015);
      impact.getWorld().playSound(impact, Sound.ENTITY_ARROW_HIT_PLAYER, 0.4F, 1.25F);
   }

   private static Location bombardmentImpactLocation(Location origin, ProjectileBombardmentPlan.Impact impact) {
      return origin.clone().add(impact.offsetX(), 0.15, impact.offsetZ());
   }

   private void launchClusterProjectile(
      final Player caster,
      final FixedAbilitySpec spec,
      final Location origin,
      final int effectiveLevel,
      final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      final Location start = caster.getEyeLocation().clone();
      final int travelTicks = Math.max(3, Math.min(12, (int)Math.ceil(start.distance(origin) / 2.5)));
      final List<ClusterImpactPlan.Impact> impacts = ClusterImpactPlan.create(
         spec.tuning().radius(), spec.tuning().repetitions(), Math.toRadians(caster.getLocation().getYaw())
      );
      final int finalTick = travelTicks + impacts.get(impacts.size() - 1).delayTicks();
      BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
            private int age;

            @Override
            protected void tick() {
               if (BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken) && caster.getWorld().equals(origin.getWorld())) {
                  if (this.age < travelTicks) {
                     BukkitClassAbilityEngine.renderProjectile(
                        spec, BukkitClassAbilityEngine.lerp(start, origin, (this.age + 1.0) / travelTicks), (this.age + 1.0) / travelTicks
                     );
                  }

                  for (ClusterImpactPlan.Impact impact : impacts) {
                     if (this.age == travelTicks + impact.delayTicks()) {
                        Location location = origin.clone().add(impact.offsetX(), 0.15, impact.offsetZ());
                        AbilityTargeting.TargetSelection selection = BukkitClassAbilityEngine.this.targeting
                           .selectArea(caster, spec, location, impact.blastRadius());
                        BukkitClassAbilityEngine.this.observeProjectileImpact(caster, spec, selection.enemies().size());
                        AbilityContribution contribution = BukkitClassAbilityEngine.this.applyProjectileAndPresent(
                           presentation, caster, spec, selection, effectiveLevel
                        );
                        if (contribution.isMeaningful()) {
                           BukkitClassAbilityEngine.this.semantics.recordContribution(caster, spec.id(), contribution);
                        }

                        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 3, impact.blastRadius() * 0.35, 0.2, impact.blastRadius() * 0.35, 0.02);
                        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.8F, 1.15F + impact.delayTicks() * 0.02F);
                     }
                  }

                  if (this.age++ >= finalTick) {
                     BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                  }
               } else {
                  BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
               }
            }
         })
         .runTaskTimer(this.plugin, 1L, 1L);
      this.track(caster.getUniqueId(), task);
   }

   private static void renderBombardmentProjectile(Location impactLocation, ProjectileBombardmentPlan.Impact impact, int age) {
      double progress = (double)(age - impact.launchTick()) / (impact.impactTick() - impact.launchTick());
      Location projectile = impactLocation.clone().add(0.0, (1.0 - progress) * 9.0, 0.0);
      projectile.getWorld().spawnParticle(Particle.CRIT, projectile, 2, 0.025, 0.08, 0.025, 0.01);
      projectile.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, projectile, 1, 0.02, 0.04, 0.02, 0.005);
   }

   private AbilityResult launchUnaimedProjectile(
      final Player player, final FixedAbilitySpec spec, final int effectiveLevel, long delayTicks, final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(player);
      final Location castOrigin = player.getEyeLocation().clone();
      final Location destination = castOrigin.clone().add(castOrigin.getDirection().normalize().multiply(Math.max(4.0, spec.tuning().range())));
      int projectiles = Math.max(1, spec.tuning().projectileCount());

      for (int index = 0; index < projectiles; index++) {
         BukkitTask launch = (new BukkitClassAbilityEngine.TrackedAbilityTask(player.getUniqueId(), spec.id()) {
               @Override
               protected void tick() {
                  if (BukkitClassAbilityEngine.this.currentCast(player, casterLifecycleToken) && player.getWorld().equals(destination.getWorld())) {
                     BukkitClassAbilityEngine.this.projectileCarriers
                        .launch(
                           player,
                           destination,
                           spec.tuning().range(),
                           2.5,
                           (projectile, hitEntity) -> BukkitClassAbilityEngine.this.impactPhysicalProjectile(
                              player, projectile, spec, hitEntity, castOrigin, casterLifecycleToken, effectiveLevel, presentation
                           ),
                           (point, progress) -> BukkitClassAbilityEngine.renderProjectile(spec, point, progress)
                        );
                  }

                  BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
               }
            })
            .runTaskLater(this.plugin, Math.max(1L, delayTicks + index * 2L));
         this.track(player.getUniqueId(), launch);
      }

      player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation(), Math.max(4, projectiles * 3), 0.3, 0.3, 0.3, 0.35);
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.8F, 1.15F);
      return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
   }

   private void launchProjectile(
      Player caster,
      FixedAbilitySpec spec,
      LivingEntity primaryTarget,
      AbilityTargeting.TargetSelection impactSelection,
      int effectiveLevel,
      long delayTicks,
      BukkitClassAbilityPresentation.Session presentation
   ) {
      if (!spec.executionTraits().mechanics().contains(AbilityMechanic.PIERCING_CAST)) {
         this.launchPhysicalProjectile(caster, spec, primaryTarget, effectiveLevel, delayTicks, presentation);
      } else {
         this.launchSimulatedPiercingProjectile(caster, spec, primaryTarget, impactSelection, effectiveLevel, delayTicks, presentation);
      }
   }

   private void launchPhysicalProjectile(
      final Player caster,
      final FixedAbilitySpec spec,
      LivingEntity primaryTarget,
      final int effectiveLevel,
      long delayTicks,
      final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      final Location castOrigin = caster.getEyeLocation().clone();
      final Location destination = primaryTarget.getLocation().add(0.0, primaryTarget.getHeight() * 0.55, 0.0);
      BukkitTask launch = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
            @Override
            protected void tick() {
               if (BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken) && caster.getWorld().equals(destination.getWorld())) {
                  BukkitClassAbilityEngine.this.projectileCarriers
                     .launch(
                        caster,
                        destination,
                        spec.tuning().range(),
                        2.5,
                        (projectile, hitEntity) -> BukkitClassAbilityEngine.this.impactPhysicalProjectile(
                           caster, projectile, spec, hitEntity, castOrigin, casterLifecycleToken, effectiveLevel, presentation
                        ),
                        (point, progress) -> BukkitClassAbilityEngine.renderProjectile(spec, point, progress)
                     );
               }

               BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
            }
         })
         .runTaskLater(this.plugin, Math.max(1L, delayTicks));
      this.track(caster.getUniqueId(), launch);
   }

   private void impactPhysicalProjectile(
      Player caster,
      Projectile projectile,
      FixedAbilitySpec spec,
      LivingEntity hitEntity,
      Location castOrigin,
      long casterLifecycleToken,
      int effectiveLevel,
      BukkitClassAbilityPresentation.Session presentation
   ) {
      if (this.currentCast(caster, casterLifecycleToken) && this.semantics.canTargetEnemy(caster, hitEntity, spec)) {
         Location impact = hitEntity.getLocation().add(0.0, hitEntity.getHeight() * 0.55, 0.0);
         if (validMinimumRangeImpact(spec, castOrigin, impact, hitEntity)) {
            if (spec.executionTraits().mechanics().contains(AbilityMechanic.DELAYED_PAYLOAD)) {
               this.armPayload(caster, spec, hitEntity, effectiveLevel, presentation);
            } else {
               AbilityTargeting.TargetSelection selection = new AbilityTargeting.TargetSelection(List.of(hitEntity), List.of(), impact);
               AbilityContribution contribution = this.effects.applyProjectile(caster, projectile, spec, selection, effectiveLevel);
               presentation.effects(selection.origin(), selection.enemies(), selection.allies());
               if (contribution.isMeaningful()) {
                  this.semantics.recordContribution(caster, spec.id(), contribution);
               }
            }

            impact.getWorld().spawnParticle(impactParticle(spec), impact, 14, 0.3, 0.3, 0.3, 0.04);
         }
      }
   }

   private void launchSimulatedPiercingProjectile(
      final Player caster,
      final FixedAbilitySpec spec,
      final LivingEntity primaryTarget,
      final AbilityTargeting.TargetSelection impactSelection,
      final int effectiveLevel,
      long delayTicks,
      final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      final Location start = caster.getEyeLocation().clone();
      final Location destination = primaryTarget.getLocation().add(0.0, primaryTarget.getHeight() * 0.55, 0.0);
      final int travelTicks = Math.max(2, Math.min(18, (int)Math.ceil(start.distance(destination) / 2.5)));
      BukkitTask launch = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
            private int age;
            private Location previous = start.clone();

            @Override
            protected void tick() {
               boolean current = BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken);
               if (current && caster.getWorld().equals(start.getWorld()) && this.age < travelTicks) {
                  this.age++;
                  double progress = (double)this.age / travelTicks;
                  Location next = BukkitClassAbilityEngine.lerp(start, destination, progress);
                  if (!BukkitClassAbilityEngine.clearSegment(this.previous, next)) {
                     next.getWorld().spawnParticle(Particle.SMOKE, next, 8, 0.12, 0.12, 0.12, 0.01);
                     BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                  } else {
                     BukkitClassAbilityEngine.renderProjectile(spec, next, progress);
                     this.previous = next;
                  }
               } else {
                  if (current && this.age >= travelTicks) {
                     this.impact();
                  }

                  BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
               }
            }

            private void impact() {
               if (primaryTarget.isValid()
                  && !primaryTarget.isDead()
                  && primaryTarget.getWorld().equals(destination.getWorld())
                  && !(primaryTarget.getLocation().distanceSquared(destination) > 10.0)) {
                  if (BukkitClassAbilityEngine.validMinimumRangeImpact(spec, start, destination, primaryTarget)) {
                     if (spec.executionTraits().mechanics().contains(AbilityMechanic.DELAYED_PAYLOAD)) {
                        BukkitClassAbilityEngine.this.armPayload(caster, spec, primaryTarget, effectiveLevel, presentation);
                     } else {
                        List<LivingEntity> currentTargets = impactSelection.enemies()
                           .stream()
                           .filter(target -> BukkitClassAbilityEngine.this.semantics.canTargetEnemy(caster, target, spec))
                           .filter(target -> BukkitClassAbilityEngine.validMinimumRangeImpact(spec, start, target.getLocation(), target))
                           .toList();
                        if (!currentTargets.isEmpty()) {
                           AbilityContribution contribution = BukkitClassAbilityEngine.this.applyAndPresent(
                              presentation,
                              caster,
                              spec,
                              new AbilityTargeting.TargetSelection(currentTargets, impactSelection.allies(), destination),
                              effectiveLevel
                           );
                           if (contribution.isMeaningful()) {
                              BukkitClassAbilityEngine.this.semantics.recordContribution(caster, spec.id(), contribution);
                           }

                           destination.getWorld().spawnParticle(BukkitClassAbilityEngine.impactParticle(spec), destination, 14, 0.3, 0.3, 0.3, 0.04);
                        }
                     }
                  }
               }
            }
         })
         .runTaskTimer(this.plugin, Math.max(1L, delayTicks), 1L);
      this.track(caster.getUniqueId(), launch);
   }

   private void launchChain(
      final Player caster,
      final FixedAbilitySpec spec,
      List<LivingEntity> candidates,
      final int effectiveLevel,
      final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      final List<LivingEntity> chain = candidates.stream()
         .filter(entity -> entity != null && entity.isValid() && !entity.isDead())
         .limit(spec.tuning().projectileCount())
         .toList();
      if (!chain.isEmpty()) {
         BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
               private int index;
               private Location previous = caster.getEyeLocation().clone();

               @Override
               protected void tick() {
                  if (BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken) && this.index < chain.size()) {
                     LivingEntity target = chain.get(this.index++);
                     if (target.isValid() && !target.isDead() && target.getWorld().equals(caster.getWorld())) {
                        Location destination = target.getLocation().add(0.0, target.getHeight() * 0.5, 0.0);
                        if (BukkitClassAbilityEngine.clearSegment(this.previous, destination)) {
                           BukkitClassAbilityEngine.renderArc(this.previous, destination, spec);
                           AbilityContribution contribution = BukkitClassAbilityEngine.this.applyAndPresent(
                              presentation, caster, spec, new AbilityTargeting.TargetSelection(List.of(target), List.of(), destination), effectiveLevel
                           );
                           if (contribution.isMeaningful()) {
                              BukkitClassAbilityEngine.this.semantics.recordContribution(caster, spec.id(), contribution);
                           }

                           this.previous = destination;
                        }
                     }
                  } else {
                     BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                  }
               }
            })
            .runTaskTimer(this.plugin, 1L, 3L);
         this.track(caster.getUniqueId(), task);
      }
   }

   private void armPayload(
      final Player caster,
      final FixedAbilitySpec spec,
      final LivingEntity attached,
      final int effectiveLevel,
      final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      BukkitTask fuse = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
            private int age;

            @Override
            protected void tick() {
               if (BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken)
                  && attached.isValid()
                  && !attached.isDead()
                  && caster.getWorld().equals(attached.getWorld())) {
                  this.age += 2;
                  Location anchor = attached.getLocation().add(0.0, attached.getHeight() * 0.65, 0.0);
                  anchor.getWorld().spawnParticle(Particle.FLAME, anchor, 2, 0.12, 0.12, 0.12, 0.01);
                  if (this.age >= 30) {
                     List<LivingEntity> enemies = anchor.getWorld()
                        .getNearbyEntities(
                           anchor, Math.max(2.0, spec.tuning().radius()), Math.max(2.0, spec.tuning().radius()), Math.max(2.0, spec.tuning().radius())
                        )
                        .stream()
                        .filter(LivingEntity.class::isInstance)
                        .map(LivingEntity.class::cast)
                        .filter(entity -> BukkitClassAbilityEngine.this.semantics.canTargetEnemy(caster, entity, spec))
                        .toList();
                     AbilityContribution contribution = BukkitClassAbilityEngine.this.applyAndPresent(
                        presentation, caster, spec, new AbilityTargeting.TargetSelection(enemies, List.of(), anchor), effectiveLevel
                     );
                     if (contribution.isMeaningful()) {
                        BukkitClassAbilityEngine.this.semantics.recordContribution(caster, spec.id(), contribution);
                     }

                     anchor.getWorld().spawnParticle(Particle.EXPLOSION, anchor, 3, 1.0, 0.4, 1.0, 0.03);
                     anchor.getWorld().playSound(anchor, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.25F);
                     BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                  }
               } else {
                  BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
               }
            }
         })
         .runTaskTimer(this.plugin, 1L, 2L);
      this.track(caster.getUniqueId(), fuse);
   }

   private void launchWoundUpZone(
      final Player caster,
      final FixedAbilitySpec spec,
      final Location origin,
      final int effectiveLevel,
      final BukkitClassAbilityPresentation.Session presentation
   ) {
      final long casterLifecycleToken = this.states.lifecycleToken(caster);
      this.states.beginWindUp(caster, spec, 30);
      BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(caster.getUniqueId(), spec.id()) {
         private int age;

         @Override
         protected void tick() {
            boolean current = BukkitClassAbilityEngine.this.currentCast(caster, casterLifecycleToken);
            if (current && this.age < 30) {
               this.age += 2;
               double radius = Math.max(0.5, spec.tuning().radius() * this.age / 30.0);
               origin.getWorld().spawnParticle(Particle.DUST_PLUME, origin, 18, radius, 0.08, radius, 0.01);
            } else {
               if (current && this.age >= 30) {
                  AbilityTargeting.TargetSelection selection = BukkitClassAbilityEngine.this.targeting.select(caster, spec, origin);
                  AbilityContribution contribution = BukkitClassAbilityEngine.this.applyAndPresent(presentation, caster, spec, selection, effectiveLevel);
                  if (contribution.isMeaningful()) {
                     BukkitClassAbilityEngine.this.semantics.recordContribution(caster, spec.id(), contribution);
                  }

                  origin.getWorld().spawnParticle(Particle.EXPLOSION, origin, 6, spec.tuning().radius() * 0.5, 0.3, spec.tuning().radius() * 0.5, 0.03);
                  origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 1.4F, 0.55F);
               }

               BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
            }
         }
      }).runTaskTimer(this.plugin, 1L, 2L);
      this.track(caster.getUniqueId(), task);
   }

   private static Location lerp(Location start, Location end, double progress) {
      return start.clone().add(end.toVector().subtract(start.toVector()).multiply(progress));
   }

   private static boolean clearSegment(Location start, Location end) {
      if (start.getWorld() != null && end.getWorld() != null && start.getWorld().equals(end.getWorld())) {
         Vector delta = end.toVector().subtract(start.toVector());
         double distance = delta.length();
         if (distance < 1.0E-6) {
            return true;
         } else {
            RayTraceResult collision = start.getWorld().rayTraceBlocks(start, delta.normalize(), distance, FluidCollisionMode.NEVER, true);
            return collision == null || collision.getHitPosition() == null;
         }
      } else {
         return false;
      }
   }

   private static void renderProjectile(FixedAbilitySpec spec, Location point, double progress) {
      Particle primary = impactParticle(spec);
      point.getWorld().spawnParticle(primary, point, 2, 0.03, 0.03, 0.03, 0.005);
      if (spec.id().startsWith("spellcaster")
         || spec.id().startsWith("mage")
         || spec.id().startsWith("occultist")
         || spec.id().startsWith("necromancer")
         || spec.id().startsWith("lich")) {
         point.getWorld().spawnParticle(Particle.ENCHANT, point, 3, 0.08, 0.08 + Math.sin(progress * Math.PI) * 0.08, 0.08, 0.01);
      }
   }

   private static void renderArc(Location start, Location end, FixedAbilitySpec spec) {
      for (int step = 1; step <= 10; step++) {
         double progress = step / 10.0;
         Location point = lerp(start, end, progress);
         point.add(0.0, Math.sin(progress * Math.PI) * 0.7, 0.0);
         point.getWorld().spawnParticle(impactParticle(spec), point, 2, 0.02, 0.02, 0.02, 0.005);
      }
   }

   private static Particle impactParticle(FixedAbilitySpec spec) {
      if (spec.effects().contains(AbilityEffect.FEAR)) {
         return Particle.SOUL_FIRE_FLAME;
      } else if (spec.effects().contains(AbilityEffect.LIFESTEAL)) {
         return Particle.SOUL;
      } else if (spec.effects().contains(AbilityEffect.SLOW)) {
         return Particle.SNOWFLAKE;
      } else {
         return !spec.id().startsWith("spellcaster")
               && !spec.id().startsWith("mage")
               && !spec.id().startsWith("occultist")
               && !spec.id().startsWith("necromancer")
               && !spec.id().startsWith("lich")
            ? Particle.CRIT
            : Particle.END_ROD;
      }
   }

   private AbilityResult executeSummon(
      final Player player, final FixedAbilitySpec spec, final int effectiveLevel, final BukkitClassAbilityPresentation.Session presentation
   ) {
      Location anchor = spec.target() == AbilityTarget.AIMED_LOCATION ? this.targeting.aimedLocation(player, spec.tuning().range()) : player.getLocation();
      final ClassMinionManager.SummonResult summon = this.classMinions.summon(player, spec, effectiveLevel, anchor);
      if (summon.status() != ClassMinionManager.Status.SUCCESS) {
         AbilityFailureReason failure = switch (summon.status()) {
            case NO_CORPSE -> AbilityFailureReason.NO_CORPSE;
            case UNSAFE_DESTINATION -> AbilityFailureReason.UNSAFE_DESTINATION;
            case RUNTIME_UNAVAILABLE -> AbilityFailureReason.ENGINE_CLOSED;
            case SUCCESS -> throw new IllegalStateException("Successful summon was handled as failure");
         };
         return AbilityResult.failure(spec.id(), failure);
      } else {
         Location origin = summon.origin();
         this.semantics
            .observe(
               new AbilityRuntimeObservation(
                  AbilityRuntimeObservation.Kind.SUMMON_SPAWNED,
                  player.getUniqueId(),
                  null,
                  spec.id(),
                  0.0,
                  Math.max(20, spec.tuning().durationTicks()),
                  summon.minions().size(),
                  spec.effects(),
                  spec.executionTraits().mechanics()
               )
            );
         presentation.effects(origin, List.of(), List.of());
         if (!spec.effects().contains(AbilityEffect.HEAL)
            && !spec.effects().contains(AbilityEffect.SHIELD)
            && !spec.effects().contains(AbilityEffect.ALLY_PROTECT)) {
            return AbilityResult.success(spec.id(), AbilityContribution.NONE);
         } else {
            int duration = Math.max(20, spec.tuning().durationTicks());
            final int pulseCount = Math.max(1, spec.tuning().repetitions());
            int pulsePeriod = Math.max(2, duration / pulseCount);
            final long casterLifecycleToken = this.states.lifecycleToken(player);
            BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(player.getUniqueId(), spec.id()) {
                  private int completed;

                  @Override
                  protected void tick() {
                     if (BukkitClassAbilityEngine.this.currentCast(player, casterLifecycleToken) && this.completed < pulseCount) {
                        List<LivingEntity> live = summon.minions()
                           .stream()
                           .filter(Entity::isValid)
                           .filter(entity -> !entity.isDead())
                           .filter(entity -> entity.getWorld().equals(player.getWorld()))
                           .toList();
                        if (live.isEmpty()) {
                           BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
                        } else {
                           for (LivingEntity servant : live) {
                              Location pulseOrigin = servant.getLocation();
                              AbilityTargeting.TargetSelection selection = BukkitClassAbilityEngine.this.targeting.select(player, spec, pulseOrigin);
                              AbilityContribution pulse = BukkitClassAbilityEngine.this.applyAndPresent(presentation, player, spec, selection, effectiveLevel);
                              if (pulse.isMeaningful()) {
                                 BukkitClassAbilityEngine.this.semantics.recordContribution(player, spec.id(), pulse);
                              }
                           }

                           this.completed++;
                        }
                     } else {
                        BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
                     }
                  }
               })
               .runTaskTimer(this.plugin, 1L, pulsePeriod);
            this.track(player.getUniqueId(), task);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
         }
      }
   }

   private AbilityResult executeMountedCharge(Player player, FixedAbilitySpec spec, int effectiveLevel, BukkitClassAbilityPresentation.Session presentation) {
      Set<UUID> struck = new HashSet<>();
      Optional<Horse> summoned = this.divineSteeds
         .summon(player, spec.tuning().durationTicks(), horse -> this.applyMountedChargeContacts(player, horse, spec, effectiveLevel, struck, presentation));
      if (summoned.isEmpty()) {
         return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
      } else {
         Location origin = summoned.get().getLocation();
         presentation.mobility(origin, origin);
         origin.getWorld().spawnParticle(Particle.END_ROD, origin.clone().add(0.0, 1.0, 0.0), 16, 0.65, 0.55, 0.65, 0.025);
         origin.getWorld().playSound(origin, Sound.ENTITY_HORSE_ARMOR, 1.0F, 1.2F);
         return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
      }
   }

   private void applyMountedChargeContacts(
      Player player, Horse horse, FixedAbilitySpec spec, int effectiveLevel, Set<UUID> struck, BukkitClassAbilityPresentation.Session presentation
   ) {
      double contactRadius = Math.max(0.75, spec.tuning().radius());

      for (Entity entity : horse.getNearbyEntities(contactRadius, Math.max(1.0, contactRadius), contactRadius)) {
         if (entity instanceof LivingEntity enemy
            && this.semantics.canTargetEnemy(player, enemy, spec)
            && this.targeting.unobstructedFrom(horse.getLocation(), horse.getHeight(), enemy)
            && struck.add(enemy.getUniqueId())) {
            Location impactLocation = enemy.getLocation().add(0.0, enemy.getHeight() * 0.5, 0.0);
            AbilityContribution impact = this.applyAndPresent(
               presentation, player, spec, new AbilityTargeting.TargetSelection(List.of(enemy), List.of(), impactLocation), effectiveLevel
            );
            if (impact.isMeaningful()) {
               this.semantics.recordContribution(player, spec.id(), impact);
            }

            impactLocation.getWorld().spawnParticle(Particle.CRIT, impactLocation, 10, 0.35, 0.35, 0.35, 0.08);
            impactLocation.getWorld().playSound(impactLocation, Sound.ENTITY_HORSE_GALLOP, 0.8F, 0.8F);
         }
      }
   }

   private AbilityResult executeDash(Player player, FixedAbilitySpec spec, int effectiveLevel, BukkitClassAbilityPresentation.Session presentation) {
      double clearDistance = SafeMovement.physicsDashClearDistance(player, spec.tuning().range());
      if (clearDistance < 0.5) {
         return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
      } else {
         Vector direction = player.getEyeLocation().getDirection().setY(0);
         if (direction.lengthSquared() < 1.0E-6) {
            return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
         } else {
            Vector previousVelocity = player.getVelocity();
            Vector dashVelocity = direction.normalize().multiply(Math.min(1.45, 0.45 + clearDistance * 0.12)).setY(Math.max(0.0, previousVelocity.getY()));
            this.states.applyOwnedVelocity(player, dashVelocity);
            Location origin = player.getLocation().clone();
            presentation.mobility(origin, origin);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 12, 0.3, 0.2, 0.3, 0.06);
            return AbilityResult.success(spec.id(), movement(clearDistance));
         }
      }
   }

   private AbilityResult executeBlink(Player player, FixedAbilitySpec spec, int effectiveLevel, BukkitClassAbilityPresentation.Session presentation) {
      Optional<BlinkPlanner.Plan> planned = BlinkPlanner.plan(player, spec.tuning().range());
      if (planned.isEmpty()) {
         return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
      } else {
         BlinkPlanner.Plan plan = planned.get();
         Location origin = player.getLocation().clone();
         Location destination = plan.destination();
         if (!InstancePlayerMovement.teleportWithinWorld(player, destination, TeleportCause.PLUGIN)) {
            return !rollbackUnexpectedMovement(player, origin)
               ? committedUnexpectedMovement(spec.id(), origin, player.getLocation())
               : AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
         } else {
            Location actual = player.getLocation();
            if (arrivedAt(actual, destination) && SafeMovement.safeStandingPlayerVolume(player, actual)) {
               this.states.applyOwnedVelocity(player, new Vector());
               player.setFallDistance(0.0F);
               presentation.mobility(origin, actual);
               double distanceSquared = origin.distanceSquared(actual);
               if (BlinkPotionFlashPolicy.shouldFlash(true, distanceSquared)) {
                  this.blinkPotionFlash.flash(player);
               }

               return AbilityResult.success(spec.id(), movement(Math.sqrt(distanceSquared)));
            } else {
               return !rollbackUnexpectedMovement(player, origin)
                  ? committedUnexpectedMovement(spec.id(), origin, player.getLocation())
                  : AbilityResult.failure(spec.id(), AbilityFailureReason.UNSAFE_DESTINATION);
            }
         }
      }
   }

   private AbilityResult executeAllyFlight(Player player, FixedAbilitySpec spec, int effectiveLevel, BukkitClassAbilityPresentation.Session presentation) {
      Location start = player.getLocation().clone();
      Optional<Player> aimedAlly = this.targeting.aimedAlly(player, spec);
      boolean started;
      if (aimedAlly.isPresent()) {
         Player ally = aimedAlly.get();
         long allyLifecycleToken = this.states.lifecycleToken(ally);
         started = this.guardianFlights
            .startToAlly(
               player,
               ally,
               spec.tuning().range(),
               spec.tuning().durationTicks(),
               (caster, candidate) -> this.isCurrentChainAlly(caster, candidate, allyLifecycleToken),
               result -> this.completeAllyFlight(player, spec, effectiveLevel, presentation, result, allyLifecycleToken)
            );
      } else {
         BukkitClassAbilityEngine.SanctuaryAnchor anchor = this.sanctuaryAnchors.get(player.getUniqueId());
         if (anchor == null || anchor.expiresAtNanos() <= System.nanoTime() || !anchor.location().getWorld().equals(player.getWorld())) {
            this.sanctuaryAnchors.remove(player.getUniqueId());
            return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
         }

         if (player.getLocation().distance(anchor.location()) > spec.tuning().range()) {
            return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
         }

         started = this.guardianFlights
            .startToAnchor(
               player,
               anchor.location(),
               spec.tuning().range(),
               spec.tuning().durationTicks(),
               result -> this.completeAllyFlight(player, spec, effectiveLevel, presentation, result, null)
            );
      }

      if (!started) {
         return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
      } else {
         presentation.mobility(start, start);
         return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
      }
   }

   private void completeAllyFlight(
      Player player,
      FixedAbilitySpec spec,
      int effectiveLevel,
      BukkitClassAbilityPresentation.Session presentation,
      GuardianFlightManager.FlightResult result,
      Long allyLifecycleToken
   ) {
      if (!this.closed) {
         presentation.mobility(result.start(), result.end());
         AbilityContribution contribution = movement(result.travelledDistance());
         if (result.arrived()) {
            Player ally = result.ally();
            List<Player> recipients;
            if (ally == null) {
               recipients = List.of(player);
            } else if (allyLifecycleToken != null && this.isCurrentChainAlly(player, ally, allyLifecycleToken)) {
               recipients = List.of(ally);
            } else {
               recipients = List.of();
            }

            if (!recipients.isEmpty()) {
               AbilityContribution support = this.applyAndPresent(
                  presentation, player, spec, new AbilityTargeting.TargetSelection(List.of(), recipients, result.end()), effectiveLevel
               );
               contribution = contribution.plus(support);
            }
         }

         if (contribution.isMeaningful()) {
            this.semantics.recordContribution(player, spec.id(), contribution);
         }
      }
   }

   private AbilityResult executeLeap(
      final Player player, final FixedAbilitySpec spec, final int effectiveLevel, final BukkitClassAbilityPresentation.Session presentation
   ) {
      final Location start = player.getLocation().clone();
      Vector horizontal = player.getEyeLocation().getDirection().setY(0.0);
      if (horizontal.lengthSquared() < 1.0E-8) {
         double yaw = Math.toRadians(start.getYaw());
         horizontal = new Vector(-Math.sin(yaw), 0.0, Math.cos(yaw));
      }

      horizontal.normalize().multiply(Math.max(0.8, spec.tuning().displacement()));
      Vector launchVelocity = horizontal.setY(0.9);
      this.states.applyOwnedVelocity(player, launchVelocity);
      player.setFallDistance(0.0F);
      presentation.mobility(start, start);
      player.getWorld().spawnParticle(Particle.CLOUD, start.clone().add(0.0, 0.15, 0.0), 14, 0.35, 0.1, 0.35, 0.06);
      final long casterLifecycleToken = this.states.lifecycleToken(player);
      BukkitTask task = (new BukkitClassAbilityEngine.TrackedAbilityTask(player.getUniqueId(), spec.id()) {
         private int age;
         private boolean airborne;

         @Override
         protected void tick() {
            if (BukkitClassAbilityEngine.this.currentCast(player, casterLifecycleToken) && player.getWorld().equals(start.getWorld())) {
               this.age++;
               this.airborne = this.airborne | !player.isOnGround();
               player.setFallDistance(0.0F);
               player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 2, 0.16, 0.12, 0.16, 0.01);
               if (this.airborne && player.isOnGround() || !this.airborne && this.age >= 4 || this.age >= 80) {
                  BukkitClassAbilityEngine.this.completeLeap(player, spec, effectiveLevel, start, presentation);
                  BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
               }
            } else {
               BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
            }
         }
      }).runTaskTimer(this.plugin, 1L, 1L);
      this.track(player.getUniqueId(), task);
      return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
   }

   private void completeLeap(Player player, FixedAbilitySpec spec, int effectiveLevel, Location start, BukkitClassAbilityPresentation.Session presentation) {
      Location landing = player.getLocation().clone();
      player.setFallDistance(0.0F);
      AbilityTargeting.TargetSelection selection = this.targeting.select(player, spec, landing);
      AbilityContribution impact = this.applyAndPresent(presentation, player, spec, selection, effectiveLevel)
         .plus(movement(horizontalDistance(start, landing)));
      if (impact.isMeaningful()) {
         this.semantics.recordContribution(player, spec.id(), impact);
      }

      presentation.mobility(start, landing);
      long terrainSeed = player.getUniqueId().getMostSignificantBits() ^ player.getUniqueId().getLeastSignificantBits() ^ player.getWorld().getGameTime();
      TerrainImpacts.show(new TerrainImpactRequest(landing, spec.tuning().radius(), 1.0, spec.tuning().durationTicks(), 48.0, terrainSeed));
      player.getWorld().spawnParticle(Particle.EXPLOSION, landing, 3, 1.2, 0.2, 1.2, 0.02);
      player.getWorld().playSound(landing, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.75F);
   }

   private static double horizontalDistance(Location first, Location second) {
      if (first.getWorld() != null && second.getWorld() != null && first.getWorld().equals(second.getWorld())) {
         double x = first.getX() - second.getX();
         double z = first.getZ() - second.getZ();
         return Math.sqrt(x * x + z * z);
      } else {
         return 0.0;
      }
   }

   private static boolean validMinimumRangeImpact(FixedAbilitySpec spec, Location castOrigin, Location impact, LivingEntity actualTarget) {
      if (castOrigin.getWorld() != null
         && impact.getWorld() != null
         && castOrigin.getWorld().equals(impact.getWorld())
         && castOrigin.getWorld().equals(actualTarget.getWorld())) {
         double minimumRange = Math.max(8.0, spec.tuning().range() * 0.25);
         return AbilityActivationPolicy.permitsImpact(
            spec.executionTraits().mechanics(), castOrigin.distance(impact), castOrigin.distance(actualTarget.getLocation()), minimumRange
         );
      } else {
         return false;
      }
   }

   private AbilityContribution applyAndPresent(
      BukkitClassAbilityPresentation.Session presentation, Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection, int effectiveLevel
   ) {
      AbilityContribution contribution = this.effects.apply(caster, spec, selection, effectiveLevel);
      presentation.effects(selection.origin(), selection.enemies(), selection.allies());
      return contribution;
   }

   private AbilityContribution applyPeriodicAndPresent(
      BukkitClassAbilityPresentation.Session presentation, Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection, int effectiveLevel
   ) {
      AbilityContribution contribution = this.effects.applyPeriodic(caster, spec, selection, effectiveLevel);
      this.semantics
         .observe(
            new AbilityRuntimeObservation(
               AbilityRuntimeObservation.Kind.FIELD_PULSE,
               caster.getUniqueId(),
               null,
               spec.id(),
               contribution.damage() + contribution.effectiveHealing(),
               spec.tuning().durationTicks(),
               selection.enemies().size() + selection.allies().size(),
               spec.effects(),
               spec.executionTraits().mechanics()
            )
         );
      presentation.effects(selection.origin(), selection.enemies(), selection.allies());
      return contribution;
   }

   private AbilityContribution applyProjectileAndPresent(
      BukkitClassAbilityPresentation.Session presentation, Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection, int effectiveLevel
   ) {
      AbilityContribution contribution = this.effects.applyProjectileEffect(caster, spec, selection, effectiveLevel);
      presentation.effects(selection.origin(), selection.enemies(), selection.allies());
      return contribution;
   }

   private static boolean hasApplicableTarget(FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection) {
      boolean enemyEffect = spec.effects().stream().anyMatch(effect -> {
         return switch (effect) {
            case DAMAGE, KNOCKBACK, PULL, LAUNCH, SLOW, WEAKEN, GLOW, PARTY_DAMAGE_MARK, TAUNT, INTERRUPT, FEAR, ROOT, BURN -> true;
            default -> false;
         };
      });
      boolean allyEffect = spec.effects().stream().anyMatch(effect -> {
         return switch (effect) {
            case HEAL, SHIELD, CLEANSE, SELF_PROTECT, ALLY_PROTECT, SPEED, HASTE, RESISTANCE, STRENGTH, SPELL_STRENGTH, SELF_VULNERABLE -> true;
            default -> false;
         };
      });
      return enemyEffect && !selection.enemies().isEmpty() || allyEffect && !selection.allies().isEmpty() || !enemyEffect && !allyEffect;
   }

   private static AbilityDefinition abilityFor(ClassLineage lineage, AbilitySlot slot) {
      return switch (slot) {
         case MOBILITY -> lineage.mobility();
         case SIGNATURE -> lineage.signature();
         case UTILITY -> lineage.utility();
      };
   }

   private static AbilityContribution movement(double distance) {
      return new AbilityContribution(0.0, 0.0, 0.0, 0.0, 0, 0, Math.max(0.0, distance));
   }

   private static boolean arrivedAt(Location actual, Location expected) {
      return actual.getWorld() != null
         && expected.getWorld() != null
         && actual.getWorld().equals(expected.getWorld())
         && actual.distanceSquared(expected) <= 0.0025000000000000005;
   }

   private static boolean rollbackUnexpectedMovement(Player player, Location origin) {
      return arrivedAt(player.getLocation(), origin)
         ? true
         : InstancePlayerMovement.teleportWithinWorld(player, origin, TeleportCause.PLUGIN) && arrivedAt(player.getLocation(), origin);
   }

   private static AbilityResult committedUnexpectedMovement(String abilityId, Location origin, Location actual) {
      double distance = origin.getWorld() != null && actual.getWorld() != null && origin.getWorld().equals(actual.getWorld()) ? origin.distance(actual) : 0.0;
      return AbilityResult.success(abilityId, movement(distance));
   }

   private static double healthFraction(Player player) {
      AttributeInstance maximum = player.getAttribute(Attribute.MAX_HEALTH);
      double max = maximum == null ? Math.max(1.0, player.getHealth()) : Math.max(1.0, maximum.getValue());
      return player.getHealth() / max;
   }

   private static long expiresAt(int durationTicks) {
      long now = System.nanoTime();
      long duration = durationTicks * 50000000L;
      return Long.MAX_VALUE - now < duration ? Long.MAX_VALUE : now + duration;
   }

   private boolean currentCast(Player caster, long lifecycleToken) {
      boolean runtimeEligible = !this.closed
         && caster != null
         && caster.isOnline()
         && caster.isValid()
         && !caster.isDead()
         && ClassAbilityEligibility.isEligible(caster);
      if (caster == null) {
         return false;
      } else {
         long currentGeneration = this.states.lifecycleToken(caster);
         return runtimeEligible && lifecycleToken == currentGeneration;
      }
   }

   private void presentConstruct(Player player, FixedAbilitySpec spec, Location origin) {
      try {
         this.constructs.present(player, spec, origin);
      } catch (RuntimeException var5) {
         this.plugin.getLogger().log(Level.FINE, "Could not display packet-only class construct for " + spec.id(), (Throwable)var5);
      }
   }

   private static Set<AbilityMechanic> constructMechanics(ClassConstructVisualRegistry.AnchorMode anchorMode) {
      return anchorMode == ClassConstructVisualRegistry.AnchorMode.FOLLOW_CASTER ? Set.of(AbilityMechanic.FOLLOW_CASTER_FIELD) : Set.of();
   }

   private void track(UUID casterId, BukkitTask task) {
      this.tasksByCaster.computeIfAbsent(casterId, ignored -> new HashSet<>()).add(task);
   }

   private void cancelTracked(UUID casterId, BukkitRunnable runnable) {
      int taskId = runnable.getTaskId();
      runnable.cancel();
      Set<BukkitTask> casterTasks = this.tasksByCaster.get(casterId);
      if (casterTasks != null) {
         casterTasks.removeIf(task -> task.getTaskId() == taskId);
         if (casterTasks.isEmpty()) {
            this.tasksByCaster.remove(casterId);
         }
      }
   }

   @Override
   public void deactivate(Player player) {
      Objects.requireNonNull(player, "player");
      UUID casterId = player.getUniqueId();
      Set<BukkitTask> casterTasks = this.tasksByCaster.remove(casterId);
      if (casterTasks != null) {
         for (BukkitTask task : new ArrayList<>(casterTasks)) {
            task.cancel();
         }
      }

      this.sanctuaryAnchors.remove(casterId);
      this.guardianFlights.cancel(player);
      this.classMinions.deactivate(player);
      this.blinkPotionFlash.deactivate(player);
      this.constructs.deactivate(player);
      this.divineSteeds.deactivate(player);
      this.projectileCarriers.deactivate(player);
      this.frenzy.clear(player);
      this.effects.clearSource(casterId);
      this.modifiers.clearSource(casterId);
      this.modifiers.clearTarget(casterId);
      this.states.clearSource(casterId);
      this.semantics.clearSource(player);
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;

         for (Set<BukkitTask> casterTasks : new ArrayList<>(this.tasksByCaster.values())) {
            for (BukkitTask task : new ArrayList<>(casterTasks)) {
               task.cancel();
            }
         }

         this.tasksByCaster.clear();
         this.sanctuaryAnchors.clear();

         try {
            this.controlAttributionLease.close();
         } catch (Exception var5) {
         }

         this.guardianFlights.close();
         this.divineSteeds.close();
         this.projectileCarriers.close();
         this.classMinions.close();
         this.blinkPotionFlash.close();
         this.constructs.close();
         this.frenzy.close();
         this.effects.close();
         this.states.close();
         this.modifiers.close();
      }
   }

   @FunctionalInterface
   private interface AbilityExecutor {
      AbilityResult execute(Player var1, FixedAbilitySpec var2, int var3, BukkitClassAbilityPresentation.Session var4);
   }

   private record SanctuaryAnchor(Location location, long expiresAtNanos) {
      private SanctuaryAnchor(Location location, long expiresAtNanos) {
         location = location.clone();
         this.location = location;
         this.expiresAtNanos = expiresAtNanos;
      }
   }

   private abstract class TrackedAbilityTask extends BukkitRunnable {
      private final UUID casterId;
      private final String abilityId;

      private TrackedAbilityTask(UUID casterId, String abilityId) {
         this.casterId = casterId;
         this.abilityId = abilityId;
      }

      public final void run() {
         try {
            this.tick();
         } catch (Error | RuntimeException var3) {
            BukkitClassAbilityEngine.this.cancelTracked(this.casterId, this);
            BukkitClassAbilityEngine.this.plugin.getLogger().log(Level.SEVERE, "Stopped failed class ability " + this.abilityId, (Throwable)var3);
            if (var3 instanceof Error error) {
               throw error;
            }
         }
      }

      protected abstract void tick();
   }
}
