package com.magmaguy.elitemobs.dungeons.utility;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

public class DungeonUtils {
   static boolean shouldRefuseUnload(boolean hasPlayers, boolean fallbackAvailable, boolean teleportSucceeded) {
      return hasPlayers && (!fallbackAvailable || !teleportSucceeded);
   }

   public static DungeonUtils.Pair getLowestAndHighestLevels(List<CustomBossEntity> customBossEntities) {
      int lowestLevel = 0;
      int highestLevel = 0;

      for (CustomBossEntity customBossEntity : customBossEntities) {
         try {
            int level = customBossEntity.getLevel();
            lowestLevel = lowestLevel == 0 ? level : Math.min(lowestLevel, level);
            highestLevel = highestLevel == 0 ? level : Math.max(highestLevel, level);
         } catch (Exception var6) {
         }
      }

      return new DungeonUtils.Pair(lowestLevel, highestLevel);
   }

   public static World loadWorld(WorldPackage worldPackage) {
      String worldName = worldPackage.getContentPackagesConfigFields().getWorldName();
      Environment environment = worldPackage.getContentPackagesConfigFields().getEnvironment();
      World world = loadWorld(worldName, environment, worldPackage.getContentPackagesConfigFields());
      if (worldPackage.getContentPackagesConfigFields().getWormholeWorldName() != null) {
         loadWorld(worldPackage.getContentPackagesConfigFields().getWormholeWorldName(), environment, worldPackage.getContentPackagesConfigFields());
      }

      if (world != null) {
         worldPackage.setInstalled(true);
      }

      return world;
   }

   public static World loadWorld(String worldName, Environment environment, ContentPackagesConfigFields contentPackagesConfigFields) {
      World world = EliteMobsWorld.loadWorld(worldName, environment, contentPackagesConfigFields);
      if (world != null) {
         PersistentObjectHandler.loadWorld(world);
      }

      return world;
   }

   public static boolean unloadWorld(WorldPackage worldPackage) {
      World packageWorld = worldPackage.getWorld();
      World wormholeWorld = worldPackage instanceof WorldDungeonPackage worldDungeonPackage ? worldDungeonPackage.getWormholeWorld() : null;
      World fallbackWorld = Bukkit.getWorlds().stream().filter(world -> world != packageWorld && world != wormholeWorld).findFirst().orElse(null);

      for (Player player : Bukkit.getOnlinePlayers()) {
         if ((player.getWorld() == packageWorld || player.getWorld() == wormholeWorld)
            && (fallbackWorld == null || !player.teleport(fallbackWorld.getSpawnLocation()))) {
            Logger.warn("Could not move " + player.getName() + " out of dungeon world " + player.getWorld().getName() + "; refusing to unload it.");
            return false;
         }
      }

      return !unloadRegisteredWorld(packageWorld, worldPackage.getContentPackagesConfigFields())
         ? false
         : wormholeWorld == packageWorld || unloadRegisteredWorld(wormholeWorld, worldPackage.getContentPackagesConfigFields());
   }

   private static boolean unloadRegisteredWorld(World world, ContentPackagesConfigFields contentPackagesConfigFields) {
      if (world == null) {
         return true;
      } else {
         EliteMobsWorld.destroy(world.getUID());
         if (Bukkit.unloadWorld(world, false)) {
            return true;
         } else {
            EliteMobsWorld.create(world.getUID(), contentPackagesConfigFields);
            Logger.warn("Bukkit refused to unload dungeon world " + world.getName() + ".");
            return false;
         }
      }
   }

   public static class Pair {
      Integer lowestValue;
      Integer highestValue;

      public Pair(Integer lowestValue, Integer highestValue) {
         this.lowestValue = lowestValue;
         this.highestValue = highestValue;
      }

      @Generated
      public Integer getLowestValue() {
         return this.lowestValue;
      }

      @Generated
      public Integer getHighestValue() {
         return this.highestValue;
      }
   }
}
