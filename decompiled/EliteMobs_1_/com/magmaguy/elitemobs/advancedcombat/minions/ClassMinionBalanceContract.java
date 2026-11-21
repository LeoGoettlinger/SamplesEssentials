package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityLevelScaling;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import java.util.Objects;

public record ClassMinionBalanceContract(
   int summonCount,
   int ownerCap,
   int uptimeTicks,
   double maxHealth,
   double damageMultiplierPerHit,
   double scaledDamageMultiplierPerHit,
   int attackPeriodTicks,
   double resourceCost
) {
   public static final int DEFAULT_OWNER_CAP = 3;

   public ClassMinionBalanceContract(
      int summonCount,
      int ownerCap,
      int uptimeTicks,
      double maxHealth,
      double damageMultiplierPerHit,
      double scaledDamageMultiplierPerHit,
      int attackPeriodTicks,
      double resourceCost
   ) {
      if (summonCount < 1 || ownerCap < 1 || summonCount > ownerCap) {
         throw new IllegalArgumentException("Summon counts must fit within the owner cap");
      } else if (uptimeTicks >= 1
         && !(maxHealth <= 0.0)
         && attackPeriodTicks >= 1
         && !(damageMultiplierPerHit < 0.0)
         && !(scaledDamageMultiplierPerHit < 0.0)
         && !(resourceCost <= 0.0)) {
         this.summonCount = summonCount;
         this.ownerCap = ownerCap;
         this.uptimeTicks = uptimeTicks;
         this.maxHealth = maxHealth;
         this.damageMultiplierPerHit = damageMultiplierPerHit;
         this.scaledDamageMultiplierPerHit = scaledDamageMultiplierPerHit;
         this.attackPeriodTicks = attackPeriodTicks;
         this.resourceCost = resourceCost;
      } else {
         throw new IllegalArgumentException("Summon balance values must be positive");
      }
   }

   public static ClassMinionBalanceContract from(FixedAbilitySpec spec, ClassMinionTheme theme, int effectiveLevel) {
      Objects.requireNonNull(spec, "spec");
      Objects.requireNonNull(theme, "theme");
      if (spec.family() != AbilityFamily.SUMMON) {
         throw new IllegalArgumentException("Only summon abilities have minion contracts: " + spec.id());
      } else {
         int boundedLevel = Math.max(1, effectiveLevel);
         double levelScale = AbilityLevelScaling.multiplier(boundedLevel);
         int uptime = Math.max(20, spec.tuning().durationTicks());
         int repetitions = Math.max(1, spec.tuning().repetitions());
         int count = Math.min(3, Math.max(1, spec.tuning().projectileCount()));
         double damage = spec.tuning().damageMultiplier();
         return new ClassMinionBalanceContract(
            count, 3, uptime, theme.baseHealth() * levelScale, damage, damage * levelScale, Math.max(1, uptime / repetitions), spec.resourceCost()
         );
      }
   }
}
