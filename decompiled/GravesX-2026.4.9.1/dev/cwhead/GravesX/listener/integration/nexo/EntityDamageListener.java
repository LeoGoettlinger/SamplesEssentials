package dev.cwhead.GravesX.listener.integration.nexo;

import dev.cwhead.GravesX.integration.Nexo;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {
   private final Nexo nexo;

   public EntityDamageListener(Nexo nexo) {
      this.nexo = nexo;
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (this.isItemFrameAndHasGrave(event)) {
         event.setCancelled(true);
      }
   }

   private boolean isItemFrameAndHasGrave(EntityDamageEvent event) {
      return event.getEntity() instanceof ItemFrame && this.nexo.getGrave(event.getEntity()) != null;
   }
}
