package com.oheers.fish.api.utils;

import com.oheers.fish.api.plugin.EMFPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class Scheduling {
   private static final Scheduling INSTANCE = new Scheduling();
   private static final EMFPlugin PLUGIN = EMFPlugin.getInstance();

   private Scheduling() {
   }

   @NonNull
   public static Scheduling getInstance() {
      return INSTANCE;
   }

   @NonNull
   public ScheduledTask runTask(@NonNull Runnable runnable) {
      return Bukkit.getGlobalRegionScheduler().run(PLUGIN, task -> runnable.run());
   }

   @NonNull
   public ScheduledTask runTaskLater(@NonNull Runnable runnable, long delayTicks) {
      return Bukkit.getGlobalRegionScheduler().runDelayed(PLUGIN, task -> runnable.run(), this.calculateDelay(delayTicks));
   }

   @NonNull
   public ScheduledTask runTaskTimer(@NonNull Runnable runnable, long delayTicks, long periodTicks) {
      return Bukkit.getGlobalRegionScheduler().runAtFixedRate(PLUGIN, task -> runnable.run(), this.calculateDelay(delayTicks), periodTicks);
   }

   @NonNull
   public ScheduledTask runTask(@NonNull Location location, @NonNull Runnable runnable) {
      return Bukkit.getRegionScheduler().run(PLUGIN, location, task -> runnable.run());
   }

   @NonNull
   public ScheduledTask runTaskLater(@NonNull Location location, @NonNull Runnable runnable, long delayTicks) {
      return Bukkit.getRegionScheduler().runDelayed(PLUGIN, location, task -> runnable.run(), this.calculateDelay(delayTicks));
   }

   @NonNull
   public ScheduledTask runTaskTimer(@NonNull Location location, @NonNull Runnable runnable, long delayTicks, long periodTicks) {
      return Bukkit.getRegionScheduler().runAtFixedRate(PLUGIN, location, task -> runnable.run(), this.calculateDelay(delayTicks), periodTicks);
   }

   @Nullable
   public ScheduledTask runTask(@NonNull Entity entity, @NonNull Runnable runnable) {
      return entity.getScheduler().run(PLUGIN, task -> runnable.run(), null);
   }

   @Nullable
   public ScheduledTask runTaskLater(@NonNull Entity entity, @NonNull Runnable runnable, long delayTicks) {
      return entity.getScheduler().runDelayed(PLUGIN, task -> runnable.run(), null, this.calculateDelay(delayTicks));
   }

   @Nullable
   public ScheduledTask runTaskTimer(@NonNull Entity entity, @NonNull Runnable runnable, long delayTicks, long periodTicks) {
      return entity.getScheduler().runAtFixedRate(PLUGIN, task -> runnable.run(), null, this.calculateDelay(delayTicks), periodTicks);
   }

   private long calculateDelay(long initialDelay) {
      return PLUGIN.isRunningOnFolia() ? Math.max(1L, initialDelay) : initialDelay;
   }
}
