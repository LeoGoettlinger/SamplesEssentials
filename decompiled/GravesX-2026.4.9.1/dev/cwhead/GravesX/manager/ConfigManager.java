package dev.cwhead.GravesX.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.bundledlibraries.configupdater.ConfigUpdater;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.ResourceUtil;
import com.ranull.graves.util.YAMLUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConfigManager {
   public static final int CURRENT_CONFIG_VERSION = 25;
   private final Graves plugin;
   private final ConfigManager.Paths paths;
   private final ConfigManager.IO io;
   private final ConfigManager.Merger merger;
   private final ConfigManager.Defaults defaults;
   private final ConfigManager.Updater updater;
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
   private volatile FileConfiguration mergedConfig;

   public ConfigManager(@NotNull Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.paths = new ConfigManager.Paths(plugin);
      this.io = new ConfigManager.IO(plugin);
      this.merger = new ConfigManager.Merger(plugin, this.io);
      this.defaults = new ConfigManager.Defaults(plugin, this.io);
      this.updater = new ConfigManager.Updater(plugin, this.paths);
   }

   public void init(boolean isDevelopmentBuild) {
      this.ensureDefaultsExist();
      this.updateIfNeeded(isDevelopmentBuild);
      this.reload();
   }

   public void ensureDefaultsExist() {
      ResourceUtil.copyResources("config", this.paths.configFolder.getPath(), false, this.plugin);
   }

   public void reload() {
      this.lock.writeLock().lock();

      try {
         FileConfiguration loaded;
         if (this.paths.legacySingleConfig.exists()) {
            loaded = this.io.loadYaml(this.paths.legacySingleConfig);
            if (loaded == null) {
               loaded = new YamlConfiguration();
            }

            this.defaults.applyResourceDefaults(loaded, this.paths.legacySingleConfig.getName());
         } else {
            loaded = this.merger.loadMergedFolderConfig(this.paths.configFolder);
         }

         this.mergedConfig = loaded;
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   @NotNull
   public FileConfiguration config() {
      FileConfiguration snapshot = this.mergedConfig;
      if (snapshot != null) {
         return snapshot;
      } else {
         this.lock.writeLock().lock();

         FileConfiguration var2;
         try {
            if (this.mergedConfig == null) {
               this.reload();
            }

            var2 = this.mergedConfig;
         } finally {
            this.lock.writeLock().unlock();
         }

         return var2;
      }
   }

   public void updateIfNeeded(boolean isDevelopmentBuild) {
      this.updater.updateIfNeeded(isDevelopmentBuild);
   }

   @Nullable
   public ConfigurationSection getConfigSection(String configKey, @NotNull Grave grave) {
      return this.getConfigSection(configKey, grave.getOwnerType(), grave.getPermissionList());
   }

   @Nullable
   public ConfigurationSection getConfigSection(String configKey, @Nullable List<Grave> graveList) {
      if (graveList == null) {
         return null;
      } else {
         for (Grave grave : graveList) {
            ConfigurationSection section = this.getConfigSection(configKey, grave.getOwnerType(), grave.getPermissionList());
            if (section != null) {
               return section;
            }
         }

         return null;
      }
   }

   @Nullable
   public ConfigurationSection getConfigSection(String configKey, @NotNull Entity entity) {
      return this.getConfigSection(configKey, entity.getType(), this.getPermissionList(entity));
   }

   @Nullable
   public ConfigurationSection getConfigSection(String configKey, @NotNull Entity entity, @Nullable List<String> permissionList) {
      return this.getConfigSection(configKey, entity.getType(), permissionList);
   }

   @Nullable
   public ConfigurationSection getConfigSection(String configKey, @Nullable EntityType entityType, @Nullable List<String> permissionList) {
      FileConfiguration cfg = this.config();
      if (permissionList != null && !permissionList.isEmpty()) {
         for (String permission : permissionList) {
            String sectionPath = "settings.permission." + permission;
            if (cfg.isConfigurationSection(sectionPath)) {
               ConfigurationSection section = cfg.getConfigurationSection(sectionPath);
               if (section != null && this.contains(section, configKey)) {
                  return section;
               }
            }
         }
      }

      if (entityType != null) {
         String sectionPath = "settings.entity." + entityType.name();
         if (cfg.isConfigurationSection(sectionPath)) {
            ConfigurationSection section = cfg.getConfigurationSection(sectionPath);
            if (section != null && this.contains(section, configKey)) {
               return section;
            }
         }
      }

      return cfg.getConfigurationSection("settings.default.default");
   }

   private boolean contains(@NotNull ConfigurationSection section, @NotNull String path) {
      return this.plugin.getVersionManager() != null && this.plugin.getVersionManager().hasConfigContains()
         ? section.contains(path, true)
         : section.contains(path);
   }

   @NotNull
   public List<String> getPermissionList(@NotNull Entity entity) {
      if (!(entity instanceof Player player)) {
         return Collections.emptyList();
      } else {
         FileConfiguration cfg = this.config();
         ArrayList permissionList = new ArrayList();
         ArrayList permissionListSorted = new ArrayList();

         for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String perm = info.getPermission();
            if (perm.startsWith("graves.permission.")) {
               String key = perm.replace("graves.permission.", "").toLowerCase(Locale.ROOT);
               if (cfg.isConfigurationSection("settings.permission." + key)) {
                  permissionList.add(key);
               }
            }
         }

         ConfigurationSection permissionRoot = cfg.getConfigurationSection("settings.permission");
         if (permissionRoot != null) {
            for (String key : permissionRoot.getKeys(false)) {
               if (permissionList.contains(key)) {
                  permissionListSorted.add(key);
               }
            }
         }

         return permissionListSorted;
      }
   }

   @NotNull
   public File getConfigFolder() {
      return this.paths.configFolder;
   }

   @NotNull
   public File getPluginsFolder() {
      return this.paths.pluginsFolder;
   }

   static final class Defaults {
      private final Graves plugin;

      Defaults(Graves plugin, ConfigManager.IO io) {
         this.plugin = plugin;
      }

      void applyResourceDefaults(@NotNull FileConfiguration cfg, @NotNull String resourcePath) {
         try {
            try (InputStream in = this.plugin.getResource(resourcePath)) {
               if (in != null) {
                  cfg.addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
                  return;
               }
            }
         } catch (IOException var8) {
            this.plugin.logStackTrace((Exception)var8);
         }
      }

      void bakeDefaults(@NotNull FileConfiguration cfg) {
         try {
            cfg.options().copyDefaults(true);
            cfg.loadFromString(cfg.saveToString());
         } catch (InvalidConfigurationException var3) {
         }
      }

      void applyFolderResourceDefaults(@NotNull FileConfiguration merged, @NotNull String fileName) {
         String resource = "config" + File.separator + fileName;

         try {
            try (InputStream in = this.plugin.getResource(resource)) {
               if (in != null) {
                  merged.addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
                  return;
               }
            }
         } catch (IOException var9) {
            this.plugin.logStackTrace((Exception)var9);
         }
      }
   }

   static final class IO {
      private final Graves plugin;

      IO(Graves plugin) {
         this.plugin = plugin;
      }

      @Nullable
      FileConfiguration loadYaml(@NotNull File file) {
         if (!YAMLUtil.isValidYAML(file)) {
            return null;
         } else {
            String jarResource = "config/" + file.getName();
            YAMLUtil.YamlParseError err = YAMLUtil.validateWithBukkit(this.plugin, file, jarResource);
            if (err != null) {
               YAMLUtil.logParseError(this.plugin, file.getName(), err);
               return null;
            } else {
               return YamlConfiguration.loadConfiguration(file);
            }
         }
      }

      @NotNull
      List<File> listFilesSorted(@NotNull File folder) {
         File[] files = folder.listFiles();
         if (files == null) {
            return Collections.emptyList();
         } else {
            Arrays.sort((Object[])files);
            return new ArrayList<>(Arrays.asList(files));
         }
      }
   }

   static final class Merger {
      private final Graves plugin;
      private final ConfigManager.IO io;
      private final ConfigManager.Defaults defaults;

      Merger(Graves plugin, ConfigManager.IO io) {
         this.plugin = plugin;
         this.io = io;
         this.defaults = new ConfigManager.Defaults(plugin, io);
      }

      @NotNull
      FileConfiguration loadMergedFolderConfig(@NotNull File folder) {
         FileConfiguration result = new YamlConfiguration();
         List<File> files = this.io.listFilesSorted(folder);
         if (files.isEmpty()) {
            return result;
         } else {
            File mainConfig = new File(folder, "config.yml");
            if (files.contains(mainConfig)) {
               files.remove(mainConfig);
               files.add(0, mainConfig);
            }

            for (File file : files) {
               if (YAMLUtil.isValidYAML(file)) {
                  if (file.isDirectory()) {
                     result.addDefaults(this.loadMergedFolderConfig(file));
                  } else {
                     FileConfiguration loaded = this.io.loadYaml(file);
                     if (loaded == null) {
                        this.plugin
                           .getLogger()
                           .warning(
                              "Unable to load config "
                                 + file.getName()
                                 + ". Plugin may not function correctly. Do not report as a bug. Correct the following issues above."
                           );
                     } else {
                        result.addDefaults(loaded);
                        this.defaults.bakeDefaults(result);
                        this.defaults.applyFolderResourceDefaults(result, file.getName());
                     }
                  }
               }
            }

            return result;
         }
      }
   }

   static final class Paths {
      final File dataFolder;
      final File pluginsFolder;
      final File configFolder;
      final File outdatedFolder;
      final File legacySingleConfig;
      final List<String> managedConfigFiles = List.of("config.yml", "entity.yml", "grave.yml");

      Paths(Graves plugin) {
         this.dataFolder = plugin.getDataFolder();
         this.pluginsFolder = this.dataFolder.getParentFile();
         this.configFolder = new File(this.dataFolder, "config");
         this.outdatedFolder = new File(this.dataFolder, "outdated");
         this.legacySingleConfig = new File(this.dataFolder, "config.yml");
      }
   }

   static final class Updater {
      private int serverIntegration;
      private final Graves plugin;
      private final ConfigManager.Paths paths;

      Updater(Graves plugin, ConfigManager.Paths paths) {
         this.plugin = plugin;
         this.paths = paths;
      }

      void updateIfNeeded(boolean isDevelopmentBuild) {
         this.serverIntegration = this.setupServerIntegration();
         File configFolder = this.paths.configFolder;
         File mainConfigFile = new File(configFolder, "config.yml");
         if (configFolder.exists()) {
            FileConfiguration mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile);
            int configVersion = mainConfig.getInt("config-version", 0);
            if (configVersion != 25 || isDevelopmentBuild) {
               this.paths.outdatedFolder.mkdirs();
               this.backupOutdatedConfigs(configVersion);
               this.plugin
                  .getLogger()
                  .warning(
                     "Outdated config detected (v"
                        + configVersion
                        + "), current version is (v25). Moving old configs to outdated folder and generating new config files."
                  );
               this.updateConfigFile("config.yml", 25, true);
               this.updateConfigFile("entity.yml", 25, false);
               this.updateConfigFile("grave.yml", 25, false);
            }
         }
      }

      private void backupOutdatedConfigs(double configVersion) {
         for (String name : this.paths.managedConfigFiles) {
            File existing = new File(this.paths.configFolder, name);
            if (existing.exists()) {
               File backup = new File(this.paths.outdatedFolder, name + "-" + configVersion);

               try {
                  Files.copy(existing.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
               } catch (IOException var8) {
                  this.plugin.logStackTrace((Exception)var8);
               }
            }
         }
      }

      private void updateConfigFile(@NotNull String fileName, int currentConfigVersion, boolean shouldUpdateConfigVersion) {
         File configFile = new File(this.paths.dataFolder, "config/" + fileName);
         if (!configFile.exists()) {
            this.plugin.getLogger().severe("File " + configFile.getAbsolutePath() + " does not exist.");
         } else {
            String resourceName = "config/" + fileName;

            try {
               try (InputStream resourceStream = this.plugin.getResource(resourceName)) {
                  if (resourceStream != null) {
                     ConfigUpdater.update(this.plugin, resourceName, configFile, Collections.emptyList());
                     if (shouldUpdateConfigVersion) {
                        FileConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
                        cfg.set("config-version", currentConfigVersion);
                        cfg.save(configFile);
                     }

                     this.plugin.getLogger().info("Config updated: " + configFile.getAbsolutePath());
                     return;
                  }

                  this.plugin.getLogger().warning("Resource " + resourceName + " not found in the JAR.");
               }
            } catch (IOException var11) {
               this.plugin.getLogger().severe("Failed to update " + fileName + ": " + var11.getMessage());
               this.plugin.logStackTrace((Exception)var11);
            }
         }
      }
   }
}
