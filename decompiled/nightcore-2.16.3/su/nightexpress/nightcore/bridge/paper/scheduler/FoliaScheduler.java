package su.nightexpress.nightcore.bridge.paper.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.scheduler.AdaptedScheduler;

public class FoliaScheduler implements AdaptedScheduler {
   private final JavaPlugin plugin;
   private final RegionScheduler regionScheduler;
   private final GlobalRegionScheduler globalRegionScheduler;
   private final AsyncScheduler asyncScheduler;

   public FoliaScheduler(@NonNull JavaPlugin plugin) {
      this.plugin = plugin;
      this.regionScheduler = plugin.getServer().getRegionScheduler();
      this.globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
      this.asyncScheduler = plugin.getServer().getAsyncScheduler();
   }

   @Override
   public void cancelTasks() {
      this.globalRegionScheduler.cancelTasks(this.plugin);
      this.asyncScheduler.cancelTasks(this.plugin);
   }

   @NonNull
   public FoliaScheduledTask runTask(@NonNull Runnable runnable) {
      return new FoliaScheduledTask(this.globalRegionScheduler.run(this.plugin, task -> runnable.run()));
   }

   @Nullable
   public FoliaScheduledTask runTask(@NonNull Entity entity, @NonNull Runnable runnable) {
      ScheduledTask scheduledTask = entity.getScheduler().run(this.plugin, task -> runnable.run(), null);
      return scheduledTask == null ? null : new FoliaScheduledTask(scheduledTask);
   }

   @NonNull
   public FoliaScheduledTask runTask(@NonNull Location location, @NonNull Runnable runnable) {
      return new FoliaScheduledTask(this.regionScheduler.run(this.plugin, location, task -> runnable.run()));
   }

   @NonNull
   public FoliaScheduledTask runTask(@NonNull Chunk chunk, @NonNull Runnable runnable) {
      return new FoliaScheduledTask(this.regionScheduler.run(this.plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), task -> runnable.run()));
   }

   @NonNull
   public FoliaScheduledTask runTaskAsync(@NonNull Runnable runnable) {
      return new FoliaScheduledTask(this.asyncScheduler.runNow(this.plugin, task -> runnable.run()));
   }

   @NonNull
   public FoliaScheduledTask runTaskLater(@NonNull Runnable runnable, long delay) {
      return delay <= 0L ? this.runTask(runnable) : new FoliaScheduledTask(this.globalRegionScheduler.runDelayed(this.plugin, task -> runnable.run(), delay));
   }

   @NonNull
   public FoliaScheduledTask runTaskLaterAsync(@NonNull Runnable runnable, long delay) {
      if (delay <= 0L) {
         return this.runTaskAsync(runnable);
      } else {
         long delayMs = ticksToMillis(delay);
         return new FoliaScheduledTask(this.asyncScheduler.runDelayed(this.plugin, task -> runnable.run(), delayMs, TimeUnit.MILLISECONDS));
      }
   }

   @NonNull
   public FoliaScheduledTask runTaskTimer(@NonNull Runnable runnable, long delay, long period) {
      return new FoliaScheduledTask(this.globalRegionScheduler.runAtFixedRate(this.plugin, task -> runnable.run(), fixDelay(delay), period));
   }

   @NonNull
   public FoliaScheduledTask runTaskTimerAsync(@NonNull Runnable runnable, long delay, long period) {
      long delayMs = fixDelay(ticksToMillis(delay));
      long periodMs = ticksToMillis(period);
      return new FoliaScheduledTask(this.asyncScheduler.runAtFixedRate(this.plugin, task -> runnable.run(), delayMs, periodMs, TimeUnit.MILLISECONDS));
   }

   private static long fixDelay(long delay) {
      return delay <= 0L ? 1L : delay;
   }

   private static long ticksToMillis(long ticks) {
      return ticks * 50L;
   }
}
