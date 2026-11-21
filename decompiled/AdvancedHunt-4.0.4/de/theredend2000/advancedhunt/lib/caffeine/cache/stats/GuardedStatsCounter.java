package de.theredend2000.advancedhunt.lib.caffeine.cache.stats;

import de.theredend2000.advancedhunt.lib.caffeine.cache.RemovalCause;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;

final class GuardedStatsCounter implements StatsCounter {
   static final Logger logger = System.getLogger(GuardedStatsCounter.class.getName());
   final StatsCounter delegate;

   GuardedStatsCounter(StatsCounter delegate) {
      this.delegate = Objects.requireNonNull(delegate);
   }

   @Override
   public void recordHits(int count) {
      try {
         this.delegate.recordHits(count);
      } catch (Throwable var3) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var3);
      }
   }

   @Override
   public void recordMisses(int count) {
      try {
         this.delegate.recordMisses(count);
      } catch (Throwable var3) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var3);
      }
   }

   @Override
   public void recordLoadSuccess(long loadTime) {
      try {
         this.delegate.recordLoadSuccess(loadTime);
      } catch (Throwable var4) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var4);
      }
   }

   @Override
   public void recordLoadFailure(long loadTime) {
      try {
         this.delegate.recordLoadFailure(loadTime);
      } catch (Throwable var4) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var4);
      }
   }

   @Override
   public void recordEviction(int weight, RemovalCause cause) {
      Objects.requireNonNull(cause);

      try {
         this.delegate.recordEviction(weight, cause);
      } catch (Throwable var4) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var4);
      }
   }

   @Override
   public CacheStats snapshot() {
      try {
         return this.delegate.snapshot();
      } catch (Throwable var2) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var2);
         return CacheStats.empty();
      }
   }

   @Override
   public String toString() {
      return this.delegate.toString();
   }
}
