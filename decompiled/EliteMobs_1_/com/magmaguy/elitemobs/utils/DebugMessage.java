package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;

public class DebugMessage {
   private static final Set<UUID> debugPlayers = new HashSet<>();
   private static final Pattern CHAT_COLOR_PATTERN = Pattern.compile("[§&][0-9a-fk-orA-FK-OR]");

   private DebugMessage() {
   }

   public static boolean toggleDebug(Player player) {
      if (debugPlayers.contains(player.getUniqueId())) {
         debugPlayers.remove(player.getUniqueId());
         return false;
      } else {
         debugPlayers.add(player.getUniqueId());
         return true;
      }
   }

   public static boolean isDebugEnabled(Player player) {
      return player != null && debugPlayers.contains(player.getUniqueId());
   }

   public static boolean isAnyDebugEnabled() {
      return !debugPlayers.isEmpty();
   }

   public static void log(String message) {
      if (isAnyDebugEnabled()) {
         Logger.warn(message);
      }
   }

   public static void log(Player player, String message) {
      if (isDebugEnabled(player)) {
         Logger.warn(message);
      }
   }

   public static void send(Player player, String message) {
      if (isDebugEnabled(player)) {
         Logger.sendMessage(player, "&8[Debug] &7" + message);
         Logger.warn("[EM-Debug:" + player.getName() + "] " + stripColors(message));
      }
   }

   public static String stripColors(String message) {
      return message == null ? "" : CHAT_COLOR_PATTERN.matcher(message).replaceAll("");
   }

   public static void damageSummary(Player player, String line) {
      if (isDebugEnabled(player)) {
         Logger.warn("[EM-Damage:" + player.getName() + "] " + line);
      }
   }

   public static void shutdown() {
      debugPlayers.clear();
   }
}
