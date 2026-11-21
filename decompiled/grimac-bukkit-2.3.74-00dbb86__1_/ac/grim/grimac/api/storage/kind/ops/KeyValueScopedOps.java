package ac.grim.grimac.api.storage.kind.ops;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Experimental
public final class KeyValueScopedOps {
   private KeyValueScopedOps() {
   }

   public record CountOp<S>(@NotNull Category<?> category, @NotNull S scope, @NotNull String scopeKey) implements KeyValueScopedOps.Op<Long> {
   }

   public record GetAllOp<S, V>(@NotNull Category<?> category, @NotNull S scope, @NotNull String scopeKey) implements KeyValueScopedOps.Op<Map<String, V>> {
   }

   public record GetOp<S, V>(@NotNull Category<?> category, @NotNull S scope, @NotNull String scopeKey, @NotNull String key)
      implements KeyValueScopedOps.Op<Optional<V>> {
   }

   public sealed interface Op<R>
      extends Operation<R>
      permits KeyValueScopedOps.GetOp,
      KeyValueScopedOps.GetAllOp,
      KeyValueScopedOps.PutOp,
      KeyValueScopedOps.PutAllOp,
      KeyValueScopedOps.RemoveOp,
      KeyValueScopedOps.RemoveAllOp,
      KeyValueScopedOps.CountOp {
   }

   public record PutAllOp<S, V>(@NotNull Category<?> category, @NotNull S scope, @NotNull String scopeKey, @NotNull Map<String, V> values)
      implements KeyValueScopedOps.Op<Void> {
   }

   public record PutOp<S, V>(@NotNull Category<?> category, @NotNull S scope, @NotNull String scopeKey, @NotNull String key, @NotNull V value)
      implements KeyValueScopedOps.Op<Void> {
   }

   public record RemoveAllOp<S>(@NotNull Category<?> category, @NotNull S scope, @NotNull String scopeKey) implements KeyValueScopedOps.Op<Void> {
   }

   public record RemoveOp<S>(@NotNull Category<?> category, @NotNull S scope, @NotNull String scopeKey, @NotNull String key)
      implements KeyValueScopedOps.Op<Void> {
   }
}
