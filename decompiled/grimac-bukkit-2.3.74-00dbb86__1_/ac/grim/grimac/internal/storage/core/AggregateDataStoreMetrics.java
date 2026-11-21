package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.DataStoreMetrics;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.concurrent.atomic.AtomicLong;

@ApiStatus.Internal
public final class AggregateDataStoreMetrics implements DataStoreMetrics {
   private final RingRegistry rings;
   private final AtomicLong readLatencyMsEma = new AtomicLong();

   public AggregateDataStoreMetrics(RingRegistry rings) {
      this.rings = rings;
   }

   public void observeReadLatencyMs(long observed) {
      long prev = this.readLatencyMsEma.get();
      long next = prev == 0L ? observed : (prev * 7L + observed) / 8L;
      this.readLatencyMsEma.lazySet(next);
   }

   @Override
   public long queuedCount() {
      return this.rings.queuedCountTotal();
   }

   @Override
   public long submittedTotal() {
      return this.rings.submittedTotal();
   }

   @Override
   public long droppedOnOverflowTotal() {
      return this.rings.droppedOnOverflowTotal();
   }

   @Override
   public long droppedOnErrorTotal() {
      return this.rings.droppedOnErrorTotal();
   }

   @Override
   public long writeBatchLatencyMsEma() {
      return this.rings.writeBatchLatencyMsEmaMax();
   }

   @Override
   public long readLatencyMsEma() {
      return this.readLatencyMsEma.get();
   }
}
