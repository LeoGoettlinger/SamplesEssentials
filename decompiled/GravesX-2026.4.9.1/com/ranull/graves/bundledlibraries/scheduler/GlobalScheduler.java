package com.ranull.graves.bundledlibraries.scheduler;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public interface GlobalScheduler {
   boolean isGlobalThread();

   default boolean isTickThread() {
      return Bukkit.getServer().isPrimaryThread();
   }

   boolean isEntityThread(Entity var1);

   boolean isRegionThread(Location var1);

   GlobalTask runTask(Runnable var1);

   GlobalTask runTaskLater(Runnable var1, long var2);

   GlobalTask runTaskTimer(Runnable var1, long var2, long var4);

   @Deprecated
   GlobalTask runTask(Plugin var1, Runnable var2);

   @Deprecated
   GlobalTask runTaskLater(Plugin var1, Runnable var2, long var3);

   @Deprecated
   GlobalTask runTaskTimer(Plugin var1, Runnable var2, long var3, long var5);

   default GlobalTask runTask(Location location, Runnable runnable) {
      return this.runTask(runnable);
   }

   default GlobalTask runTaskLater(Location location, Runnable runnable, long delay) {
      return this.runTaskLater(runnable, delay);
   }

   default GlobalTask runTaskTimer(Location location, Runnable runnable, long delay, long period) {
      return this.runTaskTimer(runnable, delay, period);
   }

   @Deprecated
   default GlobalTask scheduleSyncDelayedTask(Runnable runnable) {
      return this.runTask(runnable);
   }

   @Deprecated
   default GlobalTask scheduleSyncDelayedTask(Runnable runnable, long delay) {
      return this.runTaskLater(runnable, delay);
   }

   @Deprecated
   default GlobalTask scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
      return this.runTaskTimer(runnable, delay, period);
   }

   default GlobalTask runTask(Entity entity, Runnable runnable) {
      return this.runTask(runnable);
   }

   default GlobalTask runTaskLater(Entity entity, Runnable runnable, long delay) {
      return this.runTaskLater(runnable, delay);
   }

   default GlobalTask runTaskTimer(Entity entity, Runnable runnable, long delay, long period) {
      return this.runTaskTimer(runnable, delay, period);
   }

   GlobalTask runTaskAsynchronously(Runnable var1);

   GlobalTask runTaskLaterAsynchronously(Runnable var1, long var2);

   GlobalTask runTaskTimerAsynchronously(Runnable var1, long var2, long var4);

   @Deprecated
   GlobalTask runTaskAsynchronously(Plugin var1, Runnable var2);

   @Deprecated
   GlobalTask runTaskLaterAsynchronously(Plugin var1, Runnable var2, long var3);

   @Deprecated
   GlobalTask runTaskTimerAsynchronously(Plugin var1, Runnable var2, long var3, long var5);

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

   void cancel(int var1);

   default void execute(Location location, Runnable runnable) {
      this.execute(runnable);
   }

   default void execute(Entity entity, Runnable runnable) {
      this.execute(runnable);
   }

   void cancelAll();

   void cancelAll(Plugin var1);

   @NotNull
   static GlobalScheduler getScheduler(@NotNull Plugin plugin) {
      return SchedulerUtils.getScheduler(Objects.requireNonNull(plugin));
   }

   @NotNull
   static GlobalScheduler getScheduler() {
      return getScheduler(JavaPlugin.getProvidingPlugin(GlobalScheduler.class));
   }
}
