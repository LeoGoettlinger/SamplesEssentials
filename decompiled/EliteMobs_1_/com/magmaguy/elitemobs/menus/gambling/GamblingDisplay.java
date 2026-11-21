package com.magmaguy.elitemobs.menus.gambling;

import com.magmaguy.elitemobs.config.GamblingConfig;
import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class GamblingDisplay {
   public static Player validateClick(InventoryClickEvent event, Set<Inventory> menus) {
      if (!menus.contains(event.getInventory())) {
         return null;
      } else if (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
         event.setCancelled(true);
         return event.getWhoClicked() instanceof Player player ? player : null;
      } else {
         event.setCancelled(true);
         return null;
      }
   }

   public static void playWinSound(Player player) {
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
   }

   public static void playBigWinSound(Player player) {
      player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
   }

   public static void playLoseSound(Player player) {
      player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
   }

   public static void sendWinMessage(Player player, double amount, String gameName) {
      player.sendMessage(GamblingConfig.getWinMessage().replace("%amount%", formatCurrency(amount)).replace("%game%", gameName));
   }

   public static void sendLoseMessage(Player player, int betAmount, String gameName) {
      player.sendMessage(GamblingConfig.getLoseMessage().replace("%amount%", String.valueOf(betAmount)).replace("%game%", gameName));
   }

   public static String formatCurrency(double amount) {
      return String.format("%.2f", amount);
   }
}
