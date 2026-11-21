package github.nighter.smartspawner.spawner.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class SpawnerFileLayout {
   public static final String CUSTOM_SECTION = "custom_spawners";
   public static final String ALIASES = "aliases";
   private static final String LEGACY_SUFFIX = "_spawner";

   private SpawnerFileLayout() {
   }

   public static String legacyBaseId(String normalizedName) {
      return normalizedName.endsWith("_spawner") && normalizedName.length() > "_spawner".length()
         ? normalizedName.substring(0, normalizedName.length() - "_spawner".length())
         : null;
   }

   public static boolean isLegacy(ConfigurationSection config, String typeKey) {
      if (config.isConfigurationSection("custom_spawners")) {
         return false;
      } else {
         for (String key : config.getKeys(false)) {
            ConfigurationSection entry = config.getConfigurationSection(key);
            if (entry != null && entry.contains(typeKey)) {
               return true;
            }
         }

         return false;
      }
   }

   public static void backup(File file, Logger logger) {
      File backup = new File(file.getParentFile(), file.getName() + ".1.8-backup");

      try {
         Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
         logger.info("Converting " + file.getName() + " to the new layout. The old file was saved as " + backup.getName() + ".");
      } catch (IOException var4) {
         logger.warning("Could not back up " + file.getName() + " before converting it: " + var4.getMessage());
      }
   }

   public static boolean convertLegacy(YamlConfiguration config, String typeKey, Function<String, String> baseIdOf) {
      if (!isLegacy(config, typeKey)) {
         return false;
      } else {
         List<String> keys = new ArrayList<>(config.getKeys(false));
         Map<String, String> baseIdByKey = new LinkedHashMap<>();
         Map<String, String> baseKeyById = new LinkedHashMap<>();

         for (String key : keys) {
            ConfigurationSection entry = config.getConfigurationSection(key);
            if (entry != null) {
               String baseId = baseIdOf.apply(entry.getString(typeKey, key));
               if (baseId != null) {
                  baseIdByKey.put(key, baseId);
                  String name = SpawnerConfigName.normalize(key);
                  String current = baseKeyById.get(baseId);
                  boolean preferred = name.equals(baseId + "_spawner") || name.equals(baseId);
                  if (current == null || preferred && !isPreferred(current, baseId)) {
                     baseKeyById.put(baseId, key);
                  }
               }
            }
         }

         MemoryConfiguration out = new MemoryConfiguration();
         List<String> customs = new ArrayList<>();

         for (String keyx : keys) {
            String baseId = baseIdByKey.get(keyx);
            if (!config.isConfigurationSection(keyx)) {
               copy(config, keyx, out, keyx, Set.of());
            } else if (baseId != null && keyx.equals(baseKeyById.get(baseId))) {
               copy(config, keyx, out, baseId, Set.of(typeKey));
               if (!isPreferred(keyx, baseId)) {
                  out.set(baseId + ".aliases", List.of(SpawnerConfigName.normalize(keyx)));
               }
            } else {
               customs.add(keyx);
            }
         }

         out.createSection("custom_spawners");

         for (String keyxx : customs) {
            copy(config, keyxx, out, "custom_spawners." + keyxx, Set.of());
         }

         for (String keyxx : keys) {
            config.set(keyxx, null);
         }

         for (String keyxx : out.getKeys(false)) {
            copy(out, keyxx, config, keyxx, Set.of());
         }

         return true;
      }
   }

   private static boolean isPreferred(String key, String baseId) {
      String name = SpawnerConfigName.normalize(key);
      return name.equals(baseId + "_spawner") || name.equals(baseId);
   }

   private static void copy(ConfigurationSection from, String fromPath, ConfigurationSection to, String toPath, Set<String> skipChildren) {
      if (from.isConfigurationSection(fromPath)) {
         to.createSection(toPath);
         ConfigurationSection section = from.getConfigurationSection(fromPath);

         for (String child : section.getKeys(false)) {
            if (!skipChildren.contains(child)) {
               copy(from, fromPath + "." + child, to, toPath + "." + child, Set.of());
            }
         }
      } else {
         to.set(toPath, from.get(fromPath));
      }

      to.setComments(toPath, from.getComments(fromPath));
      to.setInlineComments(toPath, from.getInlineComments(fromPath));
   }
}
