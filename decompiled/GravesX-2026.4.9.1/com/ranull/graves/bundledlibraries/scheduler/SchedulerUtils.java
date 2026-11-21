package com.ranull.graves.bundledlibraries.scheduler;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

final class SchedulerUtils {
   private static final String PREFIX = "io.papermc.paper.threadedregions.";
   private static final boolean FOLIA_EXISTS = exists("io.papermc.paper.threadedregions.RegionizedServer");
   private static final boolean CANVAS_EXISTS = exists("io.canvasmc.canvas.server.ThreadedServer");
   private static final boolean EXPANDED_SCHEDULING_EXISTS = exists("io.papermc.paper.threadedregions.scheduler.ScheduledTask");

   private static boolean exists(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   @NotNull
   static GlobalScheduler getScheduler(final Plugin plugin) {
      if (FOLIA_EXISTS || CANVAS_EXISTS) {
         return new FoliaScheduler(plugin);
      } else {
         return (GlobalScheduler)(EXPANDED_SCHEDULING_EXISTS ? new FoliaScheduler(plugin) {
            @Override
            public boolean isGlobalThread() {
               return Bukkit.getServer().isPrimaryThread();
            }

            @Override
            public String toString() {
               return "PaperScheduler{plugin=" + plugin + '}';
            }
         } : new BukkitScheduler(plugin));
      }
   }

   @Generated
   private SchedulerUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
