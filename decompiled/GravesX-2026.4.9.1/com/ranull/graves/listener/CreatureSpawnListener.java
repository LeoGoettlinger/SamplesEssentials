package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {
   private final Graves plugin;

   public CreatureSpawnListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onCreatureSpawn(CreatureSpawnEvent event) {
      if (this.shouldStoreSpawnReason(event)) {
         this.storeSpawnReason(event);
      }
   }

   private boolean shouldStoreSpawnReason(CreatureSpawnEvent event) {
      return !event.getEntity().isDead();
   }

   private void storeSpawnReason(CreatureSpawnEvent event) {
      this.plugin.getEntityManager().setDataString(event.getEntity(), "spawnReason", event.getSpawnReason().name());
   }
}
