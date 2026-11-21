package me.chancesd.pvpmanager.sdutils.scheduler;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BukkitProvider implements SchedulerProvider {
   @NotNull
   private final JavaPlugin plugin;

   public BukkitProvider(@NotNull JavaPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void runPlatformAsync(Runnable task) {
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
   }

   @Override
   public void runPlatformAsyncLater(Runnable task, long delay) {
      Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, task, delay);
   }

   @Override
   public void runPlatformAsyncTimer(Runnable task, long delay, long period) {
      Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, task, delay, period);
   }

   @Override
   public SDTask runTask(Runnable task) {
      return new WrappedBukkitTask(Bukkit.getScheduler().runTask(this.plugin, task));
   }

   @Override
   public void runTask(Runnable task, World world, int x, int z) {
      this.runTask(task);
   }

   @Override
   public SDTask runTask(Runnable task, Entity entity) {
      return this.runTask(task);
   }

   @Override
   public void runTaskLater(Runnable task, long delay) {
      Bukkit.getScheduler().runTaskLater(this.plugin, task, delay);
   }

   @Override
   public void runTaskLater(Runnable task, Entity entity, long delay) {
      Bukkit.getScheduler().runTaskLater(this.plugin, task, delay);
   }

   @Override
   public SDTask runTaskTimer(Runnable task, Entity entity, long delay, long period) {
      return new WrappedBukkitTask(Bukkit.getScheduler().runTaskTimer(this.plugin, task, delay, period));
   }

   @Override
   public void executeConsoleCommand(String command) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
   }

   @Override
   public void executePlayerCommand(Player player, String command) {
      player.performCommand(command);
   }

   @Override
   public boolean isPrimaryThread() {
      return Bukkit.isPrimaryThread();
   }

   @Override
   public boolean isServerStopping() {
      return false;
   }

   @Override
   public CompletableFuture<Boolean> teleport(Entity entity, Location loc) {
      CompletableFuture<Boolean> future = new CompletableFuture<>();
      if (this.isPrimaryThread()) {
         boolean success = entity.teleport(loc);
         future.complete(success);
      } else {
         this.runTask(() -> {
            boolean successx = entity.teleport(loc);
            future.complete(successx);
         });
      }

      return future;
   }

   @Override
   public void cancelAllTasks() {
      Bukkit.getScheduler().cancelTasks(this.plugin);
   }
}
