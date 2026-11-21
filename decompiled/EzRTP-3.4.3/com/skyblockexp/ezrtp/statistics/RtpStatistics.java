package com.skyblockexp.ezrtp.statistics;

import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.block.Biome;

public final class RtpStatistics {
   private static final int MAX_RECENT_RESULTS = 1000;
   private final AtomicInteger totalAttempts = new AtomicInteger(0);
   private final AtomicInteger totalSuccesses = new AtomicInteger(0);
   private final AtomicInteger totalFailures = new AtomicInteger(0);
   private final AtomicInteger totalBiomeFailures = new AtomicInteger(0);
   private final AtomicInteger totalCacheHits = new AtomicInteger(0);
   private final AtomicInteger totalCacheMisses = new AtomicInteger(0);
   private final AtomicLong totalTeleportTimeMs = new AtomicLong(0L);
   private final Deque<Boolean> recentResults = new ConcurrentLinkedDeque<>();
   private final Map<Biome, RtpStatistics.BiomeStats> biomeStats = new ConcurrentHashMap<>();
   private static final int TIMING_BUFFER_SIZE = 2048;
   private final long[] timingSamples = new long[2048];
   private int timingWriteIndex = 0;
   private int timingFilled = 0;
   private final AtomicLong minTimeMs = new AtomicLong(Long.MAX_VALUE);
   private final AtomicLong maxTimeMs = new AtomicLong(0L);
   private final AtomicLong totalChunkLoads = new AtomicLong(0L);
   private final AtomicLong asyncChunkLoads = new AtomicLong(0L);
   private final AtomicLong syncChunkLoads = new AtomicLong(0L);
   private final AtomicLong totalBiomeRejections = new AtomicLong(0L);
   private final AtomicInteger maxBiomeRejectionsPerRtp = new AtomicInteger(0);
   private final AtomicInteger biomeFilterTimeouts = new AtomicInteger(0);
   private final AtomicInteger failuresSafety = new AtomicInteger(0);
   private final AtomicInteger failuresProtection = new AtomicInteger(0);
   private final AtomicInteger failuresBiome = new AtomicInteger(0);
   private final AtomicInteger failuresTimeout = new AtomicInteger(0);
   private final AtomicInteger failuresEconomy = new AtomicInteger(0);
   private final AtomicInteger failuresPlayerOfflineOrCancelled = new AtomicInteger(0);
   private final AtomicInteger failuresTeleportApi = new AtomicInteger(0);
   private final AtomicInteger failuresGenericSearchError = new AtomicInteger(0);
   private final AtomicInteger weightedSearchUses = new AtomicInteger(0);
   private final AtomicInteger uniformSearchUses = new AtomicInteger(0);
   private final AtomicInteger chunkLoadQueueHits = new AtomicInteger(0);

   public void recordWeightedSearchUse() {
      this.weightedSearchUses.incrementAndGet();
   }

   public void recordUniformSearchUse() {
      this.uniformSearchUses.incrementAndGet();
   }

   public void recordChunkLoadQueueUse() {
      this.chunkLoadQueueHits.incrementAndGet();
   }

   public int getWeightedSearchUses() {
      return this.weightedSearchUses.get();
   }

   public int getUniformSearchUses() {
      return this.uniformSearchUses.get();
   }

   public int getChunkLoadQueueHits() {
      return this.chunkLoadQueueHits.get();
   }

   public void recordAttempt(boolean success, long durationMs, Biome biome, boolean cacheHit, boolean cacheChecked) {
      this.totalAttempts.incrementAndGet();
      if (success) {
         this.totalSuccesses.incrementAndGet();
         this.totalTeleportTimeMs.addAndGet(durationMs);
         this.recordTimingSample(durationMs);
         if (biome != null) {
            RtpStatistics.BiomeStats stats = this.getBiomeStats(biome);
            stats.recordSuccess(durationMs);
            if (cacheHit) {
               stats.recordCacheHit();
            }
         }
      } else {
         this.totalFailures.incrementAndGet();
      }

      if (cacheHit) {
         this.totalCacheHits.incrementAndGet();
      } else if (cacheChecked) {
         this.totalCacheMisses.incrementAndGet();
      }

      this.recentResults.addLast(success);
      if (this.recentResults.size() > 1000) {
         this.recentResults.removeFirst();
      }
   }

   public void recordAttempt(boolean success, long durationMs, Biome biome, boolean cacheHit) {
      this.recordAttempt(success, durationMs, biome, cacheHit, cacheHit);
   }

   public void recordBiomeFailure(Biome targetBiome) {
      this.totalBiomeFailures.incrementAndGet();
      this.failuresBiome.incrementAndGet();
      if (targetBiome != null) {
         this.getBiomeStats(targetBiome).recordFailure();
      }
   }

   public void recordBiomeAttempt(Biome biome) {
      this.totalBiomeRejections.incrementAndGet();
      if (biome != null) {
         this.getBiomeStats(biome).recordAttempt();
      }
   }

   public void recordSafetyFailure() {
      this.failuresSafety.incrementAndGet();
   }

   public void recordProtectionFailure() {
      this.failuresProtection.incrementAndGet();
   }

   public void recordTimeoutFailure() {
      this.failuresTimeout.incrementAndGet();
   }

   public void recordEconomyFailure() {
      this.failuresEconomy.incrementAndGet();
   }

   public void recordPlayerOfflineOrCancelledFailure() {
      this.failuresPlayerOfflineOrCancelled.incrementAndGet();
   }

   public void recordTeleportApiFailure() {
      this.failuresTeleportApi.incrementAndGet();
   }

   public void recordGenericSearchErrorFailure() {
      this.failuresGenericSearchError.incrementAndGet();
   }

   private RtpStatistics.BiomeStats getBiomeStats(Biome biome) {
      return this.biomeStats.computeIfAbsent(biome, k -> new RtpStatistics.BiomeStats());
   }

   public int getTotalAttempts() {
      return this.totalAttempts.get();
   }

   public int getTotalSuccesses() {
      return this.totalSuccesses.get();
   }

   public int getTotalFailures() {
      return this.totalFailures.get();
   }

   public int getTotalBiomeFailures() {
      return this.totalBiomeFailures.get();
   }

   public int getTotalCacheHits() {
      return this.totalCacheHits.get();
   }

   public int getTotalCacheMisses() {
      return this.totalCacheMisses.get();
   }

   public long getTotalTeleportTimeMs() {
      return this.totalTeleportTimeMs.get();
   }

   public double getAverageTeleportTimeMs() {
      int successes = this.totalSuccesses.get();
      return successes == 0 ? 0.0 : (double)this.totalTeleportTimeMs.get() / successes;
   }

   public double getSuccessRate() {
      if (this.recentResults.isEmpty()) {
         return 0.0;
      } else {
         long successCount = this.recentResults.stream().filter(Boolean::booleanValue).count();
         return Math.min(100.0, (double)successCount / this.recentResults.size() * 100.0);
      }
   }

   public double getCacheHitRate() {
      int total = this.totalCacheHits.get() + this.totalCacheMisses.get();
      return total == 0 ? 0.0 : (double)this.totalCacheHits.get() / total * 100.0;
   }

   public Map<Biome, RtpStatistics.BiomeStats> getBiomeStats() {
      return new ConcurrentHashMap<>(this.biomeStats);
   }

   public RtpStatistics.BiomeStats getBiomeStatsFor(Biome biome) {
      return this.biomeStats.get(biome);
   }

   public RtpStatistics.FailureCauses getFailureCauses() {
      return new RtpStatistics.FailureCauses(
         this.failuresSafety.get(),
         this.failuresProtection.get(),
         this.failuresBiome.get(),
         this.failuresTimeout.get(),
         this.failuresEconomy.get(),
         this.failuresPlayerOfflineOrCancelled.get(),
         this.failuresTeleportApi.get(),
         this.failuresGenericSearchError.get()
      );
   }

   public void reset() {
      this.totalAttempts.set(0);
      this.totalSuccesses.set(0);
      this.totalFailures.set(0);
      this.totalBiomeFailures.set(0);
      this.totalCacheHits.set(0);
      this.totalCacheMisses.set(0);
      this.totalTeleportTimeMs.set(0L);
      this.biomeStats.clear();
      this.failuresSafety.set(0);
      this.failuresProtection.set(0);
      this.failuresBiome.set(0);
      this.failuresTimeout.set(0);
      this.failuresEconomy.set(0);
      this.failuresPlayerOfflineOrCancelled.set(0);
      this.failuresTeleportApi.set(0);
      this.failuresGenericSearchError.set(0);
      this.weightedSearchUses.set(0);
      this.uniformSearchUses.set(0);
      this.chunkLoadQueueHits.set(0);
      this.recentResults.clear();
      synchronized (this) {
         this.timingWriteIndex = 0;
         this.timingFilled = 0;
         Arrays.fill(this.timingSamples, 0L);
      }

      this.minTimeMs.set(Long.MAX_VALUE);
      this.maxTimeMs.set(0L);
      this.totalChunkLoads.set(0L);
      this.asyncChunkLoads.set(0L);
      this.syncChunkLoads.set(0L);
      this.totalBiomeRejections.set(0L);
      this.maxBiomeRejectionsPerRtp.set(0);
      this.biomeFilterTimeouts.set(0);
   }

   private synchronized void recordTimingSample(long ms) {
      this.timingSamples[this.timingWriteIndex % 2048] = ms;
      this.timingWriteIndex++;
      if (this.timingFilled < 2048) {
         this.timingFilled++;
      }

      long oldMin = this.minTimeMs.get();

      while (ms < oldMin && !this.minTimeMs.compareAndSet(oldMin, ms)) {
         oldMin = this.minTimeMs.get();
      }

      long oldMax = this.maxTimeMs.get();

      while (ms > oldMax && !this.maxTimeMs.compareAndSet(oldMax, ms)) {
         oldMax = this.maxTimeMs.get();
      }
   }

   public synchronized double getTimingPercentile(int percentile) {
      if (this.timingFilled == 0) {
         return 0.0;
      } else {
         long[] snapshot = Arrays.copyOf(this.timingSamples, this.timingFilled);
         Arrays.sort(snapshot);
         int idx = (int)Math.ceil(percentile / 100.0 * this.timingFilled) - 1;
         return snapshot[Math.max(0, Math.min(idx, this.timingFilled - 1))];
      }
   }

   public synchronized int getSlowOperationCount(long thresholdMs) {
      if (this.timingFilled == 0) {
         return 0;
      } else {
         int count = 0;

         for (int i = 0; i < this.timingFilled; i++) {
            if (this.timingSamples[i] > thresholdMs) {
               count++;
            }
         }

         return count;
      }
   }

   public long getMinTimeMs() {
      long v = this.minTimeMs.get();
      return v == Long.MAX_VALUE ? 0L : v;
   }

   public long getMaxTimeMs() {
      return this.maxTimeMs.get();
   }

   public void recordChunkLoad(boolean isAsync) {
      this.totalChunkLoads.incrementAndGet();
      if (isAsync) {
         this.asyncChunkLoads.incrementAndGet();
      } else {
         this.syncChunkLoads.incrementAndGet();
      }
   }

   public long getTotalChunkLoads() {
      return this.totalChunkLoads.get();
   }

   public long getAsyncChunkLoads() {
      return this.asyncChunkLoads.get();
   }

   public long getSyncChunkLoads() {
      return this.syncChunkLoads.get();
   }

   public void recordBiomeRejectionCount(int count) {
      if (count > 0) {
         int old = this.maxBiomeRejectionsPerRtp.get();

         while (count > old && !this.maxBiomeRejectionsPerRtp.compareAndSet(old, count)) {
            old = this.maxBiomeRejectionsPerRtp.get();
         }
      }
   }

   public void recordBiomeFilterTimeout() {
      this.biomeFilterTimeouts.incrementAndGet();
   }

   public long getTotalBiomeRejections() {
      return this.totalBiomeRejections.get();
   }

   public int getMaxBiomeRejectionsPerRtp() {
      return this.maxBiomeRejectionsPerRtp.get();
   }

   public int getBiomeFilterTimeouts() {
      return this.biomeFilterTimeouts.get();
   }

   public static final class BiomeStats {
      private final AtomicInteger attempts = new AtomicInteger(0);
      private final AtomicInteger successes = new AtomicInteger(0);
      private final AtomicInteger failures = new AtomicInteger(0);
      private final AtomicInteger cacheHits = new AtomicInteger(0);
      private final AtomicLong totalTimeMs = new AtomicLong(0L);

      void recordSuccess(long durationMs) {
         this.attempts.incrementAndGet();
         this.successes.incrementAndGet();
         this.totalTimeMs.addAndGet(durationMs);
      }

      void recordFailure() {
         this.attempts.incrementAndGet();
         this.failures.incrementAndGet();
      }

      void recordAttempt() {
         this.attempts.incrementAndGet();
      }

      void recordCacheHit() {
         this.cacheHits.incrementAndGet();
      }

      public int getAttempts() {
         return this.attempts.get();
      }

      public int getSuccesses() {
         return this.successes.get();
      }

      public int getFailures() {
         return this.failures.get();
      }

      public int getCacheHits() {
         return this.cacheHits.get();
      }

      public int getTotalFinds() {
         return this.successes.get() + this.cacheHits.get();
      }

      public double getSuccessRate() {
         int total = this.attempts.get();
         return total == 0 ? 0.0 : Math.min(100.0, (double)this.successes.get() / total * 100.0);
      }

      public double getAverageTimeMs() {
         int count = this.successes.get();
         return count == 0 ? 0.0 : (double)this.totalTimeMs.get() / count;
      }
   }

   public record FailureCauses(
      int safety, int protection, int biome, int timeout, int economy, int playerOfflineOrCancelled, int teleportApi, int genericSearchError
   ) {
      public int total() {
         return this.safety
            + this.protection
            + this.biome
            + this.timeout
            + this.economy
            + this.playerOfflineOrCancelled
            + this.teleportApi
            + this.genericSearchError;
      }
   }
}
