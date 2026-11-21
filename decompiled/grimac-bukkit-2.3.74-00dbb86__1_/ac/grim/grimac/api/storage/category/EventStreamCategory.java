package ac.grim.grimac.api.storage.category;

import ac.grim.grimac.api.storage.kind.EventStream;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EventStreamOps;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Map;

@ApiStatus.Experimental
public interface EventStreamCategory<E, R> extends Category<E> {
   @NotNull
   EventStream<E, R> kind();

   @NotNull
   default Operation<Page<R>> page(@NotNull String partition, @NotNull Object key, @Nullable Cursor cursor, int pageSize) {
      return new EventStreamOps.PageOp<>(this, partition, key, cursor, pageSize);
   }

   @NotNull
   default Operation<Page<R>> rangeByTime(long fromEpochMs, long toEpochMs, @Nullable Cursor cursor, int pageSize) {
      return new EventStreamOps.RangeByTimeOp<>(this, fromEpochMs, toEpochMs, cursor, pageSize);
   }

   @NotNull
   default Operation<Long> count(@NotNull String partition, @NotNull Object key) {
      return new EventStreamOps.CountOp(this, partition, key);
   }

   @NotNull
   default <K> Operation<Map<K, Long>> countMany(@NotNull String partition, @NotNull Collection<K> keys) {
      return new EventStreamOps.CountManyOp<>(this, partition, keys);
   }

   @NotNull
   default Operation<Long> countDistinct(@NotNull String partition, @NotNull Object key, @NotNull String field) {
      return new EventStreamOps.CountDistinctOp(this, partition, key, field);
   }

   @NotNull
   default Operation<Void> deleteByPartition(@NotNull String partition, @NotNull Object key) {
      return new EventStreamOps.DeleteByPartitionOp(this, partition, key);
   }

   @NotNull
   default Operation<Void> deleteOlderThan(long cutoffEpochMs) {
      return new EventStreamOps.DeleteOlderThanOp(this, cutoffEpochMs);
   }
}
