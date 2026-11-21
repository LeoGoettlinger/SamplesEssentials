package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatEnemyAuthorization;
import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

final class EliteAbilityTargetAuthorization {
   private static final Set<AbilityEffect> DISPLACEMENT_EFFECTS = EnumSet.of(
      AbilityEffect.KNOCKBACK, AbilityEffect.PULL, AbilityEffect.LAUNCH, AbilityEffect.FEAR
   );

   private EliteAbilityTargetAuthorization() {
   }

   static boolean canTarget(Player caster, LivingEntity candidate) {
      return caster != null && ClassAbilityEligibility.isEligible(caster) ? AdvancedCombatEnemyAuthorization.canTarget(caster, candidate) : false;
   }

   static boolean canTarget(Player caster, LivingEntity candidate, FixedAbilitySpec spec) {
      if (!canTarget(caster, candidate)) {
         return false;
      } else {
         EliteEntity elite = EntityTracker.getEliteMobEntity(candidate);
         if (elite == null) {
            return false;
         } else {
            AttributeInstance maximumHealth = candidate.getAttribute(Attribute.MAX_HEALTH);
            double maximum = maximumHealth == null ? Math.max(1.0, candidate.getHealth()) : Math.max(1.0, maximumHealth.getValue());
            double width = Math.max(candidate.getBoundingBox().getWidthX(), candidate.getBoundingBox().getWidthZ());
            AbilityEnemyPolicy.EnemyFacts facts = new AbilityEnemyPolicy.EnemyFacts(
               candidate.getHealth() / maximum, !elite.isNaturalEntity() || elite.getHealthMultiplier() > 1.5, candidate.getHeight() >= 2.5 || width >= 1.4
            );
            return AbilityEnemyPolicy.permits(spec.executionTraits().mechanics(), facts);
         }
      }
   }

   static boolean canApply(Player caster, LivingEntity candidate, FixedAbilitySpec spec, AbilityEffect effect) {
      if (!canTarget(caster, candidate, spec)) {
         return false;
      } else {
         EliteEntity elite = EntityTracker.getEliteMobEntity(candidate);
         if (elite == null) {
            return false;
         } else if (DISPLACEMENT_EFFECTS.contains(effect) && displacementImmune(elite, candidate)) {
            return false;
         } else {
            ClassAbilityAffectEliteEvent event = new ClassAbilityAffectEliteEvent(caster, elite, spec.id(), effect);
            Bukkit.getPluginManager().callEvent(event);
            return !event.isCancelled();
         }
      }
   }

   private static boolean displacementImmune(EliteEntity elite, LivingEntity candidate) {
      if (elite instanceof CustomBossEntity customBoss && customBoss.getCustomBossesConfigFields().isFrozen()) {
         return true;
      } else {
         AttributeInstance resistance = candidate.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
         return resistance != null && resistance.getValue() >= 1.0;
      }
   }
}
