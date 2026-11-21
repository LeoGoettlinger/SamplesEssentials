package me.byteful.plugin.leveltools.api.scheduler.impl.folia;

import java.util.concurrent.TimeUnit;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.scheduler.ScheduledTask;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class FoliaScheduler implements Scheduler {
   private final LevelToolsPlugin plugin;

   public FoliaScheduler(LevelToolsPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void asyncDelayed(Runnable runnable, long ticksDelay) {
      Bukkit.getAsyncScheduler().runDelayed(this.plugin, x -> runnable.run(), ticksDelay * 50L, TimeUnit.MILLISECONDS);
   }

   @Override
   public void syncTimer(Runnable runnable, long ticksDelay, long ticksPeriod) {
      Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, x -> runnable.run(), ticksDelay, ticksPeriod);
   }

   @Override
   public void locationDelayed(Runnable runnable, Location location, long ticksDelay) {
      Bukkit.getRegionScheduler().runDelayed(this.plugin, location, x -> runnable.run(), ticksDelay);
   }

   @Override
   public ScheduledTask asyncTimer(Runnable runnable, long ticksDelay, long ticksPeriod) {
      return new FoliaScheduledTask(
         Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin, x -> runnable.run(), ticksDelay * 50L, ticksPeriod + 50L, TimeUnit.MILLISECONDS)
      );
   }
}
