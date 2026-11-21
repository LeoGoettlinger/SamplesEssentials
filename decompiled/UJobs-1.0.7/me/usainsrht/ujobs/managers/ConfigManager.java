package me.usainsrht.ujobs.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.yaml.YamlMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
   private UJobsPlugin plugin;
   private YamlConfiguration jobsConfig;
   private YamlConfiguration leaderboardConfig;
   private Map<String, YamlMessage> messages;
   public static final YamlMessage EMPTY_YAML_MESSAGE = new YamlMessage(null);

   public ConfigManager(UJobsPlugin plugin) {
      this.plugin = plugin;
   }

   public void reload() {
      this.plugin.reloadConfig();
      this.loadConfigs();
   }

   public void loadConfigs() {
      this.plugin.saveDefaultConfig();
      this.loadMessages();
      File jobsFile = new File(this.plugin.getDataFolder(), "jobs.yml");
      if (!jobsFile.exists()) {
         this.plugin.saveResource("jobs.yml", false);
      }

      this.jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);
      File leaderBoardFile = new File(this.plugin.getDataFolder(), "leaderboard.yml");
      if (!leaderBoardFile.exists()) {
         try {
            leaderBoardFile.createNewFile();
         } catch (Exception var4) {
            this.plugin.getLogger().severe("Failed to create leaderboard.yml: " + var4.getMessage());
         }
      }

      this.leaderboardConfig = YamlConfiguration.loadConfiguration(leaderBoardFile);
   }

   public void loadMessages() {
      this.messages = new HashMap<>();
      ConfigurationSection messagesSection = this.plugin.getConfig().getConfigurationSection("messages");
      if (messagesSection != null) {
         for (String key : messagesSection.getKeys(false)) {
            YamlMessage yamlMessage = new YamlMessage(messagesSection.get(key));
            this.messages.put(key, yamlMessage);
         }
      }
   }

   public YamlMessage getMessage(String key) {
      return this.messages.getOrDefault(key, EMPTY_YAML_MESSAGE);
   }

   public void saveLeaderboard() {
      try {
         this.leaderboardConfig.save(new File(this.plugin.getDataFolder(), "leaderboard.yml"));
      } catch (Exception var2) {
         this.plugin.getLogger().severe("Failed to save leaderboard.yml: " + var2.getMessage());
      }
   }

   @Generated
   public UJobsPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public YamlConfiguration getJobsConfig() {
      return this.jobsConfig;
   }

   @Generated
   public YamlConfiguration getLeaderboardConfig() {
      return this.leaderboardConfig;
   }

   @Generated
   public Map<String, YamlMessage> getMessages() {
      return this.messages;
   }
}
