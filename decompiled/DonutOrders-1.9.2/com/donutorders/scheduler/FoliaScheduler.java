package com.donutorders.scheduler;

import com.donutorders.DonutOrders;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class FoliaScheduler {
   public static final boolean IS_FOLIA = detectFolia();

   private FoliaScheduler() {
   }

   private static boolean detectFolia() {
      try {
         Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
         return true;
      } catch (ClassNotFoundException var2) {
         String serverClass = Bukkit.getServer().getClass().getName().toLowerCase();
         return serverClass.contains("folia") || serverClass.contains("regionized");
      }
   }

   private static Plugin plugin() {
      return DonutOrders.getInstance();
   }

   private static Logger log() {
      DonutOrders instance = DonutOrders.getInstance();
      return instance != null ? instance.getLogger() : Logger.getLogger("DonutOrders");
   }

   private static void safeRun(Runnable task) {
      try {
         task.run();
      } catch (Throwable var2) {
         log().log(Level.SEVERE, "Scheduled task failed", var2);
      }
   }

   public static void runAtEntity(Entity entity, Runnable task, Runnable retired) {
      Plugin plugin = plugin();
      if (IS_FOLIA) {
         entity.getScheduler().run(plugin, t -> safeRun(task), retired);
      } else {
         try {
            Bukkit.getScheduler().runTask(plugin, task);
         } catch (UnsupportedOperationException var5) {
            entity.getScheduler().run(plugin, t -> safeRun(task), retired);
         }
      }
   }

   public static void runAtEntity(Entity entity, Runnable task) {
      runAtEntity(entity, task, () -> {});
   }

   public static void runAtEntityDelayed(Entity entity, Runnable task, Runnable retired, long delayTicks) {
      Plugin plugin = plugin();
      if (IS_FOLIA) {
         entity.getScheduler().runDelayed(plugin, t -> safeRun(task), retired, delayTicks);
      } else {
         try {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
         } catch (UnsupportedOperationException var7) {
            entity.getScheduler().runDelayed(plugin, t -> safeRun(task), retired, delayTicks);
         }
      }
   }

   public static void runAtLocation(Location location, Runnable task) {
      Plugin plugin = plugin();
      if (IS_FOLIA) {
         Bukkit.getRegionScheduler().run(plugin, location, t -> safeRun(task));
      } else {
         try {
            Bukkit.getScheduler().runTask(plugin, task);
         } catch (UnsupportedOperationException var4) {
            Bukkit.getRegionScheduler().run(plugin, location, t -> safeRun(task));
         }
      }
   }

   public static void runGlobal(Runnable task) {
      Plugin plugin = plugin();
      if (IS_FOLIA) {
         Bukkit.getGlobalRegionScheduler().run(plugin, t -> safeRun(task));
      } else {
         try {
            Bukkit.getScheduler().runTask(plugin, task);
         } catch (UnsupportedOperationException var3) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> safeRun(task));
         }
      }
   }

   public static void runGlobalDelayed(Runnable task, long delayTicks) {
      Plugin plugin = plugin();
      if (IS_FOLIA) {
         Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> safeRun(task), delayTicks);
      } else {
         try {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
         } catch (UnsupportedOperationException var5) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> safeRun(task), delayTicks);
         }
      }
   }

   public static void runGlobalRepeating(Runnable task, long initialTicks, long periodTicks) {
      Plugin plugin = plugin();
      if (IS_FOLIA) {
         Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> safeRun(task), initialTicks, periodTicks);
      } else {
         try {
            Bukkit.getScheduler().runTaskTimer(plugin, task, initialTicks, periodTicks);
         } catch (UnsupportedOperationException var7) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> safeRun(task), initialTicks, periodTicks);
         }
      }
   }

   public static void runAsync(Runnable task) {
      Plugin plugin = plugin();
      if (IS_FOLIA) {
         Bukkit.getAsyncScheduler().runNow(plugin, t -> safeRun(task));
      } else {
         try {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
         } catch (UnsupportedOperationException var3) {
            Bukkit.getAsyncScheduler().runNow(plugin, t -> safeRun(task));
         }
      }
   }
}
