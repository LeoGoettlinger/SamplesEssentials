package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import com.lmax.disruptor.EventHandler;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Internal
final class DisruptorEventHandlerAdapter<E> implements EventHandler<E> {
   private final String categoryId;
   private final StorageEventHandler<E> delegate;
   private final long warnRateMs;
   private final Logger logger;
   private final AtomicLong droppedOnErrorTotal = new AtomicLong();
   private final AtomicLong handledTotal = new AtomicLong();
   private final AtomicLong batchCommitLatencyMsEma = new AtomicLong();
   private long batchStartNanos;
   private long lastWarnAtMs;
   private long dropsAccumulatedSinceLastWarn;

   DisruptorEventHandlerAdapter(String categoryId, StorageEventHandler<E> delegate, long warnRateMs, Logger logger) {
      this.categoryId = categoryId;
      this.delegate = delegate;
      this.warnRateMs = warnRateMs;
      this.logger = logger;
   }

   @Override
   public void onEvent(E event, long sequence, boolean endOfBatch) {
      if (this.batchStartNanos == 0L) {
         this.batchStartNanos = System.nanoTime();
      }

      try {
         this.delegate.onEvent(event, sequence, endOfBatch);
         this.handledTotal.incrementAndGet();
      } catch (RuntimeException | BackendException var12) {
         this.droppedOnErrorTotal.incrementAndGet();
         this.registerDropWarn(var12);
      } finally {
         if (endOfBatch) {
            long elapsedMs = (System.nanoTime() - this.batchStartNanos) / 1000000L;
            updateEmaMs(this.batchCommitLatencyMsEma, elapsedMs);
            this.batchStartNanos = 0L;
         }
      }
   }

   private synchronized void registerDropWarn(Throwable cause) {
      this.dropsAccumulatedSinceLastWarn++;
      long now = System.currentTimeMillis();
      if (now - this.lastWarnAtMs >= this.warnRateMs) {
         this.logger
            .log(
               Level.WARNING,
               "[grim-datastore] backend event handler failed for "
                  + this.categoryId
                  + "; dropped "
                  + this.dropsAccumulatedSinceLastWarn
                  + " events in the last "
                  + this.warnRateMs
                  + "ms",
               cause
            );
         this.lastWarnAtMs = now;
         this.dropsAccumulatedSinceLastWarn = 0L;
      }
   }

   long droppedOnErrorTotal() {
      return this.droppedOnErrorTotal.get();
   }

   long handledTotal() {
      return this.handledTotal.get();
   }

   long batchCommitLatencyMsEma() {
      return this.batchCommitLatencyMsEma.get();
   }

   private static void updateEmaMs(AtomicLong ema, long observedMs) {
      long prev = ema.get();
      long next = prev == 0L ? observedMs : (prev * 7L + observedMs) / 8L;
      ema.lazySet(next);
   }
}
