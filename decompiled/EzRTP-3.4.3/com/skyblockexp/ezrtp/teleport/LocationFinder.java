package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.biome.BiomeSearchSettings;
import com.skyblockexp.ezrtp.config.safety.SafetySettings;
import com.skyblockexp.ezrtp.platform.PlatformRuntime;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformWorldAccess;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import com.skyblockexp.ezrtp.teleport.biome.WeightedRareBiomeStrategy;
import com.skyblockexp.ezrtp.teleport.filter.SearchFilter;
import com.skyblockexp.ezrtp.teleport.filter.SearchFilterChain;
import com.skyblockexp.ezrtp.teleport.filter.SearchFilterResult;
import com.skyblockexp.ezrtp.teleport.queue.ChunkLoadQueue;
import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationCause;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationMonitor;
import com.skyblockexp.ezrtp.util.BlockCompat;
import com.skyblockexp.ezrtp.util.DebugFileLogger;
import com.skyblockexp.ezrtp.util.compat.WorldBorderCompat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

public final class LocationFinder {
   private static final int NETHER_ROOF_Y = 127;
   private static final int DEFAULT_SURFACE_SCAN_DEPTH = 20;
   private static final int MAX_SURFACE_SCAN_DEPTH = 128;
   private final JavaPlugin plugin;
   private final PlatformScheduler scheduler;
   private final RtpStatistics statistics;
   private final BiomeLocationCache biomeCache;
   private final RareBiomeRegistry rareBiomeRegistry;
   private final ChunkLoadQueue chunkLoadQueue;
   private final LocationValidator validator;
   private final SearchFilterChain searchFilterChain;
   private volatile BiomeSearchStrategy searchStrategy;
   private final ChunkyProvider chunkyAPI;
   private final ChunkyWarmupCoordinator chunkyWarmupCoordinator;
   private final PlatformWorldAccess platformWorldAccess;
   private final DebugFileLogger debugFileLogger;
   private final Executor biomeFilterExecutor;
   private volatile UnsafeLocationMonitor unsafeMonitor;

   public LocationFinder(
      JavaPlugin plugin,
      RtpStatistics statistics,
      BiomeLocationCache biomeCache,
      RareBiomeRegistry rareBiomeRegistry,
      ChunkLoadQueue chunkLoadQueue,
      LocationValidator validator,
      BiomeSearchStrategy searchStrategy,
      PlatformRuntime platformRuntime,
      ChunkyProvider chunkyAPI,
      ChunkyWarmupCoordinator chunkyWarmupCoordinator
   ) {
      this(
         plugin,
         statistics,
         biomeCache,
         rareBiomeRegistry,
         chunkLoadQueue,
         validator,
         searchStrategy,
         platformRuntime,
         chunkyAPI,
         chunkyWarmupCoordinator,
         null
      );
   }

   public LocationFinder(
      JavaPlugin plugin,
      RtpStatistics statistics,
      BiomeLocationCache biomeCache,
      RareBiomeRegistry rareBiomeRegistry,
      ChunkLoadQueue chunkLoadQueue,
      LocationValidator validator,
      BiomeSearchStrategy searchStrategy,
      PlatformRuntime platformRuntime,
      ChunkyProvider chunkyAPI,
      ChunkyWarmupCoordinator chunkyWarmupCoordinator,
      Executor biomeFilterExecutor
   ) {
      this.plugin = plugin;
      this.scheduler = platformRuntime != null ? platformRuntime.scheduler() : null;
      this.statistics = statistics;
      this.biomeCache = biomeCache;
      this.rareBiomeRegistry = rareBiomeRegistry;
      this.chunkLoadQueue = chunkLoadQueue;
      this.validator = validator;
      this.searchStrategy = searchStrategy;
      this.chunkyAPI = chunkyAPI;
      this.chunkyWarmupCoordinator = chunkyWarmupCoordinator;
      this.platformWorldAccess = platformRuntime != null ? platformRuntime.worldAccess() : null;
      this.biomeFilterExecutor = biomeFilterExecutor;
      this.searchFilterChain = this.createSearchFilterChain();
      this.debugFileLogger = new DebugFileLogger(plugin);
      if (this.scheduler == null) {
         throw new IllegalArgumentException("PlatformRuntime with scheduler is required");
      }
   }

   public void setSearchStrategy(BiomeSearchStrategy searchStrategy) {
      this.searchStrategy = searchStrategy;
   }

   public void setUnsafeLocationMonitor(UnsafeLocationMonitor monitor) {
      this.unsafeMonitor = monitor;
   }

   public CompletableFuture<SearchResult> findSafeLocationAsync(World world, RandomTeleportSettings currentSettings) {
      if (world != null && currentSettings != null) {
         Runtime runtime = Runtime.getRuntime();
         long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
         if (this.chunkyWarmupCoordinator != null && !this.chunkyWarmupCoordinator.hasSufficientMemory()) {
            this.plugin.getLogger().warning(String.format("Low memory detected (%dMB free), using cache-only mode", freeMemoryMb));
            boolean cacheChecked = false;
            if (this.biomeCache.isEnabled() && LocationValidator.hasBiomeFilters(currentSettings)) {
               cacheChecked = true;
               Location cachedLocation = this.biomeCache.get(world, currentSettings.getBiomeInclude(), currentSettings.getBiomeExclude());
               if (cachedLocation != null
                  && this.validator.isSafe(cachedLocation, currentSettings)
                  && !this.validator.isProtectedByClaims(cachedLocation, currentSettings)) {
                  return CompletableFuture.completedFuture(
                     new SearchResult(Optional.of(cachedLocation), false, true, true, false, false, SearchResult.SearchLimitType.NONE)
                  );
               }
            }

            if (currentSettings.isEnableFallbackToCache() && this.biomeCache.isEnabled()) {
               cacheChecked = true;
               Location cachedLocation = this.getFallbackCachedLocation(world, currentSettings, false);
               if (cachedLocation != null) {
                  return CompletableFuture.completedFuture(
                     new SearchResult(Optional.of(cachedLocation), false, true, true, true, false, SearchResult.SearchLimitType.NONE)
                  );
               }
            }

            return CompletableFuture.completedFuture(
               new SearchResult(Optional.empty(), false, false, cacheChecked, false, false, SearchResult.SearchLimitType.NONE)
            );
         } else {
            RandomTeleportSettings adjustedSettings = currentSettings;
            if (freeMemoryMb < 512L) {
               int reducedAttempts = Math.max(8, currentSettings.getMaxAttempts() / 4);
               if (reducedAttempts < currentSettings.getMaxAttempts()) {
                  this.plugin
                     .getLogger()
                     .info(
                        String.format(
                           "Reducing max attempts from %d to %d due to low memory (%dMB free)", currentSettings.getMaxAttempts(), reducedAttempts, freeMemoryMb
                        )
                     );
                  adjustedSettings = new RandomTeleportSettings(
                     null,
                     currentSettings.getWorldName(),
                     currentSettings.getCenterX(),
                     currentSettings.getCenterZ(),
                     currentSettings.getMinimumRadius(),
                     currentSettings.getMaximumRadius(),
                     reducedAttempts,
                     currentSettings.useWorldBorderRadius(),
                     currentSettings.getUnsafeBlocks(),
                     currentSettings.getMessages(),
                     currentSettings.getParticleSettings(),
                     currentSettings.getOnJoinTeleportSettings(),
                     currentSettings.getCountdownBossBarSettings(),
                     currentSettings.getCountdownParticleSettings(),
                     currentSettings.getTeleportCost(),
                     currentSettings.getCountdownSeconds(),
                     currentSettings.isCountdownChatMessagesEnabled(),
                     currentSettings.isDebugRejectionLoggingEnabled(),
                     currentSettings.getMinY(),
                     currentSettings.getMaxY(),
                     currentSettings.getBiomeInclude(),
                     currentSettings.getBiomeExclude(),
                     currentSettings.getProtectionSettings(),
                     currentSettings.getPreCacheSettings(),
                     currentSettings.getRareBiomeOptimizationSettings(),
                     currentSettings.getChunkLoadingSettings(),
                     currentSettings.isEnableFallbackToCache(),
                     currentSettings.getBiomeSearchSettings(),
                     currentSettings.isBiomeFilteringEnabled(),
                     currentSettings.isBiomeSystemEnabled(),
                     currentSettings.getSafetySettings(),
                     currentSettings.getSearchPattern(),
                     currentSettings.getChunkyIntegrationSettings()
                  );
               }
            }

            boolean cacheCheckedx = false;
            if (this.biomeCache.isEnabled() && LocationValidator.hasBiomeFilters(currentSettings)) {
               cacheCheckedx = true;
               Location cachedLocation = this.biomeCache.get(world, currentSettings.getBiomeInclude(), currentSettings.getBiomeExclude());
               if (cachedLocation != null
                  && this.validator.isSafe(cachedLocation, currentSettings)
                  && !this.validator.isProtectedByClaims(cachedLocation, currentSettings)) {
                  return CompletableFuture.completedFuture(
                     new SearchResult(Optional.of(cachedLocation), false, true, true, false, false, SearchResult.SearchLimitType.NONE)
                  );
               }
            }

            this.logSearchStart(world, adjustedSettings);
            LocationFinder.SearchContext searchContext = new LocationFinder.SearchContext(
               adjustedSettings.getBiomeSearchSettings(), this.isRareSearch(adjustedSettings)
            );
            if (!adjustedSettings.getBiomeSearchSettings().isAsyncMode() && this.biomeFilterExecutor != null) {
               CompletableFuture<SearchResult> gate = new CompletableFuture<>();
               RandomTeleportSettings finalSettings = adjustedSettings;
               boolean finalCacheChecked = cacheCheckedx;
               this.biomeFilterExecutor
                  .execute(() -> this.attemptFindLocation(world, finalSettings, 0, false, searchContext, finalCacheChecked).whenComplete((r, ex) -> {
                     if (ex != null) {
                        gate.completeExceptionally(ex);
                     } else {
                        gate.complete(r);
                     }
                  }));
               return gate;
            } else {
               return this.attemptFindLocation(world, adjustedSettings, 0, false, searchContext, cacheCheckedx);
            }
         }
      } else {
         return CompletableFuture.completedFuture(new SearchResult(Optional.empty(), false, false, false, false, false, SearchResult.SearchLimitType.NONE));
      }
   }

   public CompletableFuture<Location> generateSafeLocationForCache(World world, RandomTeleportSettings teleportSettings) {
      if (world != null && teleportSettings != null) {
         Runtime runtime = Runtime.getRuntime();
         long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
         RandomTeleportSettings adjustedSettings = teleportSettings;
         if (freeMemoryMb < 512L) {
            int reducedAttempts = Math.max(4, teleportSettings.getMaxAttempts() / 8);
            if (reducedAttempts < teleportSettings.getMaxAttempts()) {
               this.plugin
                  .getLogger()
                  .fine(
                     String.format(
                        "Reducing cache generation attempts from %d to %d due to low memory (%dMB free)",
                        teleportSettings.getMaxAttempts(),
                        reducedAttempts,
                        freeMemoryMb
                     )
                  );
               adjustedSettings = new RandomTeleportSettings(
                  null,
                  teleportSettings.getWorldName(),
                  teleportSettings.getCenterX(),
                  teleportSettings.getCenterZ(),
                  teleportSettings.getMinimumRadius(),
                  teleportSettings.getMaximumRadius(),
                  reducedAttempts,
                  teleportSettings.useWorldBorderRadius(),
                  teleportSettings.getUnsafeBlocks(),
                  teleportSettings.getMessages(),
                  teleportSettings.getParticleSettings(),
                  teleportSettings.getOnJoinTeleportSettings(),
                  teleportSettings.getCountdownBossBarSettings(),
                  teleportSettings.getCountdownParticleSettings(),
                  teleportSettings.getTeleportCost(),
                  teleportSettings.getCountdownSeconds(),
                  teleportSettings.isCountdownChatMessagesEnabled(),
                  teleportSettings.isDebugRejectionLoggingEnabled(),
                  teleportSettings.getMinY(),
                  teleportSettings.getMaxY(),
                  teleportSettings.getBiomeInclude(),
                  teleportSettings.getBiomeExclude(),
                  teleportSettings.getProtectionSettings(),
                  teleportSettings.getPreCacheSettings(),
                  teleportSettings.getRareBiomeOptimizationSettings(),
                  teleportSettings.getChunkLoadingSettings(),
                  teleportSettings.isEnableFallbackToCache(),
                  teleportSettings.getBiomeSearchSettings(),
                  teleportSettings.isBiomeFilteringEnabled(),
                  teleportSettings.isBiomeSystemEnabled(),
                  teleportSettings.getSafetySettings(),
                  teleportSettings.getSearchPattern(),
                  teleportSettings.getChunkyIntegrationSettings()
               );
            }
         }

         return this.findSafeLocationAsync(world, adjustedSettings)
            .thenApply(result -> result != null && result.location().isPresent() ? result.location().get() : null);
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }

   public void cacheValidLocation(Location location, RandomTeleportSettings settings) {
      if (location != null && this.biomeCache.isEnabled() && LocationValidator.hasBiomeFilters(settings)) {
         this.biomeCache.cache(location);
      }
   }

   private CompletableFuture<SearchResult> attemptFindLocation(
      World world, RandomTeleportSettings currentSettings, int attempt, boolean rejectedForBiome, LocationFinder.SearchContext context, boolean cacheChecked
   ) {
      if (context.hasLimitBeenHit()) {
         return this.completeDueToLimit(world, currentSettings, context, cacheChecked);
      } else {
         int attemptLimit = this.resolveAttemptLimit(currentSettings, context);
         if (attempt < attemptLimit) {
            return this.generateCandidateLocationAsync(world, currentSettings, context)
               .thenCompose(
                  candidate -> {
                     if (context.hasLimitBeenHit()) {
                        return this.completeDueToLimit(world, currentSettings, context, cacheChecked);
                     } else {
                        SearchFilterResult filterResult = this.searchFilterChain.apply(candidate, currentSettings);
                        if (!filterResult.passed()) {
                           SearchFilterResult.RejectionReason reason = filterResult.rejectionReason();
                           if (reason == SearchFilterResult.RejectionReason.UNSAFE_OR_NULL_CANDIDATE) {
                              Optional<Location> rescued = this.attemptSafetyRecovery(candidate, currentSettings);
                              if (rescued.isPresent()) {
                                 Location recovered = rescued.get();
                                 if (this.rareBiomeRegistry != null && this.rareBiomeRegistry.isEnabled()) {
                                    Biome biome = recovered.getBlock().getBiome();
                                    if (this.rareBiomeRegistry.isRareBiome(biome)) {
                                       this.rareBiomeRegistry.registerHotspot(recovered);
                                    }
                                 }

                                 this.statistics.recordBiomeRejectionCount(context.getBiomeRejections());
                                 return CompletableFuture.completedFuture(
                                    new SearchResult(Optional.of(recovered), false, false, cacheChecked, false, false, SearchResult.SearchLimitType.NONE)
                                 );
                              }

                              this.logCandidateRejection(world, currentSettings, attempt, candidate, "unsafe_or_null_candidate");
                              this.statistics.recordSafetyFailure();
                              this.recordUnsafeCause(candidate, currentSettings);
                              return this.attemptFindLocation(world, currentSettings, attempt + 1, rejectedForBiome, context, cacheChecked);
                           }

                           if (reason == SearchFilterResult.RejectionReason.BIOME_FILTERED) {
                              this.logCandidateRejection(world, currentSettings, attempt, candidate, "biome_filtered");
                              this.statistics.recordBiomeAttempt(candidate.getBlock().getBiome());
                              if (this.biomeCache.isEnabled() && !this.validator.isProtectedByClaims(candidate, currentSettings)) {
                                 this.biomeCache.cache(candidate);
                              }

                              if (!context.recordBiomeRejection()) {
                                 return this.completeDueToLimit(world, currentSettings, context, cacheChecked);
                              }

                              return this.attemptFindLocation(world, currentSettings, attempt + 1, true, context, cacheChecked);
                           }

                           if (reason == SearchFilterResult.RejectionReason.PROTECTED_CLAIM) {
                              this.logCandidateRejection(world, currentSettings, attempt, candidate, "protected_claim");
                              this.statistics.recordProtectionFailure();
                              return this.attemptFindLocation(world, currentSettings, attempt + 1, rejectedForBiome, context, cacheChecked);
                           }
                        }

                        if (this.rareBiomeRegistry != null && this.rareBiomeRegistry.isEnabled()) {
                           Biome biome = candidate.getBlock().getBiome();
                           if (this.rareBiomeRegistry.isRareBiome(biome)) {
                              this.rareBiomeRegistry.registerHotspot(candidate);
                           }
                        }

                        this.statistics.recordBiomeRejectionCount(context.getBiomeRejections());
                        return CompletableFuture.completedFuture(
                           new SearchResult(Optional.of(candidate), false, false, cacheChecked, false, false, SearchResult.SearchLimitType.NONE)
                        );
                     }
                  }
               );
         } else {
            boolean noValidBiome = rejectedForBiome && (!currentSettings.getBiomeInclude().isEmpty() || !currentSettings.getBiomeExclude().isEmpty());
            this.statistics.recordTimeoutFailure();
            if (noValidBiome && !currentSettings.getBiomeInclude().isEmpty()) {
               currentSettings.getBiomeInclude().forEach(this.statistics::recordBiomeFailure);
            }

            if (currentSettings.isEnableFallbackToCache() && this.biomeCache.isEnabled()) {
               boolean attemptedCacheLookup = true;
               Location cachedLocation = this.getFallbackCachedLocation(world, currentSettings, false);
               if (cachedLocation != null) {
                  this.statistics.recordBiomeRejectionCount(context.getBiomeRejections());
                  return CompletableFuture.completedFuture(
                     new SearchResult(Optional.of(cachedLocation), false, true, true, true, false, SearchResult.SearchLimitType.NONE)
                  );
               } else {
                  this.statistics.recordBiomeRejectionCount(context.getBiomeRejections());
                  return CompletableFuture.completedFuture(
                     new SearchResult(Optional.empty(), noValidBiome, false, attemptedCacheLookup, false, true, SearchResult.SearchLimitType.NONE)
                  );
               }
            } else {
               try {
                  if (currentSettings.isDebugRejectionLoggingEnabled()) {
                     StringBuilder sb = new StringBuilder();
                     sb.append("EzRTP debug: search exhausted for world '").append(world.getName()).append("'\n");
                     sb.append("  center=(").append(currentSettings.getCenterX()).append(',').append(currentSettings.getCenterZ()).append(")\n");
                     sb.append("  radius=[")
                        .append(currentSettings.getMinimumRadius())
                        .append(',')
                        .append(this.resolveMaximumRadius(world, currentSettings))
                        .append("] useWorldBorder=")
                        .append(currentSettings.useWorldBorderRadius())
                        .append("\n");
                     sb.append("  attemptsAllowed=").append(attemptLimit).append(" attemptsUsed=").append(attempt).append("\n");
                     sb.append("  minY=").append(getWorldMinHeight(world)).append(" maxY=").append(getWorldMaxHeight(world)).append("\n");
                     sb.append("  biomeInclude=").append(currentSettings.getBiomeInclude()).append("\n");
                     sb.append("  biomeExclude=").append(currentSettings.getBiomeExclude()).append("\n");
                     sb.append("  rareSearch=").append(this.isRareSearch(currentSettings)).append("\n");
                     this.debugFileLogger.log(sb.toString());
                  }
               } catch (Exception var11) {
               }

               this.statistics.recordBiomeRejectionCount(context.getBiomeRejections());
               return CompletableFuture.completedFuture(
                  new SearchResult(Optional.empty(), noValidBiome, false, cacheChecked, false, false, SearchResult.SearchLimitType.NONE)
               );
            }
         }
      }
   }

   private SearchFilterChain createSearchFilterChain() {
      SearchFilter safetyFilter = (candidate, settings) -> candidate != null && this.validator.isSafe(candidate, settings)
         ? SearchFilterResult.pass()
         : SearchFilterResult.rejected(SearchFilterResult.RejectionReason.UNSAFE_OR_NULL_CANDIDATE);
      SearchFilter biomeFilter = (candidate, settings) -> !this.validator.isBiomeAllowed(candidate, settings)
         ? SearchFilterResult.rejected(SearchFilterResult.RejectionReason.BIOME_FILTERED)
         : SearchFilterResult.pass();
      SearchFilter protectionFilter = (candidate, settings) -> this.validator.isProtectedByClaims(candidate, settings)
         ? SearchFilterResult.rejected(SearchFilterResult.RejectionReason.PROTECTED_CLAIM)
         : SearchFilterResult.pass();
      return new SearchFilterChain(List.of(safetyFilter, biomeFilter, protectionFilter));
   }

   private int resolveAttemptLimit(RandomTeleportSettings settings, LocationFinder.SearchContext context) {
      int limit = settings != null ? settings.getMaxAttempts() : 0;
      if (settings != null && LocationValidator.hasBiomeFilters(settings)) {
         int minAttempts = settings.getBiomeSearchSettings() != null
            ? settings.getBiomeSearchSettings().getMinBiomeAttempts()
            : BiomeSearchSettings.defaults().getMinBiomeAttempts();
         if (context != null && context.getSettings() != null) {
            minAttempts = context.getSettings().getMinBiomeAttempts();
         }

         limit = Math.max(limit, minAttempts);
      }

      return limit;
   }

   private CompletableFuture<Location> generateCandidateLocationAsync(World world, RandomTeleportSettings currentSettings, LocationFinder.SearchContext context) {
      if (context.hasLimitBeenHit()) {
         return CompletableFuture.completedFuture(null);
      } else {
         int min = currentSettings.getMinimumRadius();
         int max = this.resolveMaximumRadius(world, currentSettings);
         if (max < min) {
            max = min;
         }

         int centerX = currentSettings.getCenterX();
         int centerZ = currentSettings.getCenterZ();
         if (currentSettings.useWorldBorderRadius()) {
            Optional<Location> center = WorldBorderCompat.getCenter(world);
            if (center.isPresent()) {
               Location borderCenter = center.get();
               centerX = (int)Math.round(borderCenter.getX());
               centerZ = (int)Math.round(borderCenter.getZ());
            }
         }

         if (this.chunkyAPI != null
            && currentSettings.getChunkyIntegrationSettings().isEnabled()
            && currentSettings.getChunkyIntegrationSettings().isAutoPregenerate()) {
            if (this.chunkyWarmupCoordinator != null && !this.chunkyWarmupCoordinator.hasSufficientMemory()) {
               this.plugin.getLogger().fine("Skipping Chunky pregeneration due to low memory");
            } else {
               String worldName = world.getName();
               int centerChunkX = centerX >> 4;
               int centerChunkZ = centerZ >> 4;
               int radiusChunks = Math.max(0, max / 16);
               boolean alreadyPlanned = false;
               if (this.chunkyWarmupCoordinator != null) {
                  alreadyPlanned = this.chunkyWarmupCoordinator.isChunkPlannedOrGenerated(worldName, centerChunkX, centerChunkZ);
               }

               try {
                  if (!alreadyPlanned && !this.chunkyAPI.isRunning(worldName)) {
                     boolean started = this.chunkyAPI
                        .startTask(
                           worldName,
                           currentSettings.getChunkyIntegrationSettings().getShape(),
                           centerX,
                           centerZ,
                           max,
                           max,
                           currentSettings.getChunkyIntegrationSettings().getPattern()
                        );
                     if (started && this.chunkyWarmupCoordinator != null) {
                        this.chunkyWarmupCoordinator.markRegionPlanned(worldName, centerChunkX, centerChunkZ, radiusChunks);
                     }
                  }
               } catch (Throwable var19) {
               }
            }
         }

         BiomeSearchStrategy strategy = (BiomeSearchStrategy)(this.searchStrategy != null ? this.searchStrategy : new UniformSearchStrategy());
         Measurements.incCandidateGeneration();
         int[] coordinates = strategy.generateCandidateCoordinates(world, centerX, centerZ, min, max, currentSettings.getBiomeInclude(), this.rareBiomeRegistry);
         if (strategy instanceof WeightedRareBiomeStrategy) {
            this.statistics.recordWeightedSearchUse();
         } else {
            this.statistics.recordUniformSearchUse();
         }

         int x = coordinates[0];
         int z = coordinates[1];
         int chunkX = x >> 4;
         int chunkZ = z >> 4;
         boolean wasChunkLoaded = world.isChunkLoaded(chunkX, chunkZ);
         if (wasChunkLoaded) {
            int highestY = this.surfaceY(world, x, z);
            int minY = getWorldMinHeight(world);
            if (highestY > minY) {
               Location snapValidated = this.snapshotValidate(world, x, z, highestY, minY);
               if (snapValidated != null) {
                  CompletableFuture<Location> immediate = new CompletableFuture<>();
                  immediate.complete(snapValidated);
                  return immediate;
               }
            }
         }

         CompletableFuture<Location> future = new CompletableFuture<>();
         if (!wasChunkLoaded) {
            if (!context.tryIncrementChunkLoad()) {
               return CompletableFuture.completedFuture(null);
            }

            if (this.chunkLoadQueue != null && this.chunkLoadQueue.isEnabled()) {
               Measurements.incChunkLoadRequest();
               this.statistics.recordChunkLoadQueueUse();
               this.chunkLoadQueue
                  .requestChunkLoad(world, chunkX, chunkZ)
                  .thenAccept(
                     chunk -> this.scheduler
                        .executeRegion(
                           world, chunkX, chunkZ, () -> this.handleCandidateResolution(world, x, z, chunkX, chunkZ, wasChunkLoaded, currentSettings, future)
                        )
                  )
                  .exceptionally(ex -> {
                     future.completeExceptionally(ex);
                     return null;
                  });
            } else {
               this.scheduler.executeRegion(world, chunkX, chunkZ, () -> {
                  Measurements.incChunkLoadRequest();
                  world.loadChunk(chunkX, chunkZ);
                  this.handleCandidateResolution(world, x, z, chunkX, chunkZ, wasChunkLoaded, currentSettings, future);
               });
            }
         } else {
            this.scheduler
               .executeRegion(world, chunkX, chunkZ, () -> this.handleCandidateResolution(world, x, z, chunkX, chunkZ, true, currentSettings, future));
         }

         return future;
      }
   }

   private void handleCandidateResolution(
      World world, int x, int z, int chunkX, int chunkZ, boolean chunkWasLoaded, RandomTeleportSettings currentSettings, CompletableFuture<Location> future
   ) {
      try {
         Location candidate = this.resolveCandidateLocation(world, x, z, currentSettings);
         if (candidate == null) {
            try {
               if (currentSettings != null && currentSettings.isDebugRejectionLoggingEnabled()) {
                  int highestY = this.surfaceY(world, x, z);
                  int minY = getWorldMinHeight(world);
                  int maxY = getWorldMaxHeight(world) - 1;
                  StringBuilder sb = new StringBuilder();
                  sb.append("EzRTP debug: candidate null for world '").append(world.getName()).append("'\n");
                  sb.append("  coords=(").append(x).append(',').append(z).append(")\n");
                  sb.append("  highestY=").append(highestY).append(" minY=").append(minY).append(" maxY=").append(maxY).append("\n");
                  Block topBlock = null;

                  try {
                     topBlock = world.getBlockAt(x, highestY, z);
                  } catch (Throwable var21) {
                  }

                  if (topBlock != null) {
                     sb.append("  topBlock=").append(topBlock.getType()).append("\n");
                  }

                  this.debugFileLogger.log(sb.toString());
               }
            } catch (Throwable var22) {
            }
         }

         future.complete(candidate);
      } catch (Exception var23) {
         future.completeExceptionally(var23);
      } finally {
         this.scheduler.executeRegion(world, chunkX, chunkZ, () -> {
            if (!chunkWasLoaded && world.isChunkLoaded(chunkX, chunkZ)) {
               world.unloadChunkRequest(chunkX, chunkZ);
            }
         });
      }
   }

   private Location resolveCandidateLocation(World world, int x, int z, RandomTeleportSettings settings) {
      int minY = getWorldMinHeight(world);
      int maxY = getWorldMaxHeight(world) - 1;
      if (world.getEnvironment() == Environment.NETHER) {
         maxY = Math.min(maxY, 126);
      }

      if (world.getEnvironment() != Environment.NORMAL) {
         int highestY = Math.min(this.surfaceY(world, x, z), maxY);
         return highestY <= minY ? null : this.findPassableLocation(world, x, z, highestY, minY, settings);
      } else {
         int highestY = this.surfaceY(world, x, z);
         return highestY <= minY ? null : this.findPassableLocation(world, x, z, highestY, minY, settings);
      }
   }

   private Location findPassableLocation(World world, int x, int z, int startY, int minY, RandomTeleportSettings settings) {
      int searchMinY = this.resolveSearchMinY(world, startY, minY, settings);

      for (int y = startY; y > searchMinY; y--) {
         Measurements.incBlockAccess();
         Block block = world.getBlockAt(x, y, z);
         if (block.getType().isSolid()) {
            Block above = block.getRelative(BlockFace.UP);
            Block twoAbove = above.getRelative(BlockFace.UP);
            if (BlockCompat.isPassable(above) && BlockCompat.isPassable(twoAbove)) {
               if (!above.isLiquid()) {
                  return new Location(world, x + 0.5, block.getY() + 1.0, z + 0.5);
               }

               Location surface = this.findSurfaceAboveLiquid(world, x, z, above.getY());
               if (surface != null) {
                  return surface;
               }
            }
         }
      }

      return null;
   }

   private int resolveSurfaceScanDepth(World world, RandomTeleportSettings settings) {
      if (settings != null && settings.getSafetySettings() != null) {
         SafetySettings safetySettings = settings.getSafetySettings();
         int configuredDepth = world != null && world.getEnvironment() == Environment.NETHER
            ? safetySettings.getMaxSurfaceScanDepthNether()
            : safetySettings.getMaxSurfaceScanDepth();
         return Math.max(1, Math.min(configuredDepth, 128));
      } else {
         return 20;
      }
   }

   private int resolveSearchMinY(World world, int startY, int minY, RandomTeleportSettings settings) {
      return Math.max(minY, startY - this.resolveSurfaceScanDepth(world, settings));
   }

   private Location findSurfaceAboveLiquid(World world, int x, int z, int startY) {
      int maxY = getWorldMaxHeight(world) - 1;

      int y;
      for (y = startY; y <= maxY; y++) {
         Measurements.incBlockAccess();
         if (!world.getBlockAt(x, y, z).isLiquid()) {
            break;
         }
      }

      if (y > maxY) {
         return null;
      } else {
         Measurements.incBlockAccess();
         Block surface = world.getBlockAt(x, y, z);
         Block above = surface.getRelative(BlockFace.UP);
         return BlockCompat.isPassable(surface) && BlockCompat.isPassable(above) ? new Location(world, x + 0.5, surface.getY(), z + 0.5) : null;
      }
   }

   private int surfaceY(World world, int x, int z) {
      if (this.platformWorldAccess != null) {
         return this.platformWorldAccess.getSurfaceY(world, x, z);
      } else {
         return world == null ? 0 : world.getHighestBlockYAt(x, z);
      }
   }

   private Location snapshotValidate(World world, int x, int z, int startY, int minY) {
      return this.platformWorldAccess == null ? null : this.platformWorldAccess.trySnapshotValidate(world, x, z, startY, minY);
   }

   private int resolveMaximumRadius(World world, RandomTeleportSettings settings) {
      if (!settings.useWorldBorderRadius()) {
         return settings.getMaximumRadius();
      } else {
         Optional<Double> radius = WorldBorderCompat.getBorderRadius(world);
         if (radius.isPresent()) {
            int resolved = (int)Math.floor(radius.get());
            return Math.max(settings.getMinimumRadius(), resolved);
         } else {
            return settings.getMaximumRadius();
         }
      }
   }

   private Location getFallbackCachedLocation(World world, RandomTeleportSettings settings, boolean requireSafety) {
      if (this.biomeCache != null && this.biomeCache.isEnabled() && world != null) {
         int attempts = 8;

         for (int i = 0; i < attempts; i++) {
            Location cachedLocation = this.biomeCache.getRandomCachedLocation(world);
            if (cachedLocation == null) {
               return null;
            }

            if ((!requireSafety || this.validator.isSafe(cachedLocation, settings)) && !this.validator.isProtectedByClaims(cachedLocation, settings)) {
               return cachedLocation;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public CompletableFuture<Location> generateSafeLocationForChunk(World world, int chunkX, int chunkZ, RandomTeleportSettings currentSettings) {
      if (world != null && currentSettings != null) {
         int x = (chunkX << 4) + 8;
         int z = (chunkZ << 4) + 8;
         boolean wasChunkLoaded = world.isChunkLoaded(chunkX, chunkZ);
         CompletableFuture<Location> future = new CompletableFuture<>();
         if (!wasChunkLoaded) {
            if (this.chunkLoadQueue != null && this.chunkLoadQueue.isEnabled()) {
               this.statistics.recordChunkLoadQueueUse();
               this.chunkLoadQueue
                  .requestChunkLoad(world, chunkX, chunkZ)
                  .thenAccept(
                     chunk -> this.scheduler
                        .executeRegion(
                           world, chunkX, chunkZ, () -> this.handleCandidateResolution(world, x, z, chunkX, chunkZ, wasChunkLoaded, currentSettings, future)
                        )
                  )
                  .exceptionally(ex -> {
                     future.completeExceptionally(ex);
                     return null;
                  });
            } else {
               this.scheduler.executeRegion(world, chunkX, chunkZ, () -> {
                  world.loadChunk(chunkX, chunkZ);
                  this.handleCandidateResolution(world, x, z, chunkX, chunkZ, wasChunkLoaded, currentSettings, future);
               });
            }
         } else {
            this.scheduler
               .executeRegion(world, chunkX, chunkZ, () -> this.handleCandidateResolution(world, x, z, chunkX, chunkZ, true, currentSettings, future));
         }

         return future;
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }

   private static int getWorldMinHeight(World world) {
      try {
         return (Integer)World.class.getMethod("getMinHeight").invoke(world);
      } catch (ReflectiveOperationException var2) {
         return 0;
      }
   }

   private static int getWorldMaxHeight(World world) {
      try {
         return (Integer)World.class.getMethod("getMaxHeight").invoke(world);
      } catch (ReflectiveOperationException var2) {
         return world.getMaxHeight();
      }
   }

   private CompletableFuture<SearchResult> completeDueToLimit(
      World world, RandomTeleportSettings currentSettings, LocationFinder.SearchContext context, boolean cacheChecked
   ) {
      this.statistics.recordTimeoutFailure();
      SearchResult.SearchLimitType limitType = context.getLimitType();
      boolean noValidBiome = limitType == SearchResult.SearchLimitType.BIOME_REJECTIONS;
      BiomeSearchSettings searchSettings = context.getSettings();
      int effectiveAttemptLimit = this.resolveAttemptLimit(currentSettings, context);
      long elapsedMillis = context.getElapsedMillis();
      boolean cacheFailoverAttempted = false;
      boolean cacheFailoverProducedLocation = false;
      BiomeSearchSettings.FailoverMode mode = searchSettings.getFailoverMode();
      if (mode == BiomeSearchSettings.FailoverMode.CACHE && this.biomeCache.isEnabled()) {
         cacheFailoverAttempted = true;
         cacheChecked = true;
         Location cachedLocation = this.getFallbackCachedLocation(world, currentSettings, false);
         if (cachedLocation != null) {
            cacheFailoverProducedLocation = true;
            this.logLimitDebug(
               world,
               currentSettings,
               context,
               limitType,
               elapsedMillis,
               effectiveAttemptLimit,
               searchSettings,
               cacheFailoverAttempted,
               cacheFailoverProducedLocation
            );
            this.statistics.recordBiomeRejectionCount(context.getBiomeRejections());
            if (limitType == SearchResult.SearchLimitType.BIOME_REJECTIONS) {
               this.statistics.recordBiomeFilterTimeout();
            }

            return CompletableFuture.completedFuture(new SearchResult(Optional.of(cachedLocation), false, true, true, true, false, limitType));
         }
      }

      this.logLimitDebug(
         world,
         currentSettings,
         context,
         limitType,
         elapsedMillis,
         effectiveAttemptLimit,
         searchSettings,
         cacheFailoverAttempted,
         cacheFailoverProducedLocation
      );
      this.statistics.recordBiomeRejectionCount(context.getBiomeRejections());
      if (limitType == SearchResult.SearchLimitType.BIOME_REJECTIONS) {
         this.statistics.recordBiomeFilterTimeout();
      }

      return CompletableFuture.completedFuture(new SearchResult(Optional.empty(), noValidBiome, false, cacheChecked, false, false, limitType));
   }

   private void logLimitDebug(
      World world,
      RandomTeleportSettings currentSettings,
      LocationFinder.SearchContext context,
      SearchResult.SearchLimitType limitType,
      long elapsedMillis,
      int effectiveAttemptLimit,
      BiomeSearchSettings searchSettings,
      boolean cacheFailoverAttempted,
      boolean cacheFailoverProducedLocation
   ) {
      if (currentSettings != null && currentSettings.isDebugRejectionLoggingEnabled()) {
         try {
            String worldName = world != null ? world.getName() : "unknown";
            StringBuilder sb = new StringBuilder();
            sb.append("EzRTP debug: search limit reached for world '").append(worldName).append("'\n");
            sb.append("  activeLimitType=").append(limitType).append('\n');
            sb.append("  elapsedWallClockMs=").append(elapsedMillis).append('\n');
            sb.append("  chunkLoadCount=").append(context.getChunkLoads()).append(" biomeRejectionCount=").append(context.getBiomeRejections()).append('\n');
            sb.append("  effectiveAttemptLimit=")
               .append(effectiveAttemptLimit)
               .append(" configuredMaxAttempts=")
               .append(currentSettings.getMaxAttempts())
               .append('\n');
            sb.append("  configuredBiomeSearchLimits=");
            sb.append("{wallClock=")
               .append(searchSettings.getMaxWallClockMillis())
               .append(", wallClockRare=")
               .append(searchSettings.getMaxWallClockMillisRare())
               .append(", biomeRejections=")
               .append(searchSettings.getMaxBiomeRejections())
               .append(", biomeRejectionsRare=")
               .append(searchSettings.getMaxBiomeRejectionsRare())
               .append(", chunkLoads=")
               .append(searchSettings.getMaxChunkLoads())
               .append(", chunkLoadsRare=")
               .append(searchSettings.getMaxChunkLoadsRare())
               .append(", minBiomeAttempts=")
               .append(searchSettings.getMinBiomeAttempts())
               .append(", failoverMode=")
               .append(searchSettings.getFailoverMode())
               .append("}\n");
            sb.append("  cacheFailoverAttempted=")
               .append(cacheFailoverAttempted)
               .append(" cacheFailoverProducedLocation=")
               .append(cacheFailoverProducedLocation);
            this.debugFileLogger.log(sb.toString());
         } catch (Exception var13) {
         }
      }
   }

   private void logSearchStart(World world, RandomTeleportSettings settings) {
      if (settings != null && settings.isDebugRejectionLoggingEnabled()) {
         try {
            String worldName = world != null ? world.getName() : "unknown";
            String strategyName = this.searchStrategy != null ? this.searchStrategy.getClass().getSimpleName() : "UniformSearchStrategy";
            String configuredPattern = settings.getSearchPattern() != null ? settings.getSearchPattern().getConfigKey() : "random";
            BiomeSearchSettings biomeSearchSettings = settings.getBiomeSearchSettings() != null
               ? settings.getBiomeSearchSettings()
               : BiomeSearchSettings.defaults();
            StringBuilder sb = new StringBuilder();
            sb.append("EzRTP debug: search start for world '").append(worldName).append("'\n");
            sb.append("  pattern=").append(configuredPattern).append(" strategy=").append(strategyName).append('\n');
            sb.append("  center=(").append(settings.getCenterX()).append(',').append(settings.getCenterZ()).append(")\n");
            sb.append("  radius=[")
               .append(settings.getMinimumRadius())
               .append(',')
               .append(this.resolveMaximumRadius(world, settings))
               .append("] useWorldBorder=")
               .append(settings.useWorldBorderRadius())
               .append('\n');
            sb.append("  maxAttempts=")
               .append(settings.getMaxAttempts())
               .append(" minBiomeAttempts=")
               .append(biomeSearchSettings.getMinBiomeAttempts())
               .append('\n');
            sb.append("  includeBiomes=").append(settings.getBiomeInclude()).append(" excludeBiomes=").append(settings.getBiomeExclude()).append('\n');
            this.debugFileLogger.log(sb.toString());
         } catch (Exception var8) {
         }
      }
   }

   private void logCandidateRejection(World world, RandomTeleportSettings settings, int attempt, Location candidate, String reason) {
      if (settings != null && settings.isDebugRejectionLoggingEnabled()) {
         if (attempt <= 0 || attempt % 10 == 0) {
            try {
               String worldName = world != null ? world.getName() : "unknown";
               StringBuilder sb = new StringBuilder();
               sb.append("EzRTP debug: candidate rejected in world '").append(worldName).append("'\n");
               sb.append("  reason=").append(reason).append(" attempt=").append(attempt).append('\n');
               if (candidate != null) {
                  sb.append("  candidate=(")
                     .append(candidate.getBlockX())
                     .append(',')
                     .append(candidate.getBlockY())
                     .append(',')
                     .append(candidate.getBlockZ())
                     .append(") biome=")
                     .append(candidate.getBlock().getBiome())
                     .append('\n');
               } else {
                  sb.append("  candidate=<null>\n");
               }

               sb.append("  strategy=").append(this.searchStrategy != null ? this.searchStrategy.getClass().getSimpleName() : "UniformSearchStrategy");
               this.debugFileLogger.log(sb.toString());
            } catch (Exception var8) {
            }
         }
      }
   }

   private void recordUnsafeCause(Location candidate, RandomTeleportSettings settings) {
      UnsafeLocationMonitor monitor = this.unsafeMonitor;
      if (monitor != null && monitor.isEnabled()) {
         if (candidate == null) {
            monitor.recordRejection(UnsafeLocationCause.NULL_CANDIDATE);
         } else {
            UnsafeLocationCause cause = this.validator.checkSafety(candidate, settings).orElse(UnsafeLocationCause.OTHER);
            monitor.recordRejection(cause);
         }
      }
   }

   private Optional<Location> attemptSafetyRecovery(Location unsafeLocation, RandomTeleportSettings settings) {
      if (unsafeLocation != null && settings != null) {
         SafetySettings safety = settings.getSafetySettings();
         if (safety != null && safety.isRescueEnabled()) {
            World world = unsafeLocation.getWorld();
            if (world == null) {
               return Optional.empty();
            } else {
               int maxOffset = Math.max(1, safety.getMaxVerticalRescue());
               Location probe = unsafeLocation.clone();

               for (int offset = 0; offset <= maxOffset; offset++) {
                  Location up = probe.clone().add(0.0, offset, 0.0);
                  if (this.validator.isSafe(up, settings) && !this.validator.isProtectedByClaims(up, settings)) {
                     return Optional.of(up);
                  }

                  if (offset != 0) {
                     Location down = probe.clone().add(0.0, -offset, 0.0);
                     if (this.validator.isSafe(down, settings) && !this.validator.isProtectedByClaims(down, settings)) {
                        return Optional.of(down);
                     }
                  }
               }

               return Optional.empty();
            }
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   boolean isRareSearch(RandomTeleportSettings currentSettings) {
      if (currentSettings == null || this.rareBiomeRegistry == null || !this.rareBiomeRegistry.isEnabled()) {
         return false;
      } else if (currentSettings.getRareBiomeOptimizationSettings() == null || !currentSettings.getRareBiomeOptimizationSettings().isEnabled()) {
         return false;
      } else {
         return currentSettings.getBiomeInclude().isEmpty() ? false : currentSettings.getBiomeInclude().stream().allMatch(this.rareBiomeRegistry::isRareBiome);
      }
   }

   private static final class SearchContext {
      private final BiomeSearchSettings settings;
      private final long startTime;
      private int biomeRejections;
      private int chunkLoads;
      private SearchResult.SearchLimitType limitType = SearchResult.SearchLimitType.NONE;
      private final boolean rareSearch;

      private SearchContext(BiomeSearchSettings settings, boolean rareSearch) {
         this.settings = settings != null ? settings : BiomeSearchSettings.defaults();
         this.startTime = System.currentTimeMillis();
         this.rareSearch = rareSearch;
      }

      private boolean hasLimitBeenHit() {
         if (this.limitType != SearchResult.SearchLimitType.NONE) {
            return true;
         } else {
            int wallClockLimit = this.rareSearch ? this.settings.getMaxWallClockMillisRare() : this.settings.getMaxWallClockMillis();
            if (wallClockLimit > 0 && System.currentTimeMillis() - this.startTime >= wallClockLimit) {
               this.limitType = SearchResult.SearchLimitType.WALL_CLOCK;
               return true;
            } else {
               return false;
            }
         }
      }

      private boolean recordBiomeRejection() {
         if (this.limitType != SearchResult.SearchLimitType.NONE) {
            return false;
         } else {
            this.biomeRejections++;
            int limit = this.rareSearch ? this.settings.getMaxBiomeRejectionsRare() : this.settings.getMaxBiomeRejections();
            if (limit > 0 && this.biomeRejections > limit) {
               this.limitType = SearchResult.SearchLimitType.BIOME_REJECTIONS;
               return false;
            } else {
               return true;
            }
         }
      }

      private boolean tryIncrementChunkLoad() {
         if (this.limitType != SearchResult.SearchLimitType.NONE) {
            return false;
         } else {
            this.chunkLoads++;
            int limit = this.rareSearch ? this.settings.getMaxChunkLoadsRare() : this.settings.getMaxChunkLoads();
            if (limit > 0 && this.chunkLoads > limit) {
               this.limitType = SearchResult.SearchLimitType.CHUNK_LOADS;
               return false;
            } else {
               return true;
            }
         }
      }

      private SearchResult.SearchLimitType getLimitType() {
         return this.limitType;
      }

      private BiomeSearchSettings getSettings() {
         return this.settings;
      }

      private long getElapsedMillis() {
         return System.currentTimeMillis() - this.startTime;
      }

      private int getBiomeRejections() {
         return this.biomeRejections;
      }

      private int getChunkLoads() {
         return this.chunkLoads;
      }
   }
}
