package cm.chunkManager.utils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class Scheduler {
   private static Boolean folia = null;

   private Scheduler() {
   }

   public static boolean isFolia() {
      if (folia == null) {
         try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
         } catch (ClassNotFoundException var1) {
            folia = false;
         }
      }

      return folia;
   }

   public static Scheduler.Handle runGlobal(JavaPlugin plugin, Runnable task) {
      if (isFolia()) {
         try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Consumer<Object> consumer = t -> task.run();
            Method method = scheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
            return new Scheduler.Handle(method.invoke(scheduler, plugin, consumer));
         } catch (Exception var5) {
            plugin.getLogger().warning("Folia global task failed: " + var5.getMessage());
            return new Scheduler.Handle(null);
         }
      } else {
         return new Scheduler.Handle(Bukkit.getScheduler().runTask(plugin, task));
      }
   }

   public static Scheduler.Handle runGlobalLater(JavaPlugin plugin, Runnable task, long delayTicks) {
      if (isFolia()) {
         try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Consumer<Object> consumer = t -> task.run();
            Method method = scheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
            return new Scheduler.Handle(method.invoke(scheduler, plugin, consumer, Math.max(1L, delayTicks)));
         } catch (Exception var7) {
            plugin.getLogger().warning("Folia delayed task failed: " + var7.getMessage());
            return new Scheduler.Handle(null);
         }
      } else {
         return new Scheduler.Handle(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
      }
   }

   public static Scheduler.Handle runGlobalTimer(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
      if (isFolia()) {
         try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Consumer<Object> consumer = t -> task.run();
            Method method = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            return new Scheduler.Handle(method.invoke(scheduler, plugin, consumer, Math.max(1L, delayTicks), Math.max(1L, periodTicks)));
         } catch (Exception var9) {
            plugin.getLogger().warning("Folia global timer failed: " + var9.getMessage());
            return new Scheduler.Handle(null);
         }
      } else {
         return new Scheduler.Handle(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
      }
   }

   public static Scheduler.Handle runAsync(JavaPlugin plugin, Runnable task) {
      if (isFolia()) {
         try {
            Object scheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Consumer<Object> consumer = t -> task.run();
            Method method = scheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);
            return new Scheduler.Handle(method.invoke(scheduler, plugin, consumer));
         } catch (Exception var5) {
            plugin.getLogger().warning("Folia async task failed: " + var5.getMessage());
            return new Scheduler.Handle(null);
         }
      } else {
         return new Scheduler.Handle(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
      }
   }

   public static Scheduler.Handle runAsyncLater(JavaPlugin plugin, Runnable task, long delayTicks) {
      if (isFolia()) {
         try {
            Object scheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Consumer<Object> consumer = t -> task.run();
            long delayMs = Math.max(1L, delayTicks * 50L);
            Method method = scheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
            return new Scheduler.Handle(method.invoke(scheduler, plugin, consumer, delayMs, TimeUnit.MILLISECONDS));
         } catch (Exception var9) {
            plugin.getLogger().warning("Folia async delayed failed: " + var9.getMessage());
            return new Scheduler.Handle(null);
         }
      } else {
         return new Scheduler.Handle(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
      }
   }

   public static Scheduler.Handle runAsyncTimer(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
      if (isFolia()) {
         try {
            Object scheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Consumer<Object> consumer = t -> task.run();
            long delayMs = Math.max(1L, delayTicks * 50L);
            long periodMs = Math.max(1L, periodTicks * 50L);
            Method method = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
            return new Scheduler.Handle(method.invoke(scheduler, plugin, consumer, delayMs, periodMs, TimeUnit.MILLISECONDS));
         } catch (Exception var13) {
            plugin.getLogger().warning("Folia async timer failed: " + var13.getMessage());
            return new Scheduler.Handle(null);
         }
      } else {
         return new Scheduler.Handle(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
      }
   }

   public static Scheduler.Handle runAt(JavaPlugin plugin, Location location, Runnable task) {
      if (isFolia()) {
         try {
            Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            scheduler.getClass().getMethod("execute", Plugin.class, Location.class, Runnable.class).invoke(scheduler, plugin, location, task);
            return new Scheduler.Handle(null);
         } catch (Exception var4) {
            plugin.getLogger().warning("Folia region task failed: " + var4.getMessage());
            return new Scheduler.Handle(null);
         }
      } else {
         return new Scheduler.Handle(Bukkit.getScheduler().runTask(plugin, task));
      }
   }

   public static void runAtChunk(JavaPlugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
      if (isFolia()) {
         try {
            Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            scheduler.getClass()
               .getMethod("execute", Plugin.class, World.class, int.class, int.class, Runnable.class)
               .invoke(scheduler, plugin, world, chunkX, chunkZ, task);
         } catch (Exception var6) {
            plugin.getLogger().warning("Folia chunk task failed: " + var6.getMessage());
         }
      } else {
         if (Bukkit.isPrimaryThread()) {
            task.run();
         } else {
            Bukkit.getScheduler().runTask(plugin, task);
         }
      }
   }

   public static Scheduler.Handle runAtChunkTimer(JavaPlugin plugin, World world, int chunkX, int chunkZ, Runnable task, long delayTicks, long periodTicks) {
      if (isFolia()) {
         try {
            Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            Consumer<Object> consumer = t -> task.run();
            Method method = scheduler.getClass()
               .getMethod("runAtFixedRate", Plugin.class, World.class, int.class, int.class, Consumer.class, long.class, long.class);
            return new Scheduler.Handle(method.invoke(scheduler, plugin, world, chunkX, chunkZ, consumer, Math.max(1L, delayTicks), Math.max(1L, periodTicks)));
         } catch (Exception var12) {
            plugin.getLogger().warning("Folia region timer failed: " + var12.getMessage());
            return new Scheduler.Handle(null);
         }
      } else {
         return new Scheduler.Handle(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
      }
   }

   public static Scheduler.Handle runEntity(JavaPlugin plugin, Entity entity, Runnable task) {
      if (isFolia()) {
         try {
            Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            Consumer<Object> consumer = t -> task.run();
            Method method = scheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class);
            return new Scheduler.Handle(method.invoke(scheduler, plugin, consumer, (Runnable)null));
         } catch (Exception var6) {
            plugin.getLogger().warning("Folia entity task failed: " + var6.getMessage());
            return new Scheduler.Handle(null);
         }
      } else {
         return new Scheduler.Handle(Bukkit.getScheduler().runTask(plugin, task));
      }
   }

   public static void cancelTasks(JavaPlugin plugin) {
      if (isFolia()) {
         try {
            Object globalSched = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            globalSched.getClass().getMethod("cancelTasks", Plugin.class).invoke(globalSched, plugin);
            Object asyncSched = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            asyncSched.getClass().getMethod("cancelTasks", Plugin.class).invoke(asyncSched, plugin);
         } catch (Exception var3) {
            plugin.getLogger().warning("Folia cancel tasks failed: " + var3.getMessage());
         }
      } else {
         Bukkit.getScheduler().cancelTasks(plugin);
      }
   }

   public static class Handle {
      private final Object task;

      Handle(Object task) {
         this.task = task;
      }

      public void cancel() {
         if (this.task != null) {
            try {
               if (this.task instanceof BukkitTask bt) {
                  bt.cancel();
               } else {
                  this.task.getClass().getMethod("cancel").invoke(this.task);
               }
            } catch (Exception var3) {
            }
         }
      }

      public boolean isCancelled() {
         if (this.task == null) {
            return true;
         } else {
            try {
               return this.task instanceof BukkitTask bt ? bt.isCancelled() : (Boolean)this.task.getClass().getMethod("isCancelled").invoke(this.task);
            } catch (Exception var3) {
               return true;
            }
         }
      }
   }
}
