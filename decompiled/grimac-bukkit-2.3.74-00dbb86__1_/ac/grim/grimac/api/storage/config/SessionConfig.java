package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record SessionConfig(long gapMs, boolean scopePerServer, long heartbeatIntervalMs) {
   public static SessionConfig defaults() {
      return new SessionConfig(600000L, true, 30000L);
   }
}
