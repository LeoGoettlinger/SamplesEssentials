package org.evenmorefish.fish.libs.dimensionfishing.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.scheduler.BukkitTask;
import org.evenmorefish.fish.libs.dimensionfishing.DimensionFishing;
import org.evenmorefish.fish.libs.dimensionfishing.util.Keys;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class HookManager {
   private static final HookManager instance = new HookManager();
   private BukkitTask task;
   private final Map<UUID, TrackedHook> trackedHooks = new HashMap<>();

   private HookManager() {
   }

   @NonNull
   public static HookManager getInstance() {
      return instance;
   }

   public void load() {
      this.task = Bukkit.getScheduler().runTaskTimer(DimensionFishing.getInstance().getPlugin(), () -> {
         Iterator<TrackedHook> iterator = this.trackedHooks.values().iterator();

         while (iterator.hasNext()) {
            TrackedHook hook = iterator.next();
            hook.tick();
            if (!hook.isShouldCustomTick()) {
               iterator.remove();
            }
         }
      }, 1L, 1L);
   }

   public void shutdown() {
      if (this.task != null) {
         this.task.cancel();
         this.task = null;
      }
   }

   public void trackHook(@NotNull TrackedHook hook) {
      this.trackedHooks.put(hook.getFishHook().getUniqueId(), hook);
   }

   public void checkCaughtEntity(@NonNull PlayerFishEvent event) {
      Entity entity = event.getCaught();
      if (entity != null) {
         if (entity.getPersistentDataContainer().has(Keys.STAND_KEY)) {
            TrackedHook tracked = this.trackedHooks.remove(event.getHook().getUniqueId());
            if (tracked == null) {
               return;
            }

            tracked.reel();
            event.setCancelled(true);
         }
      }
   }
}
