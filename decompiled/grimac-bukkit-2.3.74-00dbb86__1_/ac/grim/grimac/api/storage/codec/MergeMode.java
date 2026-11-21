package ac.grim.grimac.api.storage.codec;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum MergeMode {
   OVERWRITE,
   INSERT_ONLY,
   PRESERVE_ON_NON_NULL,
   PRESERVE_ON_NON_SENTINEL,
   MAX,
   MIN;
}
