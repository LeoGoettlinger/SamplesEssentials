package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.performance.PerformanceMonitor;
import com.skyblockexp.ezrtp.performance.PerformanceSnapshot;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import com.skyblockexp.ezrtp.teleport.queue.ChunkLoadQueue;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.compat.BiomeCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StatsSubcommand extends Subcommand {
   private static final String BIOMES_SUBCOMMAND = "biomes";
   private static final String RARE_BIOMES_SUBCOMMAND = "rare-biomes";
   private static final String PERFORMANCE_SUBCOMMAND = "performance";
   private static final int DEFAULT_PAGE_SIZE = 10;
   private final EzRtpPlugin plugin;
   private final Supplier<RandomTeleportService> teleportServiceSupplier;

   public StatsSubcommand(EzRtpPlugin plugin, Supplier<RandomTeleportService> teleportServiceSupplier) {
      super("stats", "ezrtp.stats");
      this.plugin = plugin;
      this.teleportServiceSupplier = teleportServiceSupplier;
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!this.hasPermission(sender)) {
         sender.sendMessage("§cYou don't have permission to use this command.");
         return true;
      } else {
         if (args.length > 0 && "biomes".equalsIgnoreCase(args[0])) {
            int page = this.parsePageNumber(args, 1, 1);
            this.displayBiomeStatistics(sender, page);
         } else if (args.length > 0 && "rare-biomes".equalsIgnoreCase(args[0])) {
            int page = this.parsePageNumber(args, 1, 1);
            this.displayRareBiomeStatistics(sender, page);
         } else if (args.length > 0 && "performance".equalsIgnoreCase(args[0])) {
            this.displayPerformanceStatistics(sender);
         } else {
            int page = this.parsePageNumber(args, 0, 1);
            this.displayStatistics(sender, page);
         }

         return true;
      }
   }

   @NotNull
   @Override
   public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!this.hasPermission(sender)) {
         return Collections.emptyList();
      } else if (args.length == 1) {
         List<String> suggestions = new ArrayList<>();
         suggestions.add("biomes");
         suggestions.add("rare-biomes");
         suggestions.add("performance");
         return suggestions;
      } else {
         return Collections.emptyList();
      }
   }

   private int parsePageNumber(String[] args, int index, int defaultPage) {
      if (args.length <= index) {
         return defaultPage;
      } else {
         try {
            int page = Integer.parseInt(args[index]);
            return Math.max(1, page);
         } catch (NumberFormatException var5) {
            return defaultPage;
         }
      }
   }

   private void displayStatistics(CommandSender sender, int page) {
      RandomTeleportService service = this.teleportServiceSupplier.get();
      if (service == null) {
         MessageUtil.send(sender, "<red>Teleport service not initialized.</red>");
      } else {
         RtpStatistics stats = service.getStatistics();
         BiomeLocationCache cache = service.getBiomeCache();
         BiomeLocationCache.CacheStats cacheStats = cache.getStats();
         MessageUtil.send(sender, "<gold><bold>═══════════════════════════════════════</bold></gold>");
         MessageUtil.send(sender, "<yellow><bold>EzRTP Statistics</bold></yellow>");
         MessageUtil.send(sender, "<gold><bold>═══════════════════════════════════════</bold></gold>");
         sender.sendMessage("");
         MessageUtil.send(sender, "<gold><bold>Overall RTP Performance:</bold></gold>");
         MessageUtil.send(sender, "  <gray>Total Attempts: <white>" + stats.getTotalAttempts() + "</white></gray>");
         MessageUtil.send(
            sender,
            String.format("  <gray>Successes: <green>%d</green> <gray>(<green>%.1f%%</green>)</gray>", stats.getTotalSuccesses(), stats.getSuccessRate())
         );
         MessageUtil.send(sender, "  <gray>Failures: <red>" + stats.getTotalFailures() + "</red></gray>");
         MessageUtil.send(sender, "  <gray>Average Teleport Time: <white>" + String.format("%.0f", stats.getAverageTeleportTimeMs()) + "ms</white></gray>");
         sender.sendMessage("");
         if (cache.isEnabled()) {
            MessageUtil.send(sender, "<gold><bold>Biome Cache Statistics:</bold></gold>");
            MessageUtil.send(sender, "  <gray>Status: <green>Enabled</green></gray>");
            MessageUtil.send(
               sender,
               String.format("  <gray>Cache Hits: <green>%d</green> <gray>(<green>%.1f%%</green>)</gray>", stats.getTotalCacheHits(), stats.getCacheHitRate())
            );
            MessageUtil.send(sender, "  <gray>Cache Misses: <yellow>" + stats.getTotalCacheMisses() + "</yellow></gray>");
            MessageUtil.send(sender, "  <gray>Cached Worlds: <white>" + cacheStats.worldCount() + "</white></gray>");
            MessageUtil.send(sender, "  <gray>Cached Biomes: <white>" + cacheStats.biomeCount() + "</white></gray>");
            MessageUtil.send(sender, "  <gray>Cached Locations: <white>" + cacheStats.locationCount() + "</white></gray>");
         } else {
            MessageUtil.send(sender, "<gold><bold>Biome Cache Statistics:</bold></gold>");
            MessageUtil.send(sender, "  <gray>Status: <red>Disabled</red></gray>");
            MessageUtil.send(sender, "  <yellow>Enable pre-caching in config.yml to improve biome RTP success rates!</yellow>");
         }

         sender.sendMessage("");
         RareBiomeRegistry registry = service.getRareBiomeRegistry();
         ChunkLoadQueue chunkQueue = service.getChunkLoadQueue();
         if (registry != null && registry.isEnabled()) {
            RareBiomeRegistry.RegistryStats registryStats = registry.getStats();
            MessageUtil.send(sender, "<gold><bold>Rare Biome Optimization:</bold></gold>");
            MessageUtil.send(sender, "  <gray>Status: <green>Enabled</green></gray>");
            MessageUtil.send(sender, "  <gray>Tracked Rare Biomes: <white>" + registryStats.rareBiomeCount() + "</white></gray>");
            MessageUtil.send(sender, "  <gray>Registered Hotspots: <white>" + registryStats.hotspotCount() + "</white></gray>");
            MessageUtil.send(sender, "  <gray>Weighted Search Uses: <white>" + stats.getWeightedSearchUses() + "</white></gray>");
            MessageUtil.send(sender, "  <gray>Uniform Search Uses: <white>" + stats.getUniformSearchUses() + "</white></gray>");
            if (chunkQueue != null && chunkQueue.isEnabled()) {
               MessageUtil.send(sender, "  <gray>Chunk Load Queue: <green>Enabled</green></gray>");
            }
         } else {
            MessageUtil.send(sender, "<gold><bold>Rare Biome Optimization:</bold></gold>");
            MessageUtil.send(sender, "  <gray>Status: <red>Disabled</red></gray>");
            MessageUtil.send(sender, "  <yellow>Enable in config.yml to improve rare biome RTP performance!</yellow>");
         }

         sender.sendMessage("");
         Map<Biome, RtpStatistics.BiomeStats> biomeStats = stats.getBiomeStats();
         if (!biomeStats.isEmpty()) {
            List<Entry<Biome, RtpStatistics.BiomeStats>> sortedBiomes = biomeStats.entrySet()
               .stream()
               .sorted((a, b) -> Integer.compare(b.getValue().getAttempts(), a.getValue().getAttempts()))
               .toList();
            StatsSubcommand.PageWindow pageWindow = this.buildPageWindow(page, sortedBiomes.size());
            MessageUtil.send(
               sender, String.format("<gold><bold>Top Biome Statistics</bold> <gray>(Page %d/%d)</gray>:", pageWindow.currentPage(), pageWindow.totalPages())
            );

            for (int i = pageWindow.startIndex(); i < pageWindow.endIndex(); i++) {
               Entry<Biome, RtpStatistics.BiomeStats> entry = sortedBiomes.get(i);
               Biome biome = entry.getKey();
               RtpStatistics.BiomeStats bStats = entry.getValue();
               String biomeName = this.formatBiomeName(biome);
               String rank = String.format("#%d", i + 1);
               MessageUtil.send(
                  sender,
                  String.format(
                     "  <gray>%s <white>%s</white> <gray>(<green>%d</green> attempts, <green>%.1f%%</green> success)</gray>",
                     rank,
                     biomeName,
                     bStats.getAttempts(),
                     bStats.getSuccessRate()
                  )
               );
            }

            if (pageWindow.totalPages() > 1) {
               sender.sendMessage("");
               MessageUtil.send(
                  sender,
                  String.format(
                     "<gray>Page %d/%d - Use /rtp stats %d for next page</gray>",
                     pageWindow.currentPage(),
                     pageWindow.totalPages(),
                     pageWindow.currentPage() + 1
                  )
               );
            }
         } else {
            MessageUtil.send(sender, "<gold><bold>Biome Statistics:</bold></gold>");
            MessageUtil.send(sender, "  <gray>No biome data available yet.</gray>");
         }

         sender.sendMessage("");
         RtpStatistics.FailureCauses causes = stats.getFailureCauses();
         if (causes.total() > 0) {
            MessageUtil.send(sender, "<gold><bold>Failure Causes:</bold></gold>");
            if (causes.safety() > 0) {
               MessageUtil.send(
                  sender,
                  String.format(
                     "  <gray>Safety: <red>%d</red> <gray>(<red>%.1f%%</red>)</gray>", causes.safety(), (double)causes.safety() / causes.total() * 100.0
                  )
               );
            }

            if (causes.biome() > 0) {
               MessageUtil.send(
                  sender,
                  String.format(
                     "  <gray>Biome: <red>%d</red> <gray>(<red>%.1f%%</red>)</gray>", causes.biome(), (double)causes.biome() / causes.total() * 100.0
                  )
               );
            }

            if (causes.protection() > 0) {
               MessageUtil.send(
                  sender,
                  String.format(
                     "  <gray>Protection: <red>%d</red> <gray>(<red>%.1f%%</red>)</gray>",
                     causes.protection(),
                     (double)causes.protection() / causes.total() * 100.0
                  )
               );
            }

            if (causes.timeout() > 0) {
               MessageUtil.send(
                  sender,
                  String.format(
                     "  <gray>Timeout: <red>%d</red> <gray>(<red>%.1f%%</red>)</gray>", causes.timeout(), (double)causes.timeout() / causes.total() * 100.0
                  )
               );
            }

            sender.sendMessage("");
         }

         if (cache.isEnabled() && stats.getCacheHitRate() < 30.0 && stats.getTotalCacheMisses() > 10) {
            MessageUtil.send(sender, "<yellow><bold>⚠ Warning:</bold></yellow>");
            MessageUtil.send(sender, "  <gray>Low cache hit rate detected. Consider increasing</gray>");
            MessageUtil.send(sender, "  <gray>'biomes.pre-cache.max-per-biome' or 'warmup-size' in config.yml</gray>");
            sender.sendMessage("");
         }

         MessageUtil.send(sender, "<gold><bold>View More Metrics:</bold></gold>");
         MessageUtil.send(sender, "  <gray>bStats: <aqua>https://bstats.org/plugin/bukkit/EzRTP/27735</aqua></gray>");
         MessageUtil.send(sender, "<gold><bold>═══════════════════════════════════════</bold></gold>");
      }
   }

   private void displayBiomeStatistics(CommandSender sender, int page) {
      RandomTeleportService service = this.teleportServiceSupplier.get();
      if (service == null) {
         MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.COMMAND_SERVICE_NOT_INITIALIZED));
      } else {
         RtpStatistics stats = service.getStatistics();
         BiomeLocationCache cache = service.getBiomeCache();
         BiomeLocationCache.CacheStats cacheStats = cache.getStats();
         Map<Biome, RtpStatistics.BiomeStats> biomeStats = stats.getBiomeStats();
         sender.sendMessage("§6§l═══════════════════════════════════════");
         sender.sendMessage("§e§lEzRTP Biome Statistics");
         sender.sendMessage("§6§l═══════════════════════════════════════");
         sender.sendMessage("");
         if (biomeStats.isEmpty()) {
            sender.sendMessage("§7No biome data available yet.");
            sender.sendMessage("§7Biome statistics are collected as players use /rtp");
            sender.sendMessage("§7with biome filtering enabled.");
            sender.sendMessage("");
            sender.sendMessage("§6§l═══════════════════════════════════════");
         } else {
            if (cache.isEnabled()) {
               sender.sendMessage("§6§lCache Information:");
               sender.sendMessage(String.format("  §7Cached Biomes: §f%d §7| §7Cached Locations: §f%d", cacheStats.biomeCount(), cacheStats.locationCount()));
               sender.sendMessage(
                  String.format(
                     "  §7Cache Hit Rate: §a%.1f%% §7(§a%d §7hits, §e%d §7misses)",
                     stats.getCacheHitRate(),
                     stats.getTotalCacheHits(),
                     stats.getTotalCacheMisses()
                  )
               );
               sender.sendMessage("");
            }

            List<Entry<Biome, RtpStatistics.BiomeStats>> sortedBiomes = biomeStats.entrySet()
               .stream()
               .sorted((a, b) -> Integer.compare(b.getValue().getAttempts(), a.getValue().getAttempts()))
               .toList();
            StatsSubcommand.PageWindow pageWindow = this.buildPageWindow(page, sortedBiomes.size());
            sender.sendMessage(
               String.format("§6§lBiomes by Activity §7(Page %d/%d, Total: %d biomes):", pageWindow.currentPage(), pageWindow.totalPages(), biomeStats.size())
            );
            sender.sendMessage("");

            for (int i = pageWindow.startIndex(); i < pageWindow.endIndex(); i++) {
               Entry<Biome, RtpStatistics.BiomeStats> entry = sortedBiomes.get(i);
               Biome biome = entry.getKey();
               RtpStatistics.BiomeStats bStats = entry.getValue();
               String biomeName = this.formatBiomeName(biome);
               String rank = String.format("§e#%d", i + 1);
               sender.sendMessage(String.format("  %s §f%s", rank, biomeName));
               sender.sendMessage(
                  String.format(
                     "    §7Attempts: §f%d §7| §7Successes: §a%d §7(§a%.1f%%§7)", bStats.getAttempts(), bStats.getSuccesses(), bStats.getSuccessRate()
                  )
               );
               sender.sendMessage(String.format("    §7Avg Time: §f%.0fms §7| §7Failures: §c%d", bStats.getAverageTimeMs(), bStats.getFailures()));
               if (i < pageWindow.endIndex() - 1) {
                  sender.sendMessage("");
               }
            }

            if (pageWindow.totalPages() > 1) {
               sender.sendMessage("");
               sender.sendMessage("§6§lNavigation:");
               if (pageWindow.currentPage() < pageWindow.totalPages()) {
                  sender.sendMessage(String.format("  §7Next page: §6/rtp stats biomes %d", pageWindow.currentPage() + 1));
               }

               if (pageWindow.currentPage() > 1) {
                  sender.sendMessage(String.format("  §7Previous page: §6/rtp stats biomes %d", pageWindow.currentPage() - 1));
               }
            }

            sender.sendMessage("");
            sender.sendMessage("§6§lLegend:");
            sender.sendMessage("  §7Attempts: Total RTP attempts targeting this biome");
            sender.sendMessage("  §7Successes: Successful teleports to this biome");
            sender.sendMessage("  §7Avg Time: Average time to find a safe location");
            sender.sendMessage("§6§l═══════════════════════════════════════");
         }
      }
   }

   private void displayRareBiomeStatistics(CommandSender sender, int page) {
      RandomTeleportService service = this.teleportServiceSupplier.get();
      if (service == null) {
         sender.sendMessage("§cTeleport service not initialized.");
      } else {
         RareBiomeRegistry registry = service.getRareBiomeRegistry();
         if (registry != null && registry.isEnabled()) {
            RtpStatistics stats = service.getStatistics();
            RareBiomeRegistry.RegistryStats registryStats = registry.getStats();
            ChunkLoadQueue chunkQueue = service.getChunkLoadQueue();
            sender.sendMessage("§6§l═══════════════════════════════════════");
            sender.sendMessage("§e§lRare Biome Optimization Statistics");
            sender.sendMessage("§6§l═══════════════════════════════════════");
            sender.sendMessage("");
            sender.sendMessage("§6§lSystem Status:");
            sender.sendMessage(String.format("  §7Optimization: §aEnabled"));
            sender.sendMessage(String.format("  §7Tracked Rare Biomes: §f%d", registryStats.rareBiomeCount()));
            sender.sendMessage(String.format("  §7Registered Hotspots: §f%d", registryStats.hotspotCount()));
            sender.sendMessage(String.format("  §7Worlds with Hotspots: §f%d", registryStats.worldCount()));
            sender.sendMessage("");
            sender.sendMessage("§6§lSearch Strategy Performance:");
            int totalSearches = stats.getWeightedSearchUses() + stats.getUniformSearchUses();
            if (totalSearches > 0) {
               double weightedPercent = (double)stats.getWeightedSearchUses() / totalSearches * 100.0;
               sender.sendMessage(String.format("  §7Weighted Searches: §a%d §7(§a%.1f%%§7)", stats.getWeightedSearchUses(), weightedPercent));
               sender.sendMessage(String.format("  §7Uniform Searches: §e%d §7(§e%.1f%%§7)", stats.getUniformSearchUses(), 100.0 - weightedPercent));
            } else {
               sender.sendMessage("  §7No searches recorded yet");
            }

            sender.sendMessage("");
            if (chunkQueue != null && chunkQueue.isEnabled()) {
               sender.sendMessage("§6§lChunk Load Queue:");
               sender.sendMessage(String.format("  §7Status: §aEnabled"));
               sender.sendMessage(String.format("  §7Total Uses: §f%d", stats.getChunkLoadQueueHits()));
               sender.sendMessage(String.format("  §7Current Queue Size: §f%d", chunkQueue.getQueueSize()));
               sender.sendMessage("");
            }

            Map<Biome, Integer> biomeDistribution = registryStats.biomeDistribution();
            if (!biomeDistribution.isEmpty()) {
               List<Entry<Biome, Integer>> sortedBiomes = biomeDistribution.entrySet()
                  .stream()
                  .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                  .toList();
               StatsSubcommand.PageWindow pageWindow = this.buildPageWindow(page, sortedBiomes.size());
               sender.sendMessage(String.format("§6§lHotspot Distribution §7(Page %d/%d):", pageWindow.currentPage(), pageWindow.totalPages()));
               sender.sendMessage("");

               for (int i = pageWindow.startIndex(); i < pageWindow.endIndex(); i++) {
                  Entry<Biome, Integer> entry = sortedBiomes.get(i);
                  Biome biome = entry.getKey();
                  int hotspotCount = entry.getValue();
                  String biomeName = this.formatBiomeName(biome);
                  String rank = String.format("§e#%d", i + 1);
                  sender.sendMessage(String.format("  %s §f%s §7- §a%d §7hotspot%s", rank, biomeName, hotspotCount, hotspotCount == 1 ? "" : "s"));
                  if (i < pageWindow.endIndex() - 1) {
                     sender.sendMessage("");
                  }
               }

               if (pageWindow.totalPages() > 1) {
                  sender.sendMessage("");
                  sender.sendMessage("§6§lNavigation:");
                  if (pageWindow.currentPage() < pageWindow.totalPages()) {
                     sender.sendMessage(String.format("  §7Next page: §6/rtp stats rare-biomes %d", pageWindow.currentPage() + 1));
                  }

                  if (pageWindow.currentPage() > 1) {
                     sender.sendMessage(String.format("  §7Previous page: §6/rtp stats rare-biomes %d", pageWindow.currentPage() - 1));
                  }
               }
            } else {
               sender.sendMessage("§6§lHotspot Distribution:");
               sender.sendMessage("  §7No hotspots registered yet.");
            }

            sender.sendMessage("");
            if (registryStats.hotspotCount() > 0 && totalSearches > 0) {
               sender.sendMessage("§6§lPerformance Insights:");
               double avgHotspotsPerBiome = (double)registryStats.hotspotCount() / registryStats.rareBiomeCount();
               sender.sendMessage(String.format("  §7Average hotspots per rare biome: §f%.1f", avgHotspotsPerBiome));
               if (stats.getWeightedSearchUses() > stats.getUniformSearchUses()) {
                  sender.sendMessage("  §7Weighted search is preferred for rare biomes");
                  sender.sendMessage("  §7This indicates good hotspot coverage");
               } else {
                  sender.sendMessage("  §7Uniform search is being used more often");
                  sender.sendMessage("  §7Consider adding more hotspots for better performance");
               }

               sender.sendMessage("");
            }

            sender.sendMessage("§6§lRecommendations:");
            if (registryStats.hotspotCount() == 0) {
               sender.sendMessage("  §7Add hotspots using the rare biome registry API");
               sender.sendMessage("  §7Hotspots help EzRTP find rare biomes faster");
            } else {
               sender.sendMessage("  §7Monitor hotspot usage and add more if needed");
               sender.sendMessage("  §7Weighted search should dominate for optimal performance");
            }

            sender.sendMessage("");
            sender.sendMessage("§6§l═══════════════════════════════════════");
         } else {
            sender.sendMessage("§cRare biome optimization is not enabled.");
            sender.sendMessage("§7Enable it in config.yml under biomes.rare-biome-optimization");
         }
      }
   }

   private void displayPerformanceStatistics(CommandSender sender) {
      PerformanceMonitor monitor = this.plugin.getPerformanceMonitor();
      sender.sendMessage("§6§l═══════════════════════════════════════");
      sender.sendMessage("§e§lEzRTP Performance Statistics");
      sender.sendMessage("§6§l═══════════════════════════════════════");
      sender.sendMessage("");
      if (monitor != null && monitor.isEnabled()) {
         PerformanceSnapshot snap = monitor.snapshot();
         sender.sendMessage("§6§lOverall RTP Performance:");
         sender.sendMessage(
            String.format(
               "  §7Total: §f%d §7| §7Success: §a%d §7(§a%.1f%%§7) §7| §7Failures: §c%d",
               snap.getTotalRtps(),
               snap.getSuccessRtps(),
               snap.getSuccessRatePercent(),
               snap.getFailureRtps()
            )
         );
         sender.sendMessage("");
         sender.sendMessage("§6§lResponse Time:");
         sender.sendMessage(
            String.format("  §7P50: §f%.0fms §7| §7P75: §f%.0fms §7| §7P90: §f%.0fms", snap.getTimingP50Ms(), snap.getTimingP75Ms(), snap.getTimingP90Ms())
         );
         sender.sendMessage(String.format("  §7P95: §f%.0fms §7| §7P99: §f%.0fms", snap.getTimingP95Ms(), snap.getTimingP99Ms()));
         sender.sendMessage(String.format("  §7Min: §f%dms §7| §7Max: §f%dms", snap.getTimingMinMs(), snap.getTimingMaxMs()));
         sender.sendMessage(
            String.format("  §7Slow Operations (>%dms): §e%d", (long)monitor.getSettings().getWarnings().getSlowRtpThresholdMs(), snap.getSlowOperations())
         );
         sender.sendMessage("");
         sender.sendMessage("§6§lBiome Cache:");
         sender.sendMessage(
            String.format("  §7Hit Rate: §a%.1f%% §7(hits: §a%d§7, misses: §e%d§7)", snap.getCacheHitRatePercent(), snap.getCacheHits(), snap.getCacheMisses())
         );
         sender.sendMessage(String.format("  §7Evictions: §f%d", snap.getCacheEvictions()));
         sender.sendMessage("");
         sender.sendMessage("§6§lChunk Loading:");
         sender.sendMessage(
            String.format("  §7Total: §f%d §7| §7Async: §a%d §7| §7Sync: §e%d", snap.getChunkLoadsTotal(), snap.getChunkLoadsAsync(), snap.getChunkLoadsSync())
         );
         sender.sendMessage("");
         sender.sendMessage("§6§lBiome Filtering:");
         sender.sendMessage(
            String.format("  §7Total Rejections: §f%d §7| §7Max Per RTP: §f%d", snap.getBiomeRejectionsTotal(), snap.getBiomeRejectionsMaxPerRtp())
         );
         sender.sendMessage(String.format("  §7Filter Timeouts: §f%d", snap.getBiomeFilterTimeouts()));
         sender.sendMessage("");
         boolean hasRecommendations = false;
         if (snap.getSlowOperations() > 0L && snap.getBiomeFilterTimeouts() > 0L) {
            if (!hasRecommendations) {
               sender.sendMessage("§e§lRecommendations:");
               hasRecommendations = true;
            }

            sender.sendMessage("  §7High biome filter timeouts detected — consider reducing");
            sender.sendMessage("  §7'biome-search.max-biome-rejections' or disabling biome filtering.");
         }

         if (snap.getCacheHits() + snap.getCacheMisses() > 10L && snap.getCacheHitRatePercent() < 30.0) {
            if (!hasRecommendations) {
               sender.sendMessage("§e§lRecommendations:");
               hasRecommendations = true;
            }

            sender.sendMessage("  §7Low cache hit rate — increase 'max-per-biome' or 'warmup-size'.");
         }

         if (hasRecommendations) {
            sender.sendMessage("");
         }

         sender.sendMessage("§8Generated at: " + snap.getGeneratedAt());
         sender.sendMessage("§6§l═══════════════════════════════════════");
      } else {
         sender.sendMessage("§cPerformance monitoring is disabled.");
         sender.sendMessage("§7Enable it by setting §fperformance.monitoring.enabled: true");
         sender.sendMessage("§7in §fplugins/EzRTP/performance.yml§7 and reloading the plugin.");
         sender.sendMessage("");
         sender.sendMessage("§6§l═══════════════════════════════════════");
      }
   }

   private StatsSubcommand.PageWindow buildPageWindow(int requestedPage, int itemCount) {
      int totalPages = Math.max(1, (int)Math.ceil(itemCount / 10.0));
      int currentPage = Math.min(requestedPage, totalPages);
      int startIndex = (currentPage - 1) * 10;
      int endIndex = Math.min(startIndex + 10, itemCount);
      return new StatsSubcommand.PageWindow(currentPage, totalPages, startIndex, endIndex);
   }

   private String formatBiomeName(Biome biome) {
      String name = BiomeCompat.safeName(biome).replace("_", " ");
      String[] words = name.toLowerCase(Locale.ROOT).split(" ");
      StringBuilder formatted = new StringBuilder();

      for (int i = 0; i < words.length; i++) {
         if (i > 0) {
            formatted.append(" ");
         }

         formatted.append(words[i].substring(0, 1).toUpperCase(Locale.ROOT)).append(words[i].substring(1));
      }

      return formatted.toString();
   }

   private record PageWindow(int currentPage, int totalPages, int startIndex, int endIndex) {
   }
}
