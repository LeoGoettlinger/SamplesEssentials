package com.fastasyncworldedit.core.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;

public class MultiFuture implements Future<Object[]> {
   private final List<Future<?>> futures;

   public MultiFuture(List<Future<?>> futures) {
      this.futures = futures;
   }

   @Override
   public boolean cancel(final boolean mayInterruptIfRunning) {
      return this.futures.stream().allMatch(f -> f.cancel(mayInterruptIfRunning));
   }

   @Override
   public boolean isCancelled() {
      return this.futures.stream().allMatch(Future::isCancelled);
   }

   @Override
   public boolean isDone() {
      return this.futures.stream().allMatch(Future::isDone);
   }

   public Object[] get() {
      return this.futures.stream().map(f -> {
         try {
            return f.get();
         } catch (ExecutionException | InterruptedException var2) {
            return var2;
         }
      }).toArray();
   }

   public Object[] get(final long timeout, @Nonnull final TimeUnit unit) {
      return this.futures.stream().map(f -> {
         try {
            return f.get(timeout / this.futures.size(), unit);
         } catch (ExecutionException | TimeoutException | InterruptedException var6) {
            return var6;
         }
      }).toArray();
   }
}
