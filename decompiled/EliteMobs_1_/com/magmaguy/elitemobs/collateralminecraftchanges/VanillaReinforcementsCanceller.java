package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class VanillaReinforcementsCanceller implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onSpawn(CreatureSpawnEvent event) {
      if (event.getSpawnReason().equals(SpawnReason.REINFORCEMENTS)) {
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
         if (eliteEntity != null) {
            event.setCancelled(true);
         }
      }
   }
}
