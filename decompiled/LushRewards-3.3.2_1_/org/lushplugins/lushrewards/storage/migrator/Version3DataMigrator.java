package org.lushplugins.lushrewards.storage.migrator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.utils.FilenameUtils;

public class Version3DataMigrator extends Migrator {
   private final File version2DataFolder = new File(LushRewards.getInstance().getDataFolder().getParentFile(), "ActivityRewarder/data");
   private final File lushDataFolder = new File(LushRewards.getInstance().getDataFolder(), "data");

   public Version3DataMigrator() throws FileNotFoundException {
      super("Version3Migrator");
   }

   @Override
   public boolean convert() {
      if (!this.version2DataFolder.exists()) {
         return false;
      } else {
         if (!this.lushDataFolder.exists()) {
            this.lushDataFolder.mkdirs();
         }

         AtomicInteger count = new AtomicInteger(0);
         long total = this.version2DataFolder.list().length;

         try {
            try (Stream<Path> stream = Files.list(this.version2DataFolder.toPath())) {
               stream.filter(file -> !Files.isDirectory(file))
                  .parallel()
                  .forEach(
                     entry -> {
                        File file = entry.toFile();
                        String uuid = FilenameUtils.removeExtension(file.getName());

                        try {
                           this.updateFile(file);
                           LushRewards.getInstance()
                              .getLogger()
                              .info("Translated user data for '" + uuid + "' (" + count.incrementAndGet() + "/" + total + ")");
                        } catch (IOException var8x) {
                           LushRewards.getInstance()
                              .getLogger()
                              .severe("Failed to translate user data for '" + uuid + "' (" + count.incrementAndGet() + "/" + total + ")");
                        }
                     }
                  );
            }

            return true;
         } catch (IOException var9) {
            LushRewards.getInstance().getLogger().severe("Failed to load data folder");
            return false;
         }
      }
   }

   public void updateFile(File file) throws IOException {
      String uuid = FilenameUtils.removeExtension(file.getName());
      YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
      JsonObject json = new JsonObject();
      JsonObject main = new JsonObject();
      main.addProperty("username", data.getString("name"));
      main.addProperty("minutesPlayed", data.getInt("minutes-played"));
      json.add("main", main);
      LocalDate startDate = null;
      String startDateFormatted = null;
      ConfigurationSection dailyRewardsData = data.getConfigurationSection("daily-rewards");
      if (dailyRewardsData != null) {
         JsonObject module = new JsonObject();
         if (dailyRewardsData.contains("start-date")) {
            startDate = LocalDate.parse(dailyRewardsData.getString("start-date"), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            startDateFormatted = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            module.addProperty("startDate", startDateFormatted);
         }

         module.addProperty("lastJoinDate", LocalDate.now().minusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE));
         if (dailyRewardsData.contains("last-collected-date")) {
            module.addProperty(
               "lastCollectedDate",
               LocalDate.parse(dailyRewardsData.getString("last-collected-date"), DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                  .format(DateTimeFormatter.ISO_LOCAL_DATE)
            );
         }

         JsonArray collectedDays = new JsonArray();
         if (startDate != null) {
            for (String dateRaw : dailyRewardsData.getStringList("collected-dates")) {
               LocalDate date = LocalDate.parse(dateRaw, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
               long days = startDate.until(date).get(ChronoUnit.DAYS) + 1L;
               collectedDays.add(days);
            }
         }

         module.add("collectedDays", collectedDays);
         module.addProperty("dayNum", dailyRewardsData.getInt("day-num"));
         module.addProperty("streak", dailyRewardsData.getInt("streak"));
         module.addProperty("highestStreak", dailyRewardsData.getInt("highest-streak"));
         json.add("daily-rewards", module);
      }

      ConfigurationSection dailyPlaytimeData = data.getConfigurationSection("daily-playtime-goals");
      if (dailyPlaytimeData != null) {
         JsonObject modulex = new JsonObject();
         modulex.addProperty("lastCollectedPlaytime", dailyPlaytimeData.getString("last-collected-playtime"));
         modulex.addProperty("startDate", startDateFormatted);
         modulex.addProperty("previousDayEndPlaytime", 0);
         json.add("daily-playtime-rewards", modulex);
      }

      ConfigurationSection globalPlaytimeData = data.getConfigurationSection("global-playtime-goals");
      if (globalPlaytimeData != null) {
         JsonObject modulex = new JsonObject();
         modulex.addProperty("lastCollectedPlaytime", globalPlaytimeData.getString("last-collected-playtime"));
         modulex.addProperty("startDate", startDateFormatted);
         modulex.addProperty("previousDayEndPlaytime", 0);
         json.add("global-playtime-rewards", modulex);
      }

      FileWriter writer = new FileWriter(new File(this.lushDataFolder, uuid + ".json"));
      LushRewards.getInstance().getGson().toJson(json, writer);
      writer.flush();
   }
}
