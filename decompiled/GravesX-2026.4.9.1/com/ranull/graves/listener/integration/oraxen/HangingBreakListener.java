package com.ranull.graves.listener.integration.oraxen;

import com.ranull.graves.integration.Oraxen;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;

@Deprecated
public class HangingBreakListener implements Listener {
   private final Oraxen oraxen;

   @Deprecated
   public HangingBreakListener(Oraxen oraxen) {
      this.oraxen = oraxen;
   }

   @Deprecated
   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onHangingBreak(HangingBreakEvent event) {
      if (this.isItemFrameAndHasGrave(event)) {
         event.setCancelled(true);
      }
   }

   @Deprecated
   private boolean isItemFrameAndHasGrave(HangingBreakEvent event) {
      return event.getEntity() instanceof ItemFrame && this.oraxen.getGrave(event.getEntity()) != null;
   }
}
