package com.earth2me.essentials.libs.configurate.reactive;

import com.earth2me.essentials.libs.configurate.util.CheckedSupplier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

class ExecutePublisher<V> implements Publisher<V> {
   private final CompletableFuture<V> actor = new CompletableFuture<>();
   private final Executor executor;

   ExecutePublisher(final CheckedSupplier<V, ? extends Exception> action, final Executor exec) {
      exec.execute(() -> {
         try {
            this.actor.complete(action.get());
         } catch (Exception var3) {
            this.actor.completeExceptionally(var3);
         }
      });
      this.executor = exec;
   }

   @Override
   public Disposable subscribe(final Subscriber<? super V> subscriber) {
      AtomicBoolean subscribed = new AtomicBoolean();
      this.actor.whenCompleteAsync((value, err) -> {
         if (subscribed.compareAndSet(true, false)) {
            if (err != null) {
               subscriber.onError(err);
            } else {
               try {
                  subscriber.submit(value);
                  subscriber.onClose();
               } catch (Exception var5) {
                  subscriber.onError(var5);
               }
            }
         }
      }, this.executor).exceptionally(ex -> {
         throw new Error(ex);
      });
      return () -> subscribed.set(false);
   }

   @Override
   public boolean hasSubscribers() {
      return !this.actor.isDone() && this.actor.getNumberOfDependents() > 0;
   }

   @Override
   public Executor executor() {
      return this.executor;
   }
}
