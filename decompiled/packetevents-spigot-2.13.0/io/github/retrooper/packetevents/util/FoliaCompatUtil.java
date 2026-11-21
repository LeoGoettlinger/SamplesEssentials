package io.github.retrooper.packetevents.util;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

@Deprecated
public class FoliaCompatUtil {
   private static boolean folia;
   private static BukkitScheduler bukkitScheduler;
   private static AsyncScheduler asyncScheduler;
   private static GlobalRegionScheduler globalRegionScheduler;
   private static Class<? extends Event> regionizedServerInitEventClass;

   /** @deprecated */
   public static boolean isFolia() {
      return folia;
   }

   /** @deprecated */
   public static void runTaskAsync(Plugin plugin, Runnable run) {
      if (!folia) {
         bukkitScheduler.runTaskAsynchronously(plugin, run);
      } else {
         asyncScheduler.runNow(plugin, o -> run.run());
      }
   }

   /** @deprecated */
   public static void runTaskTimerAsync(Plugin plugin, Consumer<Object> run, long delay, long period) {
      if (!folia) {
         Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> run.accept(null), delay, period);
      } else {
         globalRegionScheduler.runAtFixedRate(plugin, o -> run.accept(null), delay, period);
      }
   }

   /** @deprecated */
   public static void runTask(Plugin plugin, Consumer<Object> run) {
      if (!folia) {
         Bukkit.getScheduler().runTask(plugin, () -> run.accept(null));
      } else {
         globalRegionScheduler.run(plugin, o -> run.accept(null));
      }
   }

   /** @deprecated */
   public static void runTaskOnInit(Plugin plugin, Runnable run) {
      if (!folia) {
         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, run);
      } else {
         Bukkit.getServer()
            .getPluginManager()
            .registerEvent(regionizedServerInitEventClass, new Listener() {}, EventPriority.HIGHEST, (listener, event) -> run.run(), plugin);
      }
   }

   /** @deprecated */
   public static void runTaskForEntity(Entity entity, Plugin plugin, Runnable run, Runnable retired, long delay) {
      if (!folia) {
         Bukkit.getScheduler().runTaskLater(plugin, run, delay);
      } else {
         entity.getScheduler().execute(plugin, run, retired, delay);
      }
   }

   static {
      try {
         Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
         folia = true;
         asyncScheduler = Bukkit.getAsyncScheduler();
         globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
         regionizedServerInitEventClass = (Class<? extends Event>)Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
      } catch (ClassNotFoundException var1) {
         folia = false;
         bukkitScheduler = Bukkit.getScheduler();
      }
   }
}
