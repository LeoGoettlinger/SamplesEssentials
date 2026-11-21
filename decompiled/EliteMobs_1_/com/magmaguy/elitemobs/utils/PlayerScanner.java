package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerScanner {
   private static final int range = Math.max(Bukkit.getServer().getViewDistance() * 16, 80);

   public static List<Player> getNearbyPlayers(Location location) {
      List<Player> nearbyPlayers = new ArrayList<>();

      for (Player player : Bukkit.getOnlinePlayers()) {
         if (player.getWorld().equals(location.getWorld())
            && player.getLocation().distanceSquared(location) <= range * range
            && ElitePlayerInventory.playerInventories.get(player.getUniqueId()) != null) {
            nearbyPlayers.add(player);
         }
      }

      return nearbyPlayers;
   }
}
