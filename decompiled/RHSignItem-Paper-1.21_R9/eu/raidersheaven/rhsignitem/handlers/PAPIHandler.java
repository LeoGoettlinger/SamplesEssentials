package eu.raidersheaven.rhsignitem.handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PAPIHandler {
   private static final boolean isPAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

   public static boolean isPAPIAvailable() {
      return isPAPIEnabled;
   }

   public static String parse(Player player, String text) {
      return isPAPIAvailable() ? PlaceholderAPI.setPlaceholders(player, text) : text;
   }
}
