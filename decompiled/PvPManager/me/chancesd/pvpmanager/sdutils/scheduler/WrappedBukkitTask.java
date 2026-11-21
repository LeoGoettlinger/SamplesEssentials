package me.chancesd.pvpmanager.sdutils.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class WrappedBukkitTask implements SDTask {
   private final BukkitTask task;
   private boolean cancelled;

   public WrappedBukkitTask(BukkitTask task) {
      this.task = task;
   }

   @Override
   public void cancel() {
      this.task.cancel();
      this.cancelled = true;
   }

   @Override
   public int getTaskID() {
      return this.task.getTaskId();
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public Plugin getPlugin() {
      return this.task.getOwner();
   }
}
