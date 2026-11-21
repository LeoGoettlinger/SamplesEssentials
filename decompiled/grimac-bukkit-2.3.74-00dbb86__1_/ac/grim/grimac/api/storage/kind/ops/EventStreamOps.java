package ac.grim.grimac.api.storage.kind.ops;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Map;

@ApiStatus.Experimental
public final class EventStreamOps {
   private EventStreamOps() {
   }

   public record CountDistinctOp(@NotNull Category<?> category, @NotNull String partition, @NotNull Object key, @NotNull String field)
      implements EventStreamOps.Op<Long> {
   }

   public record CountManyOp<K>(@NotNull Category<?> category, @NotNull String partition, @NotNull Collection<K> keys)
      implements EventStreamOps.Op<Map<K, Long>> {
   }

   public record CountOp(@NotNull Category<?> category, @NotNull String partition, @NotNull Object key) implements EventStreamOps.Op<Long> {
   }

   public record DeleteByPartitionOp(@NotNull Category<?> category, @NotNull String partition, @NotNull Object key) implements EventStreamOps.Op<Void> {
   }

   public record DeleteOlderThanOp(@NotNull Category<?> category, long cutoffEpochMs) implements EventStreamOps.Op<Void> {
   }

   public sealed interface Op<R>
      extends Operation<R>
      permits EventStreamOps.PageOp,
      EventStreamOps.RangeByTimeOp,
      EventStreamOps.CountOp,
      EventStreamOps.CountManyOp,
      EventStreamOps.CountDistinctOp,
      EventStreamOps.DeleteByPartitionOp,
      EventStreamOps.DeleteOlderThanOp {
   }

   public record PageOp<R>(@NotNull Category<?> category, @NotNull String partition, @NotNull Object key, @Nullable Cursor cursor, int pageSize)
      implements EventStreamOps.Op<Page<R>> {
   }

   public record RangeByTimeOp<R>(@NotNull Category<?> category, long fromEpochMs, long toEpochMs, @Nullable Cursor cursor, int pageSize)
      implements EventStreamOps.Op<Page<R>> {
   }
}
