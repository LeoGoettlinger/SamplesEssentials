package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum WaitStrategyType {
   BLOCKING,
   TIMEOUT_BLOCKING,
   SLEEPING,
   YIELDING,
   BUSY_SPIN;
}
