package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.Location;

public class SpawnRadiusDifficultyIncrementer {
   private static final double DEFAULT_BLOCK_DISTANCE_BETWEEN_LEVEL_INCREMENTS = 100.0;

   public static int distanceFromSpawnLevel(Location location) {
      double blockDistanceBetweenLevelIncrements = MobCombatSettingsConfig.getBlockDistanceBetweenLevelIncrements();
      Location spawnLocation = location.getWorld().getSpawnLocation();
      double distanceFromSpawn = spawnLocation.distance(location);
      return calculateDistanceBasedLevel(distanceFromSpawn, blockDistanceBetweenLevelIncrements);
   }

   @Deprecated
   public static int distanceFromSpawnLevelIncrease(Location location) {
      return distanceFromSpawnLevel(location);
   }

   public static int calculateDistanceBasedLevel(double distanceFromSpawn, double blockDistanceBetweenLevelIncrements) {
      double distanceBetweenIncrements = sanitizeBlockDistanceBetweenLevelIncrements(blockDistanceBetweenLevelIncrements);
      return Double.isFinite(distanceFromSpawn) && !(distanceFromSpawn <= 0.0) ? Math.max(1, (int)(distanceFromSpawn / distanceBetweenIncrements)) : 1;
   }

   private static double sanitizeBlockDistanceBetweenLevelIncrements(double blockDistanceBetweenLevelIncrements) {
      return Double.isFinite(blockDistanceBetweenLevelIncrements) && !(blockDistanceBetweenLevelIncrements <= 0.0)
         ? blockDistanceBetweenLevelIncrements
         : 100.0;
   }
}
