package cm.chunkManager.components;

import cm.chunkManager.utils.Scheduler;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;

public class TaskManager {
   private final JavaPlugin plugin;
   private final Map<String, Scheduler.Handle> tasks = new HashMap<>();

   public TaskManager(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public void startTask(String name, Runnable task, long delay, long period) {
      this.cancelTask(name);
      this.tasks.put(name, Scheduler.runGlobalTimer(this.plugin, task, delay, period));
   }

   public void startAsyncTask(String name, Runnable task, long delay, long period) {
      this.cancelTask(name);
      this.tasks.put(name, Scheduler.runAsyncTimer(this.plugin, task, delay, period));
   }

   public void runTask(String name, Runnable task) {
      this.tasks.put(name, Scheduler.runGlobal(this.plugin, task));
   }

   public void runTaskLater(String name, Runnable task, long delay) {
      this.cancelTask(name);
      Scheduler.Handle handle = Scheduler.runGlobalLater(this.plugin, () -> {
         task.run();
         this.tasks.remove(name);
      }, delay);
      this.tasks.put(name, handle);
   }

   public void runAsyncTask(String name, Runnable task) {
      this.tasks.put(name, Scheduler.runAsync(this.plugin, task));
   }

   public void cancelTask(String name) {
      Scheduler.Handle handle = this.tasks.get(name);
      if (handle != null && !handle.isCancelled()) {
         handle.cancel();
         this.tasks.remove(name);
      }
   }

   public void cancelAllTasks() {
      this.tasks.values().forEach(handle -> {
         if (handle != null && !handle.isCancelled()) {
            handle.cancel();
         }
      });
      this.tasks.clear();
      Scheduler.cancelTasks(this.plugin);
   }

   public boolean isTaskRunning(String name) {
      Scheduler.Handle handle = this.tasks.get(name);
      return handle != null && !handle.isCancelled();
   }
}
