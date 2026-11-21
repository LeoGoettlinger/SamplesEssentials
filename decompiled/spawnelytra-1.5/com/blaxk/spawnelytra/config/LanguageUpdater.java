package com.blaxk.spawnelytra.config;

import com.blaxk.spawnelytra.util.BackupUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public enum LanguageUpdater {
   private static final List<String> SUPPORTED = List.of("en", "de", "es", "fr", "ar", "pl");
   private static final List<String> DEPRECATED = List.of("hi", "zh");
   private static final String REQUIRED_LANG_VERSION = "1.5";

   public static void updateLanguages(JavaPlugin plugin) {
      updateLanguages(plugin, false);
   }

   public static void updateLanguages(JavaPlugin plugin, boolean removeDeprecated) {
      File dataFolder = plugin.getDataFolder();
      File langDir = new File(dataFolder, "lang");
      if (!langDir.exists() && !langDir.mkdirs()) {
         plugin.getLogger().warning("Failed to create language directory: " + langDir.getAbsolutePath());
      } else {
         if (removeDeprecated) {
            for (String code : DEPRECATED) {
               File f = new File(langDir, code + ".yml");
               if (f.exists()) {
                  BackupUtil.backupFile(plugin, f, "lang/" + f.getName());

                  try {
                     Files.deleteIfExists(f.toPath());
                  } catch (IOException var12) {
                     plugin.getLogger().warning("Failed to delete deprecated language file '" + f.getName() + "': " + var12.getMessage());
                  }
               }
            }
         }

         for (String codex : SUPPORTED) {
            File f = new File(langDir, codex + ".yml");
            if (!f.exists()) {
               try (InputStream in = plugin.getResource("lang/" + codex + ".yml")) {
                  if (in != null) {
                     Files.write(f.toPath(), in.readAllBytes());
                  }
               } catch (IOException var16) {
                  plugin.getLogger().warning("Failed to write language file '" + codex + ".yml': " + var16.getMessage());
               }
            } else if (needsLanguageUpgrade(f)) {
               BackupUtil.backupFile(plugin, f, "lang/" + f.getName());

               try (InputStream inx = plugin.getResource("lang/" + codex + ".yml")) {
                  if (inx != null) {
                     Files.write(f.toPath(), inx.readAllBytes());
                  } else {
                     plugin.getLogger().warning("Missing bundled resource for '" + codex + ".yml'.");
                  }
               } catch (IOException var14) {
                  plugin.getLogger().warning("Failed to update language file '" + codex + ".yml': " + var14.getMessage());
               }
            }
         }
      }
   }

   private static boolean needsLanguageUpgrade(File langFile) {
      try {
         FileConfiguration cfg = YamlConfiguration.loadConfiguration(langFile);
         String fileVersion = cfg.getString("lang-version", null);
         if (fileVersion != null && "1.5".equalsIgnoreCase(fileVersion.trim())) {
            Set<String> keys = new HashSet<>(cfg.getKeys(false));
            if (!keys.contains("new_version_available") && !keys.contains("update_to_version") && !keys.contains("download_link")) {
               for (String k : keys) {
                  String v = cfg.getString(k, "");
                  if (v != null && (v.contains("{key}") || v.contains("{currentVersion}") || v.contains("{latestVersion}"))) {
                     return true;
                  }
               }

               return false;
            } else {
               return true;
            }
         } else {
            return true;
         }
      } catch (Exception var7) {
         return true;
      }
   }
}
