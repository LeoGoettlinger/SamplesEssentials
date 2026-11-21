package de.mistrx.buildpaste.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Debug {
   public static boolean showLog = false;

   public static void Log(String message) {
      if (showLog) {
         for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
            player2.sendMessage(ChatColor.GRAY + message);
         }
      }
   }

   public static void Warning(String message) {
      if (showLog) {
         for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
            player2.sendMessage(ChatColor.RED + message);
         }
      }
   }
}
