package me.clip.placeholderapi.scheduler.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState;
import me.clip.placeholderapi.scheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.plugin.Plugin;

public class FoliaScheduledTask implements MyScheduledTask {
   private final ScheduledTask task;

   public FoliaScheduledTask(ScheduledTask task) {
      this.task = task;
   }

   @Override
   public void cancel() {
      this.task.cancel();
   }

   @Override
   public boolean isCancelled() {
      return this.task.isCancelled();
   }

   @Override
   public Plugin getOwningPlugin() {
      return this.task.getOwningPlugin();
   }

   @Override
   public boolean isCurrentlyRunning() {
      ExecutionState state = this.task.getExecutionState();
      return state == ExecutionState.RUNNING || state == ExecutionState.CANCELLED_RUNNING;
   }

   @Override
   public boolean isRepeatingTask() {
      return this.task.isRepeatingTask();
   }
}
