package cm.chunkManager.components;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldSettingsManager {
   private final JavaPlugin plugin;
   private final Map<String, WorldSettingsManager.WorldSettings> worldSettings;
   private final File settingsFile;
   private YamlConfiguration config;
   private Plugin multiverseCore;
   private boolean multiverseEnabled;

   public WorldSettingsManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.worldSettings = new HashMap<>();
      this.settingsFile = new File(plugin.getDataFolder(), "worlds.yml");
      this.detectMultiverse();
      this.loadSettings();
   }

   private void detectMultiverse() {
      this.multiverseCore = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
      this.multiverseEnabled = this.multiverseCore != null && this.multiverseCore.isEnabled();
      if (this.multiverseEnabled) {
         this.plugin.getLogger().info("Multiverse-Core detected - integration enabled");
      }
   }

   public boolean isMultiverseEnabled() {
      return this.multiverseEnabled;
   }

   public void loadSettings() {
      if (!this.settingsFile.exists()) {
         this.createDefaultSettings();
      }

      this.config = YamlConfiguration.loadConfiguration(this.settingsFile);
      this.worldSettings.clear();
      ConfigurationSection worlds = this.config.getConfigurationSection("worlds");
      if (worlds != null) {
         for (String worldName : worlds.getKeys(false)) {
            ConfigurationSection ws = worlds.getConfigurationSection(worldName);
            if (ws != null) {
               WorldSettingsManager.WorldSettings settings = new WorldSettingsManager.WorldSettings(
                  ws.getInt("chunk-radius", 3),
                  ws.getInt("max-chunks-per-tick", 5),
                  ws.getInt("safety-radius", 3),
                  ws.getBoolean("enabled", true),
                  ws.getBoolean("smart-unloading", true),
                  ws.getInt("priority", 5)
               );
               this.worldSettings.put(worldName, settings);
            }
         }

         this.syncWithServer();
      }
   }

   private void syncWithServer() {
      for (World world : Bukkit.getWorlds()) {
         if (!this.worldSettings.containsKey(world.getName())) {
            this.worldSettings.put(world.getName(), this.createDefaultForWorld(world));
         }
      }
   }

   private WorldSettingsManager.WorldSettings createDefaultForWorld(World world) {
      WorldSettingsManager.WorldSettings settings = new WorldSettingsManager.WorldSettings();
      switch (world.getEnvironment()) {
         case NETHER:
            settings.chunkRadius = 2;
            settings.maxChunksPerTick = 3;
            settings.priority = 3;
            break;
         case THE_END:
            settings.chunkRadius = 2;
            settings.maxChunksPerTick = 3;
            settings.priority = 2;
            break;
         default:
            if (world.getName().equalsIgnoreCase("world")) {
               settings.priority = 10;
            }
      }

      return settings;
   }

   private void createDefaultSettings() {
      this.config = new YamlConfiguration();

      for (World world : Bukkit.getWorlds()) {
         WorldSettingsManager.WorldSettings settings = this.createDefaultForWorld(world);
         this.saveWorldToConfig(world.getName(), settings);
      }

      this.saveConfig();
   }

   private void saveWorldToConfig(String worldName, WorldSettingsManager.WorldSettings settings) {
      String path = "worlds." + worldName;
      this.config.set(path + ".enabled", settings.enabled);
      this.config.set(path + ".chunk-radius", settings.chunkRadius);
      this.config.set(path + ".max-chunks-per-tick", settings.maxChunksPerTick);
      this.config.set(path + ".safety-radius", settings.safetyRadius);
      this.config.set(path + ".smart-unloading", settings.smartUnloading);
      this.config.set(path + ".priority", settings.priority);
   }

   public void saveSettings() {
      for (Entry<String, WorldSettingsManager.WorldSettings> entry : this.worldSettings.entrySet()) {
         this.saveWorldToConfig(entry.getKey(), entry.getValue());
      }

      this.saveConfig();
   }

   public void saveWorld(String worldName) {
      WorldSettingsManager.WorldSettings settings = this.worldSettings.get(worldName);
      if (settings != null) {
         this.saveWorldToConfig(worldName, settings);
         this.saveConfig();
      }
   }

   private void saveConfig() {
      try {
         this.config.save(this.settingsFile);
      } catch (IOException var2) {
         this.plugin.getLogger().warning("Failed to save world settings: " + var2.getMessage());
      }
   }

   public WorldSettingsManager.WorldSettings getSettings(World world) {
      return this.getSettings(world.getName());
   }

   public WorldSettingsManager.WorldSettings getSettings(String worldName) {
      return this.worldSettings.computeIfAbsent(worldName, k -> new WorldSettingsManager.WorldSettings());
   }

   public void setSettings(String worldName, WorldSettingsManager.WorldSettings settings) {
      this.worldSettings.put(worldName, settings);
      this.saveWorld(worldName);
   }

   public void updateRadius(String worldName, int radius) {
      WorldSettingsManager.WorldSettings settings = this.getSettings(worldName);
      settings.chunkRadius = Math.max(1, Math.min(10, radius));
      this.saveWorld(worldName);
   }

   public void updateMaxChunks(String worldName, int max) {
      WorldSettingsManager.WorldSettings settings = this.getSettings(worldName);
      settings.maxChunksPerTick = Math.max(1, Math.min(20, max));
      this.saveWorld(worldName);
   }

   public void updateSafetyRadius(String worldName, int radius) {
      WorldSettingsManager.WorldSettings settings = this.getSettings(worldName);
      settings.safetyRadius = Math.max(1, Math.min(8, radius));
      this.saveWorld(worldName);
   }

   public void updateEnabled(String worldName, boolean enabled) {
      WorldSettingsManager.WorldSettings settings = this.getSettings(worldName);
      settings.enabled = enabled;
      this.saveWorld(worldName);
   }

   public void updateSmartUnloading(String worldName, boolean enabled) {
      WorldSettingsManager.WorldSettings settings = this.getSettings(worldName);
      settings.smartUnloading = enabled;
      this.saveWorld(worldName);
   }

   public void updatePriority(String worldName, int priority) {
      WorldSettingsManager.WorldSettings settings = this.getSettings(worldName);
      settings.priority = Math.max(1, Math.min(10, priority));
      this.saveWorld(worldName);
   }

   public Set<String> getConfiguredWorlds() {
      return this.worldSettings.keySet();
   }

   public int getEffectiveRadius(World world) {
      WorldSettingsManager.WorldSettings settings = this.getSettings(world);
      return !settings.enabled ? 0 : settings.chunkRadius;
   }

   public boolean isWorldEnabled(World world) {
      return this.getSettings(world).enabled;
   }

   public boolean isSmartUnloadingEnabled(World world) {
      WorldSettingsManager.WorldSettings settings = this.getSettings(world);
      return settings.enabled && settings.smartUnloading;
   }

   public String getWorldDisplayName(World world) {
      if (this.multiverseEnabled) {
         try {
            Object mvWorld = this.multiverseCore.getClass().getMethod("getMVWorldManager").invoke(this.multiverseCore);
            Object mvWorldObj = mvWorld.getClass().getMethod("getMVWorld", World.class).invoke(mvWorld, world);
            if (mvWorldObj != null) {
               return (String)mvWorldObj.getClass().getMethod("getAlias").invoke(mvWorldObj);
            }
         } catch (Exception var4) {
         }
      }

      return world.getName();
   }

   public String getWorldType(World world) {
      if (this.multiverseEnabled) {
         try {
            Object mvWorld = this.multiverseCore.getClass().getMethod("getMVWorldManager").invoke(this.multiverseCore);
            Object mvWorldObj = mvWorld.getClass().getMethod("getMVWorld", World.class).invoke(mvWorld, world);
            if (mvWorldObj != null) {
               Object envType = mvWorldObj.getClass().getMethod("getEnvironment").invoke(mvWorldObj);
               return envType.toString();
            }
         } catch (Exception var5) {
         }
      }

      return world.getEnvironment().name();
   }

   public void reload() {
      this.loadSettings();
   }

   public static class WorldSettings {
      public int chunkRadius;
      public int maxChunksPerTick;
      public int safetyRadius;
      public boolean enabled;
      public boolean smartUnloading;
      public int priority;

      public WorldSettings() {
         this.chunkRadius = 3;
         this.maxChunksPerTick = 5;
         this.safetyRadius = 3;
         this.enabled = true;
         this.smartUnloading = true;
         this.priority = 5;
      }

      public WorldSettings(int radius, int maxChunks, int safety, boolean enabled, boolean smart, int priority) {
         this.chunkRadius = radius;
         this.maxChunksPerTick = maxChunks;
         this.safetyRadius = safety;
         this.enabled = enabled;
         this.smartUnloading = smart;
         this.priority = priority;
      }
   }
}
