package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.google.common.util.concurrent.internal.InternalFutures;
import com.google.errorprone.annotations.ForOverride;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
abstract class AbstractCatchingFuture<V, X extends Throwable, F, T> extends FluentFuture.TrustedFuture<V> implements Runnable {
   @CheckForNull
   ListenableFuture<? extends V> inputFuture;
   @CheckForNull
   Class<X> exceptionType;
   @CheckForNull
   F fallback;

   static <V, X extends Throwable> ListenableFuture<V> create(
      ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback, Executor executor
   ) {
      AbstractCatchingFuture.CatchingFuture<V, X> future = new AbstractCatchingFuture.CatchingFuture<>(input, exceptionType, fallback);
      input.addListener(future, MoreExecutors.rejectionPropagatingExecutor(executor, future));
      return future;
   }

   static <X extends Throwable, V> ListenableFuture<V> create(
      ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback, Executor executor
   ) {
      AbstractCatchingFuture.AsyncCatchingFuture<V, X> future = new AbstractCatchingFuture.AsyncCatchingFuture<>(input, exceptionType, fallback);
      input.addListener(future, MoreExecutors.rejectionPropagatingExecutor(executor, future));
      return future;
   }

   AbstractCatchingFuture(ListenableFuture<? extends V> inputFuture, Class<X> exceptionType, F fallback) {
      this.inputFuture = Preconditions.checkNotNull(inputFuture);
      this.exceptionType = Preconditions.checkNotNull(exceptionType);
      this.fallback = Preconditions.checkNotNull(fallback);
   }

   @Override
   public final void run() {
      ListenableFuture<? extends V> localInputFuture = this.inputFuture;
      Class<X> localExceptionType = this.exceptionType;
      F localFallback = this.fallback;
      if (!(localInputFuture == null | localExceptionType == null | localFallback == null) && !this.isCancelled()) {
         this.inputFuture = null;
         V sourceResult = null;
         Throwable throwable = null;

         try {
            if (localInputFuture instanceof InternalFutureFailureAccess) {
               throwable = InternalFutures.tryInternalFastPathGetFailure((InternalFutureFailureAccess)localInputFuture);
            }

            if (throwable == null) {
               sourceResult = Futures.getDone((Future<V>)localInputFuture);
            }
         } catch (ExecutionException var16) {
            throwable = var16.getCause();
            if (throwable == null) {
               throwable = new NullPointerException("Future type " + localInputFuture.getClass() + " threw " + var16.getClass() + " without a cause");
            }
         } catch (Error | RuntimeException var17) {
            throwable = var17;
         }

         if (throwable == null) {
            this.set(NullnessCasts.uncheckedCastNullableTToT(sourceResult));
         } else if (!Platform.isInstanceOfThrowableClass(throwable, localExceptionType)) {
            this.setFuture(localInputFuture);
         } else {
            X castThrowable = (X)throwable;

            T fallbackResult;
            label91: {
               try {
                  fallbackResult = this.doFallback(localFallback, castThrowable);
                  break label91;
               } catch (Throwable var14) {
                  Platform.restoreInterruptIfIsInterruptedException(var14);
                  this.setException(var14);
               } finally {
                  this.exceptionType = null;
                  this.fallback = null;
               }

               return;
            }

            this.setResult(fallbackResult);
         }
      }
   }

   @CheckForNull
   @Override
   protected String pendingToString() {
      ListenableFuture<? extends V> localInputFuture = this.inputFuture;
      Class<X> localExceptionType = this.exceptionType;
      F localFallback = this.fallback;
      String superString = super.pendingToString();
      String resultString = "";
      if (localInputFuture != null) {
         resultString = "inputFuture=[" + localInputFuture + "], ";
      }

      if (localExceptionType != null && localFallback != null) {
         return resultString + "exceptionType=[" + localExceptionType + "], fallback=[" + localFallback + "]";
      } else {
         return superString != null ? resultString + superString : null;
      }
   }

   @ParametricNullness
   @ForOverride
   abstract T doFallback(F fallback, X throwable) throws Exception;

   @ForOverride
   abstract void setResult(@ParametricNullness T result);

   @Override
   protected final void afterDone() {
      this.maybePropagateCancellationTo(this.inputFuture);
      this.inputFuture = null;
      this.exceptionType = null;
      this.fallback = null;
   }

   private static final class AsyncCatchingFuture<V, X extends Throwable>
      extends AbstractCatchingFuture<V, X, AsyncFunction<? super X, ? extends V>, ListenableFuture<? extends V>> {
      AsyncCatchingFuture(ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback) {
         super(input, exceptionType, fallback);
      }

      ListenableFuture<? extends V> doFallback(AsyncFunction<? super X, ? extends V> fallback, X cause) throws Exception {
         ListenableFuture<? extends V> replacement = fallback.apply(cause);
         Preconditions.checkNotNull(
            replacement, "AsyncFunction.apply returned null instead of a Future. Did you mean to return immediateFuture(null)? %s", fallback
         );
         return replacement;
      }

      void setResult(ListenableFuture<? extends V> result) {
         this.setFuture(result);
      }
   }

   private static final class CatchingFuture<V, X extends Throwable> extends AbstractCatchingFuture<V, X, Function<? super X, ? extends V>, V> {
      CatchingFuture(ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback) {
         super(input, exceptionType, fallback);
      }

      @ParametricNullness
      V doFallback(Function<? super X, ? extends V> fallback, X cause) throws Exception {
         return (V)fallback.apply(cause);
      }

      @Override
      void setResult(@ParametricNullness V result) {
         this.set(result);
      }
   }
}
