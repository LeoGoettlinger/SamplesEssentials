package de.theredend2000.advancedhunt.lib.xseries.profiles.objects.cache;

import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public abstract class TimedCacheableProfileable extends CacheableProfileable {
   private long lastUpdate;

   @NotNull
   protected Duration expiresAfter() {
      return Duration.ofHours(6L);
   }

   @Override
   public final boolean hasExpired(boolean renew) {
      Duration expiresAfter = this.expiresAfter();
      if (expiresAfter.isZero()) {
         return false;
      } else if (super.hasExpired(renew)) {
         return true;
      } else if (this.cache == null && this.lastError == null) {
         return true;
      } else {
         long now = System.currentTimeMillis();
         if (this.lastUpdate == 0L) {
            if (renew) {
               this.lastUpdate = now;
            }

            return true;
         } else {
            long diff = now - this.lastUpdate;
            if (diff >= expiresAfter.toMillis()) {
               if (renew) {
                  this.lastUpdate = now;
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }
}
