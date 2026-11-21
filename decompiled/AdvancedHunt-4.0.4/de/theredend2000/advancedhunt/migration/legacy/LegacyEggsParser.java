package de.theredend2000.advancedhunt.migration.legacy;

import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardType;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class LegacyEggsParser {
   private LegacyEggsParser() {
   }

   public static List<LegacyEggsParser.LegacyCollection> parseAll(File legacyRootFolder) {
      File eggsDir = new File(legacyRootFolder, "eggs");
      if (eggsDir.exists() && eggsDir.isDirectory()) {
         File[] files = eggsDir.listFiles((dir, namex) -> namex.toLowerCase().endsWith(".yml"));
         if (files != null && files.length != 0) {
            List<LegacyEggsParser.LegacyCollection> out = new ArrayList<>();

            for (File f : files) {
               String name = f.getName();
               if (name.toLowerCase().endsWith(".yml")) {
                  name = name.substring(0, name.length() - 4);
               }

               LegacyEggsParser.LegacyCollection parsed = parseCollectionFile(name, f);
               if (parsed != null) {
                  out.add(parsed);
               }
            }

            return out;
         } else {
            return Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }

   private static LegacyEggsParser.LegacyCollection parseCollectionFile(String collectionName, File file) {
      FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
      boolean enabled = cfg.getBoolean("Enabled", true);
      boolean onePlayer = cfg.getBoolean("OnePlayer", false);
      int maxEggs = cfg.getInt("MaxEggs", 0);
      ConfigurationSection resetSec = cfg.getConfigurationSection("Reset");
      LegacyEggsParser.LegacyReset reset = null;
      if (resetSec != null) {
         reset = new LegacyEggsParser.LegacyReset(
            resetSec.getInt("Year", 0),
            resetSec.getInt("Month", 0),
            resetSec.getInt("Date", 0),
            resetSec.getInt("Hour", 0),
            resetSec.getInt("Minute", 0),
            resetSec.getInt("Second", 0)
         );
      }

      List<Reward> globalRewards = parseCommandRewards(cfg.getConfigurationSection("GlobalRewards"));
      List<LegacyEggsParser.LegacyPlacedEgg> placed = parsePlacedEggs(cfg.getConfigurationSection("PlacedEggs"));
      String order = cfg.getString("RequirementsOrder", "OR");
      ConfigurationSection reqSec = cfg.getConfigurationSection("Requirements");
      LegacyEggsParser.LegacyRequirements req = new LegacyEggsParser.LegacyRequirements(order, reqSec);
      return new LegacyEggsParser.LegacyCollection(collectionName, enabled, onePlayer, maxEggs, reset, globalRewards, placed, req);
   }

   private static List<LegacyEggsParser.LegacyPlacedEgg> parsePlacedEggs(ConfigurationSection sec) {
      if (sec == null) {
         return Collections.emptyList();
      } else {
         List<LegacyEggsParser.LegacyPlacedEgg> out = new ArrayList<>();

         for (String key : sec.getKeys(false)) {
            ConfigurationSection egg = sec.getConfigurationSection(key);
            if (egg != null) {
               String world = egg.getString("World");
               int x = egg.getInt("X");
               int y = egg.getInt("Y");
               int z = egg.getInt("Z");
               List<Reward> rewards = parseCommandRewards(egg.getConfigurationSection("Rewards"));
               if (world != null && !world.trim().isEmpty()) {
                  out.add(new LegacyEggsParser.LegacyPlacedEgg(world, x, y, z, rewards));
               }
            }
         }

         return out;
      }
   }

   private static List<Reward> parseCommandRewards(ConfigurationSection rewards) {
      if (rewards == null) {
         return Collections.emptyList();
      } else {
         List<Reward> out = new ArrayList<>();

         for (String key : rewards.getKeys(false)) {
            ConfigurationSection r = rewards.getConfigurationSection(key);
            if (r != null) {
               boolean enabled = r.getBoolean("enabled", true);
               if (enabled) {
                  double chance = r.getDouble("chance", 100.0);
                  String command = r.getString("command");
                  if (command != null && !command.trim().isEmpty()) {
                     command = LegacyMigrationUtil.migrateLegacyPlaceholders(command);
                     out.add(new Reward(RewardType.COMMAND, chance, null, null, command));
                  }
               }
            }
         }

         out.removeIf(Objects::isNull);
         return out;
      }
   }

   public static final class LegacyCollection {
      public final String name;
      public final boolean enabled;
      public final boolean onePlayer;
      public final int maxEggs;
      public final LegacyEggsParser.LegacyReset reset;
      public final List<Reward> globalRewards;
      public final List<LegacyEggsParser.LegacyPlacedEgg> placedEggs;
      public final LegacyEggsParser.LegacyRequirements requirements;

      private LegacyCollection(
         String name,
         boolean enabled,
         boolean onePlayer,
         int maxEggs,
         LegacyEggsParser.LegacyReset reset,
         List<Reward> globalRewards,
         List<LegacyEggsParser.LegacyPlacedEgg> placedEggs,
         LegacyEggsParser.LegacyRequirements requirements
      ) {
         this.name = name;
         this.enabled = enabled;
         this.onePlayer = onePlayer;
         this.maxEggs = maxEggs;
         this.reset = reset;
         this.globalRewards = globalRewards;
         this.placedEggs = placedEggs;
         this.requirements = requirements;
      }
   }

   public static final class LegacyPlacedEgg {
      public final String world;
      public final int x;
      public final int y;
      public final int z;
      public final List<Reward> rewards;

      private LegacyPlacedEgg(String world, int x, int y, int z, List<Reward> rewards) {
         this.world = world;
         this.x = x;
         this.y = y;
         this.z = z;
         this.rewards = rewards;
      }
   }

   public static final class LegacyRequirements {
      public final String order;
      public final ConfigurationSection raw;

      private LegacyRequirements(String order, ConfigurationSection raw) {
         this.order = order;
         this.raw = raw;
      }
   }

   public static final class LegacyReset {
      public final int year;
      public final int month;
      public final int date;
      public final int hour;
      public final int minute;
      public final int second;

      private LegacyReset(int year, int month, int date, int hour, int minute, int second) {
         this.year = year;
         this.month = month;
         this.date = date;
         this.hour = hour;
         this.minute = minute;
         this.second = second;
      }
   }
}
