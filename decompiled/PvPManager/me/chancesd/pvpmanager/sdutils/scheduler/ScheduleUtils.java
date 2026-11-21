package me.chancesd.pvpmanager.sdutils.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ScheduleUtils {
   private static ScheduledExecutorService executor;
   private static final boolean FOLIA_SUPPORT = checkFolia();
   private static final List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();
   private static SchedulerProvider provider;

   private ScheduleUtils() {
   }

   public static void setupExecutor(JavaPlugin plugin) {
      provider = (SchedulerProvider)(FOLIA_SUPPORT ? new FoliaProvider(plugin) : new BukkitProvider(plugin));
      executor = Executors.newScheduledThreadPool(
         Math.max(4, Runtime.getRuntime().availableProcessors() / 2),
         new ThreadFactoryBuilder().setNameFormat(plugin.getName() + " Worker Thread - %d").build()
      );
   }

   public static void setExecutor(ScheduledExecutorService executor) {
      ScheduleUtils.executor = executor;
   }

   public static void runAsync(@NotNull Runnable task) {
      executor.execute(new ScheduleUtils.ExceptionRunnable(task));
   }

   public static ScheduledFuture<?> runAsyncLater(@NotNull Runnable task, long delay, TimeUnit unit) {
      if (delay < 0L) {
         Log.warning("Cannot schedule task: delay cannot be negative (" + delay + ")");
         return null;
      } else {
         return executor.schedule(new ScheduleUtils.ExceptionRunnable(task), delay, unit);
      }
   }

   public static ScheduledFuture<?> runAsyncTimer(@NotNull Runnable task, long delay, long period, TimeUnit unit) {
      if (delay < 0L) {
         Log.warning("Cannot schedule task: delay cannot be negative (" + delay + ")");
         return null;
      } else if (period <= 0L) {
         Log.warning("Cannot schedule task: period must be positive (" + period + " " + unit + "). Skipping task scheduling.");
         return null;
      } else {
         ScheduledFuture<?> scheduledTask = executor.scheduleAtFixedRate(new ScheduleUtils.ExceptionRunnable(task), delay, period, unit);
         scheduledTasks.add(scheduledTask);
         return scheduledTask;
      }
   }

   public static <T> Future<T> runPlatformTask(Supplier<T> task) {
      CompletableFuture<T> future = new CompletableFuture<>();
      provider.runTask(() -> {
         try {
            T result = task.get();
            future.complete(result);
         } catch (Throwable var3) {
            future.completeExceptionally(var3);
            Log.severe(var3.getMessage(), var3);
         }
      });
      return future;
   }

   public static <T> Future<T> runPlatformTask(Supplier<T> task, World world, int x, int z) {
      CompletableFuture<T> future = new CompletableFuture<>();
      provider.runTask(() -> {
         try {
            T result = task.get();
            future.complete(result);
         } catch (Throwable var3x) {
            future.completeExceptionally(var3x);
            Log.severe(var3x.getMessage(), var3x);
         }
      }, world, x, z);
      return future;
   }

   public static void runPlatformAsync(Runnable task) {
      provider.runPlatformAsync(task);
   }

   public static void runPlatformAsyncLater(Runnable task, long delay) {
      provider.runPlatformAsyncLater(task, delay);
   }

   public static void runPlatformAsyncTimer(Runnable task, long delay, long period) {
      provider.runPlatformAsyncTimer(task, delay, period);
   }

   public static void runPlatformTask(Runnable task) {
      provider.runTask(task);
   }

   public static void runPlatformTask(Runnable task, @NotNull Entity entity) {
      provider.runTask(task, entity);
   }

   public static void runPlatformTaskLater(Runnable task, long delay) {
      provider.runTaskLater(task, delay);
   }

   public static void runPlatformTaskLater(Runnable task, Entity entity, long delay) {
      provider.runTaskLater(task, entity, delay);
   }

   public static SDTask runPlatformTaskTimer(SDCancellableTask task, Entity entity, long delay, long period) {
      SDTask sdTask = provider.runTaskTimer(task, entity, delay, period);
      task.setTask(sdTask);
      return sdTask;
   }

   public static void executeConsoleCommand(String command) {
      provider.executeConsoleCommand(command);
   }

   public static void executePlayerCommand(Player player, String command) {
      provider.executePlayerCommand(player, command);
   }

   public static void ensureMainThread(Runnable task) {
      if (provider.isPrimaryThread()) {
         task.run();
      } else {
         runPlatformTask(task);
      }
   }

   public static SDTask ensureMainThread(Runnable task, @NotNull Entity entity) {
      if (Bukkit.isPrimaryThread()) {
         task.run();
         return null;
      } else {
         return provider.runTask(task, entity);
      }
   }

   public static CompletableFuture<Boolean> teleport(Entity entity, @NotNull Location loc) {
      return provider.teleport(entity, loc);
   }

   public static CompletableFuture<Boolean> teleport(Entity entity, @NotNull Location loc, @NotNull String errorMessage) {
      CompletableFuture<Boolean> future = provider.teleport(entity, loc);
      future.whenComplete((success, ex) -> {
         if (!success || ex != null) {
            Log.severe(errorMessage, ex);
         }
      });
      return future;
   }

   public static void cancelAllTasks() {
      scheduledTasks.forEach(scheduledTask -> scheduledTask.cancel(false));
      scheduledTasks.clear();
      executor.shutdown();
      provider.cancelAllTasks();
   }

   public static ExecutorService newBoundedCachedThreadPool(int corePoolSize, int maxPoolSize, ThreadFactory threadFactory) {
      BlockingQueue<Runnable> queue = new LinkedTransferQueue<Runnable>() {
         private static final long serialVersionUID = 4672233456178006928L;

         public boolean offer(Runnable e) {
            return this.tryTransfer(e);
         }
      };
      ThreadPoolExecutor threadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60L, TimeUnit.SECONDS, queue, threadFactory);
      threadPool.setRejectedExecutionHandler((runnable, poolExecutor) -> {
         try {
            poolExecutor.getQueue().put(runnable);
         } catch (InterruptedException var3x) {
            Thread.currentThread().interrupt();
         }
      });
      return threadPool;
   }

   public static CompletableFuture<Void> thenRunSync(CompletableFuture<?> future, Runnable action) {
      CompletableFuture<Void> result = new CompletableFuture<>();
      future.thenRun(() -> provider.runTask(new ScheduleUtils.ExceptionRunnable(() -> {
         action.run();
         result.complete(null);
      }))).exceptionally(throwable -> {
         Log.severe("Exception in async chain before thenRunSync: " + throwable.getMessage(), throwable);
         result.completeExceptionally(throwable);
         return null;
      });
      return result;
   }

   private static boolean checkFolia() {
      try {
         Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
         Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
         return true;
      } catch (Throwable var1) {
         return false;
      }
   }

   public static boolean isFolia() {
      return FOLIA_SUPPORT;
   }

   public static boolean isServerStopping() {
      return Utils.isPaper() && provider.isServerStopping();
   }

   public static class ExceptionRunnable implements Runnable {
      private final Runnable task;

      public ExceptionRunnable(Runnable task) {
         this.task = task;
      }

      @Override
      public void run() {
         try {
            this.task.run();
         } catch (Throwable var2) {
            Log.severe(var2.getMessage(), var2);
         }
      }
   }
}
