package ac.grim.grimac.api.storage.codec;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum SearchType {
   KEYWORD,
   KEYWORD_PREFIX,
   TEXT,
   NUMERIC,
   VECTOR;
}
