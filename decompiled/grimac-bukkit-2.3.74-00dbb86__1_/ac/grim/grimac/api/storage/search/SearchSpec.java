package ac.grim.grimac.api.storage.search;

import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
public record SearchSpec(
   @NotNull List<SearchSpec.Match> matches,
   @NotNull List<SearchSpec.Filter> filters,
   @NotNull List<SearchSpec.FacetRequest> facets,
   @NotNull List<SearchSpec.Sort> sort,
   @Nullable SearchSpec.VectorQuery vector,
   @NotNull Rank rank,
   int pageSize,
   @Nullable Cursor cursor
) {
   public SearchSpec(
      @NotNull List<SearchSpec.Match> matches,
      @NotNull List<SearchSpec.Filter> filters,
      @NotNull List<SearchSpec.FacetRequest> facets,
      @NotNull List<SearchSpec.Sort> sort,
      @Nullable SearchSpec.VectorQuery vector,
      @NotNull Rank rank,
      int pageSize,
      @Nullable Cursor cursor
   ) {
      matches = List.copyOf(matches);
      filters = List.copyOf(filters);
      facets = List.copyOf(facets);
      sort = List.copyOf(sort);
      this.matches = matches;
      this.filters = filters;
      this.facets = facets;
      this.sort = sort;
      this.vector = vector;
      this.rank = rank;
      this.pageSize = pageSize;
      this.cursor = cursor;
   }

   @NotNull
   public static SearchSpec.Builder builder() {
      return new SearchSpec.Builder();
   }

   public static final class Builder {
      private final List<SearchSpec.Match> matches = new ArrayList<>();
      private final List<SearchSpec.Filter> filters = new ArrayList<>();
      private final List<SearchSpec.FacetRequest> facets = new ArrayList<>();
      private final List<SearchSpec.Sort> sort = new ArrayList<>();
      private SearchSpec.VectorQuery vector;
      private Rank rank = Rank.BM25;
      private int pageSize = 20;
      private Cursor cursor;

      @NotNull
      public SearchSpec.Builder match(@NotNull String field, @NotNull String query, @NotNull MatchMode mode) {
         return this.match(field, query, mode, 1.0);
      }

      @NotNull
      public SearchSpec.Builder match(@NotNull String field, @NotNull String query, @NotNull MatchMode mode, double boost) {
         this.matches.add(new SearchSpec.Match(field, query, mode, boost));
         return this;
      }

      @NotNull
      public SearchSpec.Builder filter(@NotNull String field, @NotNull FilterOp op, @Nullable Object value) {
         this.filters.add(new SearchSpec.Filter(field, op, value));
         return this;
      }

      @NotNull
      public SearchSpec.Builder facet(@NotNull String field, @NotNull FacetType type) {
         this.facets.add(new SearchSpec.FacetRequest(field, type, null));
         return this;
      }

      @NotNull
      public SearchSpec.Builder facet(@NotNull String field, @NotNull FacetType type, @NotNull Object params) {
         this.facets.add(new SearchSpec.FacetRequest(field, type, params));
         return this;
      }

      @NotNull
      public SearchSpec.Builder sort(@NotNull String field, @NotNull Direction dir) {
         this.sort.add(new SearchSpec.Sort(field, dir));
         return this;
      }

      @NotNull
      public SearchSpec.Builder vector(@NotNull String field, @NotNull float[] v, int k) {
         this.vector = new SearchSpec.VectorQuery(field, v, k);
         return this;
      }

      @NotNull
      public SearchSpec.Builder rank(@NotNull Rank r) {
         this.rank = r;
         return this;
      }

      @NotNull
      public SearchSpec.Builder pageSize(int n) {
         this.pageSize = n;
         return this;
      }

      @NotNull
      public SearchSpec.Builder cursor(@Nullable Cursor c) {
         this.cursor = c;
         return this;
      }

      @NotNull
      public SearchSpec build() {
         return new SearchSpec(this.matches, this.filters, this.facets, this.sort, this.vector, this.rank, this.pageSize, this.cursor);
      }
   }

   public record FacetRequest(@NotNull String field, @NotNull FacetType type, @Nullable Object params) {
   }

   public record Filter(@NotNull String field, @NotNull FilterOp op, @Nullable Object value) {
   }

   public record Match(@NotNull String field, @NotNull String query, @NotNull MatchMode mode, double boost) {
   }

   public record Sort(@NotNull String field, @NotNull Direction direction) {
   }

   public record VectorQuery(@NotNull String field, @NotNull float[] vector, int k) {
   }
}
