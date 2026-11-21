package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

public class TamePrevention implements Listener {
   @EventHandler(
      ignoreCancelled = true
   )
   public void onTame(EntityTameEvent event) {
      if (EntityTracker.isEliteMob(event.getEntity())) {
         event.setCancelled(true);
         if (event.getEntity() instanceof Wolf wolf) {
            wolf.setAngry(true);
         }
      }
   }
}
