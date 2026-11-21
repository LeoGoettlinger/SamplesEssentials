package com.blaxk.spawnelytra.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public enum SchedulerUtil {
   private static final boolean FOLIA;

   public static boolean isFolia() {
      return FOLIA;
   }

   public static SchedulerUtil.TaskHandle runAsync(Plugin plugin, Runnable task) {
      if (FOLIA) {
         ScheduledTask t = Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
         return new SchedulerUtil.FoliaTaskHandle(t);
      } else {
         BukkitTask t = Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
         return new SchedulerUtil.BukkitTaskHandle(t);
      }
   }

   public static SchedulerUtil.TaskHandle runAsyncRepeating(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
      if (FOLIA) {
         long initialDelayMs = Math.max(1L, initialDelayTicks * 50L);
         long periodMs = Math.max(1L, periodTicks * 50L);
         ScheduledTask t = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
         return new SchedulerUtil.FoliaTaskHandle(t);
      } else {
         BukkitTask t = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, initialDelayTicks, periodTicks);
         return new SchedulerUtil.BukkitTaskHandle(t);
      }
   }

   public static SchedulerUtil.TaskHandle runNow(Plugin plugin, Runnable task) {
      if (FOLIA) {
         ScheduledTask t = Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
         return new SchedulerUtil.FoliaTaskHandle(t);
      } else {
         BukkitTask t = Bukkit.getScheduler().runTask(plugin, task);
         return new SchedulerUtil.BukkitTaskHandle(t);
      }
   }

   public static SchedulerUtil.TaskHandle runAtEntityNow(Plugin plugin, Player entity, Runnable task) {
      if (FOLIA) {
         ScheduledTask t = entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
         return new SchedulerUtil.FoliaTaskHandle(t);
      } else {
         return new SchedulerUtil.BukkitTaskHandle(Bukkit.getScheduler().runTask(plugin, task));
      }
   }

   public static SchedulerUtil.TaskHandle runAtEntityLater(Plugin plugin, Player entity, long delayTicks, Runnable task) {
      if (FOLIA) {
         ScheduledTask t = entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, Math.max(1L, delayTicks));
         return new SchedulerUtil.FoliaTaskHandle(t);
      } else {
         return new SchedulerUtil.BukkitTaskHandle(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
      }
   }

   public static SchedulerUtil.TaskHandle runAtEntityTimer(Plugin plugin, Player entity, long initialDelayTicks, long periodTicks, Runnable task) {
      if (FOLIA) {
         ScheduledTask t = entity.getScheduler()
            .runAtFixedRate(plugin, scheduledTask -> task.run(), null, Math.max(1L, initialDelayTicks), Math.max(1L, periodTicks));
         return new SchedulerUtil.FoliaTaskHandle(t);
      } else {
         return new SchedulerUtil.BukkitTaskHandle(Bukkit.getScheduler().runTaskTimer(plugin, task, initialDelayTicks, periodTicks));
      }
   }

   static {
      boolean folia;
      try {
         Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
         folia = true;
      } catch (ClassNotFoundException var2) {
         folia = false;
      }

      FOLIA = folia;
   }

   private static final class BukkitTaskHandle implements SchedulerUtil.TaskHandle {
      private final BukkitTask handle;

      BukkitTaskHandle(BukkitTask handle) {
         this.handle = handle;
      }

      @Override
      public void cancel() {
         if (this.handle != null) {
            this.handle.cancel();
         }
      }
   }

   private static final class FoliaTaskHandle implements SchedulerUtil.TaskHandle {
      private final ScheduledTask handle;

      FoliaTaskHandle(ScheduledTask handle) {
         this.handle = handle;
      }

      @Override
      public void cancel() {
         if (this.handle != null) {
            this.handle.cancel();
         }
      }
   }

   public interface TaskHandle {
      void cancel();
   }
}
