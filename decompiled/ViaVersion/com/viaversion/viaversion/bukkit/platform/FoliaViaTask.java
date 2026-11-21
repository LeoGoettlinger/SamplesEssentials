package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.api.platform.PlatformTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public record FoliaViaTask(ScheduledTask task) implements PlatformTask<ScheduledTask> {
   @Override
   public void cancel() {
      this.task.cancel();
   }
}
