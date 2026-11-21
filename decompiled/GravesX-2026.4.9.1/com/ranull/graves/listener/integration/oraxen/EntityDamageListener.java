package com.ranull.graves.listener.integration.oraxen;

import com.ranull.graves.integration.Oraxen;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

@Deprecated
public class EntityDamageListener implements Listener {
   private final Oraxen oraxen;

   @Deprecated
   public EntityDamageListener(Oraxen oraxen) {
      this.oraxen = oraxen;
   }

   @Deprecated
   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (this.isItemFrameAndHasGrave(event)) {
         event.setCancelled(true);
      }
   }

   @Deprecated
   private boolean isItemFrameAndHasGrave(EntityDamageEvent event) {
      return event.getEntity() instanceof ItemFrame && this.oraxen.getGrave(event.getEntity()) != null;
   }
}
