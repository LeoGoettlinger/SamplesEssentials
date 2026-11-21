package com.magmaguy.elitemobs.magmacore.nightbreak;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.IntPredicate;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

final class NightbreakPluginAsyncWorkTracker {
   private static final long SHUTDOWN_QUIESCENCE_TIMEOUT_SECONDS = 30L;
   private final Map<JavaPlugin, Set<NightbreakPluginAsyncWorkTracker.Work>> activeWork = new IdentityHashMap<>();
   private final IntPredicate taskActive;

   NightbreakPluginAsyncWorkTracker() {
      this(taskId -> Bukkit.getScheduler().isQueued(taskId) || Bukkit.getScheduler().isCurrentlyRunning(taskId));
   }

   NightbreakPluginAsyncWorkTracker(IntPredicate taskActive) {
      this.taskActive = taskActive;
   }

   synchronized NightbreakPluginAsyncWorkTracker.Work register(JavaPlugin plugin) {
      this.pruneCompleted(plugin);
      NightbreakPluginAsyncWorkTracker.Work work = new NightbreakPluginAsyncWorkTracker.Work(plugin);
      this.activeWork.computeIfAbsent(plugin, ignored -> ConcurrentHashMap.newKeySet()).add(work);
      return work;
   }

   synchronized List<NightbreakPluginAsyncWorkTracker.Work> snapshot(JavaPlugin plugin) {
      this.pruneCompleted(plugin);
      Set<NightbreakPluginAsyncWorkTracker.Work> currentWork = this.activeWork.get(plugin);
      return (List<NightbreakPluginAsyncWorkTracker.Work>)(currentWork == null ? List.of() : new ArrayList<>(currentWork));
   }

   private void pruneCompleted(JavaPlugin plugin) {
      Set<NightbreakPluginAsyncWorkTracker.Work> currentWork = this.activeWork.get(plugin);
      if (currentWork != null) {
         currentWork.removeIf(work -> work.canRetire() && !this.isTaskActive(work));
         if (currentWork.isEmpty()) {
            this.activeWork.remove(plugin);
         }
      }
   }

   void awaitQuiescence(String pluginName, List<NightbreakPluginAsyncWorkTracker.Work> work) {
      if (!work.isEmpty()) {
         if (work.stream().anyMatch(NightbreakPluginAsyncWorkTracker.Work::isRunningOnCurrentThread)) {
            throw new IllegalStateException(pluginName + " cannot shut down reentrantly from an updater worker.");
         } else {
            long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(30L);
            CompletableFuture<?>[] dispatchesAndBodies = work.stream()
               .flatMap(item -> Stream.of(item.dispatchCompleted(), item.completion()))
               .toArray(CompletableFuture[]::new);

            try {
               CompletableFuture.allOf(dispatchesAndBodies).get(remainingNanos(deadlineNanos), TimeUnit.NANOSECONDS);

               while (work.stream().anyMatch(this::isTaskActive)) {
                  long remainingNanos = remainingNanos(deadlineNanos);
                  TimeUnit.NANOSECONDS.sleep(Math.min(remainingNanos, TimeUnit.MILLISECONDS.toNanos(10L)));
               }

               synchronized (this) {
                  work.forEach(this::unregisterLocked);
               }
            } catch (InterruptedException var10) {
               Thread.currentThread().interrupt();
               throw new IllegalStateException("Interrupted while waiting for " + pluginName + " updater work to stop.", var10);
            } catch (ExecutionException var11) {
               throw new IllegalStateException(pluginName + " updater termination completed exceptionally.", var11.getCause());
            } catch (TimeoutException var12) {
               throw new IllegalStateException(
                  pluginName + " updater work did not stop within 30 seconds; refusing to complete shutdown while shaded code is still running.", var12
               );
            }
         }
      }
   }

   private boolean isTaskActive(NightbreakPluginAsyncWorkTracker.Work work) {
      int taskId = work.taskId();
      return taskId >= 0 && this.taskActive.test(taskId);
   }

   private static long remainingNanos(long deadlineNanos) throws TimeoutException {
      long remainingNanos = deadlineNanos - System.nanoTime();
      if (remainingNanos <= 0L) {
         throw new TimeoutException("Updater shutdown deadline elapsed.");
      } else {
         return remainingNanos;
      }
   }

   private void unregisterLocked(NightbreakPluginAsyncWorkTracker.Work work) {
      Set<NightbreakPluginAsyncWorkTracker.Work> currentWork = this.activeWork.get(work.plugin);
      if (currentWork != null) {
         currentWork.remove(work);
         if (currentWork.isEmpty()) {
            this.activeWork.remove(work.plugin);
         }
      }
   }

   static final class Work implements AutoCloseable {
      private final JavaPlugin plugin;
      private final CompletableFuture<Void> completion = new CompletableFuture<>();
      private final CompletableFuture<Void> dispatchCompleted = new CompletableFuture<>();
      private final CompletableFuture<Void> shutdownRequested = new CompletableFuture<>();
      private BukkitTask task;
      private Thread runningThread;
      private boolean terminal;
      private boolean dispatchSettled;

      private Work(JavaPlugin plugin) {
         this.plugin = plugin;
      }

      synchronized boolean start() {
         if (!this.terminal && this.runningThread == null) {
            this.runningThread = Thread.currentThread();
            this.dispatchCompleted.complete(null);
            return true;
         } else {
            return false;
         }
      }

      void attach(BukkitTask task) {
         boolean cancel;
         synchronized (this) {
            this.task = task;
            this.dispatchSettled = true;
            cancel = this.terminal || this.shutdownRequested.isDone();
         }

         this.dispatchCompleted.complete(null);
         if (cancel) {
            cancelTask(task);
         }
      }

      void dispatchFailed() {
         synchronized (this) {
            this.dispatchSettled = true;
         }

         this.dispatchCompleted.complete(null);
         this.close();
      }

      void requestShutdown() {
         BukkitTask taskToCancel;
         boolean complete;
         synchronized (this) {
            this.shutdownRequested.complete(null);
            taskToCancel = this.task;
            complete = this.runningThread == null && !this.terminal;
            if (complete) {
               this.terminal = true;
            }
         }

         if (taskToCancel != null) {
            cancelTask(taskToCancel);
         }

         if (complete) {
            this.publishCompletion();
         }
      }

      CompletableFuture<Void> cancellationRequested() {
         return this.shutdownRequested;
      }

      CompletableFuture<Void> dispatchCompleted() {
         return this.dispatchCompleted;
      }

      CompletableFuture<Void> completion() {
         return this.completion;
      }

      synchronized boolean isRunningOnCurrentThread() {
         return this.runningThread == Thread.currentThread();
      }

      @Override
      public void close() {
         boolean complete;
         synchronized (this) {
            complete = !this.terminal;
            this.terminal = true;
            this.runningThread = null;
         }

         if (complete) {
            this.publishCompletion();
         }
      }

      private void publishCompletion() {
         this.completion.complete(null);
      }

      synchronized int taskId() {
         return this.task == null ? -1 : this.task.getTaskId();
      }

      synchronized boolean canRetire() {
         return this.dispatchSettled && this.terminal && this.runningThread == null && this.completion.isDone();
      }

      private static void cancelTask(BukkitTask task) {
         try {
            task.cancel();
         } catch (RuntimeException var2) {
         }
      }
   }
}
