package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.implementation.folia.task;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.task.WrappedTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FoliaWrappedTask implements WrappedTask {
   private final ScheduledTask task;

   public FoliaWrappedTask(@NotNull ScheduledTask task) {
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

   @NotNull
   @Override
   public Plugin getOwningPlugin() {
      return this.task.getOwningPlugin();
   }
}
