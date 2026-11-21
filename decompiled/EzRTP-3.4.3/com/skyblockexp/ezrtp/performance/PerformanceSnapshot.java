package com.skyblockexp.ezrtp.performance;

import java.util.Locale;

public final class PerformanceSnapshot {
   private final String generatedAt;
   private final long totalRtps;
   private final long successRtps;
   private final long failureRtps;
   private final double successRatePercent;
   private final double timingP50Ms;
   private final double timingP75Ms;
   private final double timingP90Ms;
   private final double timingP95Ms;
   private final double timingP99Ms;
   private final long timingMinMs;
   private final long timingMaxMs;
   private final long slowOperations;
   private final double cacheHitRatePercent;
   private final long cacheHits;
   private final long cacheMisses;
   private final long cacheEvictions;
   private final long chunkLoadsTotal;
   private final long chunkLoadsAsync;
   private final long chunkLoadsSync;
   private final long biomeRejectionsTotal;
   private final long biomeRejectionsMaxPerRtp;
   private final long biomeFilterTimeouts;

   public PerformanceSnapshot(
      String generatedAt,
      long totalRtps,
      long successRtps,
      long failureRtps,
      double successRatePercent,
      double timingP50Ms,
      double timingP75Ms,
      double timingP90Ms,
      double timingP95Ms,
      double timingP99Ms,
      long timingMinMs,
      long timingMaxMs,
      long slowOperations,
      double cacheHitRatePercent,
      long cacheHits,
      long cacheMisses,
      long cacheEvictions,
      long chunkLoadsTotal,
      long chunkLoadsAsync,
      long chunkLoadsSync,
      long biomeRejectionsTotal,
      long biomeRejectionsMaxPerRtp,
      long biomeFilterTimeouts
   ) {
      this.generatedAt = generatedAt;
      this.totalRtps = totalRtps;
      this.successRtps = successRtps;
      this.failureRtps = failureRtps;
      this.successRatePercent = successRatePercent;
      this.timingP50Ms = timingP50Ms;
      this.timingP75Ms = timingP75Ms;
      this.timingP90Ms = timingP90Ms;
      this.timingP95Ms = timingP95Ms;
      this.timingP99Ms = timingP99Ms;
      this.timingMinMs = timingMinMs;
      this.timingMaxMs = timingMaxMs;
      this.slowOperations = slowOperations;
      this.cacheHitRatePercent = cacheHitRatePercent;
      this.cacheHits = cacheHits;
      this.cacheMisses = cacheMisses;
      this.cacheEvictions = cacheEvictions;
      this.chunkLoadsTotal = chunkLoadsTotal;
      this.chunkLoadsAsync = chunkLoadsAsync;
      this.chunkLoadsSync = chunkLoadsSync;
      this.biomeRejectionsTotal = biomeRejectionsTotal;
      this.biomeRejectionsMaxPerRtp = biomeRejectionsMaxPerRtp;
      this.biomeFilterTimeouts = biomeFilterTimeouts;
   }

   public String getGeneratedAt() {
      return this.generatedAt;
   }

   public long getTotalRtps() {
      return this.totalRtps;
   }

   public long getSuccessRtps() {
      return this.successRtps;
   }

   public long getFailureRtps() {
      return this.failureRtps;
   }

   public double getSuccessRatePercent() {
      return this.successRatePercent;
   }

   public double getTimingP50Ms() {
      return this.timingP50Ms;
   }

   public double getTimingP75Ms() {
      return this.timingP75Ms;
   }

   public double getTimingP90Ms() {
      return this.timingP90Ms;
   }

   public double getTimingP95Ms() {
      return this.timingP95Ms;
   }

   public double getTimingP99Ms() {
      return this.timingP99Ms;
   }

   public long getTimingMinMs() {
      return this.timingMinMs;
   }

   public long getTimingMaxMs() {
      return this.timingMaxMs;
   }

   public long getSlowOperations() {
      return this.slowOperations;
   }

   public double getCacheHitRatePercent() {
      return this.cacheHitRatePercent;
   }

   public long getCacheHits() {
      return this.cacheHits;
   }

   public long getCacheMisses() {
      return this.cacheMisses;
   }

   public long getCacheEvictions() {
      return this.cacheEvictions;
   }

   public long getChunkLoadsTotal() {
      return this.chunkLoadsTotal;
   }

   public long getChunkLoadsAsync() {
      return this.chunkLoadsAsync;
   }

   public long getChunkLoadsSync() {
      return this.chunkLoadsSync;
   }

   public long getBiomeRejectionsTotal() {
      return this.biomeRejectionsTotal;
   }

   public long getBiomeRejectionsMaxPerRtp() {
      return this.biomeRejectionsMaxPerRtp;
   }

   public long getBiomeFilterTimeouts() {
      return this.biomeFilterTimeouts;
   }

   public String toJson() {
      StringBuilder sb = new StringBuilder(512);
      sb.append("{\n");
      sb.append("  \"generatedAt\": \"").append(this.generatedAt).append("\",\n");
      sb.append("  \"rtp\": {\n");
      sb.append("    \"total\": ").append(this.totalRtps).append(",\n");
      sb.append("    \"success\": ").append(this.successRtps).append(",\n");
      sb.append("    \"failure\": ").append(this.failureRtps).append(",\n");
      sb.append("    \"successRatePercent\": ").append(fmt(this.successRatePercent)).append("\n");
      sb.append("  },\n");
      sb.append("  \"timing\": {\n");
      sb.append("    \"p50ms\": ").append(fmt(this.timingP50Ms)).append(",\n");
      sb.append("    \"p75ms\": ").append(fmt(this.timingP75Ms)).append(",\n");
      sb.append("    \"p90ms\": ").append(fmt(this.timingP90Ms)).append(",\n");
      sb.append("    \"p95ms\": ").append(fmt(this.timingP95Ms)).append(",\n");
      sb.append("    \"p99ms\": ").append(fmt(this.timingP99Ms)).append(",\n");
      sb.append("    \"minMs\": ").append(this.timingMinMs).append(",\n");
      sb.append("    \"maxMs\": ").append(this.timingMaxMs).append(",\n");
      sb.append("    \"slowOperations\": ").append(this.slowOperations).append("\n");
      sb.append("  },\n");
      sb.append("  \"cache\": {\n");
      sb.append("    \"hitRatePercent\": ").append(fmt(this.cacheHitRatePercent)).append(",\n");
      sb.append("    \"hits\": ").append(this.cacheHits).append(",\n");
      sb.append("    \"misses\": ").append(this.cacheMisses).append(",\n");
      sb.append("    \"evictions\": ").append(this.cacheEvictions).append("\n");
      sb.append("  },\n");
      sb.append("  \"chunkLoading\": {\n");
      sb.append("    \"total\": ").append(this.chunkLoadsTotal).append(",\n");
      sb.append("    \"async\": ").append(this.chunkLoadsAsync).append(",\n");
      sb.append("    \"sync\": ").append(this.chunkLoadsSync).append("\n");
      sb.append("  },\n");
      sb.append("  \"biomeFiltering\": {\n");
      sb.append("    \"totalRejections\": ").append(this.biomeRejectionsTotal).append(",\n");
      sb.append("    \"maxRejectionsPerRtp\": ").append(this.biomeRejectionsMaxPerRtp).append(",\n");
      sb.append("    \"filterTimeouts\": ").append(this.biomeFilterTimeouts).append("\n");
      sb.append("  }\n");
      sb.append("}");
      return sb.toString();
   }

   private static String fmt(double value) {
      return String.format(Locale.US, "%.2f", value);
   }
}
