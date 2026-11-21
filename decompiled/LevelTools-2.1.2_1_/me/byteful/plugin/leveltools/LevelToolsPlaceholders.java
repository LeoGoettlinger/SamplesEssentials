package me.byteful.plugin.leveltools;

import java.util.Locale;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LevelToolsPlaceholders extends PlaceholderExpansion {
   @NotNull
   public String getIdentifier() {
      return "leveltools";
   }

   @NotNull
   public String getAuthor() {
      return "byteful";
   }

   @NotNull
   public String getVersion() {
      return LevelToolsPlugin.getInstance().getDescription().getVersion();
   }

   public boolean canRegister() {
      return true;
   }

   public boolean persist() {
      return true;
   }

   @Nullable
   public String onPlaceholderRequest(Player player, @NotNull String params) {
      if (player == null) {
         return null;
      } else {
         ItemStack hand = !LevelToolsUtil.supportsDualWielding() ? player.getItemInHand() : player.getInventory().getItemInMainHand();
         if (!LevelToolsUtil.isSupportedTool(hand.getType())) {
            return "N/A";
         } else {
            LevelToolsItem item = LevelToolsUtil.createLevelToolsItem(hand);
            String var5 = params.toLowerCase(Locale.ROOT).replace(" ", "_");
            switch (var5) {
               case "level":
                  return "" + item.getLevel();
               case "xp":
                  return "" + item.getXp();
               case "max_xp":
                  return "" + item.getMaxXp();
               case "progress_bar":
                  DisplayProfile displayProfile = this.getDisplayProfile(hand);
                  return LevelToolsUtil.createProgressBar(item.getXp(), item.getMaxXp(), displayProfile);
               case "item_profile": {
                  ItemProfile profile = LevelToolsUtil.getItemProfile(hand.getType());
                  return profile != null ? profile.getId() : "N/A";
               }
               case "max_level": {
                  ItemProfile profile = LevelToolsUtil.getItemProfile(hand.getType());
                  return profile != null ? String.valueOf(profile.getMaxLevel()) : "N/A";
               }
               default:
                  return "N/A";
            }
         }
      }
   }

   @Nullable
   private DisplayProfile getDisplayProfile(ItemStack item) {
      ItemProfile itemProfile = LevelToolsUtil.getItemProfile(item.getType());
      if (itemProfile == null) {
         return null;
      } else {
         ProfileManager profileManager = LevelToolsPlugin.getInstance().getProfileManager();
         return profileManager == null ? null : profileManager.getDisplayProfileFor(itemProfile);
      }
   }
}
