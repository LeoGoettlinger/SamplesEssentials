package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record MigrationConfig(boolean skip, long maxDurationMs) {
   public static MigrationConfig defaults() {
      return new MigrationConfig(false, 0L);
   }
}
