package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.implementation.folia;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.WrappedScheduler;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.implementation.folia.task.FoliaWrappedTask;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.task.WrappedTask;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.type.ImplementationType;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public final class FoliaWrappedScheduler implements WrappedScheduler {
   private static final ImplementationType IMPLEMENTATION_TYPE = ImplementationType.FOLIA;
   private static final Runnable EMPTY_RUNNABLE = () -> {};
   private final Plugin plugin;
   private final GlobalRegionScheduler globalRegionScheduler;
   private final AsyncScheduler asyncScheduler;
   private final RegionScheduler regionScheduler;

   public FoliaWrappedScheduler(@NotNull Plugin plugin) {
      this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null!");
      this.globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
      this.asyncScheduler = Bukkit.getAsyncScheduler();
      this.regionScheduler = Bukkit.getRegionScheduler();
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
      this.globalRegionScheduler.cancelTasks(this.plugin);
      this.asyncScheduler.cancelTasks(this.plugin);
   }

   @Override
   public void cancelTask(@NotNull WrappedTask wrappedTask) {
      wrappedTask.cancel();
   }

   @NotNull
   @Override
   public WrappedTask runTask(@NotNull Runnable runnable) {
      return this.setupTask(this.globalRegionScheduler.run(this.plugin, __ -> runnable.run()));
   }

   @NotNull
   @Override
   public WrappedTask runTaskAsynchronously(@NotNull Runnable runnable) {
      return this.setupTask(this.asyncScheduler.runNow(this.plugin, __ -> runnable.run()));
   }

   @Nullable
   @Override
   public WrappedTask runTaskAtEntity(@NotNull Entity entity, @NotNull Runnable runnable) {
      return this.setupTask(entity.getScheduler().run(this.plugin, __ -> runnable.run(), EMPTY_RUNNABLE));
   }

   @NotNull
   @Override
   public WrappedTask runTaskAtLocation(@NotNull Location location, @NotNull Runnable runnable) {
      return this.setupTask(this.regionScheduler.run(this.plugin, location, __ -> runnable.run()));
   }

   @NotNull
   @Override
   public WrappedTask runTaskTimer(@NotNull Runnable runnable, long delay, long period) {
      return this.setupTask(this.globalRegionScheduler.runAtFixedRate(this.plugin, __ -> runnable.run(), delay, period));
   }

   @NotNull
   @Override
   public WrappedTask runTaskTimerAsynchronously(@NotNull Runnable runnable, long delay, long period) {
      return this.setupTask(this.asyncScheduler.runAtFixedRate(this.plugin, __ -> runnable.run(), toMillis(delay), toMillis(period), TimeUnit.MILLISECONDS));
   }

   @Nullable
   @Override
   public WrappedTask runTaskTimerAtEntity(@NotNull Entity entity, @NotNull Runnable runnable, long delay, long period) {
      return this.setupTask(entity.getScheduler().runAtFixedRate(this.plugin, __ -> runnable.run(), EMPTY_RUNNABLE, delay, period));
   }

   @NotNull
   @Override
   public WrappedTask runTaskTimerAtLocation(@NotNull Location location, @NotNull Runnable runnable, long delay, long period) {
      return this.setupTask(this.regionScheduler.runAtFixedRate(this.plugin, location, __ -> runnable.run(), delay, period));
   }

   @NotNull
   @Override
   public WrappedTask runTaskLater(@NotNull Runnable runnable, long delay) {
      return this.setupTask(this.globalRegionScheduler.runDelayed(this.plugin, __ -> runnable.run(), delay));
   }

   @NotNull
   @Override
   public WrappedTask runTaskLaterAsynchronously(@NotNull Runnable runnable, long delay) {
      return this.setupTask(this.asyncScheduler.runDelayed(this.plugin, __ -> runnable.run(), toMillis(delay), TimeUnit.MILLISECONDS));
   }

   @Nullable
   @Override
   public WrappedTask runTaskLaterAtEntity(@NotNull Entity entity, @NotNull Runnable runnable, long delay) {
      return this.setupTask(entity.getScheduler().runDelayed(this.plugin, __ -> runnable.run(), EMPTY_RUNNABLE, delay));
   }

   @NotNull
   @Override
   public WrappedTask runTaskLaterAtLocation(@NotNull Location location, @NotNull Runnable runnable, long delay) {
      return this.setupTask(this.regionScheduler.runDelayed(this.plugin, location, __ -> runnable.run(), delay));
   }

   @UnknownNullability
   private WrappedTask setupTask(@Nullable ScheduledTask task) {
      return task == null ? null : new FoliaWrappedTask(task);
   }

   public static long toMillis(long ticks) {
      return ticks * 50L;
   }
}
