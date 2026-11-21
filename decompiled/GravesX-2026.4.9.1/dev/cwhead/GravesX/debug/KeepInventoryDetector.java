package dev.cwhead.GravesX.debug;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.compatibility.CompatibilityGameRule;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

public final class KeepInventoryDetector {
   private static Graves plugin;
   private static volatile boolean installed = false;
   private static final Set<String> HOOKED = Collections.newSetFromMap(new ConcurrentHashMap<>());

   private KeepInventoryDetector() {
   }

   public static synchronized void install(Graves pluginInstance) {
      if (!installed) {
         installed = true;
         plugin = pluginInstance;

         try {
            wrapAllExcept(plugin);
         } catch (Throwable var4) {
            Throwable t = var4;
            installed = false;
            plugin.getLogger().warning("[KeepInventoryDetector] Install failed: " + var4.getClass().getSimpleName());

            try {
               plugin.logStackTrace(t instanceof Exception ? (Exception)t : new Exception(t));
            } catch (Throwable var3) {
            }
         }
      }
   }

   public static void wrapPlugin(Plugin target) {
      if (plugin != null && target != null && target != plugin) {
         String key = target.getName();
         if (HOOKED.add(key)) {
            try {
               HandlerList list = PlayerDeathEvent.getHandlerList();
               RegisteredListener[] regs = (RegisteredListener[])list.getRegisteredListeners().clone();
               Map<Listener, List<RegisteredListener>> byListener = new LinkedHashMap<>();
               Field execField = RegisteredListener.class.getDeclaredField("executor");
               execField.setAccessible(true);

               for (RegisteredListener r : regs) {
                  if (r.getPlugin() == target) {
                     EventExecutor ex = (EventExecutor)execField.get(r);
                     if (!(ex instanceof KeepInventoryDetector.Wrapper)) {
                        byListener.computeIfAbsent(r.getListener(), k -> new ArrayList<>()).add(r);
                     }
                  }
               }

               if (byListener.isEmpty()) {
                  return;
               }

               PluginManager pm = Bukkit.getPluginManager();

               for (Entry<Listener, List<RegisteredListener>> entry : byListener.entrySet()) {
                  Listener listener = entry.getKey();
                  List<RegisteredListener> rs = entry.getValue();
                  list.unregister(listener);

                  for (RegisteredListener rx : rs) {
                     Plugin owner = rx.getPlugin();
                     EventPriority priority = rx.getPriority();
                     boolean ignoreCancelled = rx.isIgnoringCancelled();
                     EventExecutor original = (EventExecutor)execField.get(rx);
                     EventExecutor wrapper = new KeepInventoryDetector.Wrapper(original, owner, priority);
                     pm.registerEvent(PlayerDeathEvent.class, listener, priority, wrapper, owner, ignoreCancelled);
                  }
               }
            } catch (Throwable var18) {
               HOOKED.remove(key);
               plugin.getLogger()
                  .warning("[KeepInventoryDetector] wrapPlugin failed for " + key + ": " + var18.getClass().getSimpleName() + ": " + var18.getMessage());
            }
         }
      }
   }

   public static void unhookPlugin(Plugin target) {
      if (target != null) {
         HOOKED.remove(target.getName());
      }
   }

   private static void wrapAllExcept(Plugin self) throws Exception {
      HandlerList list = PlayerDeathEvent.getHandlerList();
      RegisteredListener[] snapshot = (RegisteredListener[])list.getRegisteredListeners().clone();
      Field execField = RegisteredListener.class.getDeclaredField("executor");
      execField.setAccessible(true);
      LinkedHashSet<Plugin> owners = new LinkedHashSet<>();

      for (RegisteredListener r : snapshot) {
         Plugin owner = r.getPlugin();
         if (owner != self) {
            EventExecutor ex = (EventExecutor)execField.get(r);
            if (!(ex instanceof KeepInventoryDetector.Wrapper)) {
               owners.add(owner);
            }
         }
      }

      for (Plugin owner : owners) {
         wrapPlugin(owner);
      }
   }

   public static void logWorldsWithGameruleKeepInventoryTrue(Graves plugin) {
      for (World w : Bukkit.getWorlds()) {
         if (isKeepInventoryGameruleTrue(w)) {
            plugin.debugMessage(
               "[KeepInventoryDetector] World '"
                  + w.getName()
                  + "' has Keep Inventory Gamerule enabled. Graves will not spawn in this world without using a bypass permission.",
               2
            );
         }
      }
   }

   private static boolean isKeepInventoryGameruleTrue(World w) {
      try {
         return CompatibilityGameRule.getBoolean(w, "keepInventory");
      } catch (Exception var4) {
         try {
            return CompatibilityGameRule.getBoolean(w, "keep_inventory");
         } catch (Exception var3) {
            return false;
         }
      }
   }

   private static boolean safeGetKeepInventory(PlayerDeathEvent e, boolean fallback) {
      try {
         return e.getKeepInventory();
      } catch (Throwable var3) {
         return fallback;
      }
   }

   private static boolean safeGetKeepLevel(PlayerDeathEvent e, boolean fallback) {
      try {
         return e.getKeepLevel();
      } catch (Throwable var3) {
         return fallback;
      }
   }

   private static final class Wrapper implements EventExecutor {
      private final EventExecutor delegate;
      private final Plugin owner;
      private final EventPriority priority;

      private Wrapper(EventExecutor delegate, Plugin owner, EventPriority priority) {
         this.delegate = delegate;
         this.owner = owner;
         this.priority = priority;
      }

      public void execute(@NotNull Listener l, @NotNull Event e) throws EventException {
         if (e instanceof PlayerDeathEvent pd) {
            boolean beforeKeepInventory = KeepInventoryDetector.safeGetKeepInventory(pd, false);
            boolean beforeKeepLevel = KeepInventoryDetector.safeGetKeepLevel(pd, false);
            this.delegate.execute(l, e);
            boolean afterKeepInventory = KeepInventoryDetector.safeGetKeepInventory(pd, beforeKeepInventory);
            boolean afterKeepLevel = KeepInventoryDetector.safeGetKeepLevel(pd, beforeKeepLevel);
            if (!beforeKeepInventory && afterKeepInventory) {
               KeepInventoryDetector.plugin
                  .debugMessage(
                     "[KeepInventoryDetector] Keep Inventory was allowed by plugin "
                        + this.owner.getName()
                        + " v."
                        + this.owner.getDescription().getVersion()
                        + " with priority "
                        + this.priority
                        + ". Graves will not spawn in this world without using our keepInventory bypass permission or editing "
                        + this.owner.getName()
                        + "'s configuration. If there is no way to disable keepInventory through configuration or permission, contact "
                        + this.owner.getName()
                        + "'s plugin author(s) to suggest a configuration option or permission.",
                     2
                  );
            }

            if (!beforeKeepLevel && afterKeepLevel) {
               KeepInventoryDetector.plugin
                  .debugMessage(
                     "[KeepInventoryDetector] Keep Experience Level was allowed by plugin "
                        + this.owner.getName()
                        + " v."
                        + this.owner.getDescription().getVersion()
                        + " with priority "
                        + this.priority
                        + ". Experience levels will not be dropped for Graves while this is enabled, unless using a bypass permission or editing "
                        + this.owner.getName()
                        + "'s configuration. If there is no way to disable keep experience through configuration or permission, contact "
                        + this.owner.getName()
                        + "'s plugin author(s) to suggest a configuration option or permission.",
                     2
                  );
            }
         } else {
            this.delegate.execute(l, e);
         }
      }
   }
}
