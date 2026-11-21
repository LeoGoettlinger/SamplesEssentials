package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record RetentionRule(boolean enabled, long maxAgeDays) {
   public static final RetentionRule DISABLED = new RetentionRule(false, 0L);

   public long maxAgeMs() {
      return this.maxAgeDays * 24L * 60L * 60L * 1000L;
   }
}
