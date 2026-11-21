package me.clip.placeholderapi.scheduler;

import me.clip.placeholderapi.scheduler.bukkit.BukkitScheduler;
import me.clip.placeholderapi.scheduler.folia.FoliaScheduler;
import me.clip.placeholderapi.scheduler.paper.PaperScheduler;
import me.clip.placeholderapi.scheduler.scheduling.schedulers.TaskScheduler;
import me.clip.placeholderapi.scheduler.utils.JavaUtil;
import org.bukkit.plugin.Plugin;

public class UniversalScheduler {
   private static final boolean IS_FOLIA = JavaUtil.classExists("io.papermc.paper.threadedregions.RegionizedServer");
   private static final boolean IS_CANVAS = JavaUtil.classExists("io.canvasmc.canvas.server.ThreadedServer");
   private static final boolean IS_EXPANDED_SCHEDULING_AVAILABLE = JavaUtil.classExists("io.papermc.paper.threadedregions.scheduler.ScheduledTask");

   public static TaskScheduler getScheduler(Plugin plugin) {
      return (TaskScheduler)(!IS_FOLIA && !IS_CANVAS
         ? (IS_EXPANDED_SCHEDULING_AVAILABLE ? new PaperScheduler(plugin) : new BukkitScheduler(plugin))
         : new FoliaScheduler(plugin));
   }
}
