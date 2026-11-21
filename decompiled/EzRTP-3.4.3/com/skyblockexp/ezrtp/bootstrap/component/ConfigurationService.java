package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.ForceRtpConfiguration;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.performance.PerformanceSettings;
import com.skyblockexp.ezrtp.config.safety.UnsafeLocationSettings;
import com.skyblockexp.ezrtp.economy.EconomyService;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ConfigurationService {
   private int pluginExtensions;
   private final EzRtpPlugin plugin;
   private MessageProvider messageProvider;
   private ConfigurationSection effectiveBaseConfiguration;

   public ConfigurationService(EzRtpPlugin plugin) {
      this.plugin = plugin;
   }

   public EzRtpConfiguration reloadConfiguration() {
      this.ensureAdditionalConfigFiles();
      this.plugin.reloadConfig();
      FileConfiguration rtpConfiguration = this.loadExternalConfiguration("rtp.yml");
      FileConfiguration limitsConfiguration = this.loadExternalConfiguration("limits.yml");
      FileConfiguration storageConfiguration = this.loadExternalConfiguration("storage.yml");
      this.effectiveBaseConfiguration = this.buildEffectiveBaseConfiguration(
         this.plugin.getConfig(), rtpConfiguration, limitsConfiguration, storageConfiguration
      );
      this.setupMessagePrefix();
      this.setupMessageProvider();
      String language = this.effectiveBaseConfiguration.getString("language", "en");
      FileConfiguration messagesConfiguration = this.loadExternalConfiguration("messages/" + language + ".yml");
      FileConfiguration guiConfiguration = this.loadExternalConfiguration("gui.yml");
      FileConfiguration factionGuiConfiguration = this.loadExternalConfiguration("faction-gui.yml");
      FileConfiguration queueConfiguration = this.loadExternalConfiguration("queue.yml");
      FileConfiguration networkConfiguration = this.loadExternalConfiguration("network.yml");
      ConfigurationSection fallbackMessages = this.effectiveBaseConfiguration.getConfigurationSection("messages");
      ConfigurationSection fallbackGui = this.effectiveBaseConfiguration.getConfigurationSection("gui");
      ConfigurationSection fallbackQueue = this.effectiveBaseConfiguration.getConfigurationSection("queue");
      ConfigurationSection fallbackNetwork = this.effectiveBaseConfiguration.getConfigurationSection("network");
      return EzRtpConfiguration.fromConfigurations(
         this.effectiveBaseConfiguration,
         this.selectSection(messagesConfiguration, "messages", fallbackMessages),
         this.selectSection(guiConfiguration, "gui", fallbackGui),
         this.selectSection(factionGuiConfiguration, "faction-gui", null),
         this.selectSection(queueConfiguration, "queue", fallbackQueue),
         this.selectSection(networkConfiguration, "network", fallbackNetwork),
         this.plugin.getLogger()
      );
   }

   public ForceRtpConfiguration reloadForceRtpConfiguration() {
      FileConfiguration forceRtpConfiguration = this.loadExternalConfiguration("force-rtp.yml");
      return ForceRtpConfiguration.fromConfiguration(forceRtpConfiguration);
   }

   public MessageProvider getMessageProvider() {
      return this.messageProvider;
   }

   public ConfigurationSection getEffectiveBaseConfiguration() {
      return (ConfigurationSection)(this.effectiveBaseConfiguration != null ? this.effectiveBaseConfiguration : this.plugin.getConfig());
   }

   public void validateEconomyConfiguration(EzRtpConfiguration configuration, EconomyService economyService) {
      if (!economyService.isEnabled()) {
         boolean requiresEconomy = false;
         RandomTeleportSettings defaultSettings = configuration.getDefaultSettings();
         if (defaultSettings != null && defaultSettings.getTeleportCost() > 0.0) {
            requiresEconomy = true;
         }

         if (!requiresEconomy && configuration.getGuiSettings() != null) {
            requiresEconomy = configuration.getGuiSettings()
               .getWorldOptions()
               .stream()
               .anyMatch(option -> option.getSettings() != null && option.getSettings().getTeleportCost() > 0.0);
         }

         if (requiresEconomy) {
            this.plugin.getLogger().warning("Teleport costs are configured but Vault is unavailable. Teleports will not deduct currency.");
         }
      }
   }

   public void ensureAdditionalConfigFiles() {
      this.saveResourceIfMissing("rtp.yml");
      this.saveResourceIfMissing("limits.yml");
      this.saveResourceIfMissing("storage.yml");
      this.saveResourceIfMissing("gui.yml");
      this.saveResourceIfMissing("faction-gui.yml");
      this.saveResourceIfMissing("queue.yml");
      this.saveResourceIfMissing("network.yml");
      this.saveResourceIfMissing("force-rtp.yml");
      this.saveResourceIfMissing("performance.yml");
      this.saveResourceIfMissing("unsafe-location-monitoring.yml");
   }

   public PerformanceSettings reloadPerformanceConfiguration() {
      FileConfiguration performanceConfiguration = this.loadExternalConfiguration("performance.yml");
      return PerformanceSettings.fromConfiguration(performanceConfiguration);
   }

   public UnsafeLocationSettings reloadUnsafeLocationConfiguration() {
      FileConfiguration config = this.loadExternalConfiguration("unsafe-location-monitoring.yml");
      return UnsafeLocationSettings.fromConfiguration(config);
   }

   private void setupMessagePrefix() {
      String prefix = this.effectiveBaseConfiguration.getString("message-prefix", "");
      MessageUtil.setPrefix(prefix);
   }

   private void setupMessageProvider() {
      String language = this.effectiveBaseConfiguration.getString("language", "en");
      File messagesDir = new File(this.plugin.getDataFolder(), "messages");
      if (!messagesDir.exists()) {
         messagesDir.mkdirs();
      }

      File defaultLanguageFile = new File(messagesDir, "en.yml");
      if (!defaultLanguageFile.exists()) {
         try {
            this.plugin.saveResource("messages/en.yml", false);
         } catch (IllegalArgumentException var8) {
            this.plugin.getLogger().warning("Could not save default language file: " + var8.getMessage());
         }
      }

      File legacyMessages = new File(this.plugin.getDataFolder(), "messages.yml");
      File targetLanguageFile = new File(messagesDir, language + ".yml");
      if (legacyMessages.exists()) {
         try {
            boolean shouldCopy = false;
            if (!targetLanguageFile.exists()) {
               shouldCopy = true;
            } else if (legacyMessages.lastModified() > targetLanguageFile.lastModified()) {
               shouldCopy = true;
            }

            if (shouldCopy) {
               Files.copy(legacyMessages.toPath(), targetLanguageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
               this.plugin.getLogger().info("Detected legacy messages.yml - migrated to messages/" + targetLanguageFile.getName());
            }
         } catch (IOException var7) {
            this.plugin.getLogger().warning("Failed to migrate legacy messages.yml: " + var7.getMessage());
         }
      }

      this.repairCorruptedLanguageFile(language, targetLanguageFile);
      this.ensureLanguageFileHasAllKeys(language, targetLanguageFile);
      this.messageProvider = MessageProvider.load(messagesDir, language, this.plugin.getLogger());
   }

   private void repairCorruptedLanguageFile(String language, File languageFile) {
      if (languageFile != null && languageFile.exists()) {
         try {
            List<String> lines = Files.readAllLines(languageFile.toPath(), StandardCharsets.UTF_8);
            boolean javaSnippetDetected = lines.stream()
               .anyMatch(line -> line.contains("package com.skyblockexp.ezrtp.message") || line.contains("case TELEPORTING ->"));
            if (!javaSnippetDetected) {
               return;
            }

            File backup = new File(languageFile.getParentFile(), language.toLowerCase() + ".yml.bak");
            Files.copy(languageFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if ("en".equalsIgnoreCase(language)) {
               this.plugin.saveResource("messages/en.yml", true);
               this.plugin.getLogger().warning("Detected corrupted messages/en.yml. Backed up to messages/en.yml.bak and restored defaults.");
            } else {
               YamlConfiguration restored = new YamlConfiguration();

               for (MessageKey key : MessageKey.values()) {
                  restored.set(key.getKey(), MessageProvider.createDefault("en", this.plugin.getLogger()).getMessage(key));
               }

               restored.save(languageFile);
               this.plugin
                  .getLogger()
                  .warning(
                     "Detected corrupted language file '" + language + ".yml'. Backed up to " + backup.getName() + " and restored default keys in English."
                  );
            }
         } catch (IOException var11) {
            this.plugin.getLogger().warning("Failed to verify/repair messages/" + language + ".yml: " + var11.getMessage());
         } catch (IllegalArgumentException var12) {
            this.plugin.getLogger().warning("Failed to restore bundled messages/" + language + ".yml: " + var12.getMessage());
         }
      }
   }

   private void ensureLanguageFileHasAllKeys(String language, File languageFile) {
      if (languageFile != null && languageFile.exists()) {
         try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
            ConfigurationSection section = (ConfigurationSection)(config.isConfigurationSection("messages")
               ? config.getConfigurationSection("messages")
               : config);
            if (section == null) {
               section = config;
            }

            int added = 0;
            MessageProvider defaults = MessageProvider.createDefault("en", this.plugin.getLogger());

            for (MessageKey key : MessageKey.values()) {
               if (!section.contains(key.getKey())) {
                  section.set(key.getKey(), defaults.getMessage(key));
                  added++;
               }
            }

            if (added > 0) {
               config.save(languageFile);
               this.plugin.getLogger().info("Added " + added + " missing message keys to messages/" + language + ".yml");
            }
         } catch (Exception var11) {
            this.plugin.getLogger().warning("Failed to backfill missing keys for messages/" + language + ".yml: " + var11.getMessage());
         }
      }
   }

   private void saveResourceIfMissing(String resource) {
      this.pluginExtensions = this.registerPluginExtensions();
      File target = new File(this.plugin.getDataFolder(), resource);
      if (!target.exists()) {
         try {
            this.plugin.saveResource(resource, false);
         } catch (IllegalArgumentException var4) {
            this.plugin.getLogger().warning("Unable to save default resource '" + resource + "': " + var4.getMessage());
         }
      }
   }

   private FileConfiguration loadExternalConfiguration(String fileName) {
      File file = new File(this.plugin.getDataFolder(), fileName);
      if (!file.exists()) {
         this.saveResourceIfMissing(fileName);
      }

      return !file.exists() ? null : YamlConfiguration.loadConfiguration(file);
   }

   private ConfigurationSection selectSection(FileConfiguration configuration, String nestedKey, ConfigurationSection fallback) {
      ConfigurationSection section = null;
      if (configuration != null) {
         if (nestedKey != null && configuration.isConfigurationSection(nestedKey)) {
            section = configuration.getConfigurationSection(nestedKey);
         }

         if (section == null && !configuration.getKeys(false).isEmpty()) {
            section = configuration;
         }
      }

      if (section != null && !section.getKeys(true).isEmpty()) {
         return section;
      } else {
         return fallback != null && !fallback.getKeys(true).isEmpty() ? fallback : null;
      }
   }

   private ConfigurationSection buildEffectiveBaseConfiguration(
      ConfigurationSection base, ConfigurationSection rtpConfiguration, ConfigurationSection limitsConfiguration, ConfigurationSection storageConfiguration
   ) {
      MemoryConfiguration merged = new MemoryConfiguration();
      this.copySection(base, merged);
      this.copySection(rtpConfiguration, merged);
      this.copySection(limitsConfiguration, merged);
      this.copySection(storageConfiguration, merged);
      return merged;
   }

   private void copySection(ConfigurationSection source, ConfigurationSection target) {
      if (source != null && target != null) {
         for (String key : source.getKeys(false)) {
            Object value = source.get(key);
            if (value instanceof ConfigurationSection sectionValue) {
               ConfigurationSection existing = target.getConfigurationSection(key);
               if (existing == null) {
                  existing = target.createSection(key);
               }

               this.copySection(sectionValue, existing);
            } else {
               target.set(key, value);
            }
         }
      }
   }
}
