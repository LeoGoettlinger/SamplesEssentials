package com.ranull.graves.listener.integration.furnitureengine;

import com.mira.furnitureengine.events.FurnitureBreakEvent;
import com.ranull.graves.integration.FurnitureEngine;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Deprecated
public class FurnitureBreakListener implements Listener {
   private final FurnitureEngine furnitureEngine;

   @Deprecated
   public FurnitureBreakListener(FurnitureEngine furnitureEngine) {
      this.furnitureEngine = furnitureEngine;
   }

   @Deprecated
   @EventHandler
   public void onFurnitureBreak(FurnitureBreakEvent event) {
      ItemFrame itemFrame = this.furnitureEngine.getItemFrame(event.getFurnitureLocation());
      if (itemFrame != null) {
         if (this.isFurnitureAssociatedWithGrave(event, itemFrame)) {
            event.setCancelled(true);
         }
      }
   }

   @Deprecated
   private boolean isFurnitureAssociatedWithGrave(FurnitureBreakEvent event, ItemFrame itemFrame) {
      return this.furnitureEngine.getGrave(event.getFurnitureLocation(), itemFrame.getUniqueId()) != null;
   }
}
