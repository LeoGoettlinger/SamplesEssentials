package de.oliver.fancylib.serverSoftware.schedulers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class BukkitScheduler implements FancyScheduler {
   BukkitTask bukkitTask;
   JavaPlugin plugin;

   public BukkitScheduler(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   @NotNull
   @Override
   public FancyScheduler runTask(Location location, Runnable task) {
      this.bukkitTask = Bukkit.getScheduler().runTask(this.plugin, task);
      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskAsynchronously(Runnable task) {
      this.bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskLater(Location location, long delay, Runnable task) {
      this.bukkitTask = Bukkit.getScheduler().runTaskLater(this.plugin, task, delay);
      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskLaterAsynchronously(long delay, Runnable task) {
      this.bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, task, delay);
      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskTimer(Location location, long delay, long period, Runnable task) {
      this.bukkitTask = Bukkit.getScheduler().runTaskTimer(this.plugin, task, delay, period);
      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskTimerAsynchronously(long delay, long period, Runnable task) {
      this.bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, task, delay, period);
      return this;
   }

   @Override
   public void cancel() {
      if (!this.bukkitTask.isCancelled()) {
         this.bukkitTask.cancel();
      }
   }
}
