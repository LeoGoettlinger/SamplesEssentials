package dev.cwhead.GravesX.listener.integration.nexo;

import dev.cwhead.GravesX.integration.Nexo;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;

public class HangingBreakListener implements Listener {
   private final Nexo nexo;

   public HangingBreakListener(Nexo nexo) {
      this.nexo = nexo;
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onHangingBreak(HangingBreakEvent event) {
      if (this.isItemFrameAndHasGrave(event)) {
         event.setCancelled(true);
      }
   }

   private boolean isItemFrameAndHasGrave(HangingBreakEvent event) {
      return event.getEntity() instanceof ItemFrame && this.nexo.getGrave(event.getEntity()) != null;
   }
}
