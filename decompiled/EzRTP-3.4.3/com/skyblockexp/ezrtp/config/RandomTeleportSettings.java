package com.skyblockexp.ezrtp.config;

import com.skyblockexp.ezrtp.config.biome.BiomePreCacheSettings;
import com.skyblockexp.ezrtp.config.biome.BiomeSearchSettings;
import com.skyblockexp.ezrtp.config.biome.ChunkyIntegrationSettings;
import com.skyblockexp.ezrtp.config.biome.RareBiomeOptimizationSettings;
import com.skyblockexp.ezrtp.config.effects.CountdownBossBarSettings;
import com.skyblockexp.ezrtp.config.effects.CountdownParticleSettings;
import com.skyblockexp.ezrtp.config.effects.ParticleSettings;
import com.skyblockexp.ezrtp.config.pvptag.PvpTagIntegrationSettings;
import com.skyblockexp.ezrtp.config.safety.ProtectionSettings;
import com.skyblockexp.ezrtp.config.safety.SafetySettings;
import com.skyblockexp.ezrtp.config.teleport.ChunkLoadingSettings;
import com.skyblockexp.ezrtp.config.teleport.OnJoinTeleportSettings;
import com.skyblockexp.ezrtp.config.teleport.SearchPattern;
import com.skyblockexp.ezrtp.config.teleport.TeleportMessages;
import com.skyblockexp.ezrtp.util.compat.BiomeCompat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

public final class RandomTeleportSettings {
   private final ConfigurationSection configSection;
   private final String worldName;
   private final int centerX;
   private final int centerZ;
   private final int minimumRadius;
   private final int maximumRadius;
   private final int maxAttempts;
   private final boolean useWorldBorderRadius;
   private final Set<Material> unsafeBlocks;
   private final TeleportMessages messages;
   private final ParticleSettings particleSettings;
   private final OnJoinTeleportSettings onJoinTeleportSettings;
   private final CountdownBossBarSettings countdownBossBarSettings;
   private final CountdownParticleSettings countdownParticleSettings;
   private final double teleportCost;
   private final int countdownSeconds;
   private final boolean countdownChatMessagesEnabled;
   private final boolean debugRejectionLogging;
   private final Integer minY;
   private final Integer maxY;
   private final Set<Biome> biomeInclude;
   private final Set<Biome> biomeExclude;
   private final ProtectionSettings protectionSettings;
   private final BiomePreCacheSettings preCacheSettings;
   private final RareBiomeOptimizationSettings rareBiomeOptimizationSettings;
   private final ChunkLoadingSettings chunkLoadingSettings;
   private final boolean enableFallbackToCache;
   private final BiomeSearchSettings biomeSearchSettings;
   private final boolean biomeFilteringEnabled;
   private final boolean biomeSystemEnabled;
   private final SafetySettings safetySettings;
   private final SearchPattern searchPattern;
   private final ChunkyIntegrationSettings chunkyIntegrationSettings;
   private final boolean suppressPlayerMessages;
   private final boolean cancelOnMove;
   private final double cancelDistance;
   private final double warnDistance;
   private final PvpTagIntegrationSettings pvpTagIntegrationSettings;
   private final boolean heatmapEnabled;
   private final EzCountdownIntegrationSettings ezCountdownIntegrationSettings;

   public RandomTeleportSettings(
      ConfigurationSection configSection,
      String worldName,
      int centerX,
      int centerZ,
      int minimumRadius,
      int maximumRadius,
      int maxAttempts,
      boolean useWorldBorderRadius,
      Set<Material> unsafeBlocks,
      TeleportMessages messages,
      ParticleSettings particleSettings,
      OnJoinTeleportSettings onJoinTeleportSettings,
      CountdownBossBarSettings countdownBossBarSettings,
      CountdownParticleSettings countdownParticleSettings,
      double teleportCost,
      int countdownSeconds,
      boolean countdownChatMessagesEnabled,
      boolean debugRejectionLogging,
      Integer minY,
      Integer maxY,
      Set<Biome> biomeInclude,
      Set<Biome> biomeExclude,
      ProtectionSettings protectionSettings,
      BiomePreCacheSettings preCacheSettings,
      RareBiomeOptimizationSettings rareBiomeOptimizationSettings,
      ChunkLoadingSettings chunkLoadingSettings,
      boolean enableFallbackToCache,
      BiomeSearchSettings biomeSearchSettings,
      boolean biomeFilteringEnabled,
      boolean biomeSystemEnabled,
      SafetySettings safetySettings,
      SearchPattern searchPattern,
      ChunkyIntegrationSettings chunkyIntegrationSettings
   ) {
      this(
         configSection,
         worldName,
         centerX,
         centerZ,
         minimumRadius,
         maximumRadius,
         maxAttempts,
         useWorldBorderRadius,
         unsafeBlocks,
         messages,
         particleSettings,
         onJoinTeleportSettings,
         countdownBossBarSettings,
         countdownParticleSettings,
         teleportCost,
         countdownSeconds,
         countdownChatMessagesEnabled,
         debugRejectionLogging,
         minY,
         maxY,
         biomeInclude,
         biomeExclude,
         protectionSettings,
         preCacheSettings,
         rareBiomeOptimizationSettings,
         chunkLoadingSettings,
         enableFallbackToCache,
         biomeSearchSettings,
         biomeFilteringEnabled,
         biomeSystemEnabled,
         safetySettings,
         searchPattern,
         chunkyIntegrationSettings,
         false,
         true,
         2.0,
         1.0,
         PvpTagIntegrationSettings.defaults(),
         false,
         EzCountdownIntegrationSettings.disabled()
      );
   }

   public RandomTeleportSettings(
      ConfigurationSection configSection,
      String worldName,
      int centerX,
      int centerZ,
      int minimumRadius,
      int maximumRadius,
      int maxAttempts,
      boolean useWorldBorderRadius,
      Set<Material> unsafeBlocks,
      TeleportMessages messages,
      ParticleSettings particleSettings,
      OnJoinTeleportSettings onJoinTeleportSettings,
      CountdownBossBarSettings countdownBossBarSettings,
      CountdownParticleSettings countdownParticleSettings,
      double teleportCost,
      int countdownSeconds,
      boolean countdownChatMessagesEnabled,
      boolean debugRejectionLogging,
      Integer minY,
      Integer maxY,
      Set<Biome> biomeInclude,
      Set<Biome> biomeExclude,
      ProtectionSettings protectionSettings,
      BiomePreCacheSettings preCacheSettings,
      RareBiomeOptimizationSettings rareBiomeOptimizationSettings,
      ChunkLoadingSettings chunkLoadingSettings,
      boolean enableFallbackToCache,
      BiomeSearchSettings biomeSearchSettings,
      boolean biomeFilteringEnabled,
      boolean biomeSystemEnabled,
      SafetySettings safetySettings,
      SearchPattern searchPattern,
      ChunkyIntegrationSettings chunkyIntegrationSettings,
      boolean suppressPlayerMessages
   ) {
      this(
         configSection,
         worldName,
         centerX,
         centerZ,
         minimumRadius,
         maximumRadius,
         maxAttempts,
         useWorldBorderRadius,
         unsafeBlocks,
         messages,
         particleSettings,
         onJoinTeleportSettings,
         countdownBossBarSettings,
         countdownParticleSettings,
         teleportCost,
         countdownSeconds,
         countdownChatMessagesEnabled,
         debugRejectionLogging,
         minY,
         maxY,
         biomeInclude,
         biomeExclude,
         protectionSettings,
         preCacheSettings,
         rareBiomeOptimizationSettings,
         chunkLoadingSettings,
         enableFallbackToCache,
         biomeSearchSettings,
         biomeFilteringEnabled,
         biomeSystemEnabled,
         safetySettings,
         searchPattern,
         chunkyIntegrationSettings,
         suppressPlayerMessages,
         true,
         2.0,
         1.0,
         PvpTagIntegrationSettings.defaults(),
         false,
         EzCountdownIntegrationSettings.disabled()
      );
   }

   public RandomTeleportSettings(
      ConfigurationSection configSection,
      String worldName,
      int centerX,
      int centerZ,
      int minimumRadius,
      int maximumRadius,
      int maxAttempts,
      boolean useWorldBorderRadius,
      Set<Material> unsafeBlocks,
      TeleportMessages messages,
      ParticleSettings particleSettings,
      OnJoinTeleportSettings onJoinTeleportSettings,
      CountdownBossBarSettings countdownBossBarSettings,
      CountdownParticleSettings countdownParticleSettings,
      double teleportCost,
      int countdownSeconds,
      boolean countdownChatMessagesEnabled,
      boolean debugRejectionLogging,
      Integer minY,
      Integer maxY,
      Set<Biome> biomeInclude,
      Set<Biome> biomeExclude,
      ProtectionSettings protectionSettings,
      BiomePreCacheSettings preCacheSettings,
      RareBiomeOptimizationSettings rareBiomeOptimizationSettings,
      ChunkLoadingSettings chunkLoadingSettings,
      boolean enableFallbackToCache,
      BiomeSearchSettings biomeSearchSettings,
      boolean biomeFilteringEnabled,
      boolean biomeSystemEnabled,
      SafetySettings safetySettings,
      SearchPattern searchPattern,
      ChunkyIntegrationSettings chunkyIntegrationSettings,
      boolean suppressPlayerMessages,
      boolean cancelOnMove,
      double cancelDistance,
      double warnDistance,
      PvpTagIntegrationSettings pvpTagIntegrationSettings,
      boolean heatmapEnabled,
      EzCountdownIntegrationSettings ezCountdownIntegrationSettings
   ) {
      this.configSection = configSection;
      this.worldName = worldName;
      this.centerX = centerX;
      this.centerZ = centerZ;
      this.minimumRadius = minimumRadius;
      this.maximumRadius = maximumRadius;
      this.maxAttempts = maxAttempts;
      this.useWorldBorderRadius = useWorldBorderRadius;
      this.unsafeBlocks = unsafeBlocks;
      this.messages = messages;
      this.particleSettings = particleSettings;
      this.onJoinTeleportSettings = onJoinTeleportSettings;
      this.countdownBossBarSettings = countdownBossBarSettings;
      this.countdownParticleSettings = countdownParticleSettings;
      this.teleportCost = teleportCost;
      this.countdownSeconds = countdownSeconds;
      this.countdownChatMessagesEnabled = countdownChatMessagesEnabled;
      this.debugRejectionLogging = debugRejectionLogging;
      this.minY = minY;
      this.maxY = maxY;
      this.biomeInclude = biomeInclude != null ? biomeInclude : Collections.emptySet();
      this.biomeExclude = biomeExclude != null ? biomeExclude : Collections.emptySet();
      this.protectionSettings = protectionSettings != null ? protectionSettings : new ProtectionSettings(false, Collections.emptyList());
      this.preCacheSettings = preCacheSettings != null ? preCacheSettings : BiomePreCacheSettings.disabled();
      this.rareBiomeOptimizationSettings = rareBiomeOptimizationSettings != null ? rareBiomeOptimizationSettings : RareBiomeOptimizationSettings.disabled();
      this.chunkLoadingSettings = chunkLoadingSettings != null ? chunkLoadingSettings : ChunkLoadingSettings.defaults();
      this.enableFallbackToCache = enableFallbackToCache;
      this.biomeSearchSettings = biomeSearchSettings != null ? biomeSearchSettings : BiomeSearchSettings.defaults();
      this.biomeFilteringEnabled = biomeFilteringEnabled;
      this.biomeSystemEnabled = biomeSystemEnabled;
      this.safetySettings = safetySettings != null ? safetySettings : SafetySettings.defaults();
      this.searchPattern = searchPattern != null ? searchPattern : SearchPattern.RANDOM;
      this.chunkyIntegrationSettings = chunkyIntegrationSettings != null ? chunkyIntegrationSettings : ChunkyIntegrationSettings.defaults();
      this.suppressPlayerMessages = suppressPlayerMessages;
      this.cancelOnMove = cancelOnMove;
      this.cancelDistance = Math.max(0.0, cancelDistance);
      this.warnDistance = Math.max(0.0, warnDistance);
      this.pvpTagIntegrationSettings = pvpTagIntegrationSettings != null ? pvpTagIntegrationSettings : PvpTagIntegrationSettings.defaults();
      this.heatmapEnabled = heatmapEnabled;
      this.ezCountdownIntegrationSettings = ezCountdownIntegrationSettings != null ? ezCountdownIntegrationSettings : EzCountdownIntegrationSettings.disabled();
   }

   public Integer getMinY() {
      return this.minY;
   }

   public Integer getMaxY() {
      return this.maxY;
   }

   public boolean isDebugRejectionLoggingEnabled() {
      return this.debugRejectionLogging;
   }

   public Set<Biome> getBiomeInclude() {
      return this.biomeInclude;
   }

   public Set<Biome> getBiomeExclude() {
      return this.biomeExclude;
   }

   public ProtectionSettings getProtectionSettings() {
      return this.protectionSettings;
   }

   public BiomePreCacheSettings getPreCacheSettings() {
      return this.preCacheSettings;
   }

   public RareBiomeOptimizationSettings getRareBiomeOptimizationSettings() {
      return this.rareBiomeOptimizationSettings;
   }

   public ChunkLoadingSettings getChunkLoadingSettings() {
      return this.chunkLoadingSettings;
   }

   public boolean isEnableFallbackToCache() {
      return this.enableFallbackToCache;
   }

   public BiomeSearchSettings getBiomeSearchSettings() {
      return this.biomeSearchSettings;
   }

   public boolean isBiomeFilteringEnabled() {
      return this.biomeFilteringEnabled;
   }

   public boolean isBiomeSystemEnabled() {
      return this.biomeSystemEnabled;
   }

   public SafetySettings getSafetySettings() {
      return this.safetySettings;
   }

   public SearchPattern getSearchPattern() {
      return this.searchPattern;
   }

   public boolean isSuppressPlayerMessages() {
      return this.suppressPlayerMessages;
   }

   public boolean isCancelOnMove() {
      return this.cancelOnMove;
   }

   public double getCancelDistance() {
      return this.cancelDistance;
   }

   public double getWarnDistance() {
      return this.warnDistance;
   }

   public String getWorldName() {
      return this.worldName;
   }

   public int getCenterX() {
      return this.centerX;
   }

   public int getCenterZ() {
      return this.centerZ;
   }

   public int getMinimumRadius() {
      return this.minimumRadius;
   }

   public int getMaximumRadius() {
      return this.maximumRadius;
   }

   public int getMaxAttempts() {
      return this.maxAttempts;
   }

   public boolean useWorldBorderRadius() {
      return this.useWorldBorderRadius;
   }

   public Set<Material> getUnsafeBlocks() {
      return this.unsafeBlocks;
   }

   public TeleportMessages getMessages() {
      return this.messages;
   }

   public ParticleSettings getParticleSettings() {
      return this.particleSettings;
   }

   public OnJoinTeleportSettings getOnJoinTeleportSettings() {
      return this.onJoinTeleportSettings;
   }

   public CountdownBossBarSettings getCountdownBossBarSettings() {
      return this.countdownBossBarSettings;
   }

   public CountdownParticleSettings getCountdownParticleSettings() {
      return this.countdownParticleSettings;
   }

   public double getTeleportCost() {
      return this.teleportCost;
   }

   public ConfigurationSection getConfigSection() {
      return this.configSection;
   }

   public ChunkyIntegrationSettings getChunkyIntegrationSettings() {
      return this.chunkyIntegrationSettings;
   }

   public PvpTagIntegrationSettings getPvpTagIntegrationSettings() {
      return this.pvpTagIntegrationSettings;
   }

   public EzCountdownIntegrationSettings getEzCountdownIntegrationSettings() {
      return this.ezCountdownIntegrationSettings;
   }

   public boolean isHeatmapEnabled() {
      return this.heatmapEnabled;
   }

   public boolean isAutoWorld() {
      return "auto".equalsIgnoreCase(this.worldName);
   }

   public RandomTeleportSettings withWorldName(String newWorldName) {
      return new RandomTeleportSettings(
         this.configSection,
         newWorldName,
         this.centerX,
         this.centerZ,
         this.minimumRadius,
         this.maximumRadius,
         this.maxAttempts,
         this.useWorldBorderRadius,
         this.unsafeBlocks,
         this.messages,
         this.particleSettings,
         this.onJoinTeleportSettings,
         this.countdownBossBarSettings,
         this.countdownParticleSettings,
         this.teleportCost,
         this.countdownSeconds,
         this.countdownChatMessagesEnabled,
         this.debugRejectionLogging,
         this.minY,
         this.maxY,
         this.biomeInclude,
         this.biomeExclude,
         this.protectionSettings,
         this.preCacheSettings,
         this.rareBiomeOptimizationSettings,
         this.chunkLoadingSettings,
         this.enableFallbackToCache,
         this.biomeSearchSettings,
         this.biomeFilteringEnabled,
         this.biomeSystemEnabled,
         this.safetySettings,
         this.searchPattern,
         this.chunkyIntegrationSettings,
         this.suppressPlayerMessages,
         this.cancelOnMove,
         this.cancelDistance,
         this.warnDistance,
         this.pvpTagIntegrationSettings,
         this.heatmapEnabled,
         this.ezCountdownIntegrationSettings
      );
   }

   public RandomTeleportSettings withSuppressPlayerMessages(boolean suppress) {
      return new RandomTeleportSettings(
         this.configSection,
         this.worldName,
         this.centerX,
         this.centerZ,
         this.minimumRadius,
         this.maximumRadius,
         this.maxAttempts,
         this.useWorldBorderRadius,
         this.unsafeBlocks,
         this.messages,
         this.particleSettings,
         this.onJoinTeleportSettings,
         this.countdownBossBarSettings,
         this.countdownParticleSettings,
         this.teleportCost,
         this.countdownSeconds,
         this.countdownChatMessagesEnabled,
         this.debugRejectionLogging,
         this.minY,
         this.maxY,
         this.biomeInclude,
         this.biomeExclude,
         this.protectionSettings,
         this.preCacheSettings,
         this.rareBiomeOptimizationSettings,
         this.chunkLoadingSettings,
         this.enableFallbackToCache,
         this.biomeSearchSettings,
         this.biomeFilteringEnabled,
         this.biomeSystemEnabled,
         this.safetySettings,
         this.searchPattern,
         this.chunkyIntegrationSettings,
         suppress,
         this.cancelOnMove,
         this.cancelDistance,
         this.warnDistance,
         this.pvpTagIntegrationSettings,
         this.heatmapEnabled,
         this.ezCountdownIntegrationSettings
      );
   }

   public static RandomTeleportSettings fromConfiguration(ConfigurationSection section, Logger logger) {
      if (section == null) {
         return new RandomTeleportSettings(
            null,
            "world",
            0,
            0,
            100,
            1000,
            10,
            false,
            Collections.emptySet(),
            TeleportMessages.defaultMessages(),
            new ParticleSettings(false, Particle.PORTAL, 40, 0.5, 1.0, 0.5, 0.0, false),
            new OnJoinTeleportSettings(false, false, "", 20L),
            CountdownBossBarSettings.disabled(),
            CountdownParticleSettings.disabled(),
            0.0,
            0,
            true,
            false,
            null,
            null,
            Collections.emptySet(),
            Collections.emptySet(),
            new ProtectionSettings(false, Collections.emptyList()),
            BiomePreCacheSettings.disabled(),
            RareBiomeOptimizationSettings.disabled(),
            ChunkLoadingSettings.defaults(),
            true,
            BiomeSearchSettings.defaults(),
            true,
            true,
            SafetySettings.defaults(),
            SearchPattern.RANDOM,
            ChunkyIntegrationSettings.defaults(),
            false,
            true,
            2.0,
            1.0,
            PvpTagIntegrationSettings.defaults(),
            false,
            EzCountdownIntegrationSettings.disabled()
         );
      } else {
         String worldName = section.getString("world", "world");
         int centerX = 0;
         int centerZ = 0;
         if (section.isConfigurationSection("center")) {
            ConfigurationSection center = section.getConfigurationSection("center");
            centerX = center.getInt("x", 0);
            centerZ = center.getInt("z", 0);
         }

         int minRadius = 100;
         int maxRadius = 1000;
         boolean useWorldBorder = false;
         if (section.isConfigurationSection("radius")) {
            ConfigurationSection radius = section.getConfigurationSection("radius");
            minRadius = radius.getInt("min", 100);
            maxRadius = radius.contains("max") ? radius.getInt("max") : 1000;
            useWorldBorder = radius.getBoolean("use-world-border", false);
         }

         int maxAttempts = section.getInt("max-attempts", 10);
         double teleportCost = section.getDouble("cost", 0.0);
         ConfigurationSection countdownSection = section.getConfigurationSection("countdown");
         int countdownSeconds = countdownSection != null
            ? countdownSection.getInt("seconds", section.getInt("countdown-seconds", 0))
            : section.getInt("countdown-seconds", 0);
         boolean countdownChatMessagesEnabled = countdownSection != null ? countdownSection.getBoolean("chat-messages", true) : true;
         boolean debugRejectionLogging = section.getBoolean("debug-rejection-logging", false);
         Integer minY = section.contains("min-y") ? section.getInt("min-y") : null;
         Integer maxY = section.contains("max-y") ? section.getInt("max-y") : null;
         Set<Material> unsafeBlocks = new HashSet<>();
         if (section.isList("unsafe-blocks")) {
            for (Object o : section.getList("unsafe-blocks")) {
               if (o instanceof String s) {
                  try {
                     unsafeBlocks.add(Material.valueOf(s.toUpperCase(Locale.ROOT)));
                  } catch (IllegalArgumentException var49) {
                  }
               }
            }
         }

         ConfigurationSection biomesSection = section.getConfigurationSection("biomes");
         Set<Biome> biomeInclude = parseBiomeList(biomesSection, "include");
         Set<Biome> biomeExclude = parseBiomeList(biomesSection, "exclude");
         TeleportMessages messages = TeleportMessages.fromConfiguration(section.getConfigurationSection("messages"));
         ParticleSettings particleSettings = ParticleSettings.fromConfiguration(
            section.getConfigurationSection("particles"), logger, ParticleSettings.disabled()
         );
         OnJoinTeleportSettings onJoinTeleportSettings = OnJoinTeleportSettings.fromConfiguration(section.getConfigurationSection("on-join"));
         CountdownBossBarSettings countdownBossBarSettings = CountdownBossBarSettings.fromConfiguration(
            countdownSection != null ? countdownSection.getConfigurationSection("bossbar") : null, logger
         );
         CountdownParticleSettings countdownParticleSettings = CountdownParticleSettings.fromConfiguration(
            countdownSection != null ? countdownSection.getConfigurationSection("particles") : null, logger
         );
         ProtectionSettings protectionSettings = ProtectionSettings.fromConfiguration(section.getConfigurationSection("protection"));
         BiomePreCacheSettings preCacheSettings = biomesSection != null
            ? BiomePreCacheSettings.fromConfiguration(biomesSection.getConfigurationSection("pre-cache"))
            : BiomePreCacheSettings.disabled();
         RareBiomeOptimizationSettings rareBiomeSettings = biomesSection != null
            ? RareBiomeOptimizationSettings.fromConfiguration(biomesSection.getConfigurationSection("rare-biome-optimization"))
            : RareBiomeOptimizationSettings.disabled();
         ChunkLoadingSettings chunkLoadingSettings = ChunkLoadingSettings.fromConfiguration(
            section.getConfigurationSection("chunk-loading"), ChunkLoadingSettings.defaults()
         );
         boolean biomeSystemEnabled = biomesSection == null || biomesSection.getBoolean("enabled", true);
         boolean biomeFilteringEnabled = biomesSection == null || biomesSection.getBoolean("biome-filtering.enabled", true);
         ConfigurationSection budgetSection = null;
         if (biomesSection != null) {
            ConfigurationSection bfSection = biomesSection.getConfigurationSection("biome-filtering");
            if (bfSection != null && bfSection.isConfigurationSection("performance-budget")) {
               budgetSection = bfSection.getConfigurationSection("performance-budget");
            } else if (biomesSection.isConfigurationSection("search")) {
               budgetSection = biomesSection.getConfigurationSection("search");
            }
         }

         boolean filtersActive = !biomeInclude.isEmpty() || !biomeExclude.isEmpty();
         BiomeSearchSettings baseFallback = filtersActive ? BiomeSearchSettings.filterAwareDefaults() : BiomeSearchSettings.defaults();
         BiomeSearchSettings searchSettings = BiomeSearchSettings.fromConfiguration(budgetSection, baseFallback);
         SafetySettings safetySettings = SafetySettings.fromConfiguration(section.getConfigurationSection("safety"), SafetySettings.defaults());
         SearchPattern searchPattern = SearchPattern.fromConfig(section.getString("search-pattern"), SearchPattern.RANDOM);
         ChunkyIntegrationSettings chunkyIntegrationSettings = ChunkyIntegrationSettings.fromConfiguration(
            section.getConfigurationSection("chunky-integration"), ChunkyIntegrationSettings.defaults()
         );
         ConfigurationSection rtpSection = section.getConfigurationSection("rtp");
         boolean enableFallbackToCache = rtpSection != null ? rtpSection.getBoolean("enable_fallback_to_cache", true) : true;
         boolean cancelOnMove = countdownSection != null ? countdownSection.getBoolean("cancel-on-move", true) : true;
         double cancelDistance = countdownSection != null ? countdownSection.getDouble("cancel-distance", 2.0) : 2.0;
         double warnDistance = countdownSection != null ? countdownSection.getDouble("warn-distance", 1.0) : 1.0;
         PvpTagIntegrationSettings pvpTagIntegrationSettings = PvpTagIntegrationSettings.fromConfiguration(
            section.getConfigurationSection("pvp-tag-integration"), PvpTagIntegrationSettings.defaults()
         );
         boolean heatmapEnabled = section.getBoolean("heatmap.enabled", false);
         EzCountdownIntegrationSettings ezCountdownIntegrationSettings = EzCountdownIntegrationSettings.fromConfiguration(
            countdownSection != null ? countdownSection.getConfigurationSection("ezcountdown") : null, logger
         );
         return new RandomTeleportSettings(
            section,
            worldName,
            centerX,
            centerZ,
            minRadius,
            maxRadius,
            maxAttempts,
            useWorldBorder,
            unsafeBlocks,
            messages,
            particleSettings,
            onJoinTeleportSettings,
            countdownBossBarSettings,
            countdownParticleSettings,
            teleportCost,
            countdownSeconds,
            countdownChatMessagesEnabled,
            debugRejectionLogging,
            minY,
            maxY,
            biomeInclude,
            biomeExclude,
            protectionSettings,
            preCacheSettings,
            rareBiomeSettings,
            chunkLoadingSettings,
            enableFallbackToCache,
            searchSettings,
            biomeFilteringEnabled,
            biomeSystemEnabled,
            safetySettings,
            searchPattern,
            chunkyIntegrationSettings,
            false,
            cancelOnMove,
            cancelDistance,
            warnDistance,
            pvpTagIntegrationSettings,
            heatmapEnabled,
            ezCountdownIntegrationSettings
         );
      }
   }

   public static RandomTeleportSettings fromConfiguration(ConfigurationSection section, Logger logger, RandomTeleportSettings fallback) {
      if (section == null) {
         return fallback != null ? fallback : fromConfiguration(section, logger);
      } else {
         String worldName = section.getString("world", fallback != null ? fallback.getWorldName() : "world");
         int centerX = fallback != null ? fallback.getCenterX() : 0;
         int centerZ = fallback != null ? fallback.getCenterZ() : 0;
         if (section.isConfigurationSection("center")) {
            ConfigurationSection center = section.getConfigurationSection("center");
            centerX = center.getInt("x", centerX);
            centerZ = center.getInt("z", centerZ);
         }

         int minRadius = fallback != null ? fallback.getMinimumRadius() : 100;
         int maxRadius = fallback != null ? fallback.getMaximumRadius() : 1000;
         boolean useWorldBorder = fallback != null && fallback.useWorldBorderRadius();
         if (section.isConfigurationSection("radius")) {
            ConfigurationSection radius = section.getConfigurationSection("radius");
            minRadius = radius.getInt("min", minRadius);
            maxRadius = radius.contains("max") ? radius.getInt("max") : maxRadius;
            useWorldBorder = radius.getBoolean("use-world-border", useWorldBorder);
         }

         int maxAttempts = section.getInt("max-attempts", fallback != null ? fallback.getMaxAttempts() : 10);
         double teleportCost = section.getDouble("cost", fallback != null ? fallback.getTeleportCost() : 0.0);
         ConfigurationSection countdownSection = section.getConfigurationSection("countdown");
         int countdownSeconds = countdownSection != null
            ? countdownSection.getInt("seconds", section.getInt("countdown-seconds", fallback != null ? fallback.getCountdownSeconds() : 0))
            : section.getInt("countdown-seconds", fallback != null ? fallback.getCountdownSeconds() : 0);
         boolean countdownChatMessagesEnabled = countdownSection != null
            ? countdownSection.getBoolean("chat-messages", fallback != null ? fallback.isCountdownChatMessagesEnabled() : true)
            : (fallback != null ? fallback.isCountdownChatMessagesEnabled() : true);
         boolean debugRejectionLogging = section.getBoolean("debug-rejection-logging", fallback != null && fallback.isDebugRejectionLoggingEnabled());
         Integer minY = section.contains("min-y") ? section.getInt("min-y") : (fallback != null ? fallback.getMinY() : null);
         Integer maxY = section.contains("max-y") ? section.getInt("max-y") : (fallback != null ? fallback.getMaxY() : null);
         Set<Material> unsafeBlocks = new HashSet<>();
         if (section.isList("unsafe-blocks")) {
            for (Object o : section.getList("unsafe-blocks")) {
               if (o instanceof String s) {
                  try {
                     unsafeBlocks.add(Material.valueOf(s.toUpperCase(Locale.ROOT)));
                  } catch (IllegalArgumentException var53) {
                  }
               }
            }
         } else if (fallback != null) {
            unsafeBlocks.addAll(fallback.getUnsafeBlocks());
         }

         ConfigurationSection biomesSection = section.getConfigurationSection("biomes");
         Set<Biome> biomeInclude = !isBiomeSectionEmpty(biomesSection)
            ? parseBiomeList(biomesSection, "include")
            : (fallback != null ? fallback.getBiomeInclude() : Collections.emptySet());
         Set<Biome> biomeExclude = !isBiomeSectionEmpty(biomesSection)
            ? parseBiomeList(biomesSection, "exclude")
            : (fallback != null ? fallback.getBiomeExclude() : Collections.emptySet());
         TeleportMessages messages = section.isConfigurationSection("messages")
            ? TeleportMessages.fromConfiguration(section.getConfigurationSection("messages"))
            : (fallback != null ? fallback.getMessages() : TeleportMessages.defaultMessages());
         ParticleSettings particleSettings = ParticleSettings.fromConfiguration(
            section.getConfigurationSection("particles"), logger, fallback != null ? fallback.getParticleSettings() : ParticleSettings.disabled()
         );
         OnJoinTeleportSettings onJoinTeleportSettings = section.isConfigurationSection("on-join")
            ? OnJoinTeleportSettings.fromConfiguration(section.getConfigurationSection("on-join"))
            : (fallback != null ? fallback.getOnJoinTeleportSettings() : OnJoinTeleportSettings.fromConfiguration(null));
         CountdownBossBarSettings countdownBossBarSettings = countdownSection != null
            ? CountdownBossBarSettings.fromConfiguration(countdownSection.getConfigurationSection("bossbar"), logger)
            : (fallback != null ? fallback.getCountdownBossBarSettings() : CountdownBossBarSettings.disabled());
         CountdownParticleSettings countdownParticleSettings = countdownSection != null
            ? CountdownParticleSettings.fromConfiguration(countdownSection.getConfigurationSection("particles"), logger)
            : (fallback != null ? fallback.getCountdownParticleSettings() : CountdownParticleSettings.disabled());
         ProtectionSettings protectionSettings = section.isConfigurationSection("protection")
            ? ProtectionSettings.fromConfiguration(section.getConfigurationSection("protection"), fallback != null ? fallback.getProtectionSettings() : null)
            : (fallback != null ? fallback.getProtectionSettings() : new ProtectionSettings(false, Collections.emptyList()));
         BiomePreCacheSettings preCacheSettings = biomesSection != null && biomesSection.isConfigurationSection("pre-cache")
            ? BiomePreCacheSettings.fromConfiguration(biomesSection.getConfigurationSection("pre-cache"))
            : (fallback != null ? fallback.getPreCacheSettings() : BiomePreCacheSettings.disabled());
         RareBiomeOptimizationSettings rareBiomeSettings = biomesSection != null && biomesSection.isConfigurationSection("rare-biome-optimization")
            ? RareBiomeOptimizationSettings.fromConfiguration(biomesSection.getConfigurationSection("rare-biome-optimization"))
            : (fallback != null ? fallback.getRareBiomeOptimizationSettings() : RareBiomeOptimizationSettings.disabled());
         ChunkLoadingSettings chunkLoadingSettings = ChunkLoadingSettings.fromConfiguration(
            section.getConfigurationSection("chunk-loading"), fallback != null ? fallback.getChunkLoadingSettings() : ChunkLoadingSettings.defaults()
         );
         boolean biomeSystemEnabled = biomesSection != null && biomesSection.isSet("enabled")
            ? biomesSection.getBoolean("enabled", true)
            : (fallback != null ? fallback.isBiomeSystemEnabled() : true);
         boolean biomeFilteringEnabled = biomesSection != null && biomesSection.isSet("biome-filtering.enabled")
            ? biomesSection.getBoolean("biome-filtering.enabled", true)
            : (fallback != null ? fallback.isBiomeFilteringEnabled() : true);
         ConfigurationSection budgetSection = null;
         if (biomesSection != null) {
            ConfigurationSection bfSection = biomesSection.getConfigurationSection("biome-filtering");
            if (bfSection != null && bfSection.isConfigurationSection("performance-budget")) {
               budgetSection = bfSection.getConfigurationSection("performance-budget");
            } else if (biomesSection.isConfigurationSection("search")) {
               budgetSection = biomesSection.getConfigurationSection("search");
            }
         }

         boolean filtersActive = !biomeInclude.isEmpty() || !biomeExclude.isEmpty();
         BiomeSearchSettings inheritedFallback = fallback != null ? fallback.getBiomeSearchSettings() : null;
         BiomeSearchSettings baseFallback = budgetSection != null
            ? (inheritedFallback != null ? inheritedFallback : (filtersActive ? BiomeSearchSettings.filterAwareDefaults() : BiomeSearchSettings.defaults()))
            : (inheritedFallback != null ? inheritedFallback : (filtersActive ? BiomeSearchSettings.filterAwareDefaults() : BiomeSearchSettings.defaults()));
         BiomeSearchSettings searchSettings = BiomeSearchSettings.fromConfiguration(budgetSection, baseFallback);
         SafetySettings safetySettings = SafetySettings.fromConfiguration(
            section.getConfigurationSection("safety"), fallback != null ? fallback.getSafetySettings() : SafetySettings.defaults()
         );
         String patternValue = section.getString(
            "search-pattern", fallback != null ? fallback.getSearchPattern().getConfigKey() : SearchPattern.RANDOM.getConfigKey()
         );
         SearchPattern searchPattern = SearchPattern.fromConfig(patternValue, fallback != null ? fallback.getSearchPattern() : SearchPattern.RANDOM);
         ChunkyIntegrationSettings chunkyIntegrationSettings = ChunkyIntegrationSettings.fromConfiguration(
            section.getConfigurationSection("chunky-integration"),
            fallback != null ? fallback.getChunkyIntegrationSettings() : ChunkyIntegrationSettings.defaults()
         );
         ConfigurationSection rtpSection = section.getConfigurationSection("rtp");
         boolean enableFallbackToCache = rtpSection != null
            ? rtpSection.getBoolean("enable_fallback_to_cache", fallback != null ? fallback.isEnableFallbackToCache() : true)
            : (fallback != null ? fallback.isEnableFallbackToCache() : true);
         boolean cancelOnMove = countdownSection != null && countdownSection.isSet("cancel-on-move")
            ? countdownSection.getBoolean("cancel-on-move", true)
            : (fallback != null ? fallback.isCancelOnMove() : true);
         double cancelDistance = countdownSection != null && countdownSection.isSet("cancel-distance")
            ? countdownSection.getDouble("cancel-distance", 2.0)
            : (fallback != null ? fallback.getCancelDistance() : 2.0);
         double warnDistance = countdownSection != null && countdownSection.isSet("warn-distance")
            ? countdownSection.getDouble("warn-distance", 1.0)
            : (fallback != null ? fallback.getWarnDistance() : 1.0);
         PvpTagIntegrationSettings pvpTagIntegrationSettings = PvpTagIntegrationSettings.fromConfiguration(
            section.getConfigurationSection("pvp-tag-integration"),
            fallback != null ? fallback.getPvpTagIntegrationSettings() : PvpTagIntegrationSettings.defaults()
         );
         boolean heatmapEnabled = section.isSet("heatmap.enabled")
            ? section.getBoolean("heatmap.enabled", false)
            : fallback != null && fallback.isHeatmapEnabled();
         ConfigurationSection ezcSection = countdownSection != null ? countdownSection.getConfigurationSection("ezcountdown") : null;
         EzCountdownIntegrationSettings ezCountdownIntegrationSettings;
         if (ezcSection != null) {
            ezCountdownIntegrationSettings = EzCountdownIntegrationSettings.fromConfiguration(ezcSection, logger);
         } else {
            ezCountdownIntegrationSettings = fallback != null ? fallback.getEzCountdownIntegrationSettings() : EzCountdownIntegrationSettings.disabled();
         }

         return new RandomTeleportSettings(
            section,
            worldName,
            centerX,
            centerZ,
            minRadius,
            maxRadius,
            maxAttempts,
            useWorldBorder,
            unsafeBlocks,
            messages,
            particleSettings,
            onJoinTeleportSettings,
            countdownBossBarSettings,
            countdownParticleSettings,
            teleportCost,
            countdownSeconds,
            countdownChatMessagesEnabled,
            debugRejectionLogging,
            minY,
            maxY,
            biomeInclude,
            biomeExclude,
            protectionSettings,
            preCacheSettings,
            rareBiomeSettings,
            chunkLoadingSettings,
            enableFallbackToCache,
            searchSettings,
            biomeFilteringEnabled,
            biomeSystemEnabled,
            safetySettings,
            searchPattern,
            chunkyIntegrationSettings,
            fallback != null && fallback.isSuppressPlayerMessages(),
            cancelOnMove,
            cancelDistance,
            warnDistance,
            pvpTagIntegrationSettings,
            heatmapEnabled,
            ezCountdownIntegrationSettings
         );
      }
   }

   public int getCountdownSeconds() {
      return this.countdownSeconds;
   }

   public boolean isCountdownChatMessagesEnabled() {
      return this.countdownChatMessagesEnabled;
   }

   private static boolean isBiomeSectionEmpty(ConfigurationSection section) {
      return section == null || !section.isList("include") && !section.isList("exclude");
   }

   private static Set<Biome> parseBiomeList(ConfigurationSection section, String key) {
      Set<Biome> biomes = new HashSet<>();
      if (section != null && section.isList(key)) {
         for (Object o : section.getList(key)) {
            if (o instanceof String s) {
               Biome b = BiomeCompat.safeValueOf(s);
               if (b != null) {
                  biomes.add(b);
               }
            }
         }

         return biomes;
      } else {
         return biomes;
      }
   }
}
