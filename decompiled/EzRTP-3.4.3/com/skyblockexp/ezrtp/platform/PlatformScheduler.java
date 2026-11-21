package com.skyblockexp.ezrtp.platform;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface PlatformScheduler {
   void executeAsync(Runnable var1);

   void executeRegion(World var1, int var2, int var3, Runnable var4);

   PlatformTask scheduleRepeating(Runnable var1, long var2, long var4);

   void executeGlobal(Runnable var1);

   PlatformTask executeGlobalDelayed(Runnable var1, long var2);

   void executeRegionDelayed(World var1, int var2, int var3, Runnable var4, long var5);

   default CompletableFuture<Boolean> teleportAsync(Player player, Location destination) {
      return CompletableFuture.completedFuture(player.teleport(destination));
   }
}
