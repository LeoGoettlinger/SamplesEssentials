package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;

class SSLMWA<K, V> extends SSLMW<K, V> {
   static final LocalCacheFactory FACTORY = SSLMWA::new;
   protected static final VarHandle EXPIRES_AFTER_ACCESS_NANOS;
   final Ticker ticker;
   final Expiry<K, V> expiry;
   final TimerWheel<K, V> timerWheel;
   volatile long expiresAfterAccessNanos;
   final Pacer pacer;

   SSLMWA(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.ticker = var1.getTicker();
      this.expiry = var1.getExpiry(this.isAsync);
      this.timerWheel = var1.expiresVariable() ? new TimerWheel<>() : null;
      this.setExpiresAfterAccessNanos(var1.getExpiresAfterAccessNanos());
      this.pacer = var1.getScheduler() == Scheduler.disabledScheduler() ? null : new Pacer(var1.getScheduler());
   }

   @Override
   public final Ticker expirationTicker() {
      return this.ticker;
   }

   @Override
   protected boolean fastpath() {
      return false;
   }

   @Override
   protected final boolean expiresVariable() {
      return this.timerWheel != null;
   }

   @Override
   public final Expiry<K, V> expiry() {
      return this.expiry;
   }

   @Override
   protected final TimerWheel<K, V> timerWheel() {
      return this.timerWheel;
   }

   @Override
   protected final long expiresAfterAccessNanos() {
      return (long)EXPIRES_AFTER_ACCESS_NANOS.getAcquire((SSLMWA)this);
   }

   @Override
   protected final void setExpiresAfterAccessNanos(long var1) {
      EXPIRES_AFTER_ACCESS_NANOS.setRelease((SSLMWA)this, (long)var1);
   }

   @Override
   protected final boolean expiresAfterAccess() {
      return this.timerWheel == null;
   }

   @Override
   public final Pacer pacer() {
      return this.pacer;
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         EXPIRES_AFTER_ACCESS_NANOS = var0.findVarHandle(SSLMWA.class, "expiresAfterAccessNanos", long.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
