package ac.grim.grimac.api.storage.query;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.List;

@ApiStatus.Experimental
public record Page<R>(List<R> items, @Nullable Cursor nextCursor) {
   public Page(List<R> items, @Nullable Cursor nextCursor) {
      items = items == null ? List.of() : List.copyOf(items);
      this.items = items;
      this.nextCursor = nextCursor;
   }

   public static <R> Page<R> empty() {
      return new Page<>(List.of(), null);
   }

   public boolean hasMore() {
      return this.nextCursor != null;
   }
}
