package com.viaversion.viaversion.bukkit.platform;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.platform.PlatformTask;
import org.bukkit.scheduler.BukkitTask;

public record BukkitViaTask(BukkitTask task) implements PlatformTask<BukkitTask> {
   @Override
   public void cancel() {
      Preconditions.checkArgument(this.task != null, "Task cannot be cancelled");
      this.task.cancel();
   }
}
