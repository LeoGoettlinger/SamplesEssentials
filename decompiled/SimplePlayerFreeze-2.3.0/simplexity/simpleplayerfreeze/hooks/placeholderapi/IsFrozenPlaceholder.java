package simplexity.simpleplayerfreeze.hooks.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import simplexity.simpleplayerfreeze.Util;
import simplexity.simpleplayerfreeze.configs.ConfigHandler;

public class IsFrozenPlaceholder extends PlaceholderExpansion {
   @NotNull
   public String getIdentifier() {
      return "spf";
   }

   @NotNull
   public String getAuthor() {
      return "Simplexity";
   }

   public boolean persist() {
      return true;
   }

   @NotNull
   public String getVersion() {
      return "1.0.0";
   }

   public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
      if (params.equalsIgnoreCase("frozenprefix")) {
         Player player = offlinePlayer.getPlayer();
         if (player == null) {
            return "Offline";
         } else if (Util.isFrozen(player)) {
            return ConfigHandler.getInstance().getPlaceholderAPIFormat();
         } else {
            return !Util.isFrozen(player) ? "" : "";
         }
      } else {
         if (params.equalsIgnoreCase("frozenbool")) {
            Player player = offlinePlayer.getPlayer();
            if (player == null) {
               return "Offline";
            }

            if (Util.isFrozen(player)) {
               return "true";
            }

            if (!Util.isFrozen(player)) {
               return "false";
            }
         }

         return "";
      }
   }
}
