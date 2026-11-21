package com.magmaguy.elitemobs.combatsystem;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.entity.Player;

public class DamageBreakdown {
   private static final Map<UUID, DamageBreakdown> activeBreakdowns = new ConcurrentHashMap<>();
   private double baseDamage = 0.0;
   private double attackSpeedFactor = 1.0;
   private double skillAdjustment = 1.0;
   private double weaponAdjustment = 1.0;
   private double cooldownOrVelocity = 1.0;
   private double sweepMultiplier = 1.0;
   private double potionMultiplier = 1.0;
   private double thornsDamage = 0.0;
   private double equipmentEnchantmentMultiplier = 1.0;
   private double enchantmentMultiplier = 1.0;
   private double arrowDamageMultiplier = 1.0;
   private int playerSkillLevel = 1;
   private int itemLevel = 0;
   private int eliteLevel = 1;
   private double damageModifier = 1.0;
   private double combatMultiplier = 1.0;
   private double skillBonusMultiplier = 1.0;
   private double critMultiplier = 1.0;
   private double formulaDamage = 0.0;
   private double finalDamage = 0.0;
   private boolean isCriticalHit = false;
   private boolean isRangedAttack = false;
   private boolean isSweepAttack = false;
   private boolean isThornsAttack = false;
   private String weaponType = "UNKNOWN";

   public static DamageBreakdown startTracking(Player player) {
      DamageBreakdown breakdown = new DamageBreakdown();
      activeBreakdowns.put(player.getUniqueId(), breakdown);
      return breakdown;
   }

   public static DamageBreakdown getActiveBreakdown(Player player) {
      return activeBreakdowns.get(player.getUniqueId());
   }

   public static DamageBreakdown getActiveBreakdown(UUID uuid) {
      return activeBreakdowns.get(uuid);
   }

   public static DamageBreakdown stopTracking(Player player) {
      return activeBreakdowns.remove(player.getUniqueId());
   }

   public static void clearAll() {
      activeBreakdowns.clear();
   }

   public static boolean isTracking(Player player) {
      return activeBreakdowns.containsKey(player.getUniqueId());
   }

   public static boolean isTracking(UUID uuid) {
      return activeBreakdowns.containsKey(uuid);
   }

   public static double calculateExpectedDamage(int weaponLevel, int skillLevel, int targetLevel, double attackSpeed) {
      double base = LevelScaling.calculateBaseDamageToElite(targetLevel);
      double speedFactor = WeaponOffenseCalculator.getLegacyAttackSpeedFactor(attackSpeed);
      double skillAdj = LevelScaling.calculateOffensiveSkillAdjustment(skillLevel, targetLevel);
      double weaponAdj = WeaponOffenseCalculator.getWeaponAdjustment(weaponLevel, targetLevel);
      return Math.max(base * speedFactor * skillAdj * weaponAdj, 1.0);
   }

   public static double calculateExpectedDamage(int weaponLevel, int skillLevel, int targetLevel, String weaponType, double attackSpeed) {
      double base = LevelScaling.calculateBaseDamageToElite(targetLevel);
      double speedFactor = WeaponOffenseCalculator.getAttackSpeedFactor(weaponType, attackSpeed);
      double skillAdj = LevelScaling.calculateOffensiveSkillAdjustment(skillLevel, targetLevel);
      double weaponAdj = WeaponOffenseCalculator.getWeaponAdjustment(weaponLevel, targetLevel);
      return Math.max(base * speedFactor * skillAdj * weaponAdj, 1.0);
   }

   public static void shutdown() {
      activeBreakdowns.clear();
   }

   public void compute() {
      if (this.isThornsAttack) {
         this.formulaDamage = this.thornsDamage;
      } else {
         this.formulaDamage = this.baseDamage
            * this.attackSpeedFactor
            * this.skillAdjustment
            * this.weaponAdjustment
            * this.cooldownOrVelocity
            * this.sweepMultiplier
            * this.potionMultiplier
            * this.equipmentEnchantmentMultiplier
            * this.enchantmentMultiplier
            * this.arrowDamageMultiplier;
      }

      this.finalDamage = Math.max(this.formulaDamage, 1.0) * this.damageModifier * this.combatMultiplier * this.critMultiplier;
   }

   public String toFormattedString() {
      this.compute();
      StringBuilder sb = new StringBuilder();
      sb.append("§6=== DAMAGE BREAKDOWN ===\n");
      sb.append(
         String.format(
            "§7Weapon: §f%s %s\n",
            this.weaponType,
            this.isThornsAttack ? "§d(Thorns)" : (this.isRangedAttack ? "§e(Ranged)" : (this.isSweepAttack ? "§b(Sweep)" : "§a(Melee)"))
         )
      );
      sb.append("\n§6--- LEVELS ---\n");
      sb.append(String.format("§7Player Skill Level: §f%d\n", this.playerSkillLevel));
      sb.append(String.format("§7Weapon Level: §f%d\n", this.itemLevel));
      sb.append(String.format("§7Elite Level: §f%d\n", this.eliteLevel));
      sb.append(String.format("§7Skill Difference: §f%d\n", this.playerSkillLevel - this.eliteLevel));
      if (this.isThornsAttack) {
         sb.append("\n§6--- THORNS ---\n");
         sb.append(String.format("§7Thorns Damage: §f%.1f\n", this.thornsDamage));
      } else {
         sb.append("\n§6--- FORMULA COMPONENTS ---\n");
         sb.append(String.format("§7Base Damage (HP/%s): §f%.1f\n", String.format("%.0f", 3.0), this.baseDamage));
         if (!this.isRangedAttack) {
            sb.append(String.format("§7Attack Speed Factor: §f%.2fx\n", this.attackSpeedFactor));
         }

         sb.append(
            String.format(
               "§7Skill Adjustment: §f%.3fx %s\n",
               this.skillAdjustment,
               this.skillAdjustment > 1.0 ? "§a(bonus)" : (this.skillAdjustment < 1.0 ? "§c(penalty)" : "§7(matched)")
            )
         );
         sb.append(
            String.format(
               "§7Weapon Adjustment: §f%.2fx %s\n",
               this.weaponAdjustment,
               this.weaponAdjustment > 1.0 ? "§a(over-level)" : (this.weaponAdjustment < 1.0 ? "§c(under-level)" : "§7(matched)")
            )
         );
         sb.append(String.format("§7%s: §f%.2f\n", this.isRangedAttack ? "Arrow Velocity" : "Attack Charge", this.cooldownOrVelocity));
         if (this.isSweepAttack) {
            sb.append(String.format("§7Sweep Reduction: §c%.0f%%\n", this.sweepMultiplier * 100.0));
         }

         if (this.potionMultiplier != 1.0) {
            sb.append(String.format("§7Potion Multiplier: §e%.2fx\n", this.potionMultiplier));
         }

         if (this.equipmentEnchantmentMultiplier != 1.0) {
            sb.append(String.format("§7Equipment Enchantment Multiplier: §e%.2fx\n", this.equipmentEnchantmentMultiplier));
         }

         if (this.enchantmentMultiplier != 1.0) {
            sb.append(String.format("§7Enchantment Multiplier: §e%.2fx\n", this.enchantmentMultiplier));
         }

         if (this.arrowDamageMultiplier != 1.0) {
            sb.append(String.format("§7Arrow Damage Multiplier: §c%.0f%%\n", this.arrowDamageMultiplier * 100.0));
         }
      }

      sb.append("\n§6--- MULTIPLIERS ---\n");
      sb.append(String.format("§7Damage Modifier: §f%.2fx\n", this.damageModifier));
      sb.append(String.format("§7Combat Multiplier: §f%.2fx\n", this.combatMultiplier));
      if (this.skillBonusMultiplier != 1.0) {
         sb.append(String.format("§7Skill Bonus Multiplier: §e%.2fx\n", this.skillBonusMultiplier));
      }

      if (this.isCriticalHit) {
         sb.append(String.format("§7Critical Hit: §e%.1fx\n", this.critMultiplier));
      }

      sb.append("\n§6--- RESULT ---\n");
      sb.append(String.format("§7Formula Damage: §f%.1f\n", this.formulaDamage));
      sb.append(String.format("§7After Multipliers: §f%.1f\n", this.finalDamage));
      sb.append(String.format("§6§lFINAL DAMAGE: §f§l%.1f\n", this.finalDamage));
      return sb.toString();
   }

   public String toCompactString() {
      this.compute();
      return String.format(
         "§7[SkLv%d WpLv%d vs Lv%d] §fBase:%.1f × Skill:%.2f × Wpn:%.2f = §e%.1f%s",
         this.playerSkillLevel,
         this.itemLevel,
         this.eliteLevel,
         this.baseDamage,
         this.skillAdjustment,
         this.weaponAdjustment,
         this.finalDamage,
         this.isCriticalHit ? " §cCRIT" : ""
      );
   }

   public String toComparisonString(double actualDamage) {
      this.compute();
      double expected = this.finalDamage;
      double difference = actualDamage - expected;
      double percentDiff = expected > 0.0 ? difference / expected * 100.0 : 0.0;
      StringBuilder sb = new StringBuilder();
      sb.append("§6=== EXPECTED VS ACTUAL ===\n");
      sb.append(String.format("§7Expected Damage: §f%.1f\n", expected));
      sb.append(String.format("§7Actual Damage: §f%.1f\n", actualDamage));
      sb.append(String.format("§7Difference: §%s%.1f (%.1f%%)\n", Math.abs(percentDiff) < 5.0 ? "a" : "c", difference, percentDiff));
      if (Math.abs(percentDiff) > 10.0) {
         sb.append("\n§c⚠ Large discrepancy detected! Check:\n");
         sb.append("§7- Skill bonuses active?\n");
         sb.append("§7- Enchantment effects?\n");
         sb.append("§7- Boss damage modifiers?\n");
         sb.append("§7- Plugin conflicts?\n");
      }

      return sb.toString();
   }

   @Generated
   public double getBaseDamage() {
      return this.baseDamage;
   }

   @Generated
   public void setBaseDamage(double baseDamage) {
      this.baseDamage = baseDamage;
   }

   @Generated
   public double getAttackSpeedFactor() {
      return this.attackSpeedFactor;
   }

   @Generated
   public void setAttackSpeedFactor(double attackSpeedFactor) {
      this.attackSpeedFactor = attackSpeedFactor;
   }

   @Generated
   public double getSkillAdjustment() {
      return this.skillAdjustment;
   }

   @Generated
   public void setSkillAdjustment(double skillAdjustment) {
      this.skillAdjustment = skillAdjustment;
   }

   @Generated
   public double getWeaponAdjustment() {
      return this.weaponAdjustment;
   }

   @Generated
   public void setWeaponAdjustment(double weaponAdjustment) {
      this.weaponAdjustment = weaponAdjustment;
   }

   @Generated
   public double getCooldownOrVelocity() {
      return this.cooldownOrVelocity;
   }

   @Generated
   public void setCooldownOrVelocity(double cooldownOrVelocity) {
      this.cooldownOrVelocity = cooldownOrVelocity;
   }

   @Generated
   public double getSweepMultiplier() {
      return this.sweepMultiplier;
   }

   @Generated
   public void setSweepMultiplier(double sweepMultiplier) {
      this.sweepMultiplier = sweepMultiplier;
   }

   @Generated
   public double getPotionMultiplier() {
      return this.potionMultiplier;
   }

   @Generated
   public void setPotionMultiplier(double potionMultiplier) {
      this.potionMultiplier = potionMultiplier;
   }

   @Generated
   public double getThornsDamage() {
      return this.thornsDamage;
   }

   @Generated
   public void setThornsDamage(double thornsDamage) {
      this.thornsDamage = thornsDamage;
   }

   @Generated
   public double getEquipmentEnchantmentMultiplier() {
      return this.equipmentEnchantmentMultiplier;
   }

   @Generated
   public void setEquipmentEnchantmentMultiplier(double equipmentEnchantmentMultiplier) {
      this.equipmentEnchantmentMultiplier = equipmentEnchantmentMultiplier;
   }

   @Generated
   public double getEnchantmentMultiplier() {
      return this.enchantmentMultiplier;
   }

   @Generated
   public void setEnchantmentMultiplier(double enchantmentMultiplier) {
      this.enchantmentMultiplier = enchantmentMultiplier;
   }

   @Generated
   public double getArrowDamageMultiplier() {
      return this.arrowDamageMultiplier;
   }

   @Generated
   public void setArrowDamageMultiplier(double arrowDamageMultiplier) {
      this.arrowDamageMultiplier = arrowDamageMultiplier;
   }

   @Generated
   public int getPlayerSkillLevel() {
      return this.playerSkillLevel;
   }

   @Generated
   public void setPlayerSkillLevel(int playerSkillLevel) {
      this.playerSkillLevel = playerSkillLevel;
   }

   @Generated
   public int getItemLevel() {
      return this.itemLevel;
   }

   @Generated
   public void setItemLevel(int itemLevel) {
      this.itemLevel = itemLevel;
   }

   @Generated
   public int getEliteLevel() {
      return this.eliteLevel;
   }

   @Generated
   public void setEliteLevel(int eliteLevel) {
      this.eliteLevel = eliteLevel;
   }

   @Generated
   public double getDamageModifier() {
      return this.damageModifier;
   }

   @Generated
   public void setDamageModifier(double damageModifier) {
      this.damageModifier = damageModifier;
   }

   @Generated
   public double getCombatMultiplier() {
      return this.combatMultiplier;
   }

   @Generated
   public void setCombatMultiplier(double combatMultiplier) {
      this.combatMultiplier = combatMultiplier;
   }

   @Generated
   public double getSkillBonusMultiplier() {
      return this.skillBonusMultiplier;
   }

   @Generated
   public void setSkillBonusMultiplier(double skillBonusMultiplier) {
      this.skillBonusMultiplier = skillBonusMultiplier;
   }

   @Generated
   public double getCritMultiplier() {
      return this.critMultiplier;
   }

   @Generated
   public void setCritMultiplier(double critMultiplier) {
      this.critMultiplier = critMultiplier;
   }

   @Generated
   public double getFormulaDamage() {
      return this.formulaDamage;
   }

   @Generated
   public double getFinalDamage() {
      return this.finalDamage;
   }

   @Generated
   public boolean isCriticalHit() {
      return this.isCriticalHit;
   }

   @Generated
   public void setCriticalHit(boolean isCriticalHit) {
      this.isCriticalHit = isCriticalHit;
   }

   @Generated
   public boolean isRangedAttack() {
      return this.isRangedAttack;
   }

   @Generated
   public void setRangedAttack(boolean isRangedAttack) {
      this.isRangedAttack = isRangedAttack;
   }

   @Generated
   public boolean isSweepAttack() {
      return this.isSweepAttack;
   }

   @Generated
   public void setSweepAttack(boolean isSweepAttack) {
      this.isSweepAttack = isSweepAttack;
   }

   @Generated
   public boolean isThornsAttack() {
      return this.isThornsAttack;
   }

   @Generated
   public void setThornsAttack(boolean isThornsAttack) {
      this.isThornsAttack = isThornsAttack;
   }

   @Generated
   public String getWeaponType() {
      return this.weaponType;
   }

   @Generated
   public void setWeaponType(String weaponType) {
      this.weaponType = weaponType;
   }
}
