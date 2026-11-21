package com.oheers.fish.api;

import com.oheers.fish.api.plugin.EMFPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

@ApiStatus.Internal
public abstract class EMFTimer {
   private final TimeUnit unit;
   private final long interval;
   private ScheduledTask task;

   public EMFTimer(@NonNull TimeUnit unit, long interval) {
      this.unit = unit;
      this.interval = interval;
   }

   public void start() {
      if (this.task == null || this.task.isCancelled()) {
         this.task = Bukkit.getGlobalRegionScheduler()
            .runAtFixedRate(EMFPlugin.getInstance(), task -> this.run(), 1L, this.unit.toSeconds(this.interval) * 20L);
      }
   }

   public void stop() {
      if (this.task != null) {
         if (!this.task.isCancelled()) {
            this.task.cancel();
         }

         this.task = null;
      }
   }

   public abstract void run();
}
