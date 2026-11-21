package ac.grim.grimac.api.storage.search;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum MatchMode {
   TERM,
   PHRASE,
   PHRASE_PREFIX,
   FUZZY,
   WILDCARD;
}
