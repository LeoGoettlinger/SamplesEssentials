package de.theredend2000.advancedhunt.lib.caffeine.cache;

import de.theredend2000.advancedhunt.lib.caffeine.cache.stats.StatsCounter;

class WILS<K, V> extends WIL<K, V> {
   static final LocalCacheFactory FACTORY = WILS::new;
   final StatsCounter statsCounter;

   WILS(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.statsCounter = var1.getStatsCounterSupplier().get();
   }

   @Override
   public final boolean isRecordingStats() {
      return true;
   }

   @Override
   public final Ticker statsTicker() {
      return Ticker.systemTicker();
   }

   @Override
   public final StatsCounter statsCounter() {
      return this.statsCounter;
   }
}
