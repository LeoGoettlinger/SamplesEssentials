package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.ForOverride;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
abstract class AbstractTransformFuture<I, O, F, T> extends FluentFuture.TrustedFuture<O> implements Runnable {
   @CheckForNull
   ListenableFuture<? extends I> inputFuture;
   @CheckForNull
   F function;

   static <I, O> ListenableFuture<O> create(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function, Executor executor) {
      Preconditions.checkNotNull(executor);
      AbstractTransformFuture.AsyncTransformFuture<I, O> output = new AbstractTransformFuture.AsyncTransformFuture<>(input, function);
      input.addListener(output, MoreExecutors.rejectionPropagatingExecutor(executor, output));
      return output;
   }

   static <I, O> ListenableFuture<O> create(ListenableFuture<I> input, Function<? super I, ? extends O> function, Executor executor) {
      Preconditions.checkNotNull(function);
      AbstractTransformFuture.TransformFuture<I, O> output = new AbstractTransformFuture.TransformFuture<>(input, function);
      input.addListener(output, MoreExecutors.rejectionPropagatingExecutor(executor, output));
      return output;
   }

   AbstractTransformFuture(ListenableFuture<? extends I> inputFuture, F function) {
      this.inputFuture = Preconditions.checkNotNull(inputFuture);
      this.function = Preconditions.checkNotNull(function);
   }

   @Override
   public final void run() {
      ListenableFuture<? extends I> localInputFuture = this.inputFuture;
      F localFunction = this.function;
      if (!(this.isCancelled() | localInputFuture == null | localFunction == null)) {
         this.inputFuture = null;
         if (localInputFuture.isCancelled()) {
            boolean unused = this.setFuture(localInputFuture);
         } else {
            I sourceResult;
            try {
               sourceResult = Futures.getDone((Future<I>)localInputFuture);
            } catch (CancellationException var13) {
               this.cancel(false);
               return;
            } catch (ExecutionException var14) {
               this.setException(var14.getCause());
               return;
            } catch (RuntimeException var15) {
               this.setException(var15);
               return;
            } catch (Error var16) {
               this.setException(var16);
               return;
            }

            T transformResult;
            label65: {
               try {
                  transformResult = this.doTransform(localFunction, sourceResult);
                  break label65;
               } catch (Throwable var17) {
                  Platform.restoreInterruptIfIsInterruptedException(var17);
                  this.setException(var17);
               } finally {
                  this.function = null;
               }

               return;
            }

            this.setResult(transformResult);
         }
      }
   }

   @ParametricNullness
   @ForOverride
   abstract T doTransform(F function, @ParametricNullness I result) throws Exception;

   @ForOverride
   abstract void setResult(@ParametricNullness T result);

   @Override
   protected final void afterDone() {
      this.maybePropagateCancellationTo(this.inputFuture);
      this.inputFuture = null;
      this.function = null;
   }

   @CheckForNull
   @Override
   protected String pendingToString() {
      ListenableFuture<? extends I> localInputFuture = this.inputFuture;
      F localFunction = this.function;
      String superString = super.pendingToString();
      String resultString = "";
      if (localInputFuture != null) {
         resultString = "inputFuture=[" + localInputFuture + "], ";
      }

      if (localFunction != null) {
         return resultString + "function=[" + localFunction + "]";
      } else {
         return superString != null ? resultString + superString : null;
      }
   }

   private static final class AsyncTransformFuture<I, O>
      extends AbstractTransformFuture<I, O, AsyncFunction<? super I, ? extends O>, ListenableFuture<? extends O>> {
      AsyncTransformFuture(ListenableFuture<? extends I> inputFuture, AsyncFunction<? super I, ? extends O> function) {
         super(inputFuture, function);
      }

      ListenableFuture<? extends O> doTransform(AsyncFunction<? super I, ? extends O> function, @ParametricNullness I input) throws Exception {
         ListenableFuture<? extends O> outputFuture = function.apply(input);
         Preconditions.checkNotNull(
            outputFuture, "AsyncFunction.apply returned null instead of a Future. Did you mean to return immediateFuture(null)? %s", function
         );
         return outputFuture;
      }

      void setResult(ListenableFuture<? extends O> result) {
         this.setFuture(result);
      }
   }

   private static final class TransformFuture<I, O> extends AbstractTransformFuture<I, O, Function<? super I, ? extends O>, O> {
      TransformFuture(ListenableFuture<? extends I> inputFuture, Function<? super I, ? extends O> function) {
         super(inputFuture, function);
      }

      @ParametricNullness
      O doTransform(Function<? super I, ? extends O> function, @ParametricNullness I input) {
         return (O)function.apply(input);
      }

      @Override
      void setResult(@ParametricNullness O result) {
         this.set(result);
      }
   }
}
