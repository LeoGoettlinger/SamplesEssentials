package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;

public class HangingBreakListener implements Listener {
   private final Graves plugin;

   public HangingBreakListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onHangingBreak(HangingBreakEvent event) {
      if (this.shouldCancelEvent(event)) {
         event.setCancelled(true);
      }
   }

   private boolean shouldCancelEvent(HangingBreakEvent event) {
      return event.getEntity() instanceof ItemFrame && this.isAssociatedWithGrave(event);
   }

   private boolean isAssociatedWithGrave(HangingBreakEvent event) {
      return this.plugin.getEntityDataManager().getGrave(event.getEntity()) != null;
   }
}
