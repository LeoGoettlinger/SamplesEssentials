package com.skyblockexp.ezrtp.platform;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitPlatformScheduler implements PlatformScheduler {
   private final Plugin plugin;
   private final PlatformRuntimeCapabilities capabilities;

   public BukkitPlatformScheduler(Plugin plugin, PlatformRuntimeCapabilities capabilities) {
      this.plugin = plugin;
      this.capabilities = capabilities;
   }

   @Override
   public void executeAsync(Runnable task) {
      this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, task);
   }

   @Override
   public void executeRegion(World world, int chunkX, int chunkZ, Runnable task) {
      if (this.capabilities.regionizedRuntime()) {
         boolean handled = world != null ? this.invokeRegionTask(world, chunkX, chunkZ, task) : this.invokeGlobalRun(task);
         if (handled) {
            return;
         }
      }

      this.plugin.getServer().getScheduler().runTask(this.plugin, task);
   }

   @Override
   public PlatformTask scheduleRepeating(Runnable task, long delayTicks, long periodTicks) {
      if (this.capabilities.regionizedRuntime()) {
         PlatformTask foliaTask = this.scheduleGlobalRepeating(task, delayTicks, periodTicks);
         return foliaTask != null ? foliaTask : () -> {};
      } else {
         BukkitTask bukkitTask = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, task, delayTicks, periodTicks);
         return bukkitTask::cancel;
      }
   }

   @Override
   public void executeGlobal(Runnable task) {
      if (!this.capabilities.regionizedRuntime() || !this.invokeGlobalRun(task)) {
         this.plugin.getServer().getScheduler().runTask(this.plugin, task);
      }
   }

   @Override
   public PlatformTask executeGlobalDelayed(Runnable task, long delayTicks) {
      if (this.capabilities.regionizedRuntime()) {
         PlatformTask foliaTask = this.invokeGlobalRunDelayed(task, delayTicks);
         if (foliaTask != null) {
            return foliaTask;
         }
      }

      BukkitTask bukkit = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, task, delayTicks);
      return bukkit::cancel;
   }

   @Override
   public void executeRegionDelayed(World world, int chunkX, int chunkZ, Runnable task, long delayTicks) {
      if (this.capabilities.regionizedRuntime()) {
         boolean handled = world != null
            ? this.invokeRegionDelayed(world, chunkX, chunkZ, task, delayTicks)
            : this.invokeGlobalRunDelayed(task, delayTicks) != null;
         if (handled) {
            return;
         }
      }

      this.plugin.getServer().getScheduler().runTaskLater(this.plugin, task, delayTicks);
   }

   @Override
   public CompletableFuture<Boolean> teleportAsync(Player player, Location destination) {
      return this.capabilities.regionizedRuntime()
         ? this.invokeTeleportAsync(player, destination)
         : CompletableFuture.completedFuture(player.teleport(destination));
   }

   private CompletableFuture<Boolean> invokeTeleportAsync(Player player, Location destination) {
      try {
         Method method = player.getClass().getMethod("teleportAsync", Location.class);
         return (CompletableFuture<Boolean>)method.invoke(player, destination);
      } catch (RuntimeException | ReflectiveOperationException var4) {
         this.logReflectionFallback("teleportAsync", var4);
         return CompletableFuture.completedFuture(player.teleport(destination));
      }
   }

   private boolean invokeGlobalRun(Runnable task) {
      try {
         Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
         Method run = globalScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
         run.invoke(globalScheduler, this.plugin, (Consumer<Object>)ignored -> task.run());
         return true;
      } catch (RuntimeException | ReflectiveOperationException var4) {
         this.logReflectionFallback("getGlobalRegionScheduler#run", var4);
         return false;
      }
   }

   private PlatformTask invokeGlobalRunDelayed(Runnable task, long delayTicks) {
      try {
         Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
         Method runDelayed = globalScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
         Object scheduledTask = runDelayed.invoke(globalScheduler, this.plugin, (Consumer<Object>)ignored -> task.run(), delayTicks);
         Method cancel = scheduledTask.getClass().getMethod("cancel");
         return () -> {
            try {
               cancel.invoke(scheduledTask);
            } catch (RuntimeException | ReflectiveOperationException var3) {
            }
         };
      } catch (RuntimeException | ReflectiveOperationException var8) {
         this.logReflectionFallback("getGlobalRegionScheduler#runDelayed", var8);
         return null;
      }
   }

   private boolean invokeRegionTask(World world, int chunkX, int chunkZ, Runnable task) {
      try {
         Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
         Method run = regionScheduler.getClass().getMethod("run", Plugin.class, World.class, int.class, int.class, Consumer.class);
         run.invoke(regionScheduler, this.plugin, world, chunkX, chunkZ, (Consumer<Object>)ignored -> task.run());
         return true;
      } catch (RuntimeException | ReflectiveOperationException var7) {
         this.logReflectionFallback("getRegionScheduler#run", var7);
         return false;
      }
   }

   private boolean invokeRegionDelayed(World world, int chunkX, int chunkZ, Runnable task, long delayTicks) {
      try {
         Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
         Method runDelayed = regionScheduler.getClass().getMethod("runDelayed", Plugin.class, World.class, int.class, int.class, Consumer.class, long.class);
         runDelayed.invoke(regionScheduler, this.plugin, world, chunkX, chunkZ, (Consumer<Object>)ignored -> task.run(), delayTicks);
         return true;
      } catch (RuntimeException | ReflectiveOperationException var9) {
         this.logReflectionFallback("getRegionScheduler#runDelayed", var9);
         return false;
      }
   }

   private PlatformTask scheduleGlobalRepeating(Runnable task, long delayTicks, long periodTicks) {
      try {
         Object globalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
         Method runAtFixedRate = globalRegionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
         Object scheduledTask = runAtFixedRate.invoke(globalRegionScheduler, this.plugin, (Consumer<Object>)ignored -> task.run(), delayTicks, periodTicks);
         Method cancel = scheduledTask.getClass().getMethod("cancel");
         return () -> {
            try {
               cancel.invoke(scheduledTask);
            } catch (RuntimeException | ReflectiveOperationException var3) {
            }
         };
      } catch (RuntimeException | ReflectiveOperationException var10) {
         this.logReflectionFallback("getGlobalRegionScheduler#runAtFixedRate", var10);
         return null;
      }
   }

   private void logReflectionFallback(String operation, Throwable ex) {
      this.plugin.getLogger().log(Level.FINE, "EzRTP: region-scheduler reflection call " + operation + " failed, falling back to standard scheduler", ex);
   }
}
