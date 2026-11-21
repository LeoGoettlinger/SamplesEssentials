package com.blaxk.spawnelytra.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import org.bukkit.plugin.java.JavaPlugin;

public enum BackupUtil {
   private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

   public static void backupFile(JavaPlugin plugin, File source, String relativePath) {
      if (source != null && source.exists()) {
         try {
            File backupsRoot = new File(plugin.getDataFolder(), "backups");
            if (!backupsRoot.exists() && !backupsRoot.mkdirs()) {
               plugin.getLogger().warning("Failed to create backups directory at: " + backupsRoot.getAbsolutePath());
            }

            String timestamp = LocalDateTime.now().format(TS);
            File tsDir = new File(backupsRoot, timestamp);
            if (!tsDir.exists() && !tsDir.mkdirs()) {
               plugin.getLogger().warning("Failed to create timestamped backups directory at: " + tsDir.getAbsolutePath());
            }

            File target = new File(tsDir, relativePath.replace('/', File.separatorChar));
            File parent = target.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
               plugin.getLogger().warning("Failed to create backup subdirectory: " + parent.getAbsolutePath());
            }

            Path src = source.toPath();
            Path dst = target.toPath();
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Created backup: " + tsDir.getName() + File.separator + relativePath);
         } catch (IOException var10) {
            plugin.getLogger().warning("Failed to create backup for '" + relativePath + "': " + var10.getMessage());
         }
      }
   }

   public static File backupDirectory(JavaPlugin plugin, File sourceDir, String relativeBase) {
      if (plugin != null && sourceDir != null && sourceDir.exists() && sourceDir.isDirectory()) {
         try {
            File backupsRoot = new File(plugin.getDataFolder(), "backups");
            if (!backupsRoot.exists() && !backupsRoot.mkdirs()) {
               plugin.getLogger().warning("Failed to create backups directory at: " + backupsRoot.getAbsolutePath());
            }

            String timestamp = LocalDateTime.now().format(TS);
            File tsDir = new File(backupsRoot, timestamp);
            if (!tsDir.exists() && !tsDir.mkdirs()) {
               plugin.getLogger().warning("Failed to create timestamped backups directory at: " + tsDir.getAbsolutePath());
            }

            Path base = sourceDir.toPath();

            try (Stream<Path> paths = Files.walk(base)) {
               paths.filter(x$0 -> Files.isRegularFile(x$0)).forEach(p -> {
                  try {
                     String rel = base.relativize(p).toString().replace('\\', '/');
                     String combined = relativeBase != null && !relativeBase.isEmpty() ? relativeBase.replace('\\', '/') + "/" + rel : rel;
                     File target = new File(tsDir, combined.replace('/', File.separatorChar));
                     File parent = target.getParentFile();
                     if (parent != null && !parent.exists() && !parent.mkdirs()) {
                        plugin.getLogger().warning("Failed to create backup subdirectory: " + parent.getAbsolutePath());
                     }

                     Files.copy(p, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                  } catch (IOException var9) {
                     plugin.getLogger().warning("Failed to back up file '" + p + "': " + var9.getMessage());
                  }
               });
            }

            plugin.getLogger().info("Created directory backup at: " + tsDir.getAbsolutePath());
            return tsDir;
         } catch (IOException var12) {
            plugin.getLogger().warning("Failed to create directory backup: " + var12.getMessage());
            return null;
         }
      } else {
         return null;
      }
   }
}
