package com.magmaguy.elitemobs.utils;

import org.bukkit.Location;
import org.bukkit.World;

public final class ConfigurationLocation {
   private ConfigurationLocation() {
   }

   public static void shutdown() {
      com.magmaguy.elitemobs.magmacore.util.ConfigurationLocation.shutdown();
   }

   public static String deserialize(String worldName, double x, double y, double z, float pitch, float yaw) {
      return com.magmaguy.elitemobs.magmacore.util.ConfigurationLocation.deserialize(worldName, x, y, z, pitch, yaw);
   }

   public static String deserialize(Location location) {
      return com.magmaguy.elitemobs.magmacore.util.ConfigurationLocation.deserialize(location);
   }

   public static Location serialize(String locationString) {
      return com.magmaguy.elitemobs.magmacore.util.ConfigurationLocation.serialize(locationString);
   }

   public static Location serialize(String locationString, boolean silent) {
      return com.magmaguy.elitemobs.magmacore.util.ConfigurationLocation.serialize(locationString, silent);
   }

   public static String worldName(String locationString) {
      return com.magmaguy.elitemobs.magmacore.util.ConfigurationLocation.worldName(locationString);
   }

   public static Location serializeWithInstance(World instancedWorld, String locationString) {
      return com.magmaguy.elitemobs.magmacore.util.ConfigurationLocation.serializeWithInstance(instancedWorld, locationString);
   }
}
