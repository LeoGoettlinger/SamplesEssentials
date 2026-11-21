package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.implementation.bukkit.task;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.task.WrappedTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class BukkitWrappedTask implements WrappedTask {
   private final BukkitTask task;

   public BukkitWrappedTask(@NotNull BukkitTask task) {
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
      return this.task.getOwner();
   }
}
