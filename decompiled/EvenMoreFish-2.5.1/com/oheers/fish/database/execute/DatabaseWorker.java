package com.oheers.fish.database.execute;

import com.oheers.fish.EvenMoreFish;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.NonNull;

public class DatabaseWorker implements Executor, AutoCloseable {
   private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
   private static final int DEFAULT_BACKLOG_WARNING_THRESHOLD = 1000;
   private final ExecutorService executor;
   private final AtomicInteger pendingTasks = new AtomicInteger();
   private final AtomicBoolean backlogWarningActive = new AtomicBoolean();
   private final int backlogWarningThreshold;

   public DatabaseWorker() {
      this("emf-db-worker", 1000);
   }

   public DatabaseWorker(@NonNull String threadNamePrefix, int backlogWarningThreshold) {
      if (backlogWarningThreshold <= 0) {
         throw new IllegalArgumentException("backlogWarningThreshold must be > 0");
      } else {
         this.backlogWarningThreshold = backlogWarningThreshold;
         this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(threadNamePrefix + "-" + THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);
            return thread;
         });
         this.debug("Database worker started with thread prefix '%s'.".formatted(threadNamePrefix));
      }
   }

   @Override
   public void execute(@NonNull Runnable task) {
      Objects.requireNonNull(task, "task");
      this.submit(() -> this.runLogged(task));
   }

   @NonNull
   public CompletableFuture<Void> write(@NonNull Runnable work) {
      Objects.requireNonNull(work, "work");
      CompletableFuture<Void> future = new CompletableFuture<>();
      this.submit(() -> {
         try {
            work.run();
            future.complete(null);
         } catch (Exception var4) {
            this.logger().log(Level.SEVERE, "Async database write failed.", (Throwable)var4);
            future.completeExceptionally(var4);
         }
      });
      return future;
   }

   @NonNull
   public <T> CompletableFuture<T> writeResult(@NonNull Supplier<T> work) {
      return this.submitFuture(work, "Async database write failed.");
   }

   @NonNull
   public <T> CompletableFuture<T> query(@NonNull Supplier<T> work) {
      return this.submitFuture(work, "Async database query failed.");
   }

   public int getPendingTasks() {
      return this.pendingTasks.get();
   }

   public boolean shutdown(long timeout, @NonNull TimeUnit unit) {
      this.executor.shutdown();

      try {
         if (this.executor.awaitTermination(timeout, unit)) {
            return true;
         }
      } catch (InterruptedException var5) {
         Thread.currentThread().interrupt();
      }

      List<Runnable> dropped = this.executor.shutdownNow();
      this.logger().warning("Database worker did not drain in time; " + dropped.size() + " pending task(s) were dropped.");
      return false;
   }

   @Override
   public void close() {
      this.executor.shutdown();
   }

   private <T> CompletableFuture<T> submitFuture(Supplier<T> work, String failureMessage) {
      Objects.requireNonNull(work, "work");
      CompletableFuture<T> future = new CompletableFuture<>();
      this.submit(() -> {
         try {
            future.complete(work.get());
         } catch (Exception var5) {
            this.logger().log(Level.SEVERE, failureMessage, (Throwable)var5);
            future.completeExceptionally(var5);
         }
      });
      return future;
   }

   private void submit(Runnable task) {
      int backlog = this.pendingTasks.incrementAndGet();
      this.warnIfBacklogged(backlog);

      try {
         this.executor.execute(() -> {
            try {
               task.run();
            } finally {
               int remaining = this.pendingTasks.decrementAndGet();
               if (remaining < this.backlogWarningThreshold) {
                  this.backlogWarningActive.set(false);
               }
            }
         });
      } catch (RejectedExecutionException var4) {
         this.pendingTasks.decrementAndGet();
         task.run();
      }
   }

   private void warnIfBacklogged(int backlog) {
      if (backlog >= this.backlogWarningThreshold && this.backlogWarningActive.compareAndSet(false, true)) {
         this.logger().warning("Database worker backlog has reached " + backlog + " pending task(s).");
      }
   }

   private void runLogged(Runnable task) {
      try {
         task.run();
      } catch (Exception var3) {
         this.logger().log(Level.SEVERE, "Async database task failed.", (Throwable)var3);
      }
   }

   private Logger logger() {
      try {
         Logger pluginLogger = EvenMoreFish.getInstance().getLogger();
         return pluginLogger == null ? this.fallbackLogger() : pluginLogger;
      } catch (RuntimeException var2) {
         return this.fallbackLogger();
      }
   }

   private Logger fallbackLogger() {
      return Logger.getLogger("EvenMoreFish");
   }

   private void debug(@NonNull String message) {
      try {
         EvenMoreFish.getInstance().debug(message);
      } catch (RuntimeException var3) {
      }
   }
}
