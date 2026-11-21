package ac.grim.grimac.api.storage;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface DataStoreMetrics {
   long queuedCount();

   long submittedTotal();

   long droppedOnOverflowTotal();

   long droppedOnErrorTotal();

   long writeBatchLatencyMsEma();

   long readLatencyMsEma();
}
