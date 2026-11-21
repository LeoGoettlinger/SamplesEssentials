package org.mineskin.options;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import org.mineskin.MineSkinClient;
import org.mineskin.MineSkinClientImpl;
import org.mineskin.data.User;
import org.mineskin.response.UserResponse;

public class AutoGenerateQueueOptions implements IQueueOptions {
   private static final int MIN_INTERVAL_MILLIS = 100;
   private static final int MAX_INTERVAL_MILLIS = 1000;
   private static final int MIN_CONCURRENCY = 1;
   private static final int MAX_CONCURRENCY = 30;
   private static final int FAILURE_STEP_MILLIS = 500;
   private static final int MAX_PENALTY_MILLIS = 10000;
   private static final int DECAY_MILLIS_PER_SECOND = 100;
   private static final int MAX_EFFECTIVE_INTERVAL_MILLIS = 10000;
   private final ScheduledExecutorService scheduler;
   private final LongSupplier clock;
   private MineSkinClient client;
   private final AtomicLong lastRefresh = new AtomicLong(0L);
   private final AtomicInteger intervalMillis = new AtomicInteger(1000);
   private final AtomicInteger concurrency = new AtomicInteger(100);
   private final AtomicInteger peakPenaltyMillis = new AtomicInteger(0);
   private final AtomicLong lastFailureAt = new AtomicLong(0L);

   public AutoGenerateQueueOptions(ScheduledExecutorService scheduler) {
      this(scheduler, System::currentTimeMillis);
   }

   AutoGenerateQueueOptions(ScheduledExecutorService scheduler, LongSupplier clock) {
      this.scheduler = scheduler;
      this.clock = clock;
   }

   public AutoGenerateQueueOptions() {
      this(Executors.newSingleThreadScheduledExecutor());
   }

   public void setClient(MineSkinClient client) {
      this.client = client;
      this.reloadGrants().exceptionally(throwable -> {
         MineSkinClientImpl.LOGGER.log(Level.WARNING, "Failed to load grants", throwable);
         return null;
      });
   }

   @Override
   public ScheduledExecutorService scheduler() {
      return this.scheduler;
   }

   @Override
   public int intervalMillis() {
      this.reloadIfNeeded();
      int effective = this.intervalMillis.get() + this.currentPenalty();
      return Math.min(effective, 10000);
   }

   @Override
   public int concurrency() {
      this.reloadIfNeeded();
      return this.concurrency.get();
   }

   @Override
   public void reportFailure() {
      int penalty;
      int baseline;
      synchronized (this) {
         int existing = this.currentPenalty();
         penalty = Math.min(10000, existing + 500);
         this.peakPenaltyMillis.set(penalty);
         this.lastFailureAt.set(this.clock.getAsLong());
         baseline = this.intervalMillis.get();
      }

      int effective = Math.min(baseline + penalty, 10000);
      MineSkinClientImpl.LOGGER
         .log(
            Level.INFO,
            "[QueueOptions] Slowing down after failure: penalty {0}ms, effective {1}ms (baseline {2}ms)",
            new Object[]{penalty, effective, baseline}
         );
   }

   private int currentPenalty() {
      int peak = this.peakPenaltyMillis.get();
      if (peak == 0) {
         return 0;
      } else {
         long elapsed = this.clock.getAsLong() - this.lastFailureAt.get();
         if (elapsed <= 0L) {
            return peak;
         } else {
            long decayed = peak - elapsed * 100L / 1000L;
            return (int)Math.max(0L, decayed);
         }
      }
   }

   private void reloadIfNeeded() {
      long now = System.currentTimeMillis();
      if (now - this.lastRefresh.get() > TimeUnit.MINUTES.toMillis(5L)) {
         this.reloadGrants().exceptionally(throwable -> {
            MineSkinClientImpl.LOGGER.log(Level.WARNING, "Failed to reload grants", throwable);
            return null;
         });
      }
   }

   public CompletableFuture<Void> reloadGrants() {
      if (this.client == null) {
         return CompletableFuture.completedFuture(null);
      } else {
         this.lastRefresh.set(System.currentTimeMillis());
         return this.client
            .misc()
            .getUser()
            .thenApply(UserResponse::getUser)
            .thenApply(User::grants)
            .thenAccept(
               grants -> {
                  grants.concurrency().ifPresent(rawConcurrent -> {
                     int concurrent = Math.min(Math.max(rawConcurrent, 1), 30);
                     int previous = this.concurrency.getAndSet(concurrent);
                     if (previous != rawConcurrent) {
                        this.client.getLogger().log(Level.FINE, "[QueueOptions] Updated concurrency from {0} to {1}", new Object[]{previous, concurrent});
                     }
                  });
                  grants.perMinute()
                     .ifPresent(
                        rawPerMinute -> {
                           int interval = Math.min(Math.max(60000 / rawPerMinute, 100), 1000);
                           int previous = this.intervalMillis.getAndSet(interval);
                           if (previous != interval) {
                              this.client
                                 .getLogger()
                                 .log(
                                    Level.FINE,
                                    "[QueueOptions] Updated interval from {0}ms to {1}ms (perMinute={2})",
                                    new Object[]{previous, interval, rawPerMinute}
                                 );
                           }
                        }
                     );
               }
            );
      }
   }
}
