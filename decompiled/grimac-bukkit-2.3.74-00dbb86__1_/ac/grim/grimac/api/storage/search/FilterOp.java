package ac.grim.grimac.api.storage.search;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum FilterOp {
   EQ,
   NEQ,
   GT,
   GTE,
   LT,
   LTE,
   IN,
   RANGE,
   EXISTS;
}
