package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;

final class SSLSMSAWR<K, V> extends SSLSMSAW<K, V> {
   static final LocalCacheFactory FACTORY = SSLSMSAWR::new;
   private static final VarHandle REFRESH_AFTER_WRITE_NANOS;
   volatile long refreshAfterWriteNanos;

   SSLSMSAWR(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.setRefreshAfterWriteNanos(var1.getRefreshAfterWriteNanos());
   }

   @Override
   protected boolean refreshAfterWrite() {
      return true;
   }

   @Override
   protected long refreshAfterWriteNanos() {
      return (long)REFRESH_AFTER_WRITE_NANOS.getAcquire((SSLSMSAWR)this);
   }

   @Override
   protected void setRefreshAfterWriteNanos(long var1) {
      REFRESH_AFTER_WRITE_NANOS.setRelease((SSLSMSAWR)this, (long)var1);
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         REFRESH_AFTER_WRITE_NANOS = var0.findVarHandle(SSLSMSAWR.class, "refreshAfterWriteNanos", long.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
