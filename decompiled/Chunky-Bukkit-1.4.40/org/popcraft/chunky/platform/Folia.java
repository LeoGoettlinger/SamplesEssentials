package org.popcraft.chunky.platform;

import io.papermc.paper.threadedregions.RegionizedServerInitEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class Folia {
   private static final boolean CONFIG_EXISTS = classExists("io.papermc.paper.threadedregions.RegionizedServer")
      || classExists("io.papermc.paper.threadedregions.RegionizedServerInitEvent");

   private Folia() {
   }

   public static boolean isFolia() {
      return CONFIG_EXISTS;
   }

   public static void schedule(Plugin plugin, Location location, Runnable runnable) {
      Bukkit.getServer().getRegionScheduler().execute(plugin, location, runnable);
   }

   public static void schedule(Plugin plugin, Entity entity, Runnable runnable, long delay) {
      entity.getScheduler().execute(plugin, runnable, () -> {}, delay);
   }

   public static void scheduleFixed(Plugin plugin, Location location, Runnable runnable, long delay, long period) {
      Bukkit.getServer().getRegionScheduler().runAtFixedRate(plugin, location, ignored -> runnable.run(), delay, period);
   }

   public static void scheduleFixedGlobal(Plugin plugin, Runnable runnable, long delay, long period) {
      Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, ignored -> runnable.run(), delay, period);
   }

   public static void cancelTasks(Plugin plugin) {
      Bukkit.getServer().getGlobalRegionScheduler().cancelTasks(plugin);
   }

   public static boolean isTickThread(@NotNull Location location) {
      return Bukkit.getServer().isOwnedByCurrentRegion(location);
   }

   public static void onServerInit(Plugin plugin, final Runnable runnable) {
      Bukkit.getPluginManager().registerEvents(new Listener() {
         @EventHandler
         public void onRegionisedServerInit(RegionizedServerInitEvent event) {
            runnable.run();
         }
      }, plugin);
   }

   private static boolean classExists(String clazz) {
      try {
         Class.forName(clazz);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }
}
