package com.ranull.graves.listener.integration.furnitureengine;

import com.mira.furnitureengine.events.FurnitureInteractEvent;
import com.ranull.graves.Graves;
import com.ranull.graves.integration.FurnitureEngine;
import com.ranull.graves.type.Grave;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Deprecated
public class FurnitureInteractListener implements Listener {
   private final Graves plugin;
   private final FurnitureEngine furnitureEngine;

   @Deprecated
   public FurnitureInteractListener(Graves plugin, FurnitureEngine furnitureEngine) {
      this.plugin = plugin;
      this.furnitureEngine = furnitureEngine;
   }

   @Deprecated
   @EventHandler
   public void onFurnitureInteract(FurnitureInteractEvent event) {
      ItemFrame itemFrame = this.furnitureEngine.getItemFrame(event.getFurnitureLocation());
      if (itemFrame != null) {
         this.handleFurnitureInteraction(event, itemFrame);
      }
   }

   @Deprecated
   private void handleFurnitureInteraction(FurnitureInteractEvent event, ItemFrame itemFrame) {
      Grave grave = this.furnitureEngine.getGrave(itemFrame.getLocation(), itemFrame.getUniqueId());
      if (grave != null) {
         event.setCancelled(this.plugin.getGraveManager().openGrave(event.getPlayer(), itemFrame.getLocation(), grave));
      }
   }
}
