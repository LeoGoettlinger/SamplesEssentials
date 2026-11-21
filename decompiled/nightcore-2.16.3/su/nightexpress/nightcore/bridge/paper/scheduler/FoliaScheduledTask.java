package su.nightexpress.nightcore.bridge.paper.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.scheduler.AdaptedTask;

public class FoliaScheduledTask implements AdaptedTask {
   private final ScheduledTask backend;

   public FoliaScheduledTask(@NonNull ScheduledTask backend) {
      this.backend = backend;
   }

   @Override
   public void cancel() {
      this.backend.cancel();
   }

   @Override
   public boolean isCancelled() {
      return this.backend.isCancelled();
   }

   @NonNull
   @Override
   public Plugin getOwningPlugin() {
      return this.backend.getOwningPlugin();
   }

   @Override
   public boolean isCurrentlyRunning() {
      ExecutionState state = this.backend.getExecutionState();
      return state == ExecutionState.RUNNING || state == ExecutionState.CANCELLED_RUNNING;
   }

   @Override
   public boolean isRepeatingTask() {
      return this.backend.isRepeatingTask();
   }
}
