package ac.grim.grimac.api.storage.search;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum FacetType {
   TERMS,
   HISTOGRAM,
   DATE_HISTOGRAM,
   RANGE;
}
