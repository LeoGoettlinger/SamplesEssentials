package com.fastasyncworldedit.core.util.task;

import com.fastasyncworldedit.core.configuration.Settings;
import java.io.Closeable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class AsyncNotifyKeyedQueue implements Closeable {
   private static final KeyQueuedExecutorService<UUID> QUEUE_SUBMISSIONS = new KeyQueuedExecutorService<>(
      new ForkJoinPool(Settings.settings().QUEUE.PARALLEL_THREADS, new FaweForkJoinWorkerThreadFactory("AsyncNotifyKeyedQueue - %s"), null, false)
   );
   private final UncaughtExceptionHandler handler;
   private final Supplier<UUID> key;
   private volatile boolean closed;

   public AsyncNotifyKeyedQueue(UncaughtExceptionHandler handler, Supplier<UUID> key) {
      this.handler = handler;
      this.key = key;
   }

   public UncaughtExceptionHandler getHandler() {
      return this.handler;
   }

   public <T> Future<T> run(Runnable task) {
      return this.call(() -> {
         task.run();
         return null;
      });
   }

   public <T> Future<T> call(Callable<T> task) {
      Future[] self = new Future[1];
      Callable<T> wrapped = () -> {
         if (!this.closed) {
            try {
               return task.call();
            } catch (Throwable var4) {
               this.handler.uncaughtException(Thread.currentThread(), var4);
            }
         }

         if (self[0] != null) {
            self[0].cancel(true);
         }

         return null;
      };
      self[0] = QUEUE_SUBMISSIONS.submit(this.key.get(), wrapped);
      return self[0];
   }

   @Override
   public void close() {
      this.closed = true;
   }

   public boolean isClosed() {
      return this.closed;
   }
}
