package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;

class SSSAW<K, V> extends SSSA<K, V> {
   static final LocalCacheFactory FACTORY = SSSAW::new;
   protected static final VarHandle EXPIRES_AFTER_WRITE_NANOS;
   final WriteOrderDeque<Node<K, V>> writeOrderDeque = new WriteOrderDeque<>();
   volatile long expiresAfterWriteNanos;

   SSSAW(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.setExpiresAfterWriteNanos(var1.getExpiresAfterWriteNanos());
   }

   @Override
   protected final WriteOrderDeque<Node<K, V>> writeOrderDeque() {
      return this.writeOrderDeque;
   }

   @Override
   protected final boolean expiresAfterWrite() {
      return true;
   }

   @Override
   protected final long expiresAfterWriteNanos() {
      return (long)EXPIRES_AFTER_WRITE_NANOS.getAcquire((SSSAW)this);
   }

   @Override
   protected final void setExpiresAfterWriteNanos(long var1) {
      EXPIRES_AFTER_WRITE_NANOS.setRelease((SSSAW)this, (long)var1);
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         EXPIRES_AFTER_WRITE_NANOS = var0.findVarHandle(SSSAW.class, "expiresAfterWriteNanos", long.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
