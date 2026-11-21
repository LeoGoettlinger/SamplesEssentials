package com.fastasyncworldedit.core.util.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class KeyQueuedExecutorService<K> {
   private final ExecutorService parent;
   private final Map<K, KeyQueuedExecutorService<K>.KeyRunner> keyQueue = new HashMap<>();

   public KeyQueuedExecutorService(ExecutorService parent) {
      this.parent = parent;
   }

   public void shutdown() {
      this.parent.shutdown();
   }

   @Nonnull
   public List<Runnable> shutdownNow() {
      return this.parent.shutdownNow();
   }

   public boolean isShutdown() {
      return this.parent.isShutdown();
   }

   public boolean isTerminated() {
      return this.parent.isTerminated();
   }

   public boolean awaitTermination(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
      return this.parent.awaitTermination(timeout, unit);
   }

   protected <T> FutureTask<T> newTaskFor(Runnable runnable, T value) {
      return new FutureTask<>(runnable, value);
   }

   protected <T> FutureTask<T> newTaskFor(Callable<T> callable) {
      return new FutureTask<>(callable);
   }

   @Nonnull
   public <T> Future<T> submit(@Nonnull K key, @Nonnull Callable<T> task) {
      FutureTask<T> ftask = this.newTaskFor(task);
      this.execute(key, ftask);
      return ftask;
   }

   @Nonnull
   public <T> Future<T> submit(@Nonnull K key, @Nonnull Runnable task, T result) {
      FutureTask<T> ftask = this.newTaskFor(task, result);
      this.execute(key, ftask);
      return ftask;
   }

   @Nonnull
   public Future<?> submit(@Nonnull K key, @Nonnull Runnable task) {
      FutureTask<Void> ftask = this.newTaskFor(task, null);
      this.execute(key, ftask);
      return ftask;
   }

   public void execute(@Nonnull K key, @Nonnull FutureTask<?> command) {
      synchronized (this.keyQueue) {
         boolean triggerRun = false;
         KeyQueuedExecutorService<K>.KeyRunner runner = this.keyQueue.get(key);
         if (runner == null) {
            runner = new KeyQueuedExecutorService.KeyRunner(key);
            this.keyQueue.put(key, runner);
            triggerRun = true;
         }

         runner.add(command);
         if (triggerRun) {
            runner.triggerRun();
         }
      }
   }

   private final class KeyRunner {
      private final Queue<FutureTask<?>> tasks = new ConcurrentLinkedQueue<>();
      private final K key;

      private KeyRunner(K key) {
         this.key = key;
      }

      void add(FutureTask<?> task) {
         if (!this.tasks.add(task)) {
            throw new RejectedExecutionException(this.rejection());
         }
      }

      void triggerRun() {
         Runnable task = this.tasks.poll();
         if (task == null) {
            throw new RejectedExecutionException(this.rejection());
         } else {
            try {
               this.run(task);
            } catch (RejectedExecutionException var6) {
               synchronized (KeyQueuedExecutorService.this.keyQueue) {
                  KeyQueuedExecutorService.this.keyQueue.remove(this.key);
               }

               throw new RejectedExecutionException(this.rejection(), var6);
            }
         }
      }

      private void run(Runnable task) {
         KeyQueuedExecutorService.this.parent.execute(() -> {
            task.run();
            Runnable next = this.tasks.poll();
            if (next == null) {
               synchronized (KeyQueuedExecutorService.this.keyQueue) {
                  next = this.tasks.poll();
                  if (next == null) {
                     KeyQueuedExecutorService.this.keyQueue.remove(this.key);
                  }
               }
            }

            if (next != null) {
               this.run(next);
            }
         });
      }

      private String rejection() {
         return "Task for the key '" + this.key + "' rejected";
      }
   }
}
