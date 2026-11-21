package org.popcraft.chunky.platform;

import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.entity.TeleportFlag.EntityState;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public final class Paper {
   private static final boolean CONFIG_EXISTS = classExists("com.destroystokyo.paper.PaperConfig")
      || classExists("io.papermc.paper.configuration.Configuration");

   private Paper() {
   }

   public static boolean isPaper() {
      return CONFIG_EXISTS;
   }

   public static CompletableFuture<Chunk> getChunkAtAsync(org.bukkit.World world, int x, int z) {
      return world.getChunkAtAsync(x, z, true);
   }

   public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
      return entity.teleportAsync(location);
   }

   public static CompletableFuture<Boolean> teleportAsyncWithPassengers(Entity entity, Location location) {
      return entity.teleportAsync(location, TeleportCause.PLUGIN, new TeleportFlag[]{EntityState.RETAIN_PASSENGERS, EntityState.RETAIN_VEHICLE});
   }

   private static boolean classExists(String clazz) {
      try {
         Class.forName(clazz);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }
}
