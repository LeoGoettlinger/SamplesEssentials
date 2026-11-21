package com.blaxk.spawnelytra.data;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataManager {
   private final JavaPlugin plugin;
   private final File dataFolder;
   private final ConcurrentHashMap<UUID, PlayerDataManager.PlayerData> playerDataMap = new ConcurrentHashMap<>();
   private boolean initialized;

   public PlayerDataManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
      this.ensureDataFolder();
   }

   private void ensureDataFolder() {
      if (!this.dataFolder.exists() && !this.dataFolder.mkdirs()) {
         this.plugin.getLogger().warning("Could not create playerdata folder: " + this.dataFolder.getAbsolutePath());
      }
   }

   public void initialize() {
      this.ensureDataFolder();
      File[] dataFiles = this.dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
      if (dataFiles != null) {
         for (File file : dataFiles) {
            this.loadPlayerData(file);
         }
      }

      this.initialized = true;
   }

   private void loadPlayerData(File file) {
      String filename = file.getName();
      if (filename.endsWith(".yml")) {
         try {
            String uuidString = filename.substring(0, filename.length() - 4);
            UUID uuid = UUID.fromString(uuidString);
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            int flyCount = config.getInt("fly_count", 0);
            int boostCount = config.getInt("boost_count", 0);
            this.playerDataMap.put(uuid, new PlayerDataManager.PlayerData(uuid, flyCount, boostCount));
         } catch (IllegalArgumentException var8) {
            this.plugin.getLogger().warning("Invalid player data file: " + file.getName());
         }
      }
   }

   public PlayerDataManager.PlayerData getPlayerData(UUID uuid) {
      if (!this.initialized) {
         this.initialize();
      }

      return this.playerDataMap.computeIfAbsent(uuid, id -> new PlayerDataManager.PlayerData(id, 0, 0));
   }

   public void incrementFlyCount(Player player) {
      PlayerDataManager.PlayerData data = this.getPlayerData(player.getUniqueId());
      data.incrementFlyCount();
      this.savePlayerData(data);
   }

   public void incrementBoostCount(Player player) {
      PlayerDataManager.PlayerData data = this.getPlayerData(player.getUniqueId());
      data.incrementBoostCount();
      this.savePlayerData(data);
   }

   public void savePlayerData(PlayerDataManager.PlayerData data) {
      try {
         File file = new File(this.dataFolder, data.getUuid().toString() + ".yml");
         FileConfiguration config = new YamlConfiguration();
         config.set("fly_count", data.getFlyCount());
         config.set("boost_count", data.getBoostCount());
         config.save(file);
      } catch (IOException var4) {
         this.plugin.getLogger().warning("Failed to save player data: " + var4.getMessage());
      }
   }

   public void saveAllPlayerData() {
      for (PlayerDataManager.PlayerData data : this.playerDataMap.values()) {
         this.savePlayerData(data);
      }
   }

   public static class PlayerData {
      private final UUID uuid;
      private int flyCount;
      private int boostCount;

      public PlayerData(UUID uuid, int flyCount, int boostCount) {
         this.uuid = uuid;
         this.flyCount = flyCount;
         this.boostCount = boostCount;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public int getFlyCount() {
         return this.flyCount;
      }

      public int getBoostCount() {
         return this.boostCount;
      }

      public void incrementFlyCount() {
         this.flyCount++;
      }

      public void incrementBoostCount() {
         this.boostCount++;
      }
   }
}
