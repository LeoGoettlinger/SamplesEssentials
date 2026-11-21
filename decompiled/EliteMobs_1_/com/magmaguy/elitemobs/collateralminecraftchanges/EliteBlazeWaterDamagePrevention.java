package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class EliteBlazeWaterDamagePrevention implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onBlazeWaterDamage(EliteMobDamagedEvent event) {
      if (event.getEntity() != null) {
         if (event.getEntity().getType().equals(EntityType.BLAZE)) {
            if (event.getEntityDamageEvent().getCause().equals(DamageCause.DROWNING)) {
               event.setCancelled(true);
            }
         }
      }
   }
}
