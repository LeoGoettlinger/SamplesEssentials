package com.magmaguy.elitemobs.advancedcombat.passives;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.advancedcombat.progression.ActiveLineageSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record PassiveAggregate(
   double outgoingDamageMultiplier,
   double incomingDamageMultiplier,
   double movementSpeedAdjustment,
   double healingDoneMultiplier,
   double abilityCostReductionFraction,
   double healingReceivedMultiplier,
   PassiveMechanics mechanics,
   List<PassiveAggregate.ResolvedTrait> conditionalTraits,
   List<PassiveAggregate.ResolvedMechanicTrait> conditionalMechanicTraits
) {
   public static final PassiveAggregate NEUTRAL = new PassiveAggregate(1.0, 1.0, 0.0, 1.0, 0.0, 1.0, PassiveMechanics.NEUTRAL, List.of(), List.of());

   public PassiveAggregate(
      double outgoingDamageMultiplier,
      double incomingDamageMultiplier,
      double movementSpeedAdjustment,
      double healingDoneMultiplier,
      double abilityCostReductionFraction,
      double healingReceivedMultiplier,
      PassiveMechanics mechanics,
      List<PassiveAggregate.ResolvedTrait> conditionalTraits,
      List<PassiveAggregate.ResolvedMechanicTrait> conditionalMechanicTraits
   ) {
      Objects.requireNonNull(mechanics, "mechanics");
      conditionalTraits = List.copyOf(conditionalTraits);
      conditionalMechanicTraits = List.copyOf(conditionalMechanicTraits);
      this.outgoingDamageMultiplier = outgoingDamageMultiplier;
      this.incomingDamageMultiplier = incomingDamageMultiplier;
      this.movementSpeedAdjustment = movementSpeedAdjustment;
      this.healingDoneMultiplier = healingDoneMultiplier;
      this.abilityCostReductionFraction = abilityCostReductionFraction;
      this.healingReceivedMultiplier = healingReceivedMultiplier;
      this.mechanics = mechanics;
      this.conditionalTraits = conditionalTraits;
      this.conditionalMechanicTraits = conditionalMechanicTraits;
   }

   public static PassiveAggregate resolve(ClassLineage lineage, ActiveLineageSnapshot snapshot, FixedPassiveRegistry registry) {
      PassiveProfile.Contribution total = new PassiveProfile.Contribution(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
      PassiveMechanics mechanics = PassiveMechanics.NEUTRAL;
      List<PassiveAggregate.ResolvedTrait> conditional = new ArrayList<>();
      List<PassiveAggregate.ResolvedMechanicTrait> conditionalMechanics = new ArrayList<>();

      for (String formId : lineage.formIds()) {
         Integer contributionLevel = snapshot.contributionLevels().get(formId);
         if (contributionLevel != null) {
            total = total.plus(registry.require(formId).atContributionLevel(contributionLevel));

            for (PassiveTrait trait : registry.traits(formId)) {
               PassiveProfile.Contribution contribution = trait.profile().atContributionLevel(contributionLevel);
               if (trait.unconditional()) {
                  total = total.plus(contribution);
               } else {
                  conditional.add(new PassiveAggregate.ResolvedTrait(trait.conditions(), contribution));
               }
            }

            for (PassiveMechanicTrait traitx : registry.mechanicTraits(formId)) {
               PassiveMechanics contribution = traitx.atContributionLevel(contributionLevel);
               if (traitx.unconditional()) {
                  mechanics = mechanics.combine(contribution);
               } else {
                  conditionalMechanics.add(new PassiveAggregate.ResolvedMechanicTrait(traitx.conditions(), contribution));
               }
            }
         }
      }

      return new PassiveAggregate(
         boundedMultiplier(1.0 + total.outgoingDamage()),
         boundedMultiplier(1.0 + total.incomingDamage()),
         Math.max(-0.5, Math.min(0.5, total.movementSpeed())),
         boundedMultiplier(1.0 + total.healingDone()),
         Math.max(0.0, Math.min(0.5, total.abilityCostReduction())),
         boundedMultiplier(1.0 + total.healingReceived()),
         mechanics,
         conditional,
         conditionalMechanics
      );
   }

   public PassiveAggregate.Evaluation evaluate(PassiveConditionContext context) {
      Objects.requireNonNull(context, "context");
      double outgoing = this.outgoingDamageMultiplier;
      double incoming = this.incomingDamageMultiplier;
      double movement = this.movementSpeedAdjustment;
      double healingDone = this.healingDoneMultiplier;
      double costReduction = this.abilityCostReductionFraction;
      double healingReceived = this.healingReceivedMultiplier;
      PassiveMechanics evaluatedMechanics = this.mechanics;

      for (PassiveAggregate.ResolvedTrait trait : this.conditionalTraits) {
         if (trait.matches(context)) {
            PassiveProfile.Contribution contribution = trait.contribution();
            outgoing += contribution.outgoingDamage();
            incoming += contribution.incomingDamage();
            movement += contribution.movementSpeed();
            healingDone += contribution.healingDone();
            costReduction += contribution.abilityCostReduction();
            healingReceived += contribution.healingReceived();
         }
      }

      for (PassiveAggregate.ResolvedMechanicTrait traitx : this.conditionalMechanicTraits) {
         if (traitx.matches(context)) {
            evaluatedMechanics = evaluatedMechanics.combine(traitx.mechanics());
         }
      }

      return new PassiveAggregate.Evaluation(
         boundedMultiplier(outgoing),
         boundedMultiplier(incoming),
         Math.max(-0.5, Math.min(0.5, movement)),
         boundedMultiplier(healingDone),
         Math.max(0.0, Math.min(0.5, costReduction)),
         boundedMultiplier(healingReceived),
         evaluatedMechanics
      );
   }

   public boolean requires(PassiveCondition condition) {
      Objects.requireNonNull(condition, "condition");
      return this.conditionalTraits.stream().anyMatch(trait -> trait.conditions().contains(condition))
         || this.conditionalMechanicTraits.stream().anyMatch(trait -> trait.conditions().contains(condition));
   }

   private static double boundedMultiplier(double value) {
      return Math.max(0.25, Math.min(3.0, value));
   }

   public record Evaluation(
      double outgoingDamageMultiplier,
      double incomingDamageMultiplier,
      double movementSpeedAdjustment,
      double healingDoneMultiplier,
      double abilityCostReductionFraction,
      double healingReceivedMultiplier,
      PassiveMechanics mechanics
   ) {
      public Evaluation(
         double outgoingDamageMultiplier,
         double incomingDamageMultiplier,
         double movementSpeedAdjustment,
         double healingDoneMultiplier,
         double abilityCostReductionFraction,
         double healingReceivedMultiplier,
         PassiveMechanics mechanics
      ) {
         Objects.requireNonNull(mechanics, "mechanics");
         this.outgoingDamageMultiplier = outgoingDamageMultiplier;
         this.incomingDamageMultiplier = incomingDamageMultiplier;
         this.movementSpeedAdjustment = movementSpeedAdjustment;
         this.healingDoneMultiplier = healingDoneMultiplier;
         this.abilityCostReductionFraction = abilityCostReductionFraction;
         this.healingReceivedMultiplier = healingReceivedMultiplier;
         this.mechanics = mechanics;
      }
   }

   public record ResolvedMechanicTrait(Set<PassiveCondition> conditions, PassiveMechanics mechanics) {
      public ResolvedMechanicTrait(Set<PassiveCondition> conditions, PassiveMechanics mechanics) {
         conditions = Set.copyOf(conditions);
         Objects.requireNonNull(mechanics, "mechanics");
         this.conditions = conditions;
         this.mechanics = mechanics;
      }

      boolean matches(PassiveConditionContext context) {
         return this.conditions.stream().allMatch(condition -> condition.matches(context));
      }
   }

   public record ResolvedTrait(Set<PassiveCondition> conditions, PassiveProfile.Contribution contribution) {
      public ResolvedTrait(Set<PassiveCondition> conditions, PassiveProfile.Contribution contribution) {
         conditions = Set.copyOf(conditions);
         Objects.requireNonNull(contribution, "contribution");
         this.conditions = conditions;
         this.contribution = contribution;
      }

      boolean matches(PassiveConditionContext context) {
         return this.conditions.stream().allMatch(condition -> condition.matches(context));
      }
   }
}
