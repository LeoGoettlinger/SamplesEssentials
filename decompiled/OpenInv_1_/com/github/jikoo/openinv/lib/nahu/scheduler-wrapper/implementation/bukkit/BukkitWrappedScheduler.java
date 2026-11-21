package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.implementation.bukkit;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.WrappedScheduler;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.implementation.bukkit.task.BukkitWrappedTask;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.task.WrappedTask;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.type.ImplementationType;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class BukkitWrappedScheduler implements WrappedScheduler {
   private static final ImplementationType IMPLEMENTATION_TYPE = ImplementationType.BUKKIT;
   private final Plugin plugin;
   private final BukkitScheduler scheduler;

   public BukkitWrappedScheduler(@NotNull Plugin plugin) {
      this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null!");
      this.scheduler = Bukkit.getScheduler();
   }

   @NotNull
   @Override
   public ImplementationType getImplementationType() {
      return IMPLEMENTATION_TYPE;
   }

   @NotNull
   @Override
   public Plugin getPlugin() {
      return this.plugin;
   }

   @Override
   public void cancelAllTasks() {
      this.scheduler.cancelTasks(this.plugin);
   }

   @Override
   public void cancelTask(@NotNull WrappedTask wrappedTask) {
      wrappedTask.cancel();
   }

   @NotNull
   @Override
   public WrappedTask runTask(@NotNull Runnable runnable) {
      return this.setupTask(this.scheduler.runTask(this.plugin, runnable));
   }

   @NotNull
   @Override
   public WrappedTask runTaskAsynchronously(@NotNull Runnable runnable) {
      return this.setupTask(this.scheduler.runTaskAsynchronously(this.plugin, runnable));
   }

   @NotNull
   @Override
   public WrappedTask runTaskAtEntity(@NotNull Entity entity, @NotNull Runnable runnable) {
      return this.runTask(runnable);
   }

   @NotNull
   @Override
   public WrappedTask runTaskAtLocation(@NotNull Location location, @NotNull Runnable runnable) {
      return this.runTask(runnable);
   }

   @NotNull
   @Override
   public WrappedTask runTaskTimer(@NotNull Runnable runnable, long delay, long period) {
      return this.setupTask(this.scheduler.runTaskTimer(this.plugin, runnable, delay, period));
   }

   @NotNull
   @Override
   public WrappedTask runTaskTimerAsynchronously(@NotNull Runnable runnable, long delay, long period) {
      return this.setupTask(this.scheduler.runTaskTimerAsynchronously(this.plugin, runnable, delay, period));
   }

   @NotNull
   @Override
   public WrappedTask runTaskTimerAtEntity(@NotNull Entity entity, @NotNull Runnable runnable, long delay, long period) {
      return this.runTaskTimer(runnable, delay, period);
   }

   @NotNull
   @Override
   public WrappedTask runTaskTimerAtLocation(@NotNull Location location, @NotNull Runnable runnable, long delay, long period) {
      return this.runTaskTimer(runnable, delay, period);
   }

   @NotNull
   @Override
   public WrappedTask runTaskLater(@NotNull Runnable runnable, long delay) {
      return this.setupTask(this.scheduler.runTaskLater(this.plugin, runnable, delay));
   }

   @NotNull
   @Override
   public WrappedTask runTaskLaterAsynchronously(@NotNull Runnable runnable, long delay) {
      return this.setupTask(this.scheduler.runTaskLaterAsynchronously(this.plugin, runnable, delay));
   }

   @NotNull
   @Override
   public WrappedTask runTaskLaterAtEntity(@NotNull Entity entity, @NotNull Runnable runnable, long delay) {
      return this.runTaskLater(runnable, delay);
   }

   @NotNull
   @Override
   public WrappedTask runTaskLaterAtLocation(@NotNull Location location, @NotNull Runnable runnable, long delay) {
      return this.runTaskLater(runnable, delay);
   }

   @NotNull
   private WrappedTask setupTask(@NotNull BukkitTask task) {
      return new BukkitWrappedTask(task);
   }
}
