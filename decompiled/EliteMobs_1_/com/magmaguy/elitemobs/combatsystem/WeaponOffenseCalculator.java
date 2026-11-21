package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

public class WeaponOffenseCalculator {
   public static final double WEAPON_BASE_BONUS = 0.5;
   public static final double WEAPON_BONUS_PER_LEVEL = 0.025;
   public static final double WEAPON_MAX_BONUS = 0.75;
   public static final double SWEEP_DAMAGE_FRACTION = 0.25;
   public static final double THORNS_PERCENT_PER_LEVEL = 0.02;
   public static final double BURST_DAMPING_EXPONENT = 0.5;

   private WeaponOffenseCalculator() {
   }

   public static double getAttackSpeedFactor(ItemStack item, double attackSpeed) {
      String typeName = item != null && !item.getType().isAir() ? item.getType().name() : null;
      return getAttackSpeedFactor(typeName, attackSpeed);
   }

   public static double getAttackSpeedFactor(String typeName, double attackSpeed) {
      if (Double.isFinite(attackSpeed) && !(attackSpeed <= 0.0)) {
         double legacyFactor = getLegacyAttackSpeedFactor(attackSpeed);
         if (typeName == null || typeName.isEmpty()) {
            return legacyFactor;
         } else {
            return usesBurstDampedAttackSpeedFactor(typeName) ? Math.pow(legacyFactor, 0.5) : legacyFactor;
         }
      } else {
         return 1.0;
      }
   }

   public static double getLegacyAttackSpeedFactor(double attackSpeed) {
      return Double.isFinite(attackSpeed) && !(attackSpeed <= 0.0) ? 1.6 / attackSpeed : 1.0;
   }

   private static boolean usesBurstDampedAttackSpeedFactor(String typeName) {
      return typeName.endsWith("_AXE") || typeName.equals("MACE") || typeName.equals("TRIDENT");
   }

   public static double getEffectiveWeaponLevel(ItemStack item) {
      return item != null && !item.getType().isAir() ? EliteItemManager.getItemLevel(item) : 0.0;
   }

   public static double getWeaponAdjustment(double weaponLevel, int mobLevel) {
      if (mobLevel <= 0) {
         mobLevel = 1;
      }

      double bonus;
      if (weaponLevel <= mobLevel) {
         bonus = 0.5 * (weaponLevel / mobLevel);
      } else {
         bonus = 0.5 + (weaponLevel - mobLevel) * 0.025;
         bonus = Math.min(bonus, 0.75);
      }

      return 0.5 + bonus;
   }

   public static double getSweepMultiplier(EntityDamageByEntityEvent event) {
      return event != null && event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK ? 0.25 : 1.0;
   }

   public static double normalizeArrowVelocity(Projectile projectile) {
      double magnitude = projectile.getVelocity().length();
      return Math.min(magnitude / 3.0, 1.0);
   }

   public static double normalizeArrowVelocity(double launchVelocityMagnitude) {
      return Math.min(launchVelocityMagnitude / 3.0, 1.0);
   }
}
