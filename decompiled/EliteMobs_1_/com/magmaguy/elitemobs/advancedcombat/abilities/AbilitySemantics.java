package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface AbilitySemantics {
   boolean isEnemy(Player var1, LivingEntity var2);

   default boolean canTargetEnemy(Player caster, LivingEntity candidate, FixedAbilitySpec spec) {
      return this.isEnemy(caster, candidate);
   }

   default boolean canApplyEnemyEffect(Player caster, LivingEntity candidate, FixedAbilitySpec spec, AbilityEffect effect) {
      return this.canTargetEnemy(caster, candidate, spec);
   }

   default Collection<Player> alliesOf(Player caster) {
      return List.of(caster);
   }

   default double healingMultiplier(Player caster) {
      return 1.0;
   }

   default AbilityMechanicModifiers mechanicModifiers(Player caster) {
      return AbilityMechanicModifiers.NEUTRAL;
   }

   default void signal(Player player, AbilityRuntimeSignal signal, int durationTicks) {
   }

   default void requestThreat(AbilitySemantics.ThreatRequest request) {
   }

   default void recordContribution(Player caster, String abilityId, AbilityContribution contribution) {
   }

   default void observe(AbilityRuntimeObservation observation) {
   }

   default void grantResource(Player caster, double amount) {
   }

   default void clearSource(Player caster) {
   }

   default List<String> activeFormIds(Player player) {
      return List.of();
   }

   public record ThreatRequest(Player caster, List<LivingEntity> enemies, double amountPerEnemy, int forcedTargetTicks, String abilityId) {
      public ThreatRequest(Player caster, List<LivingEntity> enemies, double amountPerEnemy, int forcedTargetTicks, String abilityId) {
         caster = Objects.requireNonNull(caster, "caster");
         enemies = List.copyOf(enemies);
         if (!Double.isFinite(amountPerEnemy) || amountPerEnemy < 0.0) {
            throw new IllegalArgumentException("amountPerEnemy must be finite and non-negative");
         } else if (forcedTargetTicks < 0) {
            throw new IllegalArgumentException("forcedTargetTicks must not be negative");
         } else {
            Objects.requireNonNull(abilityId, "abilityId");
            this.caster = caster;
            this.enemies = enemies;
            this.amountPerEnemy = amountPerEnemy;
            this.forcedTargetTicks = forcedTargetTicks;
            this.abilityId = abilityId;
         }
      }
   }
}
