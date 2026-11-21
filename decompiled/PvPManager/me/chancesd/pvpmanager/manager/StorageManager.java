package me.chancesd.pvpmanager.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.sdutils.database.DatabaseConfigBuilder;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.storage.SQLStorage;
import me.chancesd.pvpmanager.storage.Storage;
import me.chancesd.pvpmanager.tasks.StorageSaveTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class StorageManager {
   private final PvPManager plugin;
   private final Storage storage;

   public StorageManager(PvPManager plugin) {
      this.plugin = plugin;
      this.storage = new SQLStorage(plugin);
      this.convertYMLToSQL();
      ScheduleUtils.runAsyncTimer(new StorageSaveTask(plugin, this.storage), 300L, 300L, TimeUnit.SECONDS);
   }

   public void shutdown() {
      this.storage.shutdown();
   }

   public Storage getStorage() {
      return this.storage;
   }

   public void convertFromCurrent(DatabaseConfigBuilder.DatabaseType dbType, @NotNull CommandSender sender, long start) {
      Storage destinationStorage = new SQLStorage(this.plugin, dbType);
      this.convertTo(destinationStorage, this.storage.getAllUserData(), sender, start);
      destinationStorage.shutdown();
   }

   private void convertYMLToSQL() {
      File usersFile = new File(this.plugin.getDataFolder(), "users.yml");
      if (usersFile.exists()) {
         long start = System.currentTimeMillis();
         Log.info("Converting users.yml file to SQL database");
         YamlConfiguration users = new YamlConfiguration();

         try {
            users.load(usersFile);
         } catch (InvalidConfigurationException | IOException var13) {
            Log.severe("Error converting to SQL, can't load users file", var13);
         }

         ConfigurationSection section = users.getConfigurationSection("players");
         if (section == null) {
            Log.warning("Error converting users file, couldn't read players section");
         } else {
            List<Map<String, Object>> rows = new ArrayList<>();
            Map<String, Object> keys = section.getValues(false);

            for (Entry<String, Object> entry : keys.entrySet()) {
               ConfigurationSection entrySection = (ConfigurationSection)entry.getValue();
               Map<String, Object> values = entrySection.getValues(false);
               values.put("uuid", entry.getKey());
               rows.add(values);
            }

            this.convertTo(this.storage, rows, Bukkit.getConsoleSender(), start);

            try {
               Files.delete(usersFile.toPath());
            } catch (IOException var12) {
               Log.warning("Error deleting the users.yml file, please remove it or the plugin will try to convert it on every server restart", var12);
            }
         }
      }
   }

   private void convertTo(Storage destination, final List<Map<String, Object>> allRows, @NotNull final CommandSender sender, long start) {
      final AtomicBoolean conversionFinished = new AtomicBoolean();
      final AtomicInteger usersConverted = new AtomicInteger();
      new Timer().schedule(new TimerTask() {
         @Override
         public void run() {
            if (conversionFinished.get()) {
               this.cancel();
            } else {
               ChatUtils.send(sender, ChatColor.GOLD + "Converting database... " + usersConverted + "/" + allRows.size());
            }
         }
      }, 0L, 1000L);

      for (Map<String, Object> row : allRows) {
         UUID uuid = UUID.fromString((String)row.get("uuid"));
         if (destination.saveUserData(uuid, row)) {
            usersConverted.incrementAndGet();
         }
      }

      conversionFinished.set(true);
      ChatUtils.send(sender, ChatColor.GOLD + "Converted " + usersConverted + " out of " + allRows.size() + " users to the new database");
      ChatUtils.send(
         sender, ChatColor.DARK_GREEN + "Database conversion finished in " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds"
      );
   }
}
