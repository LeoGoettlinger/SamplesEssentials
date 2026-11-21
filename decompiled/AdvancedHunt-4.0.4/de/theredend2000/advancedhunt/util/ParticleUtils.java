package de.theredend2000.advancedhunt.util;

import de.theredend2000.advancedhunt.platform.PlatformAccess;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ParticleUtils {
   public static void spawnParticle(Location location, String particleName, int count, double offsetX, double offsetY, double offsetZ, double speed) {
      PlatformAccess.get().spawnParticle(location, particleName, count, offsetX, offsetY, offsetZ, speed);
   }

   public static void spawnParticleForPlayer(
      Player player, Location location, String particleName, int count, double offsetX, double offsetY, double offsetZ, double speed
   ) {
      PlatformAccess.get().spawnParticleForPlayer(player, location, particleName, count, offsetX, offsetY, offsetZ, speed);
   }

   public static boolean supportsPerPlayerParticles() {
      if (!isLegacy()) {
         return true;
      } else {
         try {
            return Bukkit.getPluginManager().isPluginEnabled("packetevents") || Bukkit.getPluginManager().isPluginEnabled("PacketEvents");
         } catch (Throwable var1) {
            return false;
         }
      }
   }

   public static boolean isLegacy() {
      String base = Bukkit.getBukkitVersion().split("-")[0];
      return base.startsWith("1.8");
   }

   public static void resetWarnings() {
   }
}
