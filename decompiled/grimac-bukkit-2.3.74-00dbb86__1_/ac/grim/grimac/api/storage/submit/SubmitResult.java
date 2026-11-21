package ac.grim.grimac.api.storage.submit;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum SubmitResult {
   QUEUED,
   DROPPED_OVERFLOW,
   DROPPED_SHUTTING_DOWN;
}
