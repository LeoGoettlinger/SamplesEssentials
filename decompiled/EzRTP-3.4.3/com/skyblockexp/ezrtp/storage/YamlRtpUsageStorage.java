package com.skyblockexp.ezrtp.storage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlRtpUsageStorage implements RtpUsageStorage {
   private final File file;
   private YamlConfiguration config;

   public YamlRtpUsageStorage(File file) {
      this.file = file;
      this.reload();
   }

   @Override
   public long getLastRtpTime(UUID player, String world) {
      return this.config.getLong("players." + player + "." + world + ".lastRtp", 0L);
   }

   @Override
   public void setLastRtpTime(UUID player, String world, long time) {
      this.config.set("players." + player + "." + world + ".lastRtp", time);
   }

   @Override
   public int getUsageCount(UUID player, String world, String period) {
      return this.config.getInt("players." + player + "." + world + ".usage." + period, 0);
   }

   @Override
   public void incrementUsage(UUID player, String world, String period) {
      String path = "players." + player + "." + world + ".usage." + period;
      int count = this.config.getInt(path, 0);
      this.config.set(path, count + 1);
   }

   @Override
   public void resetUsage(UUID player, String world, String period) {
      if (player == null && world == null) {
         if (this.config.isConfigurationSection("players")) {
            for (String p : this.config.getConfigurationSection("players").getKeys(false)) {
               for (String w : this.config.getConfigurationSection("players." + p).getKeys(false)) {
                  this.config.set("players." + p + "." + w + ".usage." + period, 0);
               }
            }
         }
      } else if (player != null && world == null) {
         if (this.config.isConfigurationSection("players." + player)) {
            for (String w : this.config.getConfigurationSection("players." + player).getKeys(false)) {
               this.config.set("players." + player + "." + w + ".usage." + period, 0);
            }
         }
      } else if (player != null || world == null) {
         this.config.set("players." + player + "." + world + ".usage." + period, 0);
      } else if (this.config.isConfigurationSection("players")) {
         for (String p : this.config.getConfigurationSection("players").getKeys(false)) {
            this.config.set("players." + p + "." + world + ".usage." + period, 0);
         }
      }
   }

   @Override
   public void save() {
      try {
         this.config.save(this.file);
      } catch (IOException var2) {
         Bukkit.getLogger().warning("[EzRTP] Failed to save RTP usage YAML: " + var2.getMessage());
      }
   }

   @Override
   public void reload() {
      if (!this.file.exists()) {
         try {
            this.file.getParentFile().mkdirs();
            this.file.createNewFile();
         } catch (IOException var2) {
            Bukkit.getLogger().warning("[EzRTP] Failed to create RTP usage YAML: " + var2.getMessage());
         }
      }

      this.config = YamlConfiguration.loadConfiguration(this.file);
   }
}
