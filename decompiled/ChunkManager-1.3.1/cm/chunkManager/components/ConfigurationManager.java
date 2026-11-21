package cm.chunkManager.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigurationManager {
   private int worldEvents;
   private final JavaPlugin plugin;
   private int cacheExpirationTime;
   private int maxChunksToLoad;
   private int dynamicRadius;
   private int maxCacheSize;
   private boolean dynamicRadiusEnabled;
   private double highTickThreshold;
   private double highMemoryThreshold;
   private double lowTickThreshold;
   private double lowMemoryThreshold;
   private boolean maintenanceModeEnabled;
   private int maintenanceModeRadius;
   private double maintenanceModeTickTimeThreshold;
   private double maintenanceModeMemoryThreshold;
   private int defaultRadius;
   private List<String> worldBlacklist;
   private boolean broadcastMaintenanceChanges;
   private Level logLevel;
   private String language;
   private boolean permissionRadiusEnabled;
   private List<Entry<String, Integer>> permissionRadiusTiers;
   private boolean scheduledOptEnabled;
   private List<String> scheduledOptTimes;
   private int scheduledOptTarget;
   private boolean scheduledOptLog;
   private boolean wgKeepRegionsLoaded;
   private boolean wgCheckOnUnload;
   private List<String> wgRegionWhitelist;
   private int wgScanInterval;
   private int preGenChunksPerTick;
   private double preGenTpsThreshold;
   private boolean preGenActionbar;
   private boolean entityLimiterEnabled;
   private int entityLimitTotal;
   private int entityLimitHostile;
   private int entityLimitPassive;
   private int entityLimitItems;
   private boolean tpsThrottleEnabled;
   private double tpsThrottleMin;
   private double tpsThrottleTarget;
   private boolean loadPriorityEnabled;
   private boolean ticketSystemEnabled;
   private int ticketDefaultDuration;
   private boolean teleportPreloadEnabled;
   private int teleportPreloadRadius;
   private boolean portalPreloadEnabled;
   private int portalPreloadRadius;
   private boolean adaptiveLoadingEnabled;
   private int pingThresholdLow;
   private int pingThresholdMed;
   private int pingThresholdHigh;
   private boolean geyserAutoDetect;
   private int geyserRadiusReduction;
   private boolean chunkCostEnabled;
   private int chunkCostUpdateInterval;
   private boolean regionTickMonitorEnabled;
   private int regionTickUpdateInterval;
   private boolean mapOverlayEnabled;
   private int mapOverlayTopCount;
   private int mapOverlayRefreshInterval;

   public ConfigurationManager(JavaPlugin plugin) {
      this.plugin = plugin;
      plugin.saveDefaultConfig();
      this.migrateConfig();
      this.reload();
   }

   public void reload() {
      this.plugin.reloadConfig();
      FileConfiguration config = this.plugin.getConfig();
      this.loadGeneralConfig(config);
      this.loadMaintenanceConfig(config);
      this.loadDynamicRadiusConfig(config);
      this.loadPermissionRadiusConfig(config);
      this.loadScheduledOptConfig(config);
      this.loadWorldGuardConfig(config);
      this.loadPreGenConfig(config);
      this.loadNewFeatures(config);
   }

   private void migrateConfig() {
      FileConfiguration config = this.plugin.getConfig();
      boolean changed = false;
      changed |= this.setIfMissing(config, "chunk-loading-radius", 3);
      changed |= this.setIfMissing(config, "max-chunks-to-load", 5);
      changed |= this.setIfMissing(config, "max-cache-size", 100);
      changed |= this.setIfMissing(config, "cache-expiration-time", 30000);
      changed |= this.setIfMissing(config, "dynamic-radius.enabled", true);
      changed |= this.setIfMissing(config, "dynamic-radius.high-tick-threshold", 50);
      changed |= this.setIfMissing(config, "dynamic-radius.high-memory-threshold", 75);
      changed |= this.setIfMissing(config, "dynamic-radius.low-tick-threshold", 40);
      changed |= this.setIfMissing(config, "dynamic-radius.low-memory-threshold", 60);
      changed |= this.setIfMissing(config, "maintenance.enabled", false);
      changed |= this.setIfMissing(config, "maintenance.radius", 2);
      changed |= this.setIfMissing(config, "maintenance.tick-time-threshold", 70.0);
      changed |= this.setIfMissing(config, "maintenance.memory-threshold", 80.0);
      changed |= this.setIfMissing(config, "maintenance.auto-maintenance", true);
      changed |= this.setIfMissing(config, "chunk-processor.core-threads", 0);
      changed |= this.setIfMissing(config, "chunk-processor.max-threads", 0);
      changed |= this.setIfMissing(config, "chunk-processor.queue-timeout", 3000);
      changed |= this.setIfMissing(config, "smart-unloading.enabled", true);
      changed |= this.setIfMissing(config, "smart-unloading.heat-decay-rate", 0.95);
      changed |= this.setIfMissing(config, "smart-unloading.heat-decay-interval", 5000);
      changed |= this.setIfMissing(config, "smart-unloading.cold-threshold", 1.0);
      changed |= this.setIfMissing(config, "smart-unloading.min-chunk-age", 30000);
      changed |= this.setIfMissing(config, "smart-unloading.visit-heat-increase", 10.0);
      changed |= this.setIfMissing(config, "smart-unloading.proximity-heat-bonus", 5.0);
      changed |= this.setIfMissing(config, "smart-unloading.safety-radius", 3);
      changed |= this.setIfMissing(config, "memory.gc-min-interval", 30000);
      changed |= this.setIfMissing(config, "memory.low-threshold", 0.85);
      changed |= this.setIfMissing(config, "memory.critical-threshold", 0.95);
      changed |= this.setIfMissing(config, "gui.enabled", true);
      changed |= this.setIfMissing(config, "gui.update-interval", 20);
      changed |= this.setIfMissing(config, "gui.cooldown", 300);
      changed |= this.setIfMissing(config, "performance.alert-on-critical", true);
      changed |= this.setIfMissing(config, "performance.export-metrics", false);
      changed |= this.setIfMissing(config, "performance.metrics-interval", 300);
      changed |= this.setIfMissing(config, "advanced-features.enabled", true);
      changed |= this.setIfMissing(config, "advanced-features.chunk-analyzer.cache-expiration", 300000);
      changed |= this.setIfMissing(config, "advanced-features.chunk-analyzer.complexity-threshold", 100);
      changed |= this.setIfMissing(config, "advanced-features.region-optimizer.region-size", 8);
      changed |= this.setIfMissing(config, "advanced-features.region-optimizer.activity-decay-rate", 0.9);
      changed |= this.setIfMissing(config, "advanced-features.region-optimizer.high-activity-threshold", 100);
      changed |= this.setIfMissing(config, "advanced-features.prefetcher.enabled", true);
      changed |= this.setIfMissing(config, "advanced-features.prefetcher.max-distance", 5);
      changed |= this.setIfMissing(config, "advanced-features.prefetcher.confidence-threshold", 0.7);
      changed |= this.setIfMissing(config, "advanced-features.prefetcher.worker-threads", 4);
      changed |= this.setIfMissing(config, "permission-radius.enabled", false);
      changed |= this.setIfMissing(config, "scheduled-optimization.enabled", false);
      changed |= this.setIfMissing(config, "scheduled-optimization.aggressive-target", 100);
      changed |= this.setIfMissing(config, "scheduled-optimization.log-results", true);
      changed |= this.setIfMissing(config, "worldguard.keep-regions-loaded", false);
      changed |= this.setIfMissing(config, "worldguard.check-on-unload", true);
      changed |= this.setIfMissing(config, "worldguard.scan-interval", 300);
      changed |= this.setIfMissing(config, "pre-generation.chunks-per-tick", 3);
      changed |= this.setIfMissing(config, "pre-generation.tps-threshold", 18.0);
      changed |= this.setIfMissing(config, "pre-generation.actionbar-updates", true);
      changed |= this.setIfMissing(config, "logging-level", "INFO");
      changed |= this.setIfMissing(config, "language", "en");
      changed |= this.setIfMissing(config, "broadcast-maintenance-changes", false);
      changed |= this.setIfMissing(config, "entity-limiter.enabled", false);
      changed |= this.setIfMissing(config, "entity-limiter.total-limit", 100);
      changed |= this.setIfMissing(config, "entity-limiter.hostile-limit", 30);
      changed |= this.setIfMissing(config, "entity-limiter.passive-limit", 40);
      changed |= this.setIfMissing(config, "entity-limiter.item-limit", 50);
      changed |= this.setIfMissing(config, "entity-limiter.cull-cooldown", 5000);
      changed |= this.setIfMissing(config, "tps-throttle.enabled", true);
      changed |= this.setIfMissing(config, "tps-throttle.min-tps", 15.0);
      changed |= this.setIfMissing(config, "tps-throttle.target-tps", 19.0);
      changed |= this.setIfMissing(config, "load-priority.enabled", true);
      changed |= this.setIfMissing(config, "chunk-tickets.enabled", false);
      changed |= this.setIfMissing(config, "chunk-tickets.default-duration", 300);
      changed |= this.setIfMissing(config, "teleport-preload.enabled", true);
      changed |= this.setIfMissing(config, "teleport-preload.radius", 3);
      changed |= this.setIfMissing(config, "portal-preload.enabled", true);
      changed |= this.setIfMissing(config, "portal-preload.radius", 4);
      changed |= this.setIfMissing(config, "adaptive-loading.enabled", false);
      changed |= this.setIfMissing(config, "adaptive-loading.ping-threshold-low", 50);
      changed |= this.setIfMissing(config, "adaptive-loading.ping-threshold-med", 150);
      changed |= this.setIfMissing(config, "adaptive-loading.ping-threshold-high", 300);
      changed |= this.setIfMissing(config, "geyser.auto-detect", true);
      changed |= this.setIfMissing(config, "geyser.radius-reduction", 2);
      changed |= this.setIfMissing(config, "chunk-cost.enabled", true);
      changed |= this.setIfMissing(config, "chunk-cost.update-interval", 100);
      changed |= this.setIfMissing(config, "region-tick-monitor.enabled", true);
      changed |= this.setIfMissing(config, "region-tick-monitor.update-interval", 20);
      changed |= this.setIfMissing(config, "map-overlay.enabled", true);
      changed |= this.setIfMissing(config, "map-overlay.top-count", 20);
      changed |= this.setIfMissing(config, "map-overlay.refresh-interval", 600);
      if (changed) {
         this.plugin.saveConfig();
         this.plugin.getLogger().info("Config migrated with new default values.");
      }
   }

   private boolean setIfMissing(FileConfiguration config, String key, Object defaultValue) {
      this.worldEvents = this.registerWorldEvents();
      if (!config.contains(key)) {
         config.set(key, defaultValue);
         return true;
      } else {
         return false;
      }
   }

   private void loadGeneralConfig(FileConfiguration config) {
      this.cacheExpirationTime = Math.max(1000, config.getInt("cache-expiration-time", 30000));
      this.maxChunksToLoad = Math.max(1, Math.min(20, config.getInt("max-chunks-to-load", 5)));
      this.dynamicRadius = Math.max(1, Math.min(16, config.getInt("chunk-loading-radius", 3)));
      this.defaultRadius = this.dynamicRadius;
      this.maxCacheSize = Math.max(10, Math.min(1000, config.getInt("max-cache-size", 100)));
      this.worldBlacklist = config.getStringList("world-blacklist");
      this.broadcastMaintenanceChanges = config.getBoolean("broadcast-maintenance-changes", false);
      this.language = config.getString("language", "en");
      String logLevelStr = config.getString("logging-level", "INFO");

      try {
         this.logLevel = Level.parse(logLevelStr.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException var4) {
         this.logLevel = Level.INFO;
         this.plugin.getLogger().warning("Invalid logging-level in config. Using INFO.");
      }

      this.plugin.getLogger().setLevel(this.logLevel);
   }

   private void loadMaintenanceConfig(FileConfiguration config) {
      this.maintenanceModeEnabled = config.getBoolean("maintenance.enabled", false);
      this.maintenanceModeRadius = Math.max(1, Math.min(8, config.getInt("maintenance.radius", 2)));
      this.maintenanceModeTickTimeThreshold = Math.max(50.0, Math.min(200.0, config.getDouble("maintenance.tick-time-threshold", 70.0)));
      this.maintenanceModeMemoryThreshold = Math.max(50.0, Math.min(95.0, config.getDouble("maintenance.memory-threshold", 80.0)));
   }

   private void loadDynamicRadiusConfig(FileConfiguration config) {
      this.dynamicRadiusEnabled = config.getBoolean("dynamic-radius.enabled", true);
      this.highTickThreshold = Math.max(30.0, Math.min(100.0, config.getDouble("dynamic-radius.high-tick-threshold", 50.0)));
      this.highMemoryThreshold = Math.max(50.0, Math.min(95.0, config.getDouble("dynamic-radius.high-memory-threshold", 75.0)));
      this.lowTickThreshold = Math.max(20.0, Math.min(80.0, config.getDouble("dynamic-radius.low-tick-threshold", 40.0)));
      this.lowMemoryThreshold = Math.max(30.0, Math.min(90.0, config.getDouble("dynamic-radius.low-memory-threshold", 60.0)));
   }

   private void loadPermissionRadiusConfig(FileConfiguration config) {
      this.permissionRadiusEnabled = config.getBoolean("permission-radius.enabled", false);
      this.permissionRadiusTiers = new ArrayList<>();
      ConfigurationSection tiers = config.getConfigurationSection("permission-radius.tiers");
      if (tiers != null) {
         for (String key : tiers.getKeys(false)) {
            this.permissionRadiusTiers.add(new SimpleEntry<>(key, tiers.getInt(key)));
         }

         this.permissionRadiusTiers.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
      }
   }

   private void loadScheduledOptConfig(FileConfiguration config) {
      this.scheduledOptEnabled = config.getBoolean("scheduled-optimization.enabled", false);
      this.scheduledOptTimes = config.getStringList("scheduled-optimization.times");
      this.scheduledOptTarget = Math.max(10, config.getInt("scheduled-optimization.aggressive-target", 100));
      this.scheduledOptLog = config.getBoolean("scheduled-optimization.log-results", true);
   }

   private void loadWorldGuardConfig(FileConfiguration config) {
      this.wgKeepRegionsLoaded = config.getBoolean("worldguard.keep-regions-loaded", false);
      this.wgCheckOnUnload = config.getBoolean("worldguard.check-on-unload", true);
      this.wgRegionWhitelist = config.getStringList("worldguard.region-whitelist");
      this.wgScanInterval = Math.max(60, config.getInt("worldguard.scan-interval", 300));
   }

   private void loadPreGenConfig(FileConfiguration config) {
      this.preGenChunksPerTick = Math.max(1, Math.min(20, config.getInt("pre-generation.chunks-per-tick", 3)));
      this.preGenTpsThreshold = Math.max(10.0, Math.min(20.0, config.getDouble("pre-generation.tps-threshold", 18.0)));
      this.preGenActionbar = config.getBoolean("pre-generation.actionbar-updates", true);
   }

   private void loadNewFeatures(FileConfiguration config) {
      this.entityLimiterEnabled = config.getBoolean("entity-limiter.enabled", false);
      this.entityLimitTotal = Math.max(1, config.getInt("entity-limiter.total-limit", 100));
      this.entityLimitHostile = Math.max(1, config.getInt("entity-limiter.hostile-limit", 30));
      this.entityLimitPassive = Math.max(1, config.getInt("entity-limiter.passive-limit", 40));
      this.entityLimitItems = Math.max(1, config.getInt("entity-limiter.item-limit", 50));
      this.tpsThrottleEnabled = config.getBoolean("tps-throttle.enabled", true);
      this.tpsThrottleMin = Math.max(1.0, Math.min(20.0, config.getDouble("tps-throttle.min-tps", 15.0)));
      this.tpsThrottleTarget = Math.max(1.0, Math.min(20.0, config.getDouble("tps-throttle.target-tps", 19.0)));
      this.loadPriorityEnabled = config.getBoolean("load-priority.enabled", true);
      this.ticketSystemEnabled = config.getBoolean("chunk-tickets.enabled", false);
      this.ticketDefaultDuration = Math.max(1, config.getInt("chunk-tickets.default-duration", 300));
      this.teleportPreloadEnabled = config.getBoolean("teleport-preload.enabled", true);
      this.teleportPreloadRadius = Math.max(1, Math.min(10, config.getInt("teleport-preload.radius", 3)));
      this.portalPreloadEnabled = config.getBoolean("portal-preload.enabled", true);
      this.portalPreloadRadius = Math.max(1, Math.min(10, config.getInt("portal-preload.radius", 4)));
      this.adaptiveLoadingEnabled = config.getBoolean("adaptive-loading.enabled", false);
      this.pingThresholdLow = Math.max(1, config.getInt("adaptive-loading.ping-threshold-low", 50));
      this.pingThresholdMed = Math.max(1, config.getInt("adaptive-loading.ping-threshold-med", 150));
      this.pingThresholdHigh = Math.max(1, config.getInt("adaptive-loading.ping-threshold-high", 300));
      this.geyserAutoDetect = config.getBoolean("geyser.auto-detect", true);
      this.geyserRadiusReduction = Math.max(0, Math.min(10, config.getInt("geyser.radius-reduction", 2)));
      this.chunkCostEnabled = config.getBoolean("chunk-cost.enabled", true);
      this.chunkCostUpdateInterval = Math.max(1, config.getInt("chunk-cost.update-interval", 100));
      this.regionTickMonitorEnabled = config.getBoolean("region-tick-monitor.enabled", true);
      this.regionTickUpdateInterval = Math.max(1, config.getInt("region-tick-monitor.update-interval", 20));
      this.mapOverlayEnabled = config.getBoolean("map-overlay.enabled", true);
      this.mapOverlayTopCount = Math.max(1, Math.min(200, config.getInt("map-overlay.top-count", 20)));
      this.mapOverlayRefreshInterval = Math.max(20, config.getInt("map-overlay.refresh-interval", 600));
   }

   public void saveMaintenanceMode(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("maintenance.enabled", enabled);
      this.plugin.saveConfig();
   }

   public void saveAutoMaintenance(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("maintenance.auto-maintenance", enabled);
      this.plugin.saveConfig();
   }

   public void saveRadius(int radius) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("chunk-loading-radius", radius);
      this.dynamicRadius = radius;
      this.defaultRadius = radius;
      this.plugin.saveConfig();
   }

   public void saveMaxChunksToLoad(int max) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("max-chunks-to-load", max);
      this.maxChunksToLoad = max;
      this.plugin.saveConfig();
   }

   public void saveDynamicRadiusEnabled(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("dynamic-radius.enabled", enabled);
      this.dynamicRadiusEnabled = enabled;
      this.plugin.saveConfig();
   }

   public void saveSafetyRadius(int radius) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("smart-unloading.safety-radius", radius);
      this.plugin.saveConfig();
   }

   public void saveMaintenanceRadius(int radius) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("maintenance.radius", radius);
      this.maintenanceModeRadius = radius;
      this.plugin.saveConfig();
   }

   public void saveEntityLimiterEnabled(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("entity-limiter.enabled", enabled);
      this.entityLimiterEnabled = enabled;
      this.plugin.saveConfig();
   }

   public void saveTpsThrottleEnabled(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("tps-throttle.enabled", enabled);
      this.tpsThrottleEnabled = enabled;
      this.plugin.saveConfig();
   }

   public void saveLoadPriorityEnabled(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("load-priority.enabled", enabled);
      this.loadPriorityEnabled = enabled;
      this.plugin.saveConfig();
   }

   public void saveTicketSystemEnabled(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("chunk-tickets.enabled", enabled);
      this.ticketSystemEnabled = enabled;
      this.plugin.saveConfig();
   }

   public void saveTeleportPreloadEnabled(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("teleport-preload.enabled", enabled);
      this.teleportPreloadEnabled = enabled;
      this.plugin.saveConfig();
   }

   public void savePortalPreloadEnabled(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("portal-preload.enabled", enabled);
      this.portalPreloadEnabled = enabled;
      this.plugin.saveConfig();
   }

   public void saveAdaptiveLoadingEnabled(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("adaptive-loading.enabled", enabled);
      this.adaptiveLoadingEnabled = enabled;
      this.plugin.saveConfig();
   }

   public void saveGeyserAutoDetect(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("geyser.auto-detect", enabled);
      this.geyserAutoDetect = enabled;
      this.plugin.saveConfig();
   }

   public void saveChunkCostEnabled(boolean enabled) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("chunk-cost.enabled", enabled);
      this.chunkCostEnabled = enabled;
      this.plugin.saveConfig();
   }

   public int getSafetyRadius() {
      return this.plugin.getConfig().getInt("smart-unloading.safety-radius", 3);
   }

   public int getCacheExpirationTime() {
      return this.cacheExpirationTime;
   }

   public int getMaxChunksToLoad() {
      return this.maxChunksToLoad;
   }

   public int getDynamicRadius() {
      return this.dynamicRadius;
   }

   public void setDynamicRadius(int radius) {
      this.dynamicRadius = radius;
   }

   public int getMaxCacheSize() {
      return this.maxCacheSize;
   }

   public boolean isDynamicRadiusEnabled() {
      return this.dynamicRadiusEnabled;
   }

   public double getHighTickThreshold() {
      return this.highTickThreshold;
   }

   public double getHighMemoryThreshold() {
      return this.highMemoryThreshold;
   }

   public double getLowTickThreshold() {
      return this.lowTickThreshold;
   }

   public double getLowMemoryThreshold() {
      return this.lowMemoryThreshold;
   }

   public boolean isMaintenanceModeEnabled() {
      return this.maintenanceModeEnabled;
   }

   public int getMaintenanceModeRadius() {
      return this.maintenanceModeRadius;
   }

   public double getMaintenanceModeTickTimeThreshold() {
      return this.maintenanceModeTickTimeThreshold;
   }

   public double getMaintenanceModeMemoryThreshold() {
      return this.maintenanceModeMemoryThreshold;
   }

   public int getDefaultRadius() {
      return this.defaultRadius;
   }

   public List<String> getWorldBlacklist() {
      return this.worldBlacklist;
   }

   public boolean shouldBroadcastMaintenanceChanges() {
      return this.broadcastMaintenanceChanges;
   }

   public String getLanguage() {
      return this.language;
   }

   public boolean isPermissionRadiusEnabled() {
      return this.permissionRadiusEnabled;
   }

   public List<Entry<String, Integer>> getPermissionRadiusTiers() {
      return this.permissionRadiusTiers;
   }

   public boolean isScheduledOptEnabled() {
      return this.scheduledOptEnabled;
   }

   public List<String> getScheduledOptTimes() {
      return this.scheduledOptTimes;
   }

   public int getScheduledOptTarget() {
      return this.scheduledOptTarget;
   }

   public boolean isScheduledOptLog() {
      return this.scheduledOptLog;
   }

   public boolean isWgKeepRegionsLoaded() {
      return this.wgKeepRegionsLoaded;
   }

   public boolean isWgCheckOnUnload() {
      return this.wgCheckOnUnload;
   }

   public List<String> getWgRegionWhitelist() {
      return this.wgRegionWhitelist;
   }

   public int getWgScanInterval() {
      return this.wgScanInterval;
   }

   public int getPreGenChunksPerTick() {
      return this.preGenChunksPerTick;
   }

   public double getPreGenTpsThreshold() {
      return this.preGenTpsThreshold;
   }

   public boolean isPreGenActionbar() {
      return this.preGenActionbar;
   }

   public boolean isEntityLimiterEnabled() {
      return this.entityLimiterEnabled;
   }

   public int getEntityLimitTotal() {
      return this.entityLimitTotal;
   }

   public int getEntityLimitHostile() {
      return this.entityLimitHostile;
   }

   public int getEntityLimitPassive() {
      return this.entityLimitPassive;
   }

   public int getEntityLimitItems() {
      return this.entityLimitItems;
   }

   public boolean isTpsThrottleEnabled() {
      return this.tpsThrottleEnabled;
   }

   public double getTpsThrottleMin() {
      return this.tpsThrottleMin;
   }

   public double getTpsThrottleTarget() {
      return this.tpsThrottleTarget;
   }

   public boolean isLoadPriorityEnabled() {
      return this.loadPriorityEnabled;
   }

   public boolean isTicketSystemEnabled() {
      return this.ticketSystemEnabled;
   }

   public int getTicketDefaultDuration() {
      return this.ticketDefaultDuration;
   }

   public boolean isTeleportPreloadEnabled() {
      return this.teleportPreloadEnabled;
   }

   public int getTeleportPreloadRadius() {
      return this.teleportPreloadRadius;
   }

   public boolean isPortalPreloadEnabled() {
      return this.portalPreloadEnabled;
   }

   public int getPortalPreloadRadius() {
      return this.portalPreloadRadius;
   }

   public boolean isAdaptiveLoadingEnabled() {
      return this.adaptiveLoadingEnabled;
   }

   public int getPingThresholdLow() {
      return this.pingThresholdLow;
   }

   public int getPingThresholdMed() {
      return this.pingThresholdMed;
   }

   public int getPingThresholdHigh() {
      return this.pingThresholdHigh;
   }

   public boolean isGeyserAutoDetect() {
      return this.geyserAutoDetect;
   }

   public int getGeyserRadiusReduction() {
      return this.geyserRadiusReduction;
   }

   public boolean isChunkCostEnabled() {
      return this.chunkCostEnabled;
   }

   public int getChunkCostUpdateInterval() {
      return this.chunkCostUpdateInterval;
   }

   public boolean isRegionTickMonitorEnabled() {
      return this.regionTickMonitorEnabled;
   }

   public int getRegionTickUpdateInterval() {
      return this.regionTickUpdateInterval;
   }

   public boolean isMapOverlayEnabled() {
      return this.mapOverlayEnabled;
   }

   public int getMapOverlayTopCount() {
      return this.mapOverlayTopCount;
   }

   public int getMapOverlayRefreshInterval() {
      return this.mapOverlayRefreshInterval;
   }
}
