package com.artillexstudios.axtrade.libs.axapi.scheduler.impl;

import com.artillexstudios.axtrade.libs.axapi.scheduler.ScheduledTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState;
import org.bukkit.plugin.Plugin;

public class FoliaScheduledTask implements ScheduledTask {
   private final io.papermc.paper.threadedregions.scheduler.ScheduledTask task;

   public FoliaScheduledTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
      this.task = task;
   }

   public static FoliaScheduledTask nullable(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
      return task == null ? null : new FoliaScheduledTask(task);
   }

   @Override
   public Plugin getOwningPlugin() {
      return this.task.getOwningPlugin();
   }

   @Override
   public void cancel() {
      this.task.cancel();
   }

   @Override
   public boolean isCancelled() {
      ExecutionState state = this.task.getExecutionState();
      return state == ExecutionState.CANCELLED || state == ExecutionState.CANCELLED_RUNNING;
   }
}
