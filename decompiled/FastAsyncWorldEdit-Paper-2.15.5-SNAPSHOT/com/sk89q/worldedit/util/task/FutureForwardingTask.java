package com.sk89q.worldedit.util.task;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.util.task.progress.Progress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;

public class FutureForwardingTask<V> extends AbstractTask<V> {
   private final ListenableFuture<V> future;

   private FutureForwardingTask(ListenableFuture<V> future, String name, @Nullable Object owner) {
      super(name, owner);
      Preconditions.checkNotNull(future);
      this.future = future;
   }

   public void addListener(Runnable listener, Executor executor) {
      this.future.addListener(listener, executor);
   }

   public boolean cancel(boolean mayInterruptIfRunning) {
      return this.future.cancel(mayInterruptIfRunning);
   }

   public boolean isCancelled() {
      return this.future.isCancelled();
   }

   public boolean isDone() {
      return this.future.isDone();
   }

   public V get() throws InterruptedException, ExecutionException {
      return (V)this.future.get();
   }

   public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return (V)this.future.get(timeout, unit);
   }

   @Override
   public Task.State getState() {
      if (this.isCancelled()) {
         return Task.State.CANCELLED;
      } else if (this.isDone()) {
         try {
            this.get();
            return Task.State.SUCCEEDED;
         } catch (InterruptedException var2) {
            return Task.State.CANCELLED;
         } catch (ExecutionException var3) {
            return Task.State.FAILED;
         }
      } else {
         return Task.State.RUNNING;
      }
   }

   @Override
   public Progress getProgress() {
      return Progress.indeterminate();
   }

   public static <V> FutureForwardingTask<V> create(ListenableFuture<V> future, String name, @Nullable Object owner) {
      return new FutureForwardingTask<>(future, name, owner);
   }
}
