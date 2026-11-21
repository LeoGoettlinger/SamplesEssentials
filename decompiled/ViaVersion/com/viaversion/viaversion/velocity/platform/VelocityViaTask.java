package com.viaversion.viaversion.velocity.platform;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.viaversion.viaversion.api.platform.PlatformTask;

public record VelocityViaTask(ScheduledTask task) implements PlatformTask<ScheduledTask> {
   @Override
   public void cancel() {
      this.task.cancel();
   }
}
