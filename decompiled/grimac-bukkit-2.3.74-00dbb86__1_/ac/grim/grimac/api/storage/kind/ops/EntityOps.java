package ac.grim.grimac.api.storage.kind.ops;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ApiStatus.Experimental
public final class EntityOps {
   private EntityOps() {
   }

   public record CountByIndexOp(@NotNull Category<?> category, @NotNull String indexName, @NotNull Object key) implements EntityOps.Op<Long> {
   }

   public record DeleteByIdOp<ID>(@NotNull Category<?> category, @NotNull ID id) implements EntityOps.Op<Void> {
   }

   public record DeleteByIndexOp(@NotNull Category<?> category, @NotNull String indexName, @NotNull Object key) implements EntityOps.Op<Void> {
   }

   public record FindByIndexOp<R>(@NotNull Category<?> category, @NotNull String indexName, @NotNull Object key, @Nullable Cursor cursor, int pageSize)
      implements EntityOps.Op<Page<R>> {
   }

   public record GetByIdOp<ID, R>(@NotNull Category<?> category, @NotNull ID id) implements EntityOps.Op<Optional<R>> {
   }

   public record GetManyOp<ID, R>(@NotNull Category<?> category, @NotNull Collection<ID> ids) implements EntityOps.Op<List<R>> {
   }

   public sealed interface Op<R>
      extends Operation<R>
      permits EntityOps.UpsertOp,
      EntityOps.GetByIdOp,
      EntityOps.GetManyOp,
      EntityOps.FindByIndexOp,
      EntityOps.PrefixIndexOp,
      EntityOps.DeleteByIdOp,
      EntityOps.DeleteByIndexOp,
      EntityOps.CountByIndexOp {
   }

   public record PrefixIndexOp<R>(@NotNull Category<?> category, @NotNull String indexName, @NotNull String prefix, @Nullable Cursor cursor, int pageSize)
      implements EntityOps.Op<Page<R>> {
   }

   public record UpsertOp<R>(@NotNull Category<?> category, @NotNull R record) implements EntityOps.Op<Void> {
   }
}
