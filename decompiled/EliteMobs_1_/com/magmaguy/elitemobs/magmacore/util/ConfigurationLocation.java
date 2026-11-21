package com.magmaguy.elitemobs.magmacore.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ConfigurationLocation {
   private static final Set<String> notLoadedWorldNames = new HashSet<>();

   private ConfigurationLocation() {
   }

   public static String deserialize(String worldName, double x, double y, double z, float pitch, float yaw) {
      return worldName + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
   }

   public static String deserialize(Location location) {
      return deserialize(
         Objects.requireNonNull(location.getWorld()).getName(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw()
      );
   }

   public static Location serialize(String locationString) {
      return serialize(locationString, false);
   }

   public static Location serialize(String locationString, boolean silent) {
      if (locationString == null) {
         return null;
      } else {
         World world = null;
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         float yaw = 0.0F;
         float pitch = 0.0F;

         try {
            String locationOnlyString = locationString.split(":")[0];
            String[] slicedString = locationOnlyString.split(",");
            if (slicedString.length != 6 && slicedString.length != 4) {
               if (slicedString.length != 5) {
                  throw new Exception();
               }

               x = Double.parseDouble(slicedString[0]);
               y = Double.parseDouble(slicedString[1]);
               z = Double.parseDouble(slicedString[2]);
               yaw = Float.parseFloat(slicedString[3]);
               pitch = Float.parseFloat(slicedString[4]);
            } else {
               world = Bukkit.getWorld(slicedString[0]);
               if (world == null && !slicedString[0].equalsIgnoreCase("same_as_boss") && !notLoadedWorldNames.contains(slicedString[0]) && !silent) {
                  notLoadedWorldNames.add(slicedString[0]);
               }

               x = Double.parseDouble(slicedString[1]);
               y = Double.parseDouble(slicedString[2]);
               z = Double.parseDouble(slicedString[3]);
               if (slicedString.length > 4) {
                  yaw = Float.parseFloat(slicedString[4]);
                  pitch = Float.parseFloat(slicedString[5]);
               } else {
                  yaw = 0.0F;
                  pitch = 0.0F;
               }
            }
         } catch (Exception var13) {
            if (locationString.equals("null")) {
               return null;
            }

            Logger.warn("Attempted to deserialize an invalid location!");
            Logger.warn("Expected location format: worldname,x,y,z,yaw,pitch");
            Logger.warn("Actual location format: " + locationString);
            return null;
         }

         return new Location(world, x, y, z, yaw, pitch);
      }
   }

   public static String worldName(String locationString) {
      String locationOnlyString = locationString.split(":")[0];
      String[] slicedString = locationOnlyString.split(",");
      return slicedString[0];
   }

   public static Location serializeWithInstance(World instancedWorld, String locationString) {
      if (locationString == null) {
         return null;
      } else {
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         float yaw = 0.0F;
         float pitch = 0.0F;

         try {
            String locationOnlyString = locationString.split(":")[0];
            String[] slicedString = locationOnlyString.split(",");
            if (slicedString.length != 6 && slicedString.length != 4) {
               if (slicedString.length != 5) {
                  throw new Exception();
               }

               x = Double.parseDouble(slicedString[0]);
               y = Double.parseDouble(slicedString[1]);
               z = Double.parseDouble(slicedString[2]);
               yaw = Float.parseFloat(slicedString[3]);
               pitch = Float.parseFloat(slicedString[4]);
            } else {
               x = Double.parseDouble(slicedString[1]);
               y = Double.parseDouble(slicedString[2]);
               z = Double.parseDouble(slicedString[3]);
               if (slicedString.length > 4) {
                  yaw = Float.parseFloat(slicedString[4]);
                  pitch = Float.parseFloat(slicedString[5]);
               } else {
                  yaw = 0.0F;
                  pitch = 0.0F;
               }
            }
         } catch (Exception var12) {
            if (locationString.equals("null")) {
               return null;
            }

            Logger.warn("Attempted to deserialize an invalid location!");
            Logger.warn("Expected location format: worldname,x,y,z,yaw,pitch");
            Logger.warn("Actual location format: " + locationString);
            return null;
         }

         return new Location(instancedWorld, x, y, z, yaw, pitch);
      }
   }

   public static void shutdown() {
      notLoadedWorldNames.clear();
   }
}
