package com.magmaguy.elitemobs.advancedcombat.presentation;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityLevelScaling;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityMechanic;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityTuning;
import com.magmaguy.elitemobs.advancedcombat.abilities.ActiveAbilityLevelScaling;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassLineage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ClassAbilityActivationFeedback {
   private static final double DEFAULT_STRENGTH_MULTIPLIER = 1.15;
   private static final double DEFAULT_PROTECTION_MULTIPLIER = 0.8;

   private ClassAbilityActivationFeedback() {
   }

   public static String message(ClassLineage lineage, AbilitySlot slot, FixedAbilitySpec spec, int effectiveLevel) {
      return message(lineage, slot, spec, effectiveLevel, spec.resourceCost());
   }

   public static String message(ClassLineage lineage, AbilitySlot slot, FixedAbilitySpec spec, int effectiveLevel, double spentResource) {
      if (Double.isFinite(spentResource) && !(spentResource < 0.0)) {
         Objects.requireNonNull(lineage, "lineage");
         Objects.requireNonNull(slot, "slot");
         Objects.requireNonNull(spec, "spec");
         AbilityDefinition ability = ability(lineage, slot);
         if (!ability.id().equals(spec.id())) {
            throw new IllegalArgumentException("Ability definition and mechanic do not match");
         } else {
            StringBuilder message = new StringBuilder(ClassPresentationTheme.resourceGradient(lineage.resourceType(), ability.displayName() + "!"));
            if (slot != AbilitySlot.MOBILITY) {
               String summary = summary(spec, effectiveLevel);
               if (!summary.isBlank()) {
                  message.append(" &f").append(summary);
               }
            }

            message.append(" &c-").append(number(spentResource)).append(" &f").append(lineage.resourceType().displayName());
            return message.toString();
         }
      } else {
         throw new IllegalArgumentException("Spent resource must be finite and non-negative");
      }
   }

   static String summary(FixedAbilitySpec spec, int effectiveLevel) {
      AbilityTuning tuning = spec.tuning();
      double levelScale = AbilityLevelScaling.multiplier(effectiveLevel);
      int levelDuration = ActiveAbilityLevelScaling.durationTicks(tuning.durationTicks(), effectiveLevel);
      List<String> outcomes = new ArrayList<>(3);
      boolean missingHealthScaling = spec.executionTraits().mechanics().contains(AbilityMechanic.MISSING_HEALTH_SCALING);
      if (spec.family() == AbilityFamily.SUMMON) {
         outcomes.add("summon" + duration(tuning.durationTicks(), " for "));
      } else if (spec.effects().contains(AbilityEffect.DAMAGE) && tuning.damageMultiplier() > 0.0) {
         int deliveries = Math.max(1, tuning.repetitions()) * Math.max(1, tuning.projectileCount());
         String damage;
         if (spec.executionTraits().mechanics().contains(AbilityMechanic.EXPANDING_PULSES)) {
            damage = number(tuning.damageMultiplier() * levelScale) + "x expanding hit";
         } else if (spec.executionTraits().mechanics().contains(AbilityMechanic.PROJECTILE_BOMBARDMENT)) {
            damage = deliveries + " arrows at " + number(tuning.damageMultiplier() * levelScale) + "x damage";
         } else {
            damage = deliveries > 1
               ? deliveries + " hits at " + number(tuning.damageMultiplier() * levelScale) + "x damage"
               : number(tuning.damageMultiplier() * levelScale) + "x damage";
         }

         if (tuning.repetitions() > 1) {
            damage = damage + duration(tuning.durationTicks(), " over ");
         }

         outcomes.add(damage);
      }

      if (spec.effects().contains(AbilityEffect.HEAL) && tuning.healingFraction() > 0.0) {
         String heal = "+" + percent(tuning.healingFraction() * levelScale) + " HP";
         if (tuning.repetitions() > 1) {
            heal = heal + duration(tuning.durationTicks(), " over ");
         }

         outcomes.add(heal);
      }

      if (spec.executionTraits().mechanics().contains(AbilityMechanic.HEAL_ECHO)) {
         double echoFraction = ActiveAbilityLevelScaling.amount(0.55, effectiveLevel);
         outcomes.add("next heal echoes " + percent(echoFraction) + duration(levelDuration, " for "));
      }

      if (spec.effects().contains(AbilityEffect.SPELL_STRENGTH)) {
         double multiplier = tuning.modifierMultiplier() > 1.0 ? tuning.modifierMultiplier() : 1.15;
         multiplier = ActiveAbilityLevelScaling.modifier(multiplier, effectiveLevel);
         outcomes.add("+" + percent(multiplier - 1.0) + " spell damage" + duration(levelDuration, " for "));
      }

      if (spec.effects().contains(AbilityEffect.SELF_VULNERABLE)) {
         double multiplier = tuning.modifierMultiplier() > 1.0 ? tuning.modifierMultiplier() : 1.15;
         multiplier = ActiveAbilityLevelScaling.modifier(multiplier, effectiveLevel);
         outcomes.add("+" + percent(multiplier - 1.0) + " damage taken" + duration(levelDuration, " for "));
      }

      if (spec.effects().contains(AbilityEffect.SHIELD) && tuning.shieldFraction() > 0.0) {
         outcomes.add("+" + percent(tuning.shieldFraction() * levelScale) + " shield" + duration(levelDuration, " for "));
      }

      if (spec.effects().contains(AbilityEffect.STRENGTH)) {
         double multiplier = tuning.modifierMultiplier() > 1.0 ? tuning.modifierMultiplier() : 1.15;
         multiplier = ActiveAbilityLevelScaling.modifier(multiplier, effectiveLevel);
         if (missingHealthScaling) {
            outcomes.add("up to +" + percent(multiplier - 1.0) + " damage");
         } else {
            outcomes.add("+" + percent(multiplier - 1.0) + " damage" + duration(levelDuration, " for "));
         }
      }

      if (spec.effects().contains(AbilityEffect.PARTY_DAMAGE_MARK) && tuning.modifierMultiplier() > 1.0) {
         double multiplier = ActiveAbilityLevelScaling.modifier(tuning.modifierMultiplier(), effectiveLevel);
         outcomes.add("+" + percent(multiplier - 1.0) + " marked damage" + duration(levelDuration, " for "));
      }

      if (spec.effects().contains(AbilityEffect.SELF_PROTECT) || spec.effects().contains(AbilityEffect.ALLY_PROTECT)) {
         double multiplier = tuning.modifierMultiplier() > 0.0 && tuning.modifierMultiplier() < 1.0 ? tuning.modifierMultiplier() : 0.8;
         multiplier = ActiveAbilityLevelScaling.modifier(multiplier, effectiveLevel);
         outcomes.add(percent(1.0 - multiplier) + " damage reduction" + duration(levelDuration, " for "));
      }

      if (spec.effects().contains(AbilityEffect.LIFESTEAL)) {
         boolean timedWindow = spec.executionTraits().mechanics().contains(AbilityMechanic.LIFESTEAL_WINDOW);
         outcomes.add("lifesteal" + (timedWindow ? duration(levelDuration, " for ") : ""));
      }

      if (spec.effects().contains(AbilityEffect.CLEANSE)) {
         outcomes.add("cleanse");
      }

      double displacement = ActiveAbilityLevelScaling.displacement(tuning.displacement(), effectiveLevel);
      if (displacement > 0.0) {
         if (spec.effects().contains(AbilityEffect.KNOCKBACK)) {
            outcomes.add("knockback " + number(displacement) + " blocks");
         } else if (spec.effects().contains(AbilityEffect.PULL)) {
            outcomes.add("pull " + number(displacement) + " blocks");
         } else if (spec.effects().contains(AbilityEffect.LAUNCH)) {
            outcomes.add("launch " + number(displacement) + " blocks");
         }
      }

      if (spec.effects().contains(AbilityEffect.SPEED)) {
         if (missingHealthScaling) {
            outcomes.add("+" + percent(0.4 * levelScale) + " speed as HP falls" + duration(levelDuration, " for "));
         } else {
            outcomes.add("Speed II" + duration(levelDuration, " for "));
         }
      }

      if (spec.effects().contains(AbilityEffect.BURN)) {
         int burnDuration = Math.max(20, Math.min(200, levelDuration));
         outcomes.add("burn" + duration(burnDuration, " for "));
      }

      List<String> controls = new ArrayList<>(6);
      if (spec.effects().contains(AbilityEffect.ROOT)) {
         controls.add("root");
      }

      if (spec.effects().contains(AbilityEffect.FEAR)) {
         controls.add("fear");
      }

      if (spec.effects().contains(AbilityEffect.INTERRUPT)) {
         controls.add("disrupt");
      }

      if (spec.effects().contains(AbilityEffect.TAUNT)) {
         controls.add("taunt");
      }

      if (spec.effects().contains(AbilityEffect.SLOW)) {
         controls.add("Slowness III");
      }

      if (spec.effects().contains(AbilityEffect.GLOW)) {
         controls.add("reveal");
      }

      if (spec.effects().contains(AbilityEffect.WEAKEN)) {
         double multiplier = ActiveAbilityLevelScaling.modifier(tuning.weakenMultiplier(), effectiveLevel);
         String weakening = controls.isEmpty() ? "-" + percent(1.0 - multiplier) + " enemy damage" : "enemy damage -" + percent(1.0 - multiplier);
         controls.add(weakening);
      }

      if (!controls.isEmpty()) {
         outcomes.add(String.join(" + ", controls) + duration(levelDuration, " for "));
      }

      return outcomes.stream().limit(2L).reduce((left, right) -> left + ", " + right).orElse("activate");
   }

   private static AbilityDefinition ability(ClassLineage lineage, AbilitySlot slot) {
      return switch (slot) {
         case MOBILITY -> lineage.mobility();
         case SIGNATURE -> lineage.signature();
         case UTILITY -> lineage.utility();
      };
   }

   private static String percent(double fraction) {
      return Math.round(fraction * 100.0 + 1.0E-9) + "%";
   }

   private static String duration(int ticks, String prefix) {
      return ticks <= 0 ? "" : prefix + number(ticks / 20.0) + "s";
   }

   private static String number(double value) {
      return Math.abs(value - Math.rint(value)) < 1.0E-9
         ? Long.toString(Math.round(value))
         : String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
   }
}
