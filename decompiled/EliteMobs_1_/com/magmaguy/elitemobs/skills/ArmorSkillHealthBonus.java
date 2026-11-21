package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

public class ArmorSkillHealthBonus {
   private static final String MODIFIER_KEY_STRING = "armor_skill_health";
   private static final double VANILLA_MAX_HEALTH = 20.0;

   private ArmorSkillHealthBonus() {
   }

   public static void applyHealthBonus(Player player) {
      if (player != null && player.isOnline()) {
         if (!isHealthBonusWorld(player)) {
            removeHealthBonus(player);
            resetPlayerHealthDisplay(player);
            clampHealthToCurrentMaxHealth(player);
         } else if (!SkillsConfig.isArmorSkillHealthBonusEnabled()) {
            resetPlayerHealthDisplay(player);
            if (SkillsConfig.isForceDefaultHealthWhenArmorSkillHealthBonusDisabled()) {
               removeHealthBonus(player);
               forceVanillaMaxHealth(player);
            }
         } else if (PlayerData.isDataLoaded(player.getUniqueId())) {
            long armorXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
            int armorLevel = SkillXPCalculator.levelFromTotalXP(armorXP);
            double bonusHealth = Math.max(0, armorLevel - 1) * 2.0;
            replaceHealthBonus(player, bonusHealth);
            updatePlayerHealthDisplay(player);
         }
      }
   }

   private static void updatePlayerHealthDisplay(Player player) {
      if (SkillsConfig.isScalePlayerHealthDisplayToVanilla()) {
         HealthDisplayCoordinator.acquire(player, HealthDisplayCoordinator.Owner.ARMOR_SKILL);
      } else {
         resetPlayerHealthDisplay(player);
      }
   }

   public static void resetPlayerHealthDisplay(Player player) {
      if (player != null) {
         HealthDisplayCoordinator.release(player, HealthDisplayCoordinator.Owner.ARMOR_SKILL);
      }
   }

   private static void clampHealthToCurrentMaxHealth(Player player) {
      AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
      if (maxHealth != null) {
         double currentMaxHealth = maxHealth.getValue();
         if (player.getHealth() > currentMaxHealth) {
            player.setHealth(currentMaxHealth);
         }
      }
   }

   private static void forceVanillaMaxHealth(Player player) {
      AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
      if (maxHealth != null) {
         maxHealth.setBaseValue(20.0);
         if (player.getHealth() > 20.0) {
            player.setHealth(20.0);
         }
      }
   }

   public static void removeHealthBonus(Player player) {
      replaceHealthBonus(player, 0.0);
   }

   private static void replaceHealthBonus(Player player, double bonusHealth) {
      if (player != null) {
         AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
         if (maxHealth != null) {
            NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "armor_skill_health");
            AttributeModifier existing = maxHealth.getModifiers().stream().filter(modifier -> modifier.getKey().equals(key)).findFirst().orElse(null);
            boolean shouldExist = bonusHealth > 0.0;
            if (shouldExist || existing != null) {
               if (!shouldExist || existing == null || !(Math.abs(existing.getAmount() - bonusHealth) < 1.0E-9)) {
                  HealthPercentagePreserver.during(player, maxHealth, () -> {
                     if (existing != null) {
                        maxHealth.removeModifier(existing);
                     }

                     if (shouldExist) {
                        maxHealth.addModifier(new AttributeModifier(key, bonusHealth, Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
                     }
                  });
               }
            }
         }
      }
   }

   public static void updateHealthBonus(Player player) {
      applyHealthBonus(player);
   }

   public static double getBonusHealth(Player player) {
      if (player == null) {
         return 0.0;
      } else if (!SkillsConfig.isArmorSkillHealthBonusEnabled()) {
         return 0.0;
      } else if (!isHealthBonusWorld(player)) {
         return 0.0;
      } else {
         long armorXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
         int armorLevel = SkillXPCalculator.levelFromTotalXP(armorXP);
         return armorLevel <= 1 ? 0.0 : (armorLevel - 1) * 2.0;
      }
   }

   public static double getConfiguredMaxHealthForArmorLevel(int armorLevel) {
      return !SkillsConfig.isArmorSkillHealthBonusEnabled() ? 20.0 : 20.0 + Math.max(0, armorLevel - 1) * 2.0;
   }

   public static double getConfiguredMaxHealthForPlayer(Player player, int armorLevel) {
      return !isHealthBonusWorld(player) ? 20.0 : getConfiguredMaxHealthForArmorLevel(armorLevel);
   }

   private static boolean isHealthBonusWorld(Player player) {
      return !SkillsConfig.isWorldExcludedFromSkills(player)
         && (!AdvancedCombatSystemConfig.isEnabled() || DungeonCombatRuntime.isInManagedCombatWorld(player));
   }

   public static int getBonusHearts(Player player) {
      return (int)(getBonusHealth(player) / 2.0);
   }
}
