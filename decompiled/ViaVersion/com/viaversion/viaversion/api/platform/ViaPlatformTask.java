package com.viaversion.viaversion.api.platform;

import com.viaversion.viaversion.api.scheduler.Task;

public record ViaPlatformTask(Task task) implements PlatformTask<Task> {
   @Override
   public void cancel() {
      this.task.cancel();
   }
}
