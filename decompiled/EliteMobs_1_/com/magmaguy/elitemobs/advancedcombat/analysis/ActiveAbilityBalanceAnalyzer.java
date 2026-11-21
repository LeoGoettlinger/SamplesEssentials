package com.magmaguy.elitemobs.advancedcombat.analysis;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityCommitEffects;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityLevelScaling;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityMechanic;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassBand;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.advancedcombat.minions.ClassMinionBalanceContract;
import com.magmaguy.elitemobs.advancedcombat.minions.ClassMinionTheme;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ActiveAbilityBalanceAnalyzer {
   private static final double LOW_OUTLIER_RATIO = 0.67;
   private static final double HIGH_OUTLIER_RATIO = 1.5;
   private static final double MAXIMUM_CASTS_PER_MINUTE = 1200.0;

   private ActiveAbilityBalanceAnalyzer() {
   }

   public static ActiveAbilityBalanceReport analyzeBuiltIns() {
      return analyze(BuiltInClassContent.catalog(), BuiltInClassContent.abilityRegistry());
   }

   public static ActiveAbilityBalanceReport analyze(ClassCatalog catalog, FixedAbilityRegistry registry) {
      Objects.requireNonNull(catalog, "catalog");
      Objects.requireNonNull(registry, "registry");
      List<ActiveAbilityBalanceAnalyzer.Draft> drafts = new ArrayList<>();

      for (ClassFormDefinition root : catalog.roots()) {
         add(drafts, registry, root.id(), root.id(), root.rootKit().resourceType(), root, root.rootKit().mobility(), 1, 100);
      }

      for (ClassFormDefinition form : catalog.forms()) {
         ClassFormDefinition root = catalog.rootOf(form.id());
         int startLevel = form.band().effectiveStart();
         int evaluationLevel = Math.min(100, form.band().effectiveEnd());
         add(drafts, registry, root.id(), form.id(), root.rootKit().resourceType(), form, form.signature(), startLevel, evaluationLevel);
         add(drafts, registry, root.id(), form.id(), root.rootKit().resourceType(), form, form.utility(), startLevel, evaluationLevel);
      }

      Map<ActiveAbilityBalanceAnalyzer.ScenarioPeer, Double> medians = scenarioMedians(drafts);
      List<ActiveAbilityBalanceReport.Row> rows = drafts.stream()
         .map(draft -> draft.toRow(medians))
         .sorted(
            Comparator.<ActiveAbilityBalanceReport.Row, Integer>comparing(row -> row.slot().ordinal())
               .thenComparing(row -> row.band().depth())
               .thenComparing(ActiveAbilityBalanceReport.Row::formId)
         )
         .toList();
      return new ActiveAbilityBalanceReport(rows);
   }

   private static void add(
      List<ActiveAbilityBalanceAnalyzer.Draft> drafts,
      FixedAbilityRegistry registry,
      String rootClassId,
      String formId,
      ClassResourceType resourceType,
      ClassFormDefinition form,
      AbilityDefinition ability,
      int startLevel,
      int evaluationLevel
   ) {
      FixedAbilitySpec spec = registry.require(ability.id());
      ActiveAbilityBalanceAnalyzer.EffectBudget budget = effectBudget(spec);
      double start = budget.scaled() * AbilityLevelScaling.multiplier(startLevel) + budget.fixed();
      double normalized = budget.scaled() * AbilityLevelScaling.multiplier(evaluationLevel) + budget.fixed();
      double base = budget.scaled() + budget.fixed();
      double netResourceCost = Math.max(0.0, spec.resourceCost() - AbilityCommitEffects.resolve(spec, evaluationLevel).resourceGrant());
      double efficiency = normalized / Math.max(1.0, netResourceCost) * 100.0;
      ActiveAbilityBalanceReport.Cadence cadence = cadence(spec, evaluationLevel, resourceType);
      List<ActiveAbilityBalanceReport.TargetScenario> scenarios = targetScenarios(spec, normalized, cadence.sustainableCastsPerMinute(), netResourceCost);
      Optional<ActiveAbilityBalanceReport.SummonMetrics> summon = summonMetrics(spec, evaluationLevel);
      drafts.add(
         new ActiveAbilityBalanceAnalyzer.Draft(
            rootClassId, formId, spec, form.band(), resourceType, startLevel, evaluationLevel, base, start, normalized, efficiency, cadence, summon, scenarios
         )
      );
   }

   private static ActiveAbilityBalanceAnalyzer.EffectBudget effectBudget(FixedAbilitySpec spec) {
      double repetitions = spec.executionTraits().mechanics().contains(AbilityMechanic.EXPANDING_PULSES) ? 1.0 : Math.max(1, spec.tuning().repetitions());
      double deliveries = repetitions * Math.max(1, spec.tuning().projectileCount());
      double scaled = spec.tuning().damageMultiplier() * deliveries;
      if (spec.executionTraits().mechanics().contains(AbilityMechanic.GUARANTEED_CRITICAL)) {
         scaled += spec.tuning().damageMultiplier() * deliveries * 0.5;
      }

      scaled += spec.tuning().healingFraction() * 10.0 * deliveries;
      scaled += spec.tuning().shieldFraction() * 8.0 * deliveries;
      if (spec.effects().contains(AbilityEffect.LIFESTEAL)) {
         scaled += spec.tuning().damageMultiplier() * deliveries * 0.22;
      }

      double fixed = movementBudget(spec);
      boolean missingHealthScaling = spec.executionTraits().mechanics().contains(AbilityMechanic.MISSING_HEALTH_SCALING);

      for (AbilityEffect effect : spec.effects()) {
         double weight = fixedEffectWeight(effect, spec);
         if (!missingHealthScaling || effect != AbilityEffect.STRENGTH && effect != AbilityEffect.SPEED) {
            if (levelScaledEffect(effect)) {
               scaled += weight;
            } else {
               fixed += weight;
            }
         } else {
            scaled += weight;
         }
      }

      for (AbilityMechanic mechanic : spec.executionTraits().mechanics()) {
         double weight = mechanicWeight(mechanic);
         if (mechanic != AbilityMechanic.MISSING_HEALTH_SCALING && !levelScaledMechanic(mechanic)) {
            fixed += weight;
         } else {
            scaled += weight;
         }
      }

      return new ActiveAbilityBalanceAnalyzer.EffectBudget(scaled, fixed);
   }

   private static boolean levelScaledEffect(AbilityEffect effect) {
      return switch (effect) {
         case CLEANSE -> false;
         default -> true;
      };
   }

   private static boolean levelScaledMechanic(AbilityMechanic mechanic) {
      return switch (mechanic) {
         case FOLLOW_CASTER_FIELD, WIND_UP, PLANTED_GUARD, CONTROL_IMMUNITY, DEBUFF_IMMUNITY, OPENS_DEFENSE_BREAK, DAMAGE_REDIRECT, MULTI_ALLY_REDIRECT, RETALIATION_RELEASE, DEATH_GUARD, BARRIER_BREAK_HEAL, THREAT_TRIGGERED_PROTECTION, DAMAGE_SHARE, HEAL_ECHO, SUSTAINED_TETHER, LIFESTEAL_WINDOW, RETALIATION_HEAL, RESOURCE_BURST, DETONATING_MARK, EXTEND_TAUNT, EXTEND_CONTROL_DURATION, WARD_BREAK_SIGNAL -> true;
         default -> false;
      };
   }

   private static List<ActiveAbilityBalanceReport.TargetScenario> targetScenarios(
      FixedAbilitySpec spec, double perTargetEffect, double sustainableCastsPerMinute, double netResourceCost
   ) {
      List<ActiveAbilityBalanceReport.TargetScenario> scenarios = new ArrayList<>();

      for (ActiveAbilityBalanceReport.TargetDensity density : ActiveAbilityBalanceReport.TargetDensity.values()) {
         int affectedTargets = affectedTargets(spec, density.targetCount());
         double castEffect = perTargetEffect * affectedTargets;
         scenarios.add(
            new ActiveAbilityBalanceReport.TargetScenario(
               density,
               density.targetCount(),
               affectedTargets,
               castEffect,
               castEffect / Math.max(1.0, netResourceCost) * 100.0,
               castEffect * sustainableCastsPerMinute,
               ActiveAbilityBalanceReport.Outlier.NONE
            )
         );
      }

      return List.copyOf(scenarios);
   }

   private static int affectedTargets(FixedAbilitySpec spec, int availableTargets) {
      if (spec.executionTraits().mechanics().contains(AbilityMechanic.PROJECTILE_BOMBARDMENT)) {
         return 1;
      } else if (spec.executionTraits().mechanics().contains(AbilityMechanic.SINGLE_ENEMY)) {
         return 1;
      } else if (spec.executionTraits().mechanics().contains(AbilityMechanic.PIERCING_CAST)) {
         return availableTargets;
      } else {
         ClassMinionTheme minionTheme = ClassMinionTheme.forAbility(spec.id()).orElse(null);
         if (minionTheme != null && !minionTheme.supportRole()) {
            return 1;
         } else {
            return switch (spec.target()) {
               case SELF, AIMED_ENEMY, AIMED_ALLY -> 1;
               case FORWARD_ENEMIES -> spec.family() == AbilityFamily.PROJECTILE ? 1 : availableTargets;
               case NEARBY_ENEMIES, NEARBY_ALLIES, MIXED_NEARBY, AIMED_LOCATION -> availableTargets;
            };
         }
      }
   }

   private static ActiveAbilityBalanceReport.Cadence cadence(FixedAbilitySpec spec, int evaluationLevel, ClassResourceType resourceType) {
      double netResourceCost = Math.max(0.0, spec.resourceCost() - AbilityCommitEffects.resolve(spec, evaluationLevel).resourceGrant());
      double recoveryPerSecond = BuiltInClassContent.resourceDefinitions().get(resourceType).inCombatTickDelta();
      double resourceRefillSeconds = netResourceCost / recoveryPerSecond;
      double sustainable = resourceRefillSeconds <= 0.0 ? 1200.0 : Math.min(1200.0, 60.0 / resourceRefillSeconds);
      return new ActiveAbilityBalanceReport.Cadence(resourceRefillSeconds, sustainable);
   }

   private static Optional<ActiveAbilityBalanceReport.SummonMetrics> summonMetrics(FixedAbilitySpec spec, int evaluationLevel) {
      ClassMinionTheme theme = ClassMinionTheme.forAbility(spec.id()).orElse(null);
      if (theme == null) {
         return Optional.empty();
      } else {
         ClassMinionBalanceContract contract = ClassMinionBalanceContract.from(spec, theme, evaluationLevel);
         return Optional.of(
            new ActiveAbilityBalanceReport.SummonMetrics(
               contract.summonCount(),
               contract.ownerCap(),
               contract.uptimeTicks(),
               Math.max(1, contract.uptimeTicks() / contract.attackPeriodTicks()),
               contract.attackPeriodTicks(),
               contract.maxHealth(),
               contract.scaledDamageMultiplierPerHit(),
               theme.supportRole()
            )
         );
      }
   }

   private static double movementBudget(FixedAbilitySpec spec) {
      return switch (spec.family()) {
         case MOUNTED_CHARGE, BALLISTIC_LEAP, SAFE_DASH, SAFE_BLINK, ALLY_FLIGHT -> Math.max(0.0, spec.tuning().range()) / 8.0;
         default -> 0.0;
      };
   }

   private static double fixedEffectWeight(AbilityEffect effect, FixedAbilitySpec spec) {
      if (effect == AbilityEffect.ALLY_PROTECT && spec.executionTraits().mechanics().contains(AbilityMechanic.THREAT_TRIGGERED_PROTECTION)) {
         return 0.0;
      } else {
         double durationFactor = Math.sqrt(Math.max(20, spec.tuning().durationTicks()) / 100.0);

         return switch (effect) {
            case CLEANSE -> 0.55;
            case DAMAGE, HEAL, LIFESTEAL, SHIELD -> 0.0;
            case KNOCKBACK -> 0.25;
            case PULL -> 0.35;
            case LAUNCH -> 0.45;
            case SLOW -> 0.35 * durationFactor;
            case WEAKEN -> Math.max(0.0, 1.0 - spec.tuning().weakenMultiplier()) * 3.0 * durationFactor;
            case GLOW -> 0.1 * durationFactor;
            case PARTY_DAMAGE_MARK -> Math.max(0.0, spec.tuning().modifierMultiplier() - 1.0) * Math.max(1.0, spec.tuning().durationTicks() / 20.0) * 0.5;
            case TAUNT -> 0.55 * durationFactor;
            case SELF_PROTECT -> 0.7 * durationFactor;
            case ALLY_PROTECT -> 0.8 * durationFactor;
            case SPEED -> 0.35 * durationFactor;
            case HASTE -> 0.3 * durationFactor;
            case RESISTANCE -> 0.7 * durationFactor;
            case STRENGTH -> Math.max(0.45, Math.max(0.0, spec.tuning().modifierMultiplier() - 1.0) * 3.0) * durationFactor;
            case SPELL_STRENGTH -> Math.max(0.45, Math.max(0.0, spec.tuning().modifierMultiplier() - 1.0) * 3.0) * durationFactor;
            case SELF_VULNERABLE -> -Math.max(0.35, Math.max(0.0, spec.tuning().modifierMultiplier() - 1.0) * 2.0) * durationFactor;
            case INTERRUPT -> 0.45;
            case FEAR -> 0.65 * durationFactor;
            case ROOT -> 0.75 * durationFactor;
            case BURN -> 0.2 * durationFactor;
         };
      }
   }

   private static double mechanicWeight(AbilityMechanic mechanic) {
      return switch (mechanic) {
         case FOLLOW_CASTER_FIELD, WIND_UP, PLANTED_GUARD, HEAL_ECHO, LOWEST_HEALTH_FIRST, PIERCING_CAST, GROUP_SCALING -> 0.25;
         case CONTROL_IMMUNITY, DAMAGE_REDIRECT, MULTI_ALLY_REDIRECT, SUSTAINED_TETHER -> 0.55;
         case DEBUFF_IMMUNITY, EXTEND_CONTROL_DURATION -> 0.55;
         case OPENS_DEFENSE_BREAK -> 0.2;
         case RETALIATION_RELEASE, LIFESTEAL_WINDOW, DETONATING_MARK, EXECUTE_DAMAGE, CHAINING_CAST, DELAYED_PAYLOAD, PARTY_BUFF -> 0.4;
         case DEATH_GUARD, DAMAGE_SHARE -> 0.8;
         case BARRIER_BREAK_HEAL, THREAT_TRIGGERED_PROTECTION, RETALIATION_HEAL, HEALTH_SCALED_MARK, CLUSTER_PROJECTILE -> 0.4;
         case RESOURCE_BURST, GUARANTEED_CRITICAL -> 0.0;
         case EXTEND_TAUNT -> 0.2;
         case WARD_BREAK_SIGNAL -> 0.0;
         case MISSING_HEALTH_SCALING -> 0.4;
         case EXPANDING_PULSES, PROJECTILE_BOMBARDMENT -> 0.0;
         case RECENT_ATTACKERS, TAUNTED_TARGETS_ONLY, WOUNDED_TARGETS_ONLY, CASTER_ONLY_FIELD, LARGE_OR_BOSS_ONLY, BOSS_ONLY, MINIMUM_RANGE, REQUIRES_MOVEMENT, LOW_HEALTH_ALLY_ONLY -> -0.15;
         case REQUIRES_ACTIVE_FIELD, REQUIRES_WIND_UP, REQUIRES_DEFENSE_BREAK, REQUIRES_CORPSE -> -0.2;
         case SINGLE_ENEMY, CASTER_ONLY_SUPPORT, ASSISTED_ALLY_TARGETING -> 0.0;
      };
   }

   private static Map<ActiveAbilityBalanceAnalyzer.ScenarioPeer, Double> scenarioMedians(List<ActiveAbilityBalanceAnalyzer.Draft> drafts) {
      Map<ActiveAbilityBalanceAnalyzer.ScenarioPeer, List<Double>> grouped = new HashMap<>();

      for (ActiveAbilityBalanceAnalyzer.Draft draft : drafts) {
         for (ActiveAbilityBalanceReport.TargetScenario scenario : draft.scenarios()) {
            ActiveAbilityBalanceAnalyzer.ScenarioPeer peer = new ActiveAbilityBalanceAnalyzer.ScenarioPeer(
               draft.spec().slot(), draft.band(), scenario.density()
            );
            grouped.computeIfAbsent(peer, ignored -> new ArrayList<>()).add(scenario.sustainableEffectPerMinute());
         }
      }

      Map<ActiveAbilityBalanceAnalyzer.ScenarioPeer, Double> medians = new HashMap<>();
      grouped.forEach((group, values) -> medians.put(group, median((List<Double>)values)));
      return medians;
   }

   private static double median(List<Double> input) {
      List<Double> sorted = input.stream().sorted().toList();
      int middle = sorted.size() / 2;
      return sorted.size() % 2 == 0 ? (sorted.get(middle - 1) + sorted.get(middle)) / 2.0 : sorted.get(middle);
   }

   static ActiveAbilityBalanceReport.Outlier detectOutlier(double efficiency, double median) {
      if (median <= 0.0) {
         return ActiveAbilityBalanceReport.Outlier.NONE;
      } else if (efficiency < median * 0.67) {
         return ActiveAbilityBalanceReport.Outlier.LOW;
      } else {
         return efficiency > median * 1.5 ? ActiveAbilityBalanceReport.Outlier.HIGH : ActiveAbilityBalanceReport.Outlier.NONE;
      }
   }

   private record Draft(
      String rootClassId,
      String formId,
      FixedAbilitySpec spec,
      ClassBand band,
      ClassResourceType resourceType,
      int startLevel,
      int evaluationLevel,
      double baseEffect,
      double startEffect,
      double normalizedEffect,
      double efficiency,
      ActiveAbilityBalanceReport.Cadence cadence,
      Optional<ActiveAbilityBalanceReport.SummonMetrics> summon,
      List<ActiveAbilityBalanceReport.TargetScenario> scenarios
   ) {
      private ActiveAbilityBalanceReport.Row toRow(Map<ActiveAbilityBalanceAnalyzer.ScenarioPeer, Double> medians) {
         List<ActiveAbilityBalanceReport.TargetScenario> rated = this.scenarios
            .stream()
            .map(
               scenario -> new ActiveAbilityBalanceReport.TargetScenario(
                  scenario.density(),
                  scenario.targetCount(),
                  scenario.affectedTargets(),
                  scenario.castEffect(),
                  scenario.resourceEfficiency(),
                  scenario.sustainableEffectPerMinute(),
                  ActiveAbilityBalanceAnalyzer.detectOutlier(
                     scenario.sustainableEffectPerMinute(),
                     medians.get(new ActiveAbilityBalanceAnalyzer.ScenarioPeer(this.spec.slot(), this.band, scenario.density()))
                  )
               )
            )
            .toList();
         boolean high = rated.stream().anyMatch(scenario -> scenario.outlier() == ActiveAbilityBalanceReport.Outlier.HIGH);
         boolean low = rated.stream().anyMatch(scenario -> scenario.outlier() == ActiveAbilityBalanceReport.Outlier.LOW);
         ActiveAbilityBalanceReport.Outlier outlier = high && low
            ? ActiveAbilityBalanceReport.Outlier.MIXED
            : (high ? ActiveAbilityBalanceReport.Outlier.HIGH : (low ? ActiveAbilityBalanceReport.Outlier.LOW : ActiveAbilityBalanceReport.Outlier.NONE));
         return new ActiveAbilityBalanceReport.Row(
            this.rootClassId,
            this.formId,
            this.spec.id(),
            this.spec.slot(),
            this.band,
            this.startLevel,
            this.evaluationLevel,
            this.resourceType,
            this.spec.resourceCost(),
            this.baseEffect,
            this.startEffect,
            this.normalizedEffect,
            this.efficiency,
            outlier,
            this.cadence,
            this.summon,
            rated
         );
      }
   }

   private record EffectBudget(double scaled, double fixed) {
   }

   private record ScenarioPeer(AbilitySlot slot, ClassBand band, ActiveAbilityBalanceReport.TargetDensity density) {
   }
}
