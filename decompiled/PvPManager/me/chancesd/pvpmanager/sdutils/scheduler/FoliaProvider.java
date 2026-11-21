package me.chancesd.pvpmanager.sdutils.scheduler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class FoliaProvider implements SchedulerProvider {
   @NotNull
   private final JavaPlugin plugin;

   public FoliaProvider(@NotNull JavaPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void runPlatformAsync(Runnable task) {
      Bukkit.getAsyncScheduler().runNow(this.plugin, scheduledTask -> task.run());
   }

   @Override
   public void runPlatformAsyncLater(Runnable task, long delay) {
      Bukkit.getAsyncScheduler().runDelayed(this.plugin, scheduledTask -> task.run(), delay * 50L, TimeUnit.MILLISECONDS);
   }

   @Override
   public void runPlatformAsyncTimer(Runnable task, long delay, long period) {
      Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin, scheduledTask -> task.run(), delay * 50L, period * 50L, TimeUnit.MILLISECONDS);
   }

   @Override
   public SDTask runTask(Runnable task) {
      Bukkit.getGlobalRegionScheduler().execute(this.plugin, task);
      return null;
   }

   @Override
   public void runTask(Runnable task, World world, int x, int z) {
      Bukkit.getRegionScheduler().execute(this.plugin, world, x, z, task);
   }

   @Override
   public SDTask runTask(Runnable task, Entity entity) {
      return new WrappedFoliaTask(entity.getScheduler().run(this.plugin, scheduledTask -> task.run(), task));
   }

   @Override
   public void runTaskLater(Runnable task, long delay) {
      Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin, scheduledTask -> task.run(), delay);
   }

   @Override
   public void runTaskLater(Runnable task, Entity entity, long delay) {
      entity.getScheduler().runDelayed(this.plugin, scheduledTask -> task.run(), task, delay);
   }

   @Override
   public SDTask runTaskTimer(Runnable task, Entity entity, long delay, long period) {
      return new WrappedFoliaTask(entity.getScheduler().runAtFixedRate(this.plugin, scheduledTask -> task.run(), null, delay, period));
   }

   @Override
   public void executeConsoleCommand(String command) {
      Bukkit.getGlobalRegionScheduler().execute(this.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
   }

   @Override
   public void executePlayerCommand(Player player, String command) {
      player.performCommand(command);
   }

   @Override
   public boolean isPrimaryThread() {
      return Bukkit.isGlobalTickThread();
   }

   @Override
   public boolean isServerStopping() {
      return Bukkit.isStopping();
   }

   @Override
   public CompletableFuture<Boolean> teleport(Entity entity, Location loc) {
      return entity.teleportAsync(loc);
   }

   @Override
   public void cancelAllTasks() {
      Bukkit.getAsyncScheduler().cancelTasks(this.plugin);
      Bukkit.getGlobalRegionScheduler().cancelTasks(this.plugin);
   }
}
