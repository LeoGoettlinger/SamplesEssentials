package de.oliver.fancynpcs.skins.cache;

import de.oliver.fancynpcs.api.skins.SkinData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public record SkinCacheData(@NotNull SkinData skinData, long lastUpdated, long timeToLive) {
   public boolean isExpired() {
      return this.timeToLive > 0L && System.currentTimeMillis() - this.lastUpdated > this.timeToLive;
   }
}
