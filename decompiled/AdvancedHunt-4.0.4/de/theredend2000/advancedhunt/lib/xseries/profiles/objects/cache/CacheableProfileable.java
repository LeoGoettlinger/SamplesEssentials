package de.theredend2000.advancedhunt.lib.xseries.profiles.objects.cache;

import de.theredend2000.advancedhunt.lib.xseries.profiles.exceptions.MojangAPIRetryException;
import de.theredend2000.advancedhunt.lib.xseries.profiles.gameprofile.MojangGameProfile;
import de.theredend2000.advancedhunt.lib.xseries.profiles.objects.Profileable;
import de.theredend2000.advancedhunt.lib.xseries.reflection.XReflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public abstract class CacheableProfileable implements Profileable {
   protected MojangGameProfile cache;
   protected Throwable lastError;

   @Override
   public final synchronized MojangGameProfile getProfile() {
      if (this.hasExpired(true)) {
         this.lastError = null;
         this.cache = null;
      }

      if (this.lastError != null && !(this.lastError instanceof MojangAPIRetryException)) {
         throw XReflection.throwCheckedException(this.lastError);
      } else {
         if (this.cache == null) {
            try {
               this.cache = this.cacheProfile();
               this.lastError = null;
            } catch (Throwable var2) {
               this.lastError = var2;
               throw var2;
            }
         }

         return this.cache;
      }
   }

   public final boolean hasExpired() {
      return this.hasExpired(false);
   }

   @Override
   public final boolean isReady() {
      return !this.hasExpired(false);
   }

   protected boolean hasExpired(boolean renew) {
      return this.lastError instanceof MojangAPIRetryException;
   }

   @NotNull
   protected abstract MojangGameProfile cacheProfile();

   @Override
   public final String toString() {
      return this.getClass().getSimpleName() + "[cache=" + this.cache + ", lastError=" + this.lastError + ']';
   }
}
