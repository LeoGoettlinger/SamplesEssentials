package com.magmaguy.elitemobs.magmacore.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.World;

public final class WorldFolderResolver {
   private static final String DIMENSIONS_DIRECTORY = "dimensions";
   private static final String DEFAULT_NAMESPACE = "minecraft";
   private static final String LEVEL_DAT = "level.dat";
   private static final String LEVEL_OLD_DAT = "level.dat_old";

   private WorldFolderResolver() {
   }

   public static Path legacyFolder(String worldName) {
      return Bukkit.getWorldContainer().toPath().resolve(worldName);
   }

   public static Path modernFolder(String worldName) {
      return modernNamespaceRoot().resolve(worldName.toLowerCase(Locale.ROOT));
   }

   public static Path modernNamespaceRoot() {
      if (usesModernStorage()) {
         return ((World)Bukkit.getWorlds().getFirst()).getWorldFolder().toPath().getParent().getParent().resolve("minecraft");
      } else {
         String levelName = Bukkit.getWorlds().isEmpty() ? "world" : ((World)Bukkit.getWorlds().getFirst()).getName();
         return Bukkit.getWorldContainer().toPath().resolve(levelName).resolve("dimensions").resolve("minecraft");
      }
   }

   public static boolean usesModernStorage() {
      if (Bukkit.getWorlds().isEmpty()) {
         return false;
      } else {
         File folder = ((World)Bukkit.getWorlds().getFirst()).getWorldFolder();
         if (folder == null) {
            return false;
         } else {
            Path parent = folder.toPath().toAbsolutePath().getParent();
            return parent != null && parent.getParent() != null && "dimensions".equals(parent.getParent().getFileName().toString());
         }
      }
   }

   public static boolean hasModernLayout(String worldName) {
      Path folder = modernFolder(worldName);
      Path paperData = folder.resolve("data").resolve("paper");
      return Files.isRegularFile(paperData.resolve("metadata.dat")) && Files.isRegularFile(paperData.resolve("level_overrides.dat"));
   }

   public static boolean hasLegacyLayout(String worldName) {
      return Files.isRegularFile(legacyFolder(worldName).resolve("level.dat"));
   }

   public static File resolve(String worldName) {
      return hasModernLayout(worldName) ? modernFolder(worldName).toFile() : legacyFolder(worldName).toFile();
   }

   public static boolean exists(String worldName) {
      return hasModernLayout(worldName) || hasLegacyLayout(worldName);
   }

   public static boolean folderExists(String worldName) {
      return Files.isDirectory(modernFolder(worldName)) || Files.isDirectory(legacyFolder(worldName));
   }

   public static Set<String> listAllWorldNames() {
      Set<String> names = new LinkedHashSet<>();
      File legacyRoot = Bukkit.getWorldContainer();
      File[] legacyChildren = legacyRoot.listFiles(File::isDirectory);
      if (legacyChildren != null) {
         for (File child : legacyChildren) {
            names.add(child.getName());
         }
      }

      File modernRoot = modernNamespaceRoot().toFile();
      File[] modernChildren = modernRoot.listFiles(File::isDirectory);
      if (modernChildren != null) {
         for (File child : modernChildren) {
            names.add(child.getName());
         }
      }

      return names;
   }

   public static boolean quarantineMigrationDebris(String worldName) {
      if (!legacyTriggersPaperMigration(worldName)) {
         return false;
      } else if (!modernHasAnyContent(worldName)) {
         return false;
      } else {
         Path legacy = legacyFolder(worldName);
         Path quarantine = legacy.resolveSibling(worldName + ".paper-migration-backup-" + System.currentTimeMillis());

         try {
            Files.move(legacy, quarantine);
            Logger.warn(
               "Quarantined stale legacy world folder "
                  + legacy
                  + " -> "
                  + quarantine
                  + " (Paper migration debris). Inspect and delete after verifying the world loads."
            );
            return true;
         } catch (IOException var4) {
            Logger.warn("Failed to quarantine legacy world folder " + legacy + ": " + var4.getMessage());
            return false;
         }
      }
   }

   private static boolean legacyTriggersPaperMigration(String worldName) {
      Path legacy = legacyFolder(worldName);
      return Files.isRegularFile(legacy.resolve("level.dat")) || Files.isRegularFile(legacy.resolve("level.dat_old"));
   }

   private static boolean modernHasAnyContent(String worldName) {
      Path modern = modernFolder(worldName);
      if (!Files.isDirectory(modern)) {
         return false;
      } else {
         try {
            boolean var3;
            try (Stream<Path> children = Files.list(modern)) {
               var3 = children.findAny().isPresent();
            }

            return var3;
         } catch (IOException var7) {
            return false;
         }
      }
   }

   public static void deleteAllLayouts(String worldName) {
      File legacy = legacyFolder(worldName).toFile();
      File modern = modernFolder(worldName).toFile();
      if (legacy.isDirectory()) {
         try {
            org.apache.commons.io.FileUtils.deleteDirectory(legacy);
         } catch (IOException var5) {
            Logger.warn("Failed to delete legacy world folder " + legacy + ": " + var5.getMessage());
         }
      }

      if (modern.isDirectory()) {
         try {
            org.apache.commons.io.FileUtils.deleteDirectory(modern);
         } catch (IOException var4) {
            Logger.warn("Failed to delete modern world folder " + modern + ": " + var4.getMessage());
         }
      }
   }
}
