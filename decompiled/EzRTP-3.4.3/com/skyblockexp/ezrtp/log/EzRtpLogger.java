package com.skyblockexp.ezrtp.log;

import java.util.Locale;
import java.util.logging.Logger;

public final class EzRtpLogger {
   private final Logger logger;

   public EzRtpLogger(Logger logger) {
      this.logger = logger;
   }

   public void logReady() {
      this.logger.info("Ready.");
   }

   public void logDisabled() {
      this.logger.info("EzRTP plugin disabled.");
   }

   public void logRuntimeModule(String modules) {
      this.logger.info("Runtime module: " + modules);
   }

   public void warnNoRuntimeModule() {
      this.logger.warning("No runtime module installed; using built-in Bukkit adapters.");
   }

   public void severeUnsupportedModuleCombination(String unsupported) {
      this.logger.severe("Unsupported EzRTP runtime module combination detected: " + unsupported + ".");
      this.logger.severe("Likely symptoms include duplicate chat messages and duplicate/random double teleports.");
      this.logger
         .severe(
            "Remove unsupported EzRTP runtime module jar(s) and keep only 'EzRTP.jar' and optionally one supported module plugin such as 'EzRTPPaperModule'."
         );
   }

   public void logPlatform(String runtime, String scheduler, String strategy) {
      this.logger.info("Platform: " + runtime + " | " + scheduler + " | " + strategy);
   }

   public void logAdapters(String gui, String messages, String sender) {
      this.logger.info("Adapters: GUI=" + gui + " | Messages=" + messages + " | Sender=" + sender);
   }

   public void logChunkyNotFound() {
      this.logger.info("Chunky plugin not found - Chunky integration disabled.");
   }

   public void warnChunkyInitFailed(String message) {
      this.logger.warning("Failed to initialize Chunky integration: " + message);
   }

   public void logChunkyDisabledByConfig() {
      this.logger.info("Chunky integration disabled in configuration.");
   }

   public void logChunkyMemorySafety(long freeMb, long thresholdMb) {
      this.logger.info(String.format("Chunky: memory safety enabled (%dMB free, threshold %dMB)", freeMb, thresholdMb));
   }

   public void logChunkyGenerationCompleted() {
      this.logger.info("Chunky generation completed");
   }

   public void warnChunkyListenerFailed(String message) {
      this.logger.warning("Failed to register Chunky listeners: " + message);
   }

   public void warnChunkyCleanupScheduleFailed(String message) {
      this.logger.warning("Failed to schedule ChunkyWarmupCoordinator cleanup: " + message);
   }

   public void warnCostsNoVault() {
      this.logger.warning("Teleport costs are configured but Vault is unavailable. Teleports will not deduct currency.");
   }

   public void warnSaveLanguageFileFailed(String message) {
      this.logger.warning("Could not save default language file: " + message);
   }

   public void logLegacyMessagesMigrated(String filename) {
      this.logger.info("Detected legacy messages.yml - migrated to messages/" + filename);
   }

   public void warnLegacyMessagesMigrationFailed(String message) {
      this.logger.warning("Failed to migrate legacy messages.yml: " + message);
   }

   public void warnSaveDefaultResourceFailed(String resource, String message) {
      this.logger.warning("Unable to save default resource '" + resource + "': " + message);
   }

   public void logVaultNotFound() {
      this.logger.info("Vault not found. Random teleport costs will be disabled.");
   }

   public void warnVaultNoProvider() {
      this.logger.warning("Vault is installed but no economy provider is registered. Random teleport costs will be disabled.");
   }

   public void logEconomy(String providerName) {
      this.logger.info("Economy: " + providerName + " (Vault)");
   }

   public void warnWithdrawFailed(double amount, String playerName, String response) {
      this.logger.warning(String.format(Locale.US, "Failed to withdraw %.2f from %s: %s", amount, playerName, response));
   }

   public void warnMessagesDirectoryMissing() {
      this.logger.warning("Messages directory does not exist, using defaults");
   }

   public void warnLanguageFileMissing(String language) {
      this.logger.warning("Language file for '" + language + "' not found, using defaults");
   }

   public void warnMissingMessageKey(String key, String language) {
      this.logger.warning("Missing message key: " + key + " in language file: " + language);
   }

   public void logMigratedLegacyMessageKey() {
      this.logger.info("Migrated legacy message key 'forcertp-target-notify' to 'forcertp-target-notification'");
   }

   public void logLocale(String language, int count) {
      this.logger.info("Locale: " + language + " (" + count + " messages)");
   }

   public void severeLangFileFailed(String language, String message) {
      this.logger.severe("Failed to load language file for '" + language + "': " + message);
   }

   public void logDailyUsageReset() {
      this.logger.info("Daily RTP usage counts reset.");
   }

   public void logWeeklyUsageReset() {
      this.logger.info("Weekly RTP usage counts reset.");
   }

   public void logMetricsEnabled(String url) {
      this.logger.info("Metrics: bStats (" + url + ")");
   }

   public void logMetricsDisabled() {
      this.logger.info("bStats metrics disabled in config.");
   }

   public void warnMetricsFailed(String message) {
      this.logger.warning("Failed to start bStats metrics: " + message);
   }

   public void fineRegisteredOptionalProvider(String className) {
      this.logger.fine("Registered optional provider: " + className);
   }

   public void warnOptionalProviderFailed(String className, String message) {
      this.logger.warning("Failed to register optional provider " + className + ": " + message);
   }

   public void fineRegisteredChunkLoadStrategy(String className) {
      this.logger.fine("Registered ChunkLoadStrategy provider: " + className);
   }

   public void fineRegisteredGuiBridge(String className) {
      this.logger.fine("Registered PlatformGuiBridge provider: " + className);
   }

   public void fineRegisteredMessageService(String className) {
      this.logger.fine("Registered PlatformMessageService provider: " + className);
   }

   public void fineRegisteredPlatformRuntime(String className) {
      this.logger.fine("Registered PlatformRuntime provider: " + className);
   }

   public void fineRegisteredSenderBridge(String className) {
      this.logger.fine("Registered PlatformSenderBridge provider: " + className);
   }

   public void logChunkLoadingPaperAsync() {
      this.logger.info("Chunk loading: Paper async API");
   }

   public void warnPaperAsyncAlwaysForcedOnNonPaper() {
      this.logger
         .warning(
            "chunk-loading.use-paper-async-api is set to 'always' but Paper API was not detected. The async fast-path will still run but chunk loading will not be genuinely non-blocking."
         );
   }

   public void warnChunkLoadFailed(String message) {
      this.logger.warning("Failed to load chunk: " + message);
   }

   public void warnChunkLoadQueueFallback(String reason) {
      this.logger.warning("Chunk load queue fallback engaged; using immediate synchronous loading. Reason: " + reason);
   }

   public void warnChunkLoadSyncFailed(String message) {
      this.logger.warning("Failed to synchronously load chunk after queue fallback: " + message);
   }

   public void warnCriticallyLowMemory(long freeMb, long totalMb) {
      this.logger.warning(String.format("Teleport cancelled due to critically low memory: %dMB free / %dMB total", freeMb, totalMb));
   }

   public void warnLowMemoryCacheOnly(long freeMb) {
      this.logger.warning(String.format("Low memory detected (%dMB free), using cache-only mode", freeMb));
   }

   public void logReducedAttemptsMemory(int from, int to, long freeMb) {
      this.logger.info(String.format("Reducing max attempts from %d to %d due to low memory (%dMB free)", from, to, freeMb));
   }

   public void fineReducedCacheGenMemory(int from, int to, long freeMb) {
      this.logger.fine(String.format("Reducing cache generation attempts from %d to %d due to low memory (%dMB free)", from, to, freeMb));
   }

   public void fineSkipChunkyLowMemory() {
      this.logger.fine("Skipping Chunky pregeneration due to low memory");
   }

   public void logRtpRejected(String location, String reason) {
      this.logger.info("RTP rejected " + location + ": " + reason);
   }

   public void warnDebugLocationSearchFailed(String playerName, String worldName, long durationMs, String exClass, String exMsg) {
      this.logger
         .warning(
            "EzRTP debug: location search failed for player '"
               + playerName
               + "' in world '"
               + worldName
               + "' after "
               + durationMs
               + "ms: "
               + exClass
               + " - "
               + exMsg
         );
   }

   public void warnDebugFailureBreadcrumb(
      String playerName,
      String worldName,
      String reasonClass,
      String limitType,
      boolean noValidBiome,
      boolean fallbackUsed,
      boolean noCacheAvailable,
      long durationMs,
      boolean cacheHit
   ) {
      this.logger
         .warning(
            "EzRTP debug: failure breadcrumb player="
               + playerName
               + " world="
               + worldName
               + " reasonClass="
               + reasonClass
               + " limitType="
               + limitType
               + " noValidBiome="
               + noValidBiome
               + " fallbackUsed="
               + fallbackUsed
               + " noCacheAvailable="
               + noCacheAvailable
               + " durationMs="
               + durationMs
               + " cacheHit="
               + cacheHit
         );
   }

   public void warnBiomePreCacheDisableMemory(long freeMb, long totalMb) {
      this.logger.warning(String.format("Disabling biome pre-cache due to low memory: %dMB free / %dMB total", freeMb, totalMb));
   }

   public void logBiomePreCacheAutoEnabled(long freeMb, long totalMb) {
      this.logger.info(String.format("Biome filters detected; auto-enabling pre-cache as configured (Memory: %dMB free / %dMB total)", freeMb, totalMb));
   }

   public void warnBiomePreCacheMemoryLow(long freeMb, long totalMb) {
      this.logger.warning(String.format("Biome filters detected but pre-cache not enabled due to low memory: %dMB free / %dMB total", freeMb, totalMb));
   }

   public void warnBiomeNoCacheNoFilter() {
      this.logger.warning("Biome filtering is configured but pre-caching is disabled.");
   }

   public void warnBiomeNoCacheRecommendation() {
      this.logger.warning("RECOMMENDATION: Enable 'biomes.pre-cache.enabled: true' in config.yml to improve RTP success rates.");
   }

   public void logSkipChunkyBiomeCacheLowMemory() {
      this.logger.info("Skipping Chunky pregeneration for biome pre-caching due to low memory");
   }

   public void logStartedChunkyPregen(String worldName) {
      this.logger.info("Started Chunky pregeneration for world '" + worldName + "' to support biome pre-caching.");
   }

   public void warnChunkyApiFailed(String message) {
      this.logger.warning("Failed to interact with Chunky API: " + message);
   }

   public void logBiomeCacheWarmupStarted(String worldName, long freeMb, long totalMb) {
      this.logger.info(String.format("Starting biome cache warmup for world '%s' (Memory: %dMB free / %dMB total)", worldName, freeMb, totalMb));
   }

   public void logBiomeFiltersRareOptimizations() {
      this.logger.info("Biome filters detected; auto-enabling rare biome optimizations as configured.");
   }

   public void warnBiomeCacheSkipWarmupLowMemory(String worldName, long freeMb, long totalMb) {
      this.logger.warning(String.format("[BiomeCache] Skipping warmup for world '%s' due to low memory: %dMB free / %dMB total", worldName, freeMb, totalMb));
   }

   public void logBiomeCacheWarmedUp(int count, String worldName, long freeMb, long totalMb) {
      this.logger.info(String.format("[BiomeCache] Warmed up %d locations for world '%s' (Memory: %dMB free / %dMB total)", count, worldName, freeMb, totalMb));
   }

   public void fineBiomeCacheWarmupBatch(String worldName, int completed, int total, long freeMb, long totalMb) {
      this.logger
         .fine(
            String.format(
               "[BiomeCache] Starting warmup batch for world '%s' (completed: %d/%d, Memory: %dMB free / %dMB total)",
               worldName,
               completed,
               total,
               freeMb,
               totalMb
            )
         );
   }

   public void logBiomeCacheReduceBatchSize(int size, long freeKb) {
      this.logger.info(String.format("[BiomeCache] Reducing warmup batch size to %d due to memory constraints (%dKB free)", size, freeKb));
   }

   public void fineBiomeCacheChunkBatch(int size, String worldName, long freeMb) {
      this.logger.fine(String.format("[BiomeCache] Processing %d chunk-based warmup locations for world '%s' (Memory: %dMB free)", size, worldName, freeMb));
   }

   public void logBiomeCacheWarmedUpFinal(int count, String worldName) {
      this.logger.info(String.format("[BiomeCache] Warmed up %d locations for world '%s'", count, worldName));
   }

   public void warnBiomeCacheAbortWarmupLowMemory(String worldName, long freeMb) {
      this.logger.warning(String.format("[BiomeCache] Aborting warmup attempt for world '%s' due to critically low memory: %dMB free", worldName, freeMb));
   }

   public void fineBiomeCacheWarmupAttempt(String worldName, long freeMb) {
      this.logger.fine(String.format("[BiomeCache] Attempting warmup location for world '%s' (Memory: %dMB free)", worldName, freeMb));
   }

   public void fineBiomeCacheCachedLocation(String location, String worldName) {
      this.logger.fine(String.format("[BiomeCache] Successfully cached location at %s in world '%s'", location, worldName));
   }

   public void warnBiomeCacheWarmupInterrupted() {
      this.logger.warning("[BiomeCache] Warmup interrupted - plugin may be shutting down");
   }

   public void fineBiomeCacheFailedGenerate(String worldName) {
      this.logger.fine(String.format("[BiomeCache] Failed to generate warmup location for world '%s'", worldName));
   }

   public void fineBiomeCachedLocation(String biome, String worldName, int total, long freeMb) {
      this.logger.fine(String.format("[BiomeCache] Cached location for %s in world '%s' (total: %d, Memory: %dMB free)", biome, worldName, total, freeMb));
   }

   public void warnHotspotTableCreateFailed(String message) {
      this.logger.warning("Failed to create hotspot table: " + message);
   }

   public void warnHotspotSaveFailed(String message) {
      this.logger.warning("Failed to save hotspot: " + message);
   }

   public void warnHotspotLoadFailed(String message) {
      this.logger.warning("Failed to load hotspots: " + message);
   }

   public void warnHotspotCloseFailed(String message) {
      this.logger.warning("Failed to close hotspot storage: " + message);
   }

   public void warnMySqlHotspotStorageInitFailed(String message) {
      this.logger.warning("Failed to initialise MySQL hotspot storage, falling back to in-memory only: " + message);
   }

   public void warnSlowRtp(long durationMs, String worldName, long thresholdMs) {
      this.logger.warning(String.format("Slow RTP: %dms in world '%s' (threshold %dms)", durationMs, worldName, thresholdMs));
   }

   public void warnPerformanceMetricsWriteFailed(String filePath, String message) {
      this.logger.warning("Failed to write performance metrics to '" + filePath + "': " + message);
   }

   public void warnSlowRtpWriteFailed(String filePath, String message) {
      this.logger.warning("Failed to write slow-RTP warning to '" + filePath + "': " + message);
   }

   public void warnHighUnsafeRate(double rate, int unsafe, int total, int minutes) {
      this.logger.warning(String.format("High unsafe location rejection rate: %.1f%% (%d/%d RTPs) in the last %d minutes", rate, unsafe, total, minutes));
   }

   public void warnUnsafeMetricsWriteFailed(String filePath, String message) {
      this.logger.warning("Failed to write unsafe-location metrics to '" + filePath + "': " + message);
   }

   public void warnUnsafeLocationWriteFailed(String filePath, String message) {
      this.logger.warning("Failed to write unsafe-location warning to '" + filePath + "': " + message);
   }

   public void warnHeatmapDisabled(String playerName) {
      this.logger.warning("EzRTP: Heatmap command invoked by " + playerName + " but heatmap.enabled is false in rtp.yml. Enable it to use heatmap features.");
   }

   public void warnHeatmapCreateFailed(String message) {
      this.logger.warning("Failed to create heatmap map: " + message);
   }

   public void logHeatmapSaved(String path) {
      this.logger.info("Heatmap saved to: " + path);
   }

   public void warnHeatmapPngSaveFailed(String message) {
      this.logger.warning("Failed to save heatmap PNG: " + message);
   }

   public void warnUpdateCheckEmptyResponse() {
      this.logger.warning("SpigotMC update check returned an empty response.");
   }

   public void logUpToDate() {
      this.logger.info("EzRTP is up to date.");
   }

   public void warnUpdateCheckFailed(String message) {
      this.logger.warning("Failed to check for EzRTP updates: " + message);
   }

   public void fineDebugLogWriteFailed(String message) {
      this.logger.fine("Failed to write debug log file: " + message);
   }

   public void warnGriefPreventionNoGetClaimAt() {
      this.logger.warning("GriefPrevention getClaimAt method not found; RTP protection will be disabled for this provider.");
   }

   public void warnGriefPreventionDataStoreNull() {
      this.logger.warning("GriefPrevention dataStore field is null; RTP protection will be disabled for this provider.");
   }

   public void warnProtectionNoProviders(String missing) {
      this.logger
         .warning(
            "EzRTP: avoid-claims is enabled but none of the configured protection providers "
               + missing
               + " are available. Protected regions will NOT be avoided. Install WorldGuard or GriefPrevention, or set avoid-claims: false in rtp.yml."
         );
   }

   public void warnProtectionProviderUnavailable(String providerId) {
      this.logger
         .warning("EzRTP protection provider '" + providerId + "' is configured but not available; protected regions will not be avoided for this provider.");
   }

   public void warnNetworkPollingFailed(String message) {
      this.logger.warning("Unable to start network status polling: " + message);
   }

   public void warnNetworkTransferFailed(String server, String message) {
      this.logger.warning(String.format("Failed to prepare network transfer to '%s': %s", server, message));
   }

   public void warnNamedCenterMissingWorld(String name) {
      this.logger.warning("Named center '" + name + "' is missing a world and was skipped.");
   }

   public void warnInvalidBossBarColor(String name) {
      this.logger.warning("Invalid bossbar color: " + name);
   }

   public void warnInvalidBossBarStyle(String name) {
      this.logger.warning("Invalid bossbar style: " + name);
   }

   public void warnInvalidParticle(String name) {
      this.logger.warning("Invalid particle type: " + name);
   }

   public void warnInvalidGuiSlot(String worldKey, int slot) {
      this.logger.warning(String.format("GUI world option '%s' uses an invalid slot '%d'. Skipping entry.", worldKey, slot));
   }

   public void warnDuplicateGuiSlot(int slot, String worldKey) {
      this.logger.warning(String.format("Duplicate GUI slot '%d' detected for world option '%s'. Skipping entry.", slot, worldKey));
   }

   public void warnNetworkServerMissingIdentifier(String key) {
      this.logger.warning(String.format("Network server '%s' is missing a valid 'bungee-server' identifier.", key));
   }

   public void warnNetworkServerInvalidPort(String key, int port) {
      this.logger.warning(String.format("Network server '%s' specifies an invalid port '%d'.", key, port));
   }

   public void warnNetworkServerInvalidIcon(String key) {
      this.logger.warning(String.format("Network server '%s' has an invalid icon configuration.", key));
   }

   public void warnGuiNetworkServerInvalidSlot(String serverId, int slot) {
      this.logger.warning(String.format("Network server '%s' uses an invalid slot '%d'. Skipping entry.", serverId, slot));
   }

   public void warnGuiNetworkServerSlotOccupied(String serverId, int slot) {
      this.logger.warning(String.format("Network server '%s' cannot use GUI slot '%d' because it is already occupied. Skipping entry.", serverId, slot));
   }

   public void logPapiFound() {
      this.logger.info("PlaceholderAPI found and enabled. Placeholder support is available.");
   }

   public void logPapiNotFound() {
      this.logger.info("PlaceholderAPI not found. Placeholder resolution will use fallback behavior.");
   }

   public void logPapiNotAvailable() {
      this.logger.info("PlaceholderAPI not available. Placeholder resolution will use fallback behavior.");
   }

   public void warnPlaceholderResolveFailed(String message) {
      this.logger.warning("Failed to resolve placeholders: " + message);
   }

   public void warnComponentPlaceholderFailed(String message) {
      this.logger.warning("Failed to resolve component placeholders: " + message);
   }

   public void logLoreUtilSkipped() {
      this.logger.info("LoreUtil.setLore: rawLore is empty, skipping");
   }

   public void logLoreUtilRawLore(String rawLore) {
      this.logger.info("LoreUtil.setLore: rawLore = " + rawLore);
   }

   public void logLoreUtilHasMethod(boolean hasMethod) {
      this.logger.info("LoreUtil.setLore: hasMethod = " + hasMethod);
   }

   public void logLoreUtilPaperApi() {
      this.logger.info("LoreUtil.setLore: Using Paper API");
   }

   public void logLoreUtilSetPaperLore(String lore) {
      this.logger.info("LoreUtil.setLore: Set Paper lore: " + lore);
   }

   public void logLoreUtilSpigotApi() {
      this.logger.info("LoreUtil.setLore: Using Spigot API");
   }

   public void logLoreUtilResolvedLine(String line) {
      this.logger.info("LoreUtil.setLore: resolvedLine = " + line);
   }

   public void logLoreUtilLegacy(String legacy) {
      this.logger.info("LoreUtil.setLore: legacy = " + legacy);
   }

   public void logLoreUtilFinalLore(String lore) {
      this.logger.info("LoreUtil.setLore: final lore = " + lore);
   }

   public void logLoreUtilMetaHasLore(boolean hasLore) {
      this.logger.info("LoreUtil.setLore: meta.hasLore() = " + hasLore);
   }

   public void logLoreUtilGetLore(String lore) {
      this.logger.info("LoreUtil.setLore: meta.getLore() = " + lore);
   }

   public void warnInvalidMiniMessage(String context, String miniMessageString, String message) {
      this.logger.warning("Invalid MiniMessage in " + context + ": '" + miniMessageString + "' - " + message);
   }

   public void logPaperAdaptersRegistered(int count) {
      this.logger.info("Registered " + count + " Paper platform adapters for EzRTP.");
   }

   public void warnNoPaperRegistriesAccessible() {
      this.logger.warning("No EzRTP registries were accessible from EzRTPPaperModule. This is safe, but module providers were not registered.");
   }

   public void logPaperModuleDisabled() {
      this.logger.info("EzRTP Paper platform module disabled.");
   }

   public void warnPaperRegistryNoRegisterProvider(String className) {
      this.logger.warning("Registry does not expose registerProvider: " + className);
   }

   public void fineRegisteredPaperProvider(String providerClass, String registryClass) {
      this.logger.fine("Registered Paper provider " + providerClass + " via " + registryClass);
   }

   public void fineSkipPaperProvider(String providerClass, String exceptionClass, String message) {
      this.logger.fine("Skipping Paper provider registration for " + providerClass + ": " + exceptionClass + " - " + message);
   }

   public void finePlaceholderApiResolutionFailed(String message) {
      this.logger.fine("PlaceholderAPI resolution failed: " + message);
   }
}
