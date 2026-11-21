package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.api.TeleportService;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.biome.BiomePreCacheSettings;
import com.skyblockexp.ezrtp.config.biome.ChunkyIntegrationSettings;
import com.skyblockexp.ezrtp.config.biome.RareBiomeOptimizationSettings;
import com.skyblockexp.ezrtp.config.network.TeleportQueueSettings;
import com.skyblockexp.ezrtp.config.teleport.ChunkLoadingSettings;
import com.skyblockexp.ezrtp.config.teleport.SearchPattern;
import com.skyblockexp.ezrtp.economy.EconomyService;
import com.skyblockexp.ezrtp.integration.EzCountdownDisplayBridge;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.performance.PerformanceMonitor;
import com.skyblockexp.ezrtp.platform.ChunkLoadStrategy;
import com.skyblockexp.ezrtp.platform.PlatformRuntime;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeCapabilitiesDetector;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.protection.ProtectionRegistry;
import com.skyblockexp.ezrtp.pvptag.PvpTagService;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.storage.HotspotStorage;
import com.skyblockexp.ezrtp.storage.MySqlHotspotStorage;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import com.skyblockexp.ezrtp.teleport.biome.WeightedRareBiomeStrategy;
import com.skyblockexp.ezrtp.teleport.queue.ChunkLoadQueue;
import com.skyblockexp.ezrtp.teleport.queue.TeleportQueueManager;
import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.CircularSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.DiamondSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.SquareSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.TriangleSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationMonitor;
import com.skyblockexp.ezrtp.util.DebugFileLogger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class RandomTeleportService implements TeleportService {
   private final JavaPlugin plugin;
   private final RtpStatistics statistics = new RtpStatistics();
   private final BiomeLocationCache biomeCache;
   private final RareBiomeRegistry rareBiomeRegistry;
   private final ChunkLoadQueue chunkLoadQueue;
   private final LocationValidator locationValidator;
   private final LocationFinder locationFinder;
   private final CountdownManager countdownManager;
   private final TeleportQueueManager queueManager;
   private final TeleportCostCalculator costCalculator;
   private final TeleportExecutor teleportExecutor;
   private final DebugFileLogger debugFileLogger;
   private final ExecutorService biomeFilterExecutor;
   private final ChunkyProvider chunkyAPI;
   private final ChunkyWarmupCoordinator chunkyWarmupCoordinator;
   private final HotspotStorage hotspotStorage;
   private final PvpTagService pvpTagService;
   private RandomTeleportSettings settings;
   private TeleportQueueSettings queueSettings;
   private BiomeSearchStrategy searchStrategy;

   public RandomTeleportService(
      JavaPlugin plugin,
      RandomTeleportSettings settings,
      TeleportQueueSettings queueSettings,
      EconomyService economyService,
      BiFunction<Player, RandomTeleportSettings, Double> costResolver,
      ProtectionRegistry protectionRegistry,
      MessageProvider messageProvider,
      ChunkLoadStrategy chunkLoadStrategy,
      PlatformRuntime platformRuntime,
      ChunkyProvider chunkyAPI,
      ChunkyWarmupCoordinator chunkyWarmupCoordinator,
      PvpTagService pvpTagService
   ) {
      this.plugin = plugin;
      this.chunkyAPI = chunkyAPI;
      this.chunkyWarmupCoordinator = chunkyWarmupCoordinator;
      this.pvpTagService = pvpTagService;
      this.debugFileLogger = new DebugFileLogger(plugin);
      this.settings = settings;
      this.queueSettings = queueSettings != null ? queueSettings : TeleportQueueSettings.disabled();
      this.biomeCache = this.createBiomeCache(settings, platformRuntime.scheduler());
      RareBiomeOptimizationSettings rareSettings = settings != null ? settings.getRareBiomeOptimizationSettings() : null;
      this.hotspotStorage = this.createHotspotStorage(rareSettings);
      this.rareBiomeRegistry = new RareBiomeRegistry(plugin, rareSettings != null ? rareSettings.getRareBiomes() : null, this.hotspotStorage);
      if (this.hotspotStorage != null) {
         platformRuntime.scheduler().executeAsync(() -> this.rareBiomeRegistry.loadFromStorage(this.hotspotStorage));
      }

      this.chunkLoadQueue = new ChunkLoadQueue(plugin, chunkLoadStrategy, platformRuntime.scheduler());
      this.chunkLoadQueue.setStatistics(this.statistics);
      this.locationValidator = new LocationValidator(plugin, protectionRegistry);
      this.searchStrategy = this.createPatternStrategy(settings);
      this.biomeFilterExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "ezrtp-biome-filter"));
      this.locationFinder = new LocationFinder(
         plugin,
         this.statistics,
         this.biomeCache,
         this.rareBiomeRegistry,
         this.chunkLoadQueue,
         this.locationValidator,
         this.searchStrategy,
         platformRuntime,
         chunkyAPI,
         chunkyWarmupCoordinator,
         this.biomeFilterExecutor
      );
      this.countdownManager = new CountdownManager(plugin, platformRuntime.scheduler(), messageProvider, pvpTagService);
      this.queueManager = new TeleportQueueManager(plugin, platformRuntime.scheduler(), this.queueSettings, messageProvider);
      this.queueManager.setPvpTagService(pvpTagService);
      this.costCalculator = new TeleportCostCalculator(economyService, costResolver);
      this.teleportExecutor = new TeleportExecutor(
         plugin,
         platformRuntime.scheduler(),
         messageProvider,
         this.statistics,
         this.costCalculator,
         this.countdownManager,
         this.locationFinder,
         this.queueManager,
         () -> this.settings
      );
      this.applyRareBiomeSettings(this.settings);
      this.applyCacheSettings(this.settings);
   }

   public CompletableFuture<Location> generateSafeLocationForChunk(World world, int chunkX, int chunkZ, RandomTeleportSettings teleportSettings) {
      return world != null && teleportSettings != null
         ? this.locationFinder.generateSafeLocationForChunk(world, chunkX, chunkZ, teleportSettings)
         : CompletableFuture.completedFuture(null);
   }

   public void reload(RandomTeleportSettings newSettings, TeleportQueueSettings newQueueSettings) {
      this.settings = newSettings;
      this.queueSettings = newQueueSettings != null ? newQueueSettings : TeleportQueueSettings.disabled();
      this.queueManager.reload(this.queueSettings);
      this.applyRareBiomeSettings(newSettings);
      this.applyCacheSettings(newSettings);
   }

   public void setEconomyService(EconomyService economyService) {
      this.costCalculator.setEconomyService(economyService);
   }

   public void setCostResolver(BiFunction<Player, RandomTeleportSettings, Double> costResolver) {
      this.costCalculator.setCostResolver(costResolver);
   }

   public void setProtectionRegistry(ProtectionRegistry protectionRegistry) {
      this.locationValidator.setProtectionRegistry(protectionRegistry);
   }

   public void setEzCountdownBridge(EzCountdownDisplayBridge bridge) {
      this.countdownManager.setEzCountdownBridge(bridge);
   }

   public RtpStatistics getStatistics() {
      return this.statistics;
   }

   public BiomeLocationCache getBiomeCache() {
      return this.biomeCache;
   }

   public RareBiomeRegistry getRareBiomeRegistry() {
      return this.rareBiomeRegistry;
   }

   public ChunkLoadQueue getChunkLoadQueue() {
      return this.chunkLoadQueue;
   }

   public void setPerformanceMonitor(PerformanceMonitor performanceMonitor) {
      this.teleportExecutor.setPerformanceMonitor(performanceMonitor);
   }

   public void setUnsafeLocationMonitor(UnsafeLocationMonitor monitor) {
      this.locationFinder.setUnsafeLocationMonitor(monitor);
   }

   public void shutdown() {
      this.teleportExecutor.shutdown();
      this.biomeCache.shutdown();
      this.rareBiomeRegistry.shutdown();
      this.chunkLoadQueue.shutdown();
      this.biomeFilterExecutor.shutdown();
      if (this.hotspotStorage != null) {
         try {
            this.hotspotStorage.close();
         } catch (Exception var2) {
            this.plugin.getLogger().warning("Failed to close hotspot storage: " + var2.getMessage());
         }
      }
   }

   public CompletableFuture<Location> generateSafeLocationForCache(World world, RandomTeleportSettings teleportSettings) {
      return this.locationFinder.generateSafeLocationForCache(world, teleportSettings);
   }

   @Override
   public void teleportPlayer(@NotNull Player player, @NotNull TeleportReason reason) {
      this.teleportExecutor.teleportPlayer(player, reason);
   }

   @Override
   public void teleportPlayer(@NotNull Player player, Object settings, @NotNull TeleportReason reason) {
      if (settings instanceof RandomTeleportSettings) {
         this.teleportPlayer(player, (RandomTeleportSettings)settings, reason);
      } else {
         this.teleportPlayer(player, reason);
      }
   }

   public void teleportPlayer(@NotNull Player player, RandomTeleportSettings teleportSettings, @NotNull TeleportReason reason) {
      this.teleportExecutor.teleportPlayer(player, teleportSettings, reason);
   }

   @Override
   public void teleportPlayer(@NotNull Player player, @NotNull TeleportReason reason, Consumer<Boolean> callback) {
      this.teleportExecutor.teleportPlayer(player, reason, callback);
   }

   public void teleportPlayer(@NotNull Player player, RandomTeleportSettings teleportSettings, @NotNull TeleportReason reason, Consumer<Boolean> callback) {
      this.teleportExecutor.teleportPlayer(player, teleportSettings, reason, callback);
   }

   @Override
   public void teleportPlayer(@NotNull Player player, Object settings, @NotNull TeleportReason reason, Consumer<Boolean> callback) {
      if (settings instanceof RandomTeleportSettings) {
         this.teleportPlayer(player, (RandomTeleportSettings)settings, reason, callback);
      } else {
         this.teleportPlayer(player, reason, callback);
      }
   }

   private void applyCacheSettings(RandomTeleportSettings currentSettings) {
      if (currentSettings != null && currentSettings.getPreCacheSettings() != null) {
         if (!currentSettings.isBiomeSystemEnabled()) {
            this.biomeCache.setEnabled(false);
            return;
         }

         boolean shouldEnable = currentSettings.getPreCacheSettings().isEnabled();
         boolean hasBiomeFilters = LocationValidator.hasBiomeFilters(currentSettings);
         Runtime runtime = Runtime.getRuntime();
         long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
         long totalMemoryMb = runtime.totalMemory() / 1048576L;
         if (freeMemoryMb < 512L && shouldEnable) {
            this.plugin.getLogger().warning(String.format("Disabling biome pre-cache due to low memory: %dMB free / %dMB total", freeMemoryMb, totalMemoryMb));
            shouldEnable = false;
         }

         if (!shouldEnable && hasBiomeFilters && currentSettings.getPreCacheSettings().isAutoEnableForFilters()) {
            if (freeMemoryMb >= 512L) {
               shouldEnable = true;
               this.plugin
                  .getLogger()
                  .info(
                     String.format(
                        "Biome filters detected; auto-enabling pre-cache as configured (Memory: %dMB free / %dMB total)", freeMemoryMb, totalMemoryMb
                     )
                  );
            } else {
               this.plugin
                  .getLogger()
                  .warning(
                     String.format("Biome filters detected but pre-cache not enabled due to low memory: %dMB free / %dMB total", freeMemoryMb, totalMemoryMb)
                  );
            }
         }

         if (hasBiomeFilters && !shouldEnable) {
            this.plugin.getLogger().warning("Biome filtering is configured but pre-caching is disabled.");
            this.plugin.getLogger().warning("RECOMMENDATION: Enable 'biomes.pre-cache.enabled: true' in config.yml to improve RTP success rates.");
         }

         this.biomeCache.setEnabled(shouldEnable);
         if (shouldEnable && hasBiomeFilters) {
            World world = Bukkit.getWorld(currentSettings.getWorldName());
            if (world != null) {
               if (this.chunkyAPI != null && currentSettings.getChunkyIntegrationSettings().isEnabled()) {
                  if (this.chunkyWarmupCoordinator != null && !this.chunkyWarmupCoordinator.hasSufficientMemory()) {
                     this.plugin.getLogger().info("Skipping Chunky pregeneration for biome pre-caching due to low memory");
                  } else {
                     String worldName = world.getName();

                     try {
                        if (!this.chunkyAPI.isRunning(worldName)) {
                           int radius = Math.max(10000, (int)(world.getWorldBorder().getSize() / 2.0));
                           boolean started = this.chunkyAPI
                              .startTask(
                                 worldName,
                                 currentSettings.getChunkyIntegrationSettings().getShape(),
                                 0.0,
                                 0.0,
                                 radius,
                                 radius,
                                 currentSettings.getChunkyIntegrationSettings().getPattern()
                              );
                           if (started) {
                              this.plugin.getLogger().info("Started Chunky pregeneration for world '" + worldName + "' to support biome pre-caching.");
                           }
                        }
                     } catch (Throwable var13) {
                        this.plugin.getLogger().warning("Failed to interact with Chunky API: " + var13.getMessage());
                     }
                  }
               }

               this.plugin
                  .getLogger()
                  .info(
                     String.format("Starting biome cache warmup for world '%s' (Memory: %dMB free / %dMB total)", world.getName(), freeMemoryMb, totalMemoryMb)
                  );
               this.biomeCache.startWarmup(world, currentSettings, this);
            }
         }
      } else {
         this.biomeCache.setEnabled(false);
      }
   }

   private void applyRareBiomeSettings(RandomTeleportSettings currentSettings) {
      RareBiomeOptimizationSettings rareSettings = currentSettings != null ? currentSettings.getRareBiomeOptimizationSettings() : null;
      boolean chunkQueueOverridden = false;
      BiomeSearchStrategy patternStrategy = this.createPatternStrategy(currentSettings);
      if (currentSettings != null && !currentSettings.isBiomeSystemEnabled()) {
         this.rareBiomeRegistry.setEnabled(false);
         this.rareBiomeRegistry.setBackgroundScanningEnabled(false);
         this.searchStrategy = patternStrategy;
         this.locationFinder.setSearchStrategy(this.searchStrategy);
      } else {
         if (rareSettings != null) {
            boolean hasBiomeFilters = LocationValidator.hasBiomeFilters(currentSettings);
            boolean enabled = rareSettings.isEnabled();
            if (!enabled && hasBiomeFilters && rareSettings.isAutoEnableForFilters()) {
               enabled = true;
               this.plugin.getLogger().info("Biome filters detected; auto-enabling rare biome optimizations as configured.");
            }

            this.rareBiomeRegistry.setEnabled(enabled && rareSettings.isHotspotTrackingEnabled());
            this.rareBiomeRegistry.setBackgroundScanningEnabled(enabled && rareSettings.isBackgroundScanningEnabled());
            if (enabled && rareSettings.useChunkLoadQueue()) {
               this.chunkLoadQueue.configure(rareSettings.getChunkProcessingIntervalTicks(), rareSettings.getMaxChunksPerTick());
               this.chunkLoadQueue.setEnabled(true);
               chunkQueueOverridden = true;
            }

            if (enabled && rareSettings.useWeightedSearch()) {
               this.searchStrategy = new WeightedRareBiomeStrategy(patternStrategy);
            } else {
               this.searchStrategy = patternStrategy;
            }
         } else {
            this.rareBiomeRegistry.setEnabled(false);
            this.rareBiomeRegistry.setBackgroundScanningEnabled(false);
            this.searchStrategy = patternStrategy;
         }

         if (!chunkQueueOverridden) {
            this.applyChunkLoadingSettings(currentSettings);
         }

         this.locationFinder.setSearchStrategy(this.searchStrategy);
         this.logSearchConfiguration(currentSettings, patternStrategy, this.searchStrategy);
      }
   }

   private void applyChunkLoadingSettings(RandomTeleportSettings currentSettings) {
      ChunkLoadingSettings chunkSettings = currentSettings != null ? currentSettings.getChunkLoadingSettings() : ChunkLoadingSettings.defaults();
      if (chunkSettings == null) {
         chunkSettings = ChunkLoadingSettings.defaults();
      }

      ChunkLoadingSettings.PaperAsyncApiMode mode = chunkSettings.getUsePaperAsyncApi();

      boolean usePaperAsync = switch (mode) {
         case ALWAYS -> true;
         case NEVER -> false;
         case AUTO_DETECT -> PlatformRuntimeCapabilitiesDetector.isPaper121PlusAsync(this.plugin.getClass().getClassLoader());
      };
      if (usePaperAsync) {
         this.chunkLoadQueue.setAsyncPassthrough(true);
         this.chunkLoadQueue.setEnabled(true);
         if (mode == ChunkLoadingSettings.PaperAsyncApiMode.ALWAYS
            && !PlatformRuntimeCapabilitiesDetector.detect(this.plugin.getClass().getClassLoader()).paperApi()) {
            this.plugin
               .getLogger()
               .warning(
                  "chunk-loading.use-paper-async-api is set to 'always' but Paper API was not detected. The async fast-path will still run but chunk loading will not be genuinely non-blocking."
               );
         } else {
            this.plugin.getLogger().info("Chunk loading: Paper async API");
         }
      } else {
         this.chunkLoadQueue.setAsyncPassthrough(false);
         ChunkLoadingSettings.LegacyThrottleSettings legacy = chunkSettings.getLegacyThrottle();
         if (legacy.isEnabled()) {
            this.chunkLoadQueue.configure(legacy.getProcessingIntervalTicks(), legacy.getMaxChunksPerTick());
            this.chunkLoadQueue.setEnabled(true);
         } else {
            this.chunkLoadQueue.setEnabled(false);
         }
      }

      if (currentSettings != null && currentSettings.getChunkyIntegrationSettings() != null) {
         ChunkyIntegrationSettings chunkySettings = currentSettings.getChunkyIntegrationSettings();
         if (chunkySettings.isMemorySafetyEnabled()) {
            this.chunkLoadQueue.configureMemorySafety(chunkySettings.getMinFreeMemoryMb());
         }
      }
   }

   private BiomeSearchStrategy createPatternStrategy(RandomTeleportSettings currentSettings) {
      SearchPattern pattern = currentSettings != null ? currentSettings.getSearchPattern() : SearchPattern.RANDOM;

      return (BiomeSearchStrategy)(switch (pattern) {
         case CIRCLE -> new CircularSearchStrategy();
         case DIAMOND -> new DiamondSearchStrategy();
         case TRIANGLE -> new TriangleSearchStrategy();
         case SQUARE -> new SquareSearchStrategy();
         default -> new UniformSearchStrategy();
      });
   }

   private HotspotStorage createHotspotStorage(RareBiomeOptimizationSettings rareSettings) {
      if (rareSettings != null && "mysql".equalsIgnoreCase(rareSettings.getPersistenceBackend())) {
         try {
            return new MySqlHotspotStorage(
               rareSettings.getPersistenceMysqlUrl(),
               rareSettings.getPersistenceMysqlUser(),
               rareSettings.getPersistenceMysqlPassword(),
               this.plugin.getLogger()
            );
         } catch (Exception var3) {
            this.plugin.getLogger().warning("Failed to initialise MySQL hotspot storage, falling back to in-memory only: " + var3.getMessage());
            return null;
         }
      } else {
         return null;
      }
   }

   private BiomeLocationCache createBiomeCache(RandomTeleportSettings initialSettings, PlatformScheduler scheduler) {
      if (initialSettings != null && initialSettings.getPreCacheSettings() != null) {
         BiomePreCacheSettings preCache = initialSettings.getPreCacheSettings();
         BiomeLocationCache cache = new BiomeLocationCache(
            this.plugin, preCache.getMaxPerBiome(), preCache.getWarmupSize(), preCache.getExpirationMinutes(), this.chunkyWarmupCoordinator, scheduler
         );
         cache.setEnabled(preCache.isEnabled());
         return cache;
      } else {
         BiomeLocationCache cache = new BiomeLocationCache(this.plugin, 16, 8, 10L, this.chunkyWarmupCoordinator, scheduler);
         cache.setEnabled(false);
         return cache;
      }
   }

   private void logSearchConfiguration(RandomTeleportSettings currentSettings, BiomeSearchStrategy patternStrategy, BiomeSearchStrategy activeStrategy) {
      if (currentSettings == null) {
         this.debugFileLogger.log("Search configuration unavailable (settings are null).");
      } else {
         SearchPattern pattern = currentSettings.getSearchPattern() != null ? currentSettings.getSearchPattern() : SearchPattern.RANDOM;
         boolean debugEnabled = currentSettings.isDebugRejectionLoggingEnabled();
         boolean weightedSearchActive = activeStrategy instanceof WeightedRareBiomeStrategy;
         boolean rareOptimizationEnabled = currentSettings.getRareBiomeOptimizationSettings() != null
            && currentSettings.getRareBiomeOptimizationSettings().isEnabled();
         boolean biomeFiltersActive = LocationValidator.hasBiomeFilters(currentSettings);
         StringBuilder sb = new StringBuilder();
         sb.append("[EzRTP] Search strategy configured: pattern=").append(pattern.getConfigKey());
         sb.append(", baseStrategy=").append(patternStrategy != null ? patternStrategy.getClass().getSimpleName() : "none");
         sb.append(", activeStrategy=").append(activeStrategy != null ? activeStrategy.getClass().getSimpleName() : "none");
         sb.append(", weightedRareSearch=").append(weightedSearchActive);
         sb.append(", rareOptimizationEnabled=").append(rareOptimizationEnabled);
         sb.append(", biomeFiltersActive=").append(biomeFiltersActive);
         sb.append(", debugRejectionLogging=").append(debugEnabled);
         this.debugFileLogger.log(sb.toString());
      }
   }
}
