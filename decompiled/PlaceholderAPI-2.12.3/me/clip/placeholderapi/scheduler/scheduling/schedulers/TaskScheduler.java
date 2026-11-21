package me.clip.placeholderapi.scheduler.scheduling.schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import me.clip.placeholderapi.scheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public interface TaskScheduler {
   boolean isGlobalThread();

   default boolean isTickThread() {
      return Bukkit.getServer().isPrimaryThread();
   }

   boolean isEntityThread(Entity var1);

   boolean isRegionThread(Location var1);

   MyScheduledTask runTask(Runnable var1);

   MyScheduledTask runTaskLater(Runnable var1, long var2);

   MyScheduledTask runTaskTimer(Runnable var1, long var2, long var4);

   @Deprecated
   default MyScheduledTask runTask(Plugin plugin, Runnable runnable) {
      return this.runTask(runnable);
   }

   @Deprecated
   default MyScheduledTask runTaskLater(Plugin plugin, Runnable runnable, long delay) {
      return this.runTaskLater(runnable, delay);
   }

   @Deprecated
   default MyScheduledTask runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
      return this.runTaskTimer(runnable, delay, period);
   }

   default MyScheduledTask runTask(Location location, Runnable runnable) {
      return this.runTask(runnable);
   }

   default MyScheduledTask runTaskLater(Location location, Runnable runnable, long delay) {
      return this.runTaskLater(runnable, delay);
   }

   default MyScheduledTask runTaskTimer(Location location, Runnable runnable, long delay, long period) {
      return this.runTaskTimer(runnable, delay, period);
   }

   @Deprecated
   default MyScheduledTask scheduleSyncDelayedTask(Runnable runnable, long delay) {
      return this.runTaskLater(runnable, delay);
   }

   @Deprecated
   default MyScheduledTask scheduleSyncDelayedTask(Runnable runnable) {
      return this.runTask(runnable);
   }

   @Deprecated
   default MyScheduledTask scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
      return this.runTaskTimer(runnable, delay, period);
   }

   default MyScheduledTask runTask(Entity entity, Runnable runnable) {
      return this.runTask(runnable);
   }

   default MyScheduledTask runTaskLater(Entity entity, Runnable runnable, long delay) {
      return this.runTaskLater(runnable, delay);
   }

   default MyScheduledTask runTaskTimer(Entity entity, Runnable runnable, long delay, long period) {
      return this.runTaskTimer(runnable, delay, period);
   }

   MyScheduledTask runTaskAsynchronously(Runnable var1);

   MyScheduledTask runTaskLaterAsynchronously(Runnable var1, long var2);

   MyScheduledTask runTaskTimerAsynchronously(Runnable var1, long var2, long var4);

   @Deprecated
   default MyScheduledTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
      return this.runTaskAsynchronously(runnable);
   }

   @Deprecated
   default MyScheduledTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
      return this.runTaskLaterAsynchronously(runnable, delay);
   }

   @Deprecated
   default MyScheduledTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long period) {
      return this.runTaskTimerAsynchronously(runnable, delay, period);
   }

   default <T> Future<T> callSyncMethod(Callable<T> task) {
      CompletableFuture<T> completableFuture = new CompletableFuture<>();
      this.execute(() -> {
         try {
            completableFuture.complete(task.call());
         } catch (Exception var3) {
            throw new RuntimeException(var3);
         }
      });
      return completableFuture;
   }

   void execute(Runnable var1);

   default void execute(Location location, Runnable runnable) {
      this.execute(runnable);
   }

   default void execute(Entity entity, Runnable runnable) {
      this.execute(runnable);
   }

   void cancelTasks();

   void cancelTasks(Plugin var1);
}
