package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.advancedcombat.damage.AdvancedDamageScaling;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

final class AbilityEffects {
   private static final double DEFAULT_PROTECTION_MULTIPLIER = 0.8;
   private static final double DEFAULT_STRENGTH_MULTIPLIER = 1.15;
   private final ClassAbilityDamage damage;
   private final AbilitySemantics semantics;
   private final TimedCombatModifiers modifiers;
   private final AbilityStateRuntime states;
   private final EliteCrowdControlRuntime crowdControl;
   private final ObservedPotionApplications potions;

   AbilityEffects(AbilitySemantics semantics, TimedCombatModifiers modifiers, AbilityStateRuntime states, ClassAbilityDamage damage) {
      this.damage = damage;
      this.semantics = semantics;
      this.modifiers = modifiers;
      this.states = states;
      this.crowdControl = new EliteCrowdControlRuntime(MetadataHandler.PLUGIN);
      this.potions = new ObservedPotionApplications(MetadataHandler.PLUGIN);
   }

   AbilityContribution apply(Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection, int effectiveLevel) {
      CombatDamageContext.ClassAbilityDelivery delivery = spec.family() == AbilityFamily.PROJECTILE
         ? CombatDamageContext.ClassAbilityDelivery.PROJECTILE
         : CombatDamageContext.ClassAbilityDelivery.DIRECT;
      return this.apply(caster, null, spec, selection, effectiveLevel, AbilityHealingType.BURST, delivery);
   }

   boolean controlledBy(Player caster, LivingEntity target) {
      return this.crowdControl.controlledBy(caster, target);
   }

   void applyMinionImpact(Player caster, FixedAbilitySpec spec, LivingEntity target, int effectiveLevel) {
      if (valid(target) && this.semantics.canTargetEnemy(caster, target, spec)) {
         AbilityMechanicModifiers mechanicModifiers = this.semantics.mechanicModifiers(caster);
         int duration = Math.max(
            20,
            Math.min(
               100,
               scaledDuration(
                  ActiveAbilityLevelScaling.durationTicks(spec.tuning().durationTicks(), effectiveLevel), mechanicModifiers.controlDurationMultiplier()
               )
            )
         );
         double potency = mechanicModifiers.controlPotencyMultiplier();
         if (spec.effects().contains(AbilityEffect.SLOW) && this.authorized(caster, target, spec, AbilityEffect.SLOW)) {
            this.applyPotion(
               caster,
               target,
               spec,
               AbilityEffect.SLOW,
               AbilityRuntimeObservation.Kind.CONTROL,
               new PotionEffect(PotionEffectType.SLOWNESS, duration, scaledAmplifier(1, potency), false, true, true),
               ignored -> {}
            );
         }

         if (spec.effects().contains(AbilityEffect.WEAKEN) && this.authorized(caster, target, spec, AbilityEffect.WEAKEN)) {
            double multiplier = this.modifiers.weakenOutgoing(caster, target, spec.tuning().weakenMultiplier(), effectiveLevel, potency, duration, spec.id());
            this.observeApplied(caster, target, spec, AbilityRuntimeObservation.Kind.MODIFIER_APPLIED, AbilityEffect.WEAKEN, multiplier, duration);
         }

         EliteEntity elite = EntityTracker.getEliteMobEntity(target);
         if (elite != null) {
            if (spec.effects().contains(AbilityEffect.INTERRUPT)
               && this.authorized(caster, target, spec, AbilityEffect.INTERRUPT)
               && this.crowdControl.interrupt(caster, elite, Math.min(40, duration))) {
               this.observeApplied(
                  caster,
                  target,
                  spec,
                  AbilityRuntimeObservation.Kind.CONTROL,
                  AbilityEffect.INTERRUPT,
                  1.0,
                  this.crowdControl.remainingTicks(caster, target, AbilityEffect.INTERRUPT)
               );
            }

            if (spec.effects().contains(AbilityEffect.FEAR)
               && this.authorized(caster, target, spec, AbilityEffect.FEAR)
               && this.crowdControl.fear(caster, elite, Math.min(60, duration))) {
               this.observeApplied(
                  caster,
                  target,
                  spec,
                  AbilityRuntimeObservation.Kind.CONTROL,
                  AbilityEffect.FEAR,
                  1.0,
                  this.crowdControl.remainingTicks(caster, target, AbilityEffect.FEAR)
               );
            }
         }
      }
   }

   AbilityContribution applyPeriodic(Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection, int effectiveLevel) {
      return this.apply(caster, null, spec, selection, effectiveLevel, AbilityHealingType.PERIODIC, CombatDamageContext.ClassAbilityDelivery.PERIODIC);
   }

   AbilityContribution applyProjectileEffect(Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection, int effectiveLevel) {
      return this.apply(caster, null, spec, selection, effectiveLevel, AbilityHealingType.BURST, CombatDamageContext.ClassAbilityDelivery.PROJECTILE);
   }

   AbilityContribution applyProjectile(
      Player caster, Projectile projectile, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection, int effectiveLevel
   ) {
      return this.apply(
         caster,
         Objects.requireNonNull(projectile, "projectile"),
         spec,
         selection,
         effectiveLevel,
         AbilityHealingType.BURST,
         CombatDamageContext.ClassAbilityDelivery.PROJECTILE
      );
   }

   private AbilityContribution apply(
      Player caster,
      Projectile projectile,
      FixedAbilitySpec spec,
      AbilityTargeting.TargetSelection selection,
      int effectiveLevel,
      AbilityHealingType healingType,
      CombatDamageContext.ClassAbilityDelivery delivery
   ) {
      selection = new AbilityTargeting.TargetSelection(selection.enemies(), this.uniqueValidAllies(caster, selection.allies()), selection.origin());
      selection = this.states.qualifyTargets(caster, spec, selection);
      Set<AbilityEffect> effects = spec.effects();
      AbilityTuning tuning = spec.tuning();
      AbilityMechanicModifiers mechanicModifiers = this.semantics.mechanicModifiers(caster);
      int levelDuration = ActiveAbilityLevelScaling.durationTicks(tuning.durationTicks(), effectiveLevel);
      int controlDuration = scaledDuration(levelDuration, mechanicModifiers.controlDurationMultiplier());
      double controlPotency = mechanicModifiers.controlPotencyMultiplier();
      EliteControlEffectPlan slowPlan = EliteControlEffectPlan.slow(2, levelDuration, mechanicModifiers);
      double displacement = ActiveAbilityLevelScaling.displacement(tuning.displacement(), effectiveLevel);
      boolean missingHealthScaling = spec.executionTraits().mechanics().contains(AbilityMechanic.MISSING_HEALTH_SCALING);
      double classRankScale = AbilityLevelScaling.multiplier(effectiveLevel);
      double totalDamage = 0.0;
      double totalHealing = 0.0;
      int controlledEnemies = 0;
      int supportedAllies = 0;
      List<LivingEntity> enemies = new ArrayList<>(selection.enemies());
      if (effects.contains(AbilityEffect.TAUNT) && enemies.isEmpty() && !AbilityEnemyPolicy.requiresQualifiedSelection(spec.executionTraits().mechanics())) {
         enemies.addAll(this.nearbyEnemies(caster, spec, Math.max(8.0, tuning.radius())));
      }

      if (spec.executionTraits().mechanics().contains(AbilityMechanic.SINGLE_ENEMY)) {
         enemies = enemies.stream().limit(1L).toList();
      }

      int qualifiedEnemyCount = enemies.size();
      double healingMultiplier = this.semantics.healingMultiplier(caster)
         * AbilityHealingPolicy.multiplier(mechanicModifiers, healingType, qualifiedEnemyCount);
      this.states.activate(caster, spec, selection, effectiveLevel);
      double availableRetaliation = spec.executionTraits().mechanics().contains(AbilityMechanic.RETALIATION_RELEASE)
         ? this.states.retaliationAvailable(caster, spec)
         : 0.0;
      boolean retaliationAvailable = availableRetaliation > 0.0;
      double releasedRetaliation = 0.0;
      List<LivingEntity> tauntedEnemies = new ArrayList<>();
      Set<LivingEntity> affectedEnemies = new LinkedHashSet<>();

      for (LivingEntity enemy : enemies) {
         if (valid(enemy) && this.semantics.canTargetEnemy(caster, enemy, spec)) {
            Map<AbilityEffect, AbilityEffects.AppliedControl> appliedControls = new EnumMap<>(AbilityEffect.class);
            boolean observedPotionControl = false;
            if (effects.contains(AbilityEffect.DAMAGE) && tuning.damageMultiplier() > 0.0 && this.authorized(caster, enemy, spec, AbilityEffect.DAMAGE)) {
               EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
               double amount = AdvancedDamageScaling.classAbility(elite, effectiveLevel, tuning.damageMultiplier() * this.states.damageMultiplier(spec, enemy));
               double retaliationBonus = retaliationAvailable ? availableRetaliation : 0.0;
               amount += retaliationBonus;
               double dealt = this.damage.apply(caster, projectile, enemy, amount, damageDomain(spec, delivery));
               if (dealt > 0.0 && retaliationBonus > 0.0) {
                  releasedRetaliation = this.states.consumeRetaliation(caster, spec, enemy);
                  retaliationAvailable = false;
               }

               totalDamage += dealt;
               if (dealt > 0.0) {
                  affectedEnemies.add(enemy);
                  this.semantics
                     .observe(
                        AbilityRuntimeObservation.effect(
                           AbilityRuntimeObservation.Kind.DAMAGE, caster.getUniqueId(), enemy.getUniqueId(), spec.id(), dealt, 0, AbilityEffect.DAMAGE
                        )
                     );
               }
            }

            if (effects.contains(AbilityEffect.KNOCKBACK) && this.authorized(caster, enemy, spec, AbilityEffect.KNOCKBACK)) {
               double changed = displaceAway(caster, enemy, displacement * controlPotency, 0.25);
               if (changed > 0.0) {
                  appliedControls.put(AbilityEffect.KNOCKBACK, new AbilityEffects.AppliedControl(changed, 0));
               }
            }

            if (effects.contains(AbilityEffect.PULL) && this.authorized(caster, enemy, spec, AbilityEffect.PULL)) {
               double changed = displaceToward(caster, enemy, displacement * controlPotency);
               if (changed > 0.0) {
                  appliedControls.put(AbilityEffect.PULL, new AbilityEffects.AppliedControl(changed, 0));
               }
            }

            if (effects.contains(AbilityEffect.LAUNCH) && this.authorized(caster, enemy, spec, AbilityEffect.LAUNCH)) {
               double changed = displaceAway(caster, enemy, displacement * 0.45 * controlPotency, Math.max(0.55, displacement * controlPotency));
               if (changed > 0.0) {
                  appliedControls.put(AbilityEffect.LAUNCH, new AbilityEffects.AppliedControl(changed, 0));
               }
            }

            if (effects.contains(AbilityEffect.SLOW) && this.authorized(caster, enemy, spec, AbilityEffect.SLOW)) {
               boolean extend = spec.executionTraits().mechanics().contains(AbilityMechanic.EXTEND_CONTROL_DURATION);
               int amplifier = slowPlan.potionAmplifier();
               int ownedRemaining = extend ? this.crowdControl.ownedSlowRemainingTicks(caster, enemy, amplifier) : 0;
               int duration = Math.max(20, slowPlan.durationTicks());
               if (ownedRemaining > 0) {
                  duration = Math.min(600, ownedRemaining + duration);
               }

               EliteEntity elitex = EntityTracker.getEliteMobEntity(enemy);
               observedPotionControl |= this.applyPotion(
                  caster,
                  enemy,
                  spec,
                  AbilityEffect.SLOW,
                  AbilityRuntimeObservation.Kind.CONTROL,
                  new PotionEffect(PotionEffectType.SLOWNESS, duration, amplifier, false, true, true),
                  actual -> {
                     if (elite != null) {
                        this.crowdControl
                           .applySlowPotency(
                              caster,
                              elite,
                              new EliteControlEffectPlan(actual.getDuration(), actual.getAmplifier(), slowPlan.additionalMovementSpeedAdjustment())
                           );
                     }

                     if (ownedRemaining > 0) {
                        this.observeMechanicTriggered(
                           caster, enemy, spec, AbilityMechanic.EXTEND_CONTROL_DURATION, AbilityEffect.SLOW, 1.0, actual.getDuration()
                        );
                     }
                  }
               );
               if (extend && this.crowdControl.extendRoot(caster, elitex, Math.max(20, controlDuration))) {
                  this.observeMechanicTriggered(
                     caster, enemy, spec, AbilityMechanic.EXTEND_CONTROL_DURATION, AbilityEffect.SLOW, 1.0, Math.max(20, controlDuration)
                  );
               }
            }

            if (effects.contains(AbilityEffect.ROOT) && this.authorized(caster, enemy, spec, AbilityEffect.ROOT)) {
               EliteEntity elitex = EntityTracker.getEliteMobEntity(enemy);
               if (this.crowdControl.root(caster, elitex, Math.max(20, controlDuration))) {
                  appliedControls.put(
                     AbilityEffect.ROOT, new AbilityEffects.AppliedControl(1.0, this.crowdControl.remainingTicks(caster, enemy, AbilityEffect.ROOT))
                  );
               }
            }

            if (effects.contains(AbilityEffect.WEAKEN) && this.authorized(caster, enemy, spec, AbilityEffect.WEAKEN)) {
               int durationx = Math.max(20, controlDuration);
               double multiplier = this.modifiers
                  .weakenOutgoing(caster, enemy, tuning.weakenMultiplier(), effectiveLevel, controlPotency, durationx, spec.id());
               this.observeApplied(caster, enemy, spec, AbilityRuntimeObservation.Kind.MODIFIER_APPLIED, AbilityEffect.WEAKEN, multiplier, durationx);
               appliedControls.put(AbilityEffect.WEAKEN, new AbilityEffects.AppliedControl(multiplier, durationx));
            }

            if (effects.contains(AbilityEffect.INTERRUPT) && this.authorized(caster, enemy, spec, AbilityEffect.INTERRUPT)) {
               EliteEntity elitex = EntityTracker.getEliteMobEntity(enemy);
               if (this.crowdControl.interrupt(caster, elitex, Math.max(10, Math.min(60, controlDuration)))) {
                  appliedControls.put(
                     AbilityEffect.INTERRUPT, new AbilityEffects.AppliedControl(1.0, this.crowdControl.remainingTicks(caster, enemy, AbilityEffect.INTERRUPT))
                  );
               }
            }

            if (effects.contains(AbilityEffect.FEAR) && this.authorized(caster, enemy, spec, AbilityEffect.FEAR)) {
               EliteEntity elitex = EntityTracker.getEliteMobEntity(enemy);
               if (this.crowdControl.fear(caster, elitex, Math.max(30, controlDuration))) {
                  appliedControls.put(
                     AbilityEffect.FEAR, new AbilityEffects.AppliedControl(1.0, this.crowdControl.remainingTicks(caster, enemy, AbilityEffect.FEAR))
                  );
               }
            }

            if (effects.contains(AbilityEffect.GLOW) && this.authorized(caster, enemy, spec, AbilityEffect.GLOW)) {
               observedPotionControl |= this.applyPotion(
                  caster,
                  enemy,
                  spec,
                  AbilityEffect.GLOW,
                  AbilityRuntimeObservation.Kind.CONTROL,
                  new PotionEffect(PotionEffectType.GLOWING, Math.max(20, levelDuration), 0, false, true, true),
                  ignored -> {}
               );
            }

            if (effects.contains(AbilityEffect.BURN) && this.authorized(caster, enemy, spec, AbilityEffect.BURN)) {
               int before = enemy.getFireTicks();
               enemy.setFireTicks(Math.max(before, Math.max(20, Math.min(200, levelDuration))));
               int actual = enemy.getFireTicks();
               if (actual > before) {
                  appliedControls.put(AbilityEffect.BURN, new AbilityEffects.AppliedControl(1.0, actual));
               }
            }

            if (effects.contains(AbilityEffect.PARTY_DAMAGE_MARK) && this.authorized(caster, enemy, spec, AbilityEffect.PARTY_DAMAGE_MARK)) {
               double markMultiplier = ActiveAbilityLevelScaling.modifier(tuning.modifierMultiplier(), effectiveLevel);
               if (markMultiplier > 1.0) {
                  this.modifiers
                     .markEnemy(
                        caster,
                        enemy,
                        this.beneficiaryIds(caster),
                        markMultiplier,
                        levelDuration,
                        spec.id(),
                        spec.executionTraits().mechanics().contains(AbilityMechanic.HEALTH_SCALED_MARK)
                     );
               }

               this.states.armDetonation(caster, spec, enemy, effectiveLevel);
               affectedEnemies.add(enemy);
               this.observeApplied(
                  caster, enemy, spec, AbilityRuntimeObservation.Kind.MODIFIER_APPLIED, AbilityEffect.PARTY_DAMAGE_MARK, markMultiplier, levelDuration
               );
            }

            if (effects.contains(AbilityEffect.TAUNT) && this.authorized(caster, enemy, spec, AbilityEffect.TAUNT)) {
               tauntedEnemies.add(enemy);
               affectedEnemies.add(enemy);
            }

            if (!appliedControls.isEmpty() || observedPotionControl) {
               controlledEnemies++;
               affectedEnemies.add(enemy);
               appliedControls.forEach(
                  (effect, actual) -> AbilityControlEvidence.applied(
                        caster.getUniqueId(), enemy.getUniqueId(), spec, Set.of(effect), actual.amount(), actual.ticks()
                     )
                     .forEach(this.semantics::observe)
               );
            }
         }
      }

      List<Player> selectedAllies = new ArrayList<>(selection.allies());
      if (spec.executionTraits().mechanics().contains(AbilityMechanic.CASTER_ONLY_FIELD)) {
         selectedAllies.removeIf(allyx -> !allyx.getUniqueId().equals(caster.getUniqueId()));
      }

      if (spec.executionTraits().mechanics().contains(AbilityMechanic.CASTER_ONLY_SUPPORT)) {
         selectedAllies.removeIf(allyx -> !allyx.getUniqueId().equals(caster.getUniqueId()));
      }

      if (spec.executionTraits().mechanics().contains(AbilityMechanic.PARTY_BUFF)) {
         double radius = Math.max(8.0, tuning.radius());

         for (Player ally : this.semantics.alliesOf(caster)) {
            if (ally != null
               && ally.isOnline()
               && ally.getWorld().equals(caster.getWorld())
               && ally.getLocation().distanceSquared(selection.origin()) <= radius * radius) {
               selectedAllies.add(ally);
            }
         }
      }

      List<Player> allies = this.uniqueValidAllies(caster, selectedAllies);
      if (spec.executionTraits().mechanics().contains(AbilityMechanic.LOWEST_HEALTH_FIRST)) {
         allies = allies.stream().sorted(Comparator.comparingDouble(AbilityEffects::healthFraction)).limit(tuning.projectileCount()).toList();
      }

      if (allies.isEmpty() && hasCasterBuff(effects) && spec.target() != AbilityTarget.AIMED_LOCATION) {
         allies = List.of(caster);
      }

      int authoredEffectDuration = effects.contains(AbilityEffect.HEAL)
         ? ActiveAbilityLevelScaling.durationTicks(AbilityHealingPolicy.durationTicks(tuning.durationTicks(), healingType, mechanicModifiers), effectiveLevel)
         : levelDuration;
      int effectDuration = this.states.linkedDurationTicks(caster, spec, authoredEffectDuration);

      for (Player allyx : allies) {
         boolean supported = false;
         if (effects.contains(AbilityEffect.HEAL) && tuning.healingFraction() > 0.0 && !this.states.suppressesImmediate(spec, AbilityEffect.HEAL)) {
            double fraction = tuning.healingFraction();
            if (spec.executionTraits().mechanics().contains(AbilityMechanic.GROUP_SCALING)) {
               fraction *= 1.0 + Math.min(0.75, Math.max(0, allies.size() - 1) * 0.15);
            }

            if (allyx.getUniqueId().equals(caster.getUniqueId())) {
               fraction *= spec.executionTraits().selfHealingMultiplier();
            }

            double healed = heal(allyx, maximumHealth(allyx) * fraction * classRankScale * healingMultiplier);
            totalHealing += healed;
            supported |= healed > 0.0;
            if (healed > 0.0) {
               this.semantics
                  .observe(
                     AbilityRuntimeObservation.effect(
                        AbilityRuntimeObservation.Kind.HEAL, caster.getUniqueId(), allyx.getUniqueId(), spec.id(), healed, effectDuration, AbilityEffect.HEAL
                     )
                  );
            }

            this.states.scheduleHealEcho(caster, allyx, healed, spec);
         }

         if (effects.contains(AbilityEffect.SHIELD) && tuning.shieldFraction() > 0.0 && !this.states.suppressesImmediate(spec, AbilityEffect.SHIELD)) {
            double shieldAmount = maximumHealth(allyx) * tuning.shieldFraction() * classRankScale * mechanicModifiers.shieldStrengthMultiplier();
            if (shieldAmount > 0.0 && Double.isFinite(shieldAmount)) {
               int amplifierx = Math.max(0, Math.min(255, (int)Math.ceil(shieldAmount / 4.0) - 1));
               double before = allyx.getAbsorptionAmount();
               supported |= this.potions
                  .apply(
                     caster.getUniqueId(),
                     allyx,
                     new PotionEffect(PotionEffectType.ABSORPTION, Math.max(20, effectDuration), amplifierx, false, true, true),
                     actual -> this.observeApplied(
                        caster,
                        ally,
                        spec,
                        AbilityRuntimeObservation.Kind.SHIELD,
                        AbilityEffect.SHIELD,
                        Math.max(0.0, ally.getAbsorptionAmount() - before),
                        actual.getDuration()
                     ),
                     () -> this.observeApplied(caster, ally, spec, AbilityRuntimeObservation.Kind.STATUS_BLOCKED, AbilityEffect.SHIELD, 0.0, 0)
                  );
            }
         }

         if (effects.contains(AbilityEffect.CLEANSE)) {
            cleanse(allyx);
            this.observeApplied(caster, allyx, spec, AbilityRuntimeObservation.Kind.STATUS_APPLIED, AbilityEffect.CLEANSE, 1.0, effectDuration);
            supported = true;
         }

         if (effects.contains(AbilityEffect.SPEED) && !missingHealthScaling) {
            supported |= this.applyPotion(
               caster,
               allyx,
               spec,
               AbilityEffect.SPEED,
               AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
               new PotionEffect(PotionEffectType.SPEED, Math.max(20, effectDuration), 1, false, true, true),
               ignored -> {}
            );
         }

         for (AbilityEffect effect : List.of(AbilityEffect.HASTE, AbilityEffect.RESISTANCE)) {
            if (effects.contains(effect)) {
               PotionEffectType type = effect == AbilityEffect.HASTE ? PotionEffectType.HASTE : PotionEffectType.RESISTANCE;
               int durationx = Math.max(20, effectDuration);
               supported |= this.applyPotion(
                  caster,
                  allyx,
                  spec,
                  effect,
                  AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                  new PotionEffect(type, durationx, 0, false, true, true),
                  ignored -> {}
               );
            }
         }

         if (effects.contains(AbilityEffect.STRENGTH) && !missingHealthScaling) {
            double multiplier = tuning.modifierMultiplier() > 1.0 ? tuning.modifierMultiplier() : 1.15;
            multiplier = ActiveAbilityLevelScaling.modifier(multiplier, effectiveLevel);
            this.modifiers.modifyOutgoing(caster, allyx, multiplier, effectDuration, spec.id());
            this.observeApplied(caster, allyx, spec, AbilityRuntimeObservation.Kind.MODIFIER_APPLIED, AbilityEffect.STRENGTH, multiplier, effectDuration);
            supported = true;
         }

         if (effects.contains(AbilityEffect.SPELL_STRENGTH)) {
            double multiplier = tuning.modifierMultiplier() > 1.0 ? tuning.modifierMultiplier() : 1.15;
            multiplier = ActiveAbilityLevelScaling.modifier(multiplier, effectiveLevel);
            this.modifiers.modifySpellOutgoing(caster, allyx, multiplier, effectDuration, spec.id());
            this.observeApplied(caster, allyx, spec, AbilityRuntimeObservation.Kind.MODIFIER_APPLIED, AbilityEffect.SPELL_STRENGTH, multiplier, effectDuration);
            supported = true;
         }

         if (effects.contains(AbilityEffect.ALLY_PROTECT) && !this.states.suppressesImmediate(spec, AbilityEffect.ALLY_PROTECT)) {
            double protection = tuning.modifierMultiplier() > 0.0 && tuning.modifierMultiplier() < 1.0 ? tuning.modifierMultiplier() : 0.8;
            protection = ActiveAbilityLevelScaling.modifier(protection, effectiveLevel);
            this.modifiers.modifyIncoming(caster, allyx, protection, effectDuration, spec.id());
            this.observeApplied(caster, allyx, spec, AbilityRuntimeObservation.Kind.MODIFIER_APPLIED, AbilityEffect.ALLY_PROTECT, protection, effectDuration);
            supported = true;
         }

         if (supported) {
            supportedAllies++;
         }
      }

      if (effects.contains(AbilityEffect.SELF_PROTECT)) {
         double protection = ActiveAbilityLevelScaling.modifier(0.8, effectiveLevel);
         this.modifiers.modifyIncoming(caster, caster, protection, effectDuration, spec.id());
         this.observeApplied(caster, caster, spec, AbilityRuntimeObservation.Kind.MODIFIER_APPLIED, AbilityEffect.SELF_PROTECT, protection, effectDuration);
         if (allies.stream().noneMatch(allyx -> allyx.getUniqueId().equals(caster.getUniqueId()))) {
            supportedAllies++;
         }
      }

      if (effects.contains(AbilityEffect.SELF_VULNERABLE)) {
         double vulnerability = tuning.modifierMultiplier() > 1.0 ? tuning.modifierMultiplier() : 1.15;
         vulnerability = ActiveAbilityLevelScaling.modifier(vulnerability, effectiveLevel);
         this.modifiers.modifyIncomingPenalty(caster, caster, vulnerability, effectDuration, spec.id());
         this.observeApplied(
            caster, caster, spec, AbilityRuntimeObservation.Kind.MODIFIER_APPLIED, AbilityEffect.SELF_VULNERABLE, vulnerability, effectDuration
         );
         supportedAllies++;
      }

      if (effects.contains(AbilityEffect.LIFESTEAL) && totalDamage > 0.0) {
         double healed = heal(caster, totalDamage * 0.22 * healingMultiplier);
         totalHealing += healed;
         if (healed > 0.0) {
            this.semantics
               .observe(
                  AbilityRuntimeObservation.effect(
                     AbilityRuntimeObservation.Kind.HEAL, caster.getUniqueId(), caster.getUniqueId(), spec.id(), healed, 0, AbilityEffect.LIFESTEAL
                  )
               );
         }
      }

      if (releasedRetaliation > 0.0 && spec.executionTraits().mechanics().contains(AbilityMechanic.RETALIATION_HEAL) && !allies.isEmpty()) {
         double budget = AbilityStateMath.retaliationHealing(releasedRetaliation, 0.45) * healingMultiplier;
         double share = budget / allies.size();

         for (Player allyx : allies) {
            double healed = heal(allyx, share);
            totalHealing += healed;
            if (healed > 0.0) {
               this.semantics
                  .observe(
                     AbilityRuntimeObservation.effect(
                        AbilityRuntimeObservation.Kind.HEAL, caster.getUniqueId(), allyx.getUniqueId(), spec.id(), healed, 0, AbilityEffect.HEAL
                     )
                  );
            }
         }
      }

      double threat = 0.0;
      if (!tauntedEnemies.isEmpty()) {
         double amountPerEnemy = Math.max(attackDamage(caster) * 10.0, 25.0);
         int forcedTicks = Math.max(20, controlDuration);
         boolean extendsTaunt = spec.executionTraits().mechanics().contains(AbilityMechanic.EXTEND_TAUNT);
         this.semantics.requestThreat(new AbilitySemantics.ThreatRequest(caster, tauntedEnemies, amountPerEnemy, forcedTicks, spec.id()));
         Set<UUID> extendedTargets = this.states.registerTaunts(caster, tauntedEnemies, forcedTicks, extendsTaunt);

         for (LivingEntity taunted : tauntedEnemies) {
            this.semantics
               .observe(
                  AbilityRuntimeObservation.effect(
                     AbilityRuntimeObservation.Kind.TAUNT,
                     caster.getUniqueId(),
                     taunted.getUniqueId(),
                     spec.id(),
                     amountPerEnemy,
                     forcedTicks,
                     AbilityEffect.TAUNT
                  )
               );
            if (extendedTargets.contains(taunted.getUniqueId())) {
               this.observeMechanicTriggered(caster, taunted, spec, AbilityMechanic.EXTEND_TAUNT, AbilityEffect.TAUNT, amountPerEnemy, forcedTicks);
            }
         }

         threat = amountPerEnemy * tauntedEnemies.size();
      }

      if (spec.executionTraits().mechanics().contains(AbilityMechanic.OPENS_DEFENSE_BREAK)) {
         this.states.registerDefenseBreaks(caster, affectedEnemies, controlDuration);
      }

      if (!affectedEnemies.isEmpty()) {
         caster.getWorld().spawnParticle(Particle.CRIT, selection.origin(), Math.min(40, 4 + affectedEnemies.size() * 3), 0.8, 0.5, 0.8, 0.05);
      }

      if (!allies.isEmpty()) {
         caster.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, selection.origin(), Math.min(30, 3 + allies.size() * 2), 0.7, 0.5, 0.7, 0.02);
      }

      return new AbilityContribution(totalDamage, totalHealing, 0.0, threat, controlledEnemies, supportedAllies, 0.0);
   }

   void clearSource(UUID sourceId) {
      this.potions.clearSource(sourceId);
      this.crowdControl.clearSource(sourceId);
   }

   void close() {
      this.potions.close();
      this.crowdControl.close();
   }

   private List<LivingEntity> nearbyEnemies(Player caster, FixedAbilitySpec spec, double radius) {
      List<LivingEntity> enemies = new ArrayList<>();

      for (Entity entity : caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius)) {
         if (entity instanceof LivingEntity living && this.semantics.canTargetEnemy(caster, living, spec)) {
            enemies.add(living);
         }
      }

      return enemies;
   }

   private boolean authorized(Player caster, LivingEntity enemy, FixedAbilitySpec spec, AbilityEffect effect) {
      return this.semantics.canApplyEnemyEffect(caster, enemy, spec, effect);
   }

   private Set<UUID> beneficiaryIds(Player caster) {
      Set<UUID> ids = new LinkedHashSet<>();
      ids.add(caster.getUniqueId());

      for (Player ally : this.semantics.alliesOf(caster)) {
         if (ally != null && ally.isOnline()) {
            ids.add(ally.getUniqueId());
         }
      }

      return Set.copyOf(ids);
   }

   private List<Player> uniqueValidAllies(Player caster, Collection<Player> input) {
      Set<UUID> currentParty = new LinkedHashSet<>();
      currentParty.add(caster.getUniqueId());

      for (Player ally : this.semantics.alliesOf(caster)) {
         if (ally != null) {
            currentParty.add(ally.getUniqueId());
         }
      }

      Map<UUID, Player> allies = new LinkedHashMap<>();

      for (Player player : input) {
         if (player != null
            && currentParty.contains(player.getUniqueId())
            && player.isOnline()
            && player.isValid()
            && !player.isDead()
            && ClassAbilityEligibility.isEligible(player)
            && player.getWorld().equals(caster.getWorld())) {
            allies.put(player.getUniqueId(), player);
         }
      }

      return List.copyOf(allies.values());
   }

   private static boolean hasCasterBuff(Set<AbilityEffect> effects) {
      return effects.contains(AbilityEffect.HEAL)
         || effects.contains(AbilityEffect.SHIELD)
         || effects.contains(AbilityEffect.CLEANSE)
         || effects.contains(AbilityEffect.SPEED)
         || effects.contains(AbilityEffect.HASTE)
         || effects.contains(AbilityEffect.RESISTANCE)
         || effects.contains(AbilityEffect.STRENGTH)
         || effects.contains(AbilityEffect.SELF_PROTECT);
   }

   private static double heal(Player target, double amount) {
      if (target.isOnline() && !target.isDead() && !(amount <= 0.0) && Double.isFinite(amount)) {
         double maximum = maximumHealth(target);
         double before = target.getHealth();
         double missing = Math.max(0.0, maximum - before);
         if (missing <= 0.0) {
            return 0.0;
         } else {
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(target, Math.min(missing, amount), RegainReason.CUSTOM);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && !(event.getAmount() <= 0.0) && Double.isFinite(event.getAmount())) {
               target.setHealth(Math.min(maximum, before + event.getAmount()));
               return Math.max(0.0, target.getHealth() - before);
            } else {
               return 0.0;
            }
         }
      } else {
         return 0.0;
      }
   }

   private boolean applyPotion(
      Player caster,
      LivingEntity target,
      FixedAbilitySpec spec,
      AbilityEffect effect,
      AbilityRuntimeObservation.Kind kind,
      PotionEffect requested,
      Consumer<PotionEffect> onApplied
   ) {
      return this.potions.apply(caster.getUniqueId(), target, requested, actual -> {
         onApplied.accept(actual);
         this.observeApplied(caster, target, spec, kind, effect, actual.getAmplifier() + 1.0, actual.getDuration());
      }, () -> this.observeApplied(caster, target, spec, AbilityRuntimeObservation.Kind.STATUS_BLOCKED, effect, 0.0, 0));
   }

   private void observeApplied(
      Player caster, LivingEntity target, FixedAbilitySpec spec, AbilityRuntimeObservation.Kind kind, AbilityEffect effect, double amount, int durationTicks
   ) {
      this.semantics
         .observe(AbilityRuntimeObservation.effect(kind, caster.getUniqueId(), target.getUniqueId(), spec.id(), amount, Math.max(0, durationTicks), effect));
   }

   private void observeMechanicTriggered(
      Player caster, LivingEntity target, FixedAbilitySpec spec, AbilityMechanic mechanic, AbilityEffect effect, double amount, int durationTicks
   ) {
      this.semantics
         .observe(AbilityControlEvidence.mechanicTriggered(caster.getUniqueId(), target.getUniqueId(), spec, mechanic, effect, amount, durationTicks));
   }

   private static void cleanse(Player target) {
      for (PotionEffectType type : List.of(
         PotionEffectType.SLOWNESS,
         PotionEffectType.WEAKNESS,
         PotionEffectType.POISON,
         PotionEffectType.WITHER,
         PotionEffectType.BLINDNESS,
         PotionEffectType.NAUSEA,
         PotionEffectType.MINING_FATIGUE,
         PotionEffectType.LEVITATION,
         PotionEffectType.DARKNESS,
         PotionEffectType.HUNGER,
         PotionEffectType.UNLUCK
      )) {
         target.removePotionEffect(type);
      }
   }

   private static double displaceAway(Player caster, LivingEntity target, double horizontal, double vertical) {
      Vector direction = target.getLocation().toVector().subtract(caster.getLocation().toVector());
      direction.setY(0);
      if (direction.lengthSquared() < 1.0E-6) {
         double yaw = Math.toRadians(caster.getLocation().getYaw());
         direction = new Vector(-Math.sin(yaw), 0.0, Math.cos(yaw));
      }

      direction.normalize().multiply(Math.max(0.1, horizontal)).setY(vertical);
      Vector before = target.getVelocity();
      target.setVelocity(before.clone().add(direction));
      return target.getVelocity().subtract(before).length();
   }

   private static double displaceToward(Player caster, LivingEntity target, double strength) {
      Vector direction = caster.getLocation().toVector().subtract(target.getLocation().toVector());
      if (direction.lengthSquared() < 1.0E-6) {
         return 0.0;
      } else {
         direction.normalize().multiply(Math.max(0.1, strength)).setY(0.15);
         Vector before = target.getVelocity();
         target.setVelocity(before.clone().add(direction));
         return target.getVelocity().subtract(before).length();
      }
   }

   private static int scaledDuration(int authoredTicks, double multiplier) {
      return authoredTicks <= 0 ? 0 : (int)Math.min(2147483647L, Math.max(1L, Math.round(authoredTicks * multiplier)));
   }

   private static int scaledAmplifier(int authoredAmplifier, double potencyMultiplier) {
      return Math.max(0, Math.min(255, (int)Math.round((authoredAmplifier + 1.0) * potencyMultiplier) - 1));
   }

   private static CombatDamageContext.ClassAbilityDamageDomain damageDomain(FixedAbilitySpec spec, CombatDamageContext.ClassAbilityDelivery delivery) {
      Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
      boolean area = spec.family() == AbilityFamily.ZONE
         || spec.target() == AbilityTarget.NEARBY_ENEMIES
         || spec.target() == AbilityTarget.MIXED_NEARBY
         || spec.target() == AbilityTarget.AIMED_LOCATION
         || spec.target() == AbilityTarget.FORWARD_ENEMIES
         || mechanics.contains(AbilityMechanic.PIERCING_CAST)
         || mechanics.contains(AbilityMechanic.CHAINING_CAST)
         || mechanics.contains(AbilityMechanic.CLUSTER_PROJECTILE)
         || mechanics.contains(AbilityMechanic.PROJECTILE_BOMBARDMENT)
         || mechanics.contains(AbilityMechanic.EXPANDING_PULSES);
      CombatDamageContext.ClassAbilityArchetype archetype = mechanics.contains(AbilityMechanic.DELAYED_PAYLOAD)
         ? CombatDamageContext.ClassAbilityArchetype.TRAP
         : (
            !mechanics.contains(AbilityMechanic.CLUSTER_PROJECTILE) && !mechanics.contains(AbilityMechanic.DETONATING_MARK)
               ? CombatDamageContext.ClassAbilityArchetype.OTHER
               : CombatDamageContext.ClassAbilityArchetype.BLAST
         );
      return new CombatDamageContext.ClassAbilityDamageDomain(
         area ? CombatDamageContext.ClassAbilityTargetShape.AREA : CombatDamageContext.ClassAbilityTargetShape.SINGLE_TARGET,
         delivery,
         archetype,
         mechanics.contains(AbilityMechanic.GUARANTEED_CRITICAL)
            ? CombatDamageContext.ClassAbilityStrikeQuality.GUARANTEED_CRITICAL
            : CombatDamageContext.ClassAbilityStrikeQuality.NORMAL
      );
   }

   private static double attackDamage(Player player) {
      AttributeInstance attribute = player.getAttribute(Attribute.ATTACK_DAMAGE);
      return attribute == null ? 1.0 : Math.max(1.0, attribute.getValue());
   }

   private static double maximumHealth(LivingEntity entity) {
      AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
      return attribute == null ? Math.max(1.0, entity.getHealth()) : Math.max(1.0, attribute.getValue());
   }

   private static double healthFraction(Player player) {
      return player.getHealth() / maximumHealth(player);
   }

   private static boolean valid(LivingEntity entity) {
      return entity != null && entity.isValid() && !entity.isDead() && entity.getHealth() > 0.0;
   }

   private record AppliedControl(double amount, int ticks) {
   }
}
