package ac.grim.grimac.api.storage.search;

import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;

@ApiStatus.Experimental
public record SearchResult<R>(@NotNull List<Scored<R>> hits, @NotNull Map<String, FacetResult> facets, long totalEstimate, @Nullable Cursor nextCursor) {
   public SearchResult(@NotNull List<Scored<R>> hits, @NotNull Map<String, FacetResult> facets, long totalEstimate, @Nullable Cursor nextCursor) {
      hits = List.copyOf(hits);
      facets = Map.copyOf(facets);
      this.hits = hits;
      this.facets = facets;
      this.totalEstimate = totalEstimate;
      this.nextCursor = nextCursor;
   }

   public boolean hasMore() {
      return this.nextCursor != null;
   }
}
