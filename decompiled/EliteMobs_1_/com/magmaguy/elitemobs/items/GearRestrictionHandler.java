package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GearRestrictionHandler {
   private static final Set<UUID> recentWarnings = new HashSet<>();

   public static void shutdown() {
      recentWarnings.clear();
   }

   public static boolean canEquip(Player player, ItemStack itemStack) {
      GearRestrictionHandler.RestrictionContext restrictionContext = getRestrictionContext(player, itemStack);
      if (!restrictionContext.enabled()) {
         return true;
      } else if (!restrictionContext.playerDataLoaded()) {
         return true;
      } else if (itemStack == null || itemStack.getType().isAir()) {
         return true;
      } else if (!EliteItemManager.isEliteMobsItem(itemStack)) {
         return true;
      } else {
         int itemLevel = EliteItemManager.getRoundedItemLevel(itemStack);
         if (itemLevel <= 0) {
            return true;
         } else if (itemLevel <= 20) {
            return true;
         } else {
            SkillType skillType = restrictionContext.skillType();
            return skillType == null ? true : restrictionContext.playerSkillLevel() >= itemLevel;
         }
      }
   }

   public static GearRestrictionHandler.RestrictionContext getRestrictionContext(Player player, ItemStack itemStack) {
      boolean enabled = AdventurersGuildConfig.isSkillBasedGearRestriction();
      if (!enabled) {
         return new GearRestrictionHandler.RestrictionContext(false, false, null, 0);
      } else {
         boolean playerDataLoaded = PlayerData.isDataLoaded(player.getUniqueId());
         SkillType skillType = itemStack != null && !itemStack.getType().isAir() ? WeaponIdentityResolver.progressionSkillIncludingArmor(itemStack) : null;
         int playerSkillLevel = playerDataLoaded && skillType != null
            ? SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), skillType))
            : 0;
         return new GearRestrictionHandler.RestrictionContext(true, playerDataLoaded, skillType, playerSkillLevel);
      }
   }

   public static int getEffectiveItemLevel(Player player, ItemStack itemStack) {
      if (!AdventurersGuildConfig.isSkillBasedGearRestriction()) {
         return EliteItemManager.getRoundedItemLevel(itemStack);
      } else {
         return !canEquip(player, itemStack) ? 0 : EliteItemManager.getRoundedItemLevel(itemStack);
      }
   }

   public static void sendRestrictionMessage(Player player, ItemStack itemStack) {
      if (AdventurersGuildConfig.isSkillBasedGearRestriction()) {
         if (PlayerData.isDataLoaded(player.getUniqueId())) {
            SkillType skillType = WeaponIdentityResolver.progressionSkillIncludingArmor(itemStack);
            if (skillType != null) {
               if (!recentWarnings.contains(player.getUniqueId())) {
                  recentWarnings.add(player.getUniqueId());
                  Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> recentWarnings.remove(player.getUniqueId()), 40L);
                  int itemLevel = EliteItemManager.getRoundedItemLevel(itemStack);
                  long skillXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
                  int playerSkillLevel = SkillXPCalculator.levelFromTotalXP(skillXP);
                  String message = AdventurersGuildConfig.getGearRestrictionMessage()
                     .replace("$itemLevel", String.valueOf(itemLevel))
                     .replace("$skillLevel", String.valueOf(playerSkillLevel))
                     .replace("$skillType", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType));
                  player.sendMessage(ChatColorConverter.convert(message));
               }
            }
         }
      }
   }

   public record RestrictionContext(boolean enabled, boolean playerDataLoaded, SkillType skillType, int playerSkillLevel) {
   }
}
