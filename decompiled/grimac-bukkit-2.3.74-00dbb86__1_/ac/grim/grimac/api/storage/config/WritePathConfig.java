package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public record WritePathConfig(
   int queueCapacity, int batchSize, long flushIntervalMs, long warnRateMs, long shutdownDrainTimeoutMs, @NotNull WaitStrategyType waitStrategy
) {
   public WritePathConfig(
      int queueCapacity, int batchSize, long flushIntervalMs, long warnRateMs, long shutdownDrainTimeoutMs, @NotNull WaitStrategyType waitStrategy
   ) {
      if (queueCapacity <= 0 || Integer.bitCount(queueCapacity) != 1) {
         throw new IllegalArgumentException("queueCapacity must be a positive power of two (got " + queueCapacity + ")");
      } else if (batchSize <= 0) {
         throw new IllegalArgumentException("batchSize must be > 0 (got " + batchSize + ")");
      } else if (flushIntervalMs < 0L) {
         throw new IllegalArgumentException("flushIntervalMs must be >= 0 (got " + flushIntervalMs + ")");
      } else if (warnRateMs < 0L) {
         throw new IllegalArgumentException("warnRateMs must be >= 0 (got " + warnRateMs + ")");
      } else if (shutdownDrainTimeoutMs < 0L) {
         throw new IllegalArgumentException("shutdownDrainTimeoutMs must be >= 0 (got " + shutdownDrainTimeoutMs + ")");
      } else {
         if (waitStrategy == null) {
            waitStrategy = WaitStrategyType.BLOCKING;
         }

         this.queueCapacity = queueCapacity;
         this.batchSize = batchSize;
         this.flushIntervalMs = flushIntervalMs;
         this.warnRateMs = warnRateMs;
         this.shutdownDrainTimeoutMs = shutdownDrainTimeoutMs;
         this.waitStrategy = waitStrategy;
      }
   }

   public static WritePathConfig defaults() {
      return new WritePathConfig(16384, 256, 1000L, 10000L, 5000L, WaitStrategyType.BLOCKING);
   }
}
