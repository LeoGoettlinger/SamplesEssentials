package com.earth2me.essentials.paperlib.features.bedspawnlocation;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BedSpawnLocationSync implements BedSpawnLocation {
   @Override
   public CompletableFuture<Location> getBedSpawnLocationAsync(Player player, boolean isUrgent) {
      return CompletableFuture.completedFuture(player.getBedSpawnLocation());
   }
}
