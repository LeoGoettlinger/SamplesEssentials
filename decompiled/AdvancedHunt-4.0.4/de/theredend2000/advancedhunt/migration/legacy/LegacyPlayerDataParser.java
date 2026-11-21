package de.theredend2000.advancedhunt.migration.legacy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class LegacyPlayerDataParser {
   private LegacyPlayerDataParser() {
   }

   public static List<LegacyPlayerDataParser.LegacyPlayerData> parseAll(File legacyRootFolder) {
      File playerDir = new File(legacyRootFolder, "playerdata");
      if (playerDir.exists() && playerDir.isDirectory()) {
         File[] files = playerDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
         if (files != null && files.length != 0) {
            List<LegacyPlayerDataParser.LegacyPlayerData> out = new ArrayList<>();

            for (File f : files) {
               String base = f.getName();
               if (base.toLowerCase().endsWith(".yml")) {
                  base = base.substring(0, base.length() - 4);
               }

               UUID uuid;
               try {
                  uuid = UUID.fromString(base);
               } catch (Exception var11) {
                  continue;
               }

               out.add(parseOne(uuid, f));
            }

            return out;
         } else {
            return Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }

   private static LegacyPlayerDataParser.LegacyPlayerData parseOne(UUID playerUuid, File file) {
      FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
      ConfigurationSection foundEggs = cfg.getConfigurationSection("FoundEggs");
      if (foundEggs == null) {
         return new LegacyPlayerDataParser.LegacyPlayerData(playerUuid, Collections.emptyList());
      } else {
         List<LegacyPlayerDataParser.LegacyFoundEgg> found = new ArrayList<>();

         for (String collectionName : foundEggs.getKeys(false)) {
            ConfigurationSection collSec = foundEggs.getConfigurationSection(collectionName);
            if (collSec != null) {
               for (String key : collSec.getKeys(false)) {
                  if (!"Count".equalsIgnoreCase(key) && !"Name".equalsIgnoreCase(key)) {
                     ConfigurationSection entry = collSec.getConfigurationSection(key);
                     if (entry != null) {
                        String world = entry.getString("World");
                        int x = entry.getInt("X");
                        int y = entry.getInt("Y");
                        int z = entry.getInt("Z");
                        if (world != null && !world.trim().isEmpty()) {
                           found.add(new LegacyPlayerDataParser.LegacyFoundEgg(collectionName, world, x, y, z));
                        }
                     }
                  }
               }
            }
         }

         return new LegacyPlayerDataParser.LegacyPlayerData(playerUuid, found);
      }
   }

   public static final class LegacyFoundEgg {
      public final String collectionName;
      public final String world;
      public final int x;
      public final int y;
      public final int z;

      private LegacyFoundEgg(String collectionName, String world, int x, int y, int z) {
         this.collectionName = collectionName;
         this.world = world;
         this.x = x;
         this.y = y;
         this.z = z;
      }
   }

   public static final class LegacyPlayerData {
      public final UUID playerUuid;
      public final List<LegacyPlayerDataParser.LegacyFoundEgg> found;

      private LegacyPlayerData(UUID playerUuid, List<LegacyPlayerDataParser.LegacyFoundEgg> found) {
         this.playerUuid = playerUuid;
         this.found = found;
      }
   }
}
