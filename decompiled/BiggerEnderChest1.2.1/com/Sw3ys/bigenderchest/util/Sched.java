package com.Sw3ys.bigenderchest.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class Sched {
   private Sched() {
   }

   public static void entity(Plugin plugin, Entity entity, Runnable task) {
      entity.getScheduler().run(plugin, t -> task.run(), null);
   }

   public static void entityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
      entity.getScheduler().runDelayed(plugin, t -> task.run(), null, Math.max(1L, delayTicks));
   }

   public static void region(Plugin plugin, Location location, Runnable task) {
      Bukkit.getRegionScheduler().execute(plugin, location, task);
   }

   public static void async(Plugin plugin, Runnable task) {
      Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
   }
}
