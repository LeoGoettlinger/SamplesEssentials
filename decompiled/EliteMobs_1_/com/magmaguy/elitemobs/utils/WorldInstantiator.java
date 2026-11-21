package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.WorldBlueprint;
import com.magmaguy.elitemobs.magmacore.util.WorldFolderResolver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;

public class WorldInstantiator {
   public static File cloneWorld(String worldName, String targetWorldName, String dungeonConfigurationFolderName) {
      return cloneWorld(worldName, targetWorldName, dungeonConfigurationFolderName, Environment.NORMAL);
   }

   private static Path blueprintFolder(String worldName, String configurationFolder) {
      Path root = MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("world_blueprints").toAbsolutePath().normalize();
      Path result = root.resolve(configurationFolder).resolve(worldName).normalize();
      if (result.startsWith(root) && !result.equals(root)) {
         return result;
      } else {
         throw new IllegalArgumentException("Blueprint path escapes world_blueprints");
      }
   }

   public static boolean validateBlueprint(String worldName, String configurationFolder, Environment environment) {
      try {
         WorldBlueprint.inspect(blueprintFolder(worldName, configurationFolder), environment).requireCompatibleServer(WorldFolderResolver.usesModernStorage());
         return true;
      } catch (RuntimeException | IOException var4) {
         Logger.warn("Cannot prepare blueprint " + worldName + ": " + var4.getMessage());
         return false;
      }
   }

   public static File cloneWorld(String worldName, String targetWorldName, String configurationFolder, Environment environment) {
      try {
         if (!targetWorldName.matches("[a-zA-Z0-9_-]+")) {
            throw new IOException("Invalid instance world name");
         } else {
            WorldBlueprint blueprint = WorldBlueprint.inspect(blueprintFolder(worldName, configurationFolder), environment);
            boolean modern = WorldFolderResolver.usesModernStorage();
            if (Bukkit.getWorld(targetWorldName) == null && !WorldFolderResolver.folderExists(targetWorldName)) {
               Path destination = blueprint.usesModernDestination(modern)
                  ? WorldFolderResolver.modernFolder(targetWorldName)
                  : WorldFolderResolver.legacyFolder(targetWorldName);
               blueprint.copyTo(destination, environment, modern);
               return destination.toFile();
            } else {
               throw new IOException("Instance destination already exists: " + targetWorldName);
            }
         }
      } catch (RuntimeException | IOException var7) {
         Logger.warn("Failed to prepare blueprint " + worldName + " as " + targetWorldName + ": " + var7.getMessage());
         return null;
      }
   }

   public static void recursivelyDelete(File file) {
      if (!file.exists()) {
         Logger.info("Attempted to recursively file " + file.getAbsolutePath() + " which doesn't exist.");
      } else {
         if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
               recursivelyDelete(subFile);
            }

            file.delete();
         } else {
            file.delete();
         }
      }
   }

   public static String getNewWorldName(String blueprintWorldName) {
      List<String> worldNames = new ArrayList<>();
      Bukkit.getWorlds().forEach(world -> worldNames.add(world.getName()));
      worldNames.addAll(WorldFolderResolver.listAllWorldNames());
      int highestNumber = 0;
      String instancePrefix = blueprintWorldName.toLowerCase(Locale.ROOT) + "_";

      for (String worldName : worldNames) {
         if (worldName.toLowerCase(Locale.ROOT).startsWith(instancePrefix)) {
            try {
               int worldNumber = Integer.parseInt(worldName.substring(instancePrefix.length()));
               if (worldNumber > highestNumber) {
                  highestNumber = worldNumber;
               }
            } catch (Exception var7) {
            }
         }
      }

      return blueprintWorldName + "_" + ++highestNumber;
   }
}
