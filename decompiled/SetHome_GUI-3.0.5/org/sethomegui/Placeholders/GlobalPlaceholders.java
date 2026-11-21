package org.sethomegui.Placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.sethomegui.SetHomeGUI;

public class GlobalPlaceholders extends PlaceholderExpansion {
   private final SetHomeGUI plugin;

   public GlobalPlaceholders(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getAuthor() {
      return "Beeted_";
   }

   @NotNull
   public String getIdentifier() {
      return "sethomegui";
   }

   @NotNull
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public boolean persist() {
      return true;
   }

   public String onRequest(OfflinePlayer player, @NotNull String params) {
      if (player == null) {
         return "0";
      } else if (params.equalsIgnoreCase("homes")) {
         int currentHomes = this.plugin.getHomeManager().getHomeCount(player.getUniqueId());
         return String.valueOf(currentHomes);
      } else if (params.equalsIgnoreCase("maxhomes")) {
         if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer != null) {
               int maxHomes = this.plugin.getHomeManager().getPlayerMaxHomes(onlinePlayer);
               return maxHomes == -1 ? "∞" : String.valueOf(maxHomes);
            }
         }

         return "0";
      } else {
         return null;
      }
   }
}
