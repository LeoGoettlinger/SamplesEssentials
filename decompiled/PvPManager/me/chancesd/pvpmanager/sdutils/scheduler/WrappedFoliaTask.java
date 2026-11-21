package me.chancesd.pvpmanager.sdutils.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

public class WrappedFoliaTask implements SDTask {
   private final ScheduledTask task;

   public WrappedFoliaTask(ScheduledTask task) {
      this.task = task;
   }

   @Override
   public void cancel() {
      this.task.cancel();
   }

   @Override
   public int getTaskID() {
      return -1;
   }

   @Override
   public boolean isCancelled() {
      return this.task.isCancelled();
   }

   @Override
   public Plugin getPlugin() {
      return this.task.getOwningPlugin();
   }
}
