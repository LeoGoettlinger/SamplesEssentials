package com.magmaguy.elitemobs.magmacore.util;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TemporaryWorldManager {
   private static final long DELETE_AFTER_UNLOAD_DELAY_TICKS = 100L;

   public static World loadVoidTemporaryWorld(String worldName, Environment environment) {
      if (Bukkit.getWorld(worldName) != null) {
         return prepareTemporaryWorld(Bukkit.getWorld(worldName));
      } else if (!WorldFolderResolver.folderExists(worldName)) {
         Logger.warn(
            "World folder for "
               + worldName
               + " does not exist at "
               + WorldFolderResolver.legacyFolder(worldName)
               + " or "
               + WorldFolderResolver.modernFolder(worldName)
               + "!"
         );
         return null;
      } else {
         WorldFolderResolver.quarantineMigrationDebris(worldName);
         Logger.info("Loading world " + worldName + " !");

         try {
            Logger.info("Creating world " + worldName + " !");
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.environment(environment);
            worldCreator.generator(new TemporaryWorldManager.VoidGenerator());
            World world = Bukkit.createWorld(worldCreator);
            prepareTemporaryWorld(world);
            Logger.info("Successfully loaded world " + worldName + " !");
            return world;
         } catch (Exception var4) {
            Logger.warn("Failed to load world " + worldName + " !");
            var4.printStackTrace();
            return null;
         }
      }
   }

   public static World loadTemporaryWorldWithGenerator(String worldName, Environment environment, ChunkGenerator generator) {
      if (Bukkit.getWorld(worldName) != null) {
         return prepareTemporaryWorld(Bukkit.getWorld(worldName));
      } else if (!WorldFolderResolver.folderExists(worldName)) {
         Logger.warn(
            "World folder for "
               + worldName
               + " does not exist at "
               + WorldFolderResolver.legacyFolder(worldName)
               + " or "
               + WorldFolderResolver.modernFolder(worldName)
               + "!"
         );
         return null;
      } else {
         WorldFolderResolver.quarantineMigrationDebris(worldName);
         Logger.info("Loading world " + worldName + " with custom generator !");

         try {
            Logger.info("Creating world " + worldName + " with custom generator !");
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.environment(environment);
            worldCreator.generator(generator);
            World world = Bukkit.createWorld(worldCreator);
            prepareTemporaryWorld(world);
            Logger.info("Successfully loaded world " + worldName + " !");
            return world;
         } catch (Exception var5) {
            Logger.warn("Failed to load world " + worldName + " !");
            var5.printStackTrace();
            return null;
         }
      }
   }

   public static World createTemporaryWorldWithGenerator(String worldName, Environment environment, ChunkGenerator generator) {
      if (Bukkit.getWorld(worldName) != null) {
         return prepareTemporaryWorld(Bukkit.getWorld(worldName));
      } else {
         WorldFolderResolver.quarantineMigrationDebris(worldName);

         try {
            Logger.info("Creating world " + worldName + " with custom generator !");
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.environment(environment);
            worldCreator.generator(generator);
            World world = Bukkit.createWorld(worldCreator);
            prepareTemporaryWorld(world);
            Logger.info("Successfully loaded world " + worldName + " !");
            return world;
         } catch (Exception var5) {
            Logger.warn("Failed to load world " + worldName + " !");
            var5.printStackTrace();
            return null;
         }
      }
   }

   public static World createVoidTemporaryWorld(String worldName, Environment environment) {
      int identifier = 1;

      while (Bukkit.getWorld(worldName + "_" + identifier) != null || WorldFolderResolver.folderExists(worldName + "_" + identifier)) {
         identifier++;
      }

      worldName = worldName + "_" + identifier;
      WorldFolderResolver.quarantineMigrationDebris(worldName);

      try {
         Logger.info("Creating world " + worldName + " !");
         WorldCreator worldCreator = new WorldCreator(worldName);
         worldCreator.environment(environment);
         worldCreator.generator(new TemporaryWorldManager.VoidGenerator());
         World world = Bukkit.createWorld(worldCreator);
         prepareTemporaryWorld(world);
         Logger.info("Successfully loaded world " + worldName + " !");
         return world;
      } catch (Exception var5) {
         Logger.warn("Failed to load world " + worldName + " !");
         var5.printStackTrace();
         return null;
      }
   }

   public static void asyncPermanentlyDeleteWorld(World world) {
      tryAsyncPermanentlyDeleteWorld(world);
   }

   public static boolean tryAsyncPermanentlyDeleteWorld(World world) {
      world.setAutoSave(false);
      final String worldName = world.getName();
      if (!Bukkit.unloadWorld(world, false)) {
         Logger.warn("Failed to unload world " + worldName + " ! Skipping folder deletion because the world is still loaded.");
         return false;
      } else {
         final Plugin plugin = MagmaCore.getInstance().getRequestingPlugin();
         (new BukkitRunnable() {
            public void run() {
               if (Bukkit.getWorld(worldName) != null) {
                  Logger.warn("Skipping folder deletion for " + worldName + " because it was loaded again before cleanup ran.");
               } else {
                  (new BukkitRunnable() {
                     public void run() {
                        TemporaryWorldManager.deleteWorldDirectory(worldName);
                     }
                  }).runTaskAsynchronously(plugin);
               }
            }
         }).runTaskLater(plugin, 100L);
         return true;
      }
   }

   public static void permanentlyDeleteWorld(World world) {
      tryPermanentlyDeleteWorld(world);
   }

   public static boolean tryPermanentlyDeleteWorld(World world) {
      return MagmaCore.getInstance().getRequestingPlugin().isEnabled() ? tryAsyncPermanentlyDeleteWorld(world) : trySyncPermanentlyDeleteWorld(world);
   }

   public static void syncPermanentlyDeleteWorld(World world) {
      trySyncPermanentlyDeleteWorld(world);
   }

   public static boolean trySyncPermanentlyDeleteWorld(World world) {
      world.setAutoSave(false);
      String worldName = world.getName();
      if (!Bukkit.unloadWorld(world, false)) {
         Logger.warn("Failed to unload world " + worldName + " ! Skipping folder deletion because the world is still loaded.");
         return false;
      } else {
         deleteWorldDirectory(worldName);
         return true;
      }
   }

   private static void deleteWorldDirectory(String worldName) {
      WorldFolderResolver.deleteAllLayouts(worldName);
      if (WorldFolderResolver.folderExists(worldName)) {
         Logger.warn("Temporary world " + worldName + " was unloaded, but its folder could not be fully deleted.");
      } else {
         Logger.info("Successfully deleted temporary world " + worldName);
      }
   }

   private static World prepareTemporaryWorld(World world) {
      if (world == null) {
         return null;
      } else {
         world.setKeepSpawnInMemory(false);
         world.setDifficulty(Difficulty.HARD);
         world.setAutoSave(false);
         return world;
      }
   }

   private static class VoidGenerator extends ChunkGenerator {
      public void generateSurface(WorldInfo info, Random random, int x, int z, ChunkData data) {
      }

      public boolean shouldGenerateNoise() {
         return false;
      }

      public boolean shouldGenerateBedrock() {
         return false;
      }

      public boolean shouldGenerateCaves() {
         return false;
      }
   }
}
