package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;

final class SSLSMWWR<K, V> extends SSLSMWW<K, V> {
   static final LocalCacheFactory FACTORY = SSLSMWWR::new;
   private static final VarHandle REFRESH_AFTER_WRITE_NANOS;
   volatile long refreshAfterWriteNanos;

   SSLSMWWR(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.setRefreshAfterWriteNanos(var1.getRefreshAfterWriteNanos());
   }

   @Override
   protected boolean refreshAfterWrite() {
      return true;
   }

   @Override
   protected long refreshAfterWriteNanos() {
      return (long)REFRESH_AFTER_WRITE_NANOS.getAcquire((SSLSMWWR)this);
   }

   @Override
   protected void setRefreshAfterWriteNanos(long var1) {
      REFRESH_AFTER_WRITE_NANOS.setRelease((SSLSMWWR)this, (long)var1);
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         REFRESH_AFTER_WRITE_NANOS = var0.findVarHandle(SSLSMWWR.class, "refreshAfterWriteNanos", long.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
