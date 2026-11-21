package com.ranull.graves.util;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public final class LocationUtil {
   private LocationUtil() {
   }

   @Nullable
   public static Location roundLocation(@Nullable Location location) {
      return new Location(location.getWorld(), Math.round((float)location.getBlockX()), Math.round(location.getY()), Math.round((float)location.getBlockZ()));
   }

   public static String locationToString(Location location) {
      return location.getWorld() != null
         ? location.getWorld().getName() + "|" + location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ()
         : null;
   }

   public static String chunkToString(Location location) {
      return location.getWorld() != null ? location.getWorld().getName() + "|" + (location.getBlockX() >> 4) + "|" + (location.getBlockZ() >> 4) : null;
   }

   public static Location chunkStringToLocation(String string) {
      String[] strings = string.split("\\|");
      return new Location(Bukkit.getServer().getWorld(strings[0]), Integer.parseInt(strings[1]) << 4, 0.0, Integer.parseInt(strings[2]) << 4);
   }

   public static Location stringToLocation(String string) {
      String[] strings = string.split("\\|");
      return new Location(Bukkit.getServer().getWorld(strings[0]), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]), Integer.parseInt(strings[3]));
   }

   public static Location getClosestLocation(Location locationBase, List<Location> locationList) {
      Location locationClosest = null;

      for (Location location : locationList) {
         if (locationClosest == null) {
            locationClosest = location;
         } else if (location.distanceSquared(locationBase) < locationClosest.distanceSquared(locationBase)) {
            locationClosest = location;
         }
      }

      return locationClosest;
   }

   public static Location deserializeLocation(String serializedLocation) {
      if (serializedLocation != null && !serializedLocation.isEmpty()) {
         String[] parts = serializedLocation.split(",");
         if (parts.length != 6) {
            return null;
         } else {
            try {
               World world = Bukkit.getWorld(parts[0]);
               if (world == null) {
                  return null;
               } else {
                  double x = Double.parseDouble(parts[1]);
                  double y = Double.parseDouble(parts[2]);
                  double z = Double.parseDouble(parts[3]);
                  float pitch = Float.parseFloat(parts[4]);
                  float yaw = Float.parseFloat(parts[5]);
                  return new Location(world, x, y, z, yaw, pitch);
               }
            } catch (NumberFormatException var11) {
               return null;
            }
         }
      } else {
         return null;
      }
   }
}
