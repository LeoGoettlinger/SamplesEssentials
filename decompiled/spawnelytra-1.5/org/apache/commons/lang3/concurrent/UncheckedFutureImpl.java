package org.apache.commons.lang3.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.exception.UncheckedInterruptedException;

final class UncheckedFutureImpl<V> extends AbstractFutureProxy<V> implements UncheckedFuture<V> {
   UncheckedFutureImpl(Future<V> future) {
      super(future);
   }

   @Override
   public V get() {
      try {
         return super.get();
      } catch (InterruptedException var2) {
         throw new UncheckedInterruptedException(var2);
      } catch (ExecutionException var3) {
         throw new UncheckedExecutionException(var3);
      }
   }

   @Override
   public V get(long timeout, TimeUnit unit) {
      try {
         return super.get(timeout, unit);
      } catch (InterruptedException var5) {
         throw new UncheckedInterruptedException(var5);
      } catch (ExecutionException var6) {
         throw new UncheckedExecutionException(var6);
      } catch (TimeoutException var7) {
         throw new UncheckedTimeoutException(var7);
      }
   }
}
