package ac.grim.grimac.api.storage.search;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.List;

@ApiStatus.Experimental
public record FacetResult(@NotNull FacetType type, @NotNull List<FacetResult.Bucket> buckets) {
   public FacetResult(@NotNull FacetType type, @NotNull List<FacetResult.Bucket> buckets) {
      buckets = List.copyOf(buckets);
      this.type = type;
      this.buckets = buckets;
   }

   public record Bucket(@NotNull Object value, long count, @Nullable Double lowerBound, @Nullable Double upperBound) {
   }
}
