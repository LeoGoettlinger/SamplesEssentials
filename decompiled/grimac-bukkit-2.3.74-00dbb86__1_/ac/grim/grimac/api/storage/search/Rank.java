package ac.grim.grimac.api.storage.search;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum Rank {
   BM25,
   TFIDF,
   NATIVE,
   NONE;
}
