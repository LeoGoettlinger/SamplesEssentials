package ac.grim.grimac.api.storage.kind.ops;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Map;

@ApiStatus.Experimental
public final class CounterOps {
   private CounterOps() {
   }

   public record GetManyOp<K>(@NotNull Category<?> category, @NotNull Collection<K> keys) implements CounterOps.Op<Map<K, Long>> {
   }

   public record GetOp<K>(@NotNull Category<?> category, @NotNull K key) implements CounterOps.Op<Long> {
   }

   public record IncrementByOp<K>(@NotNull Category<?> category, @NotNull K key, long delta) implements CounterOps.Op<Long> {
   }

   public sealed interface Op<R> extends Operation<R> permits CounterOps.GetOp, CounterOps.IncrementByOp, CounterOps.SetIfHigherOp, CounterOps.GetManyOp {
   }

   public record SetIfHigherOp<K>(@NotNull Category<?> category, @NotNull K key, long value) implements CounterOps.Op<Long> {
   }
}
