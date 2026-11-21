package com.ranull.graves.listener.integration.oraxen;

import com.ranull.graves.Graves;
import com.ranull.graves.integration.Oraxen;
import com.ranull.graves.type.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

@Deprecated
public class PlayerInteractEntityListener implements Listener {
   private final Graves plugin;
   private final Oraxen oraxen;

   @Deprecated
   public PlayerInteractEntityListener(Graves plugin, Oraxen oraxen) {
      this.plugin = plugin;
      this.oraxen = oraxen;
   }

   @Deprecated
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onFurnitureInteract(PlayerInteractEntityEvent event) {
      Entity entity = event.getRightClicked();
      if (this.isItemFrame(entity)) {
         this.handleFurnitureInteraction(event, entity);
      }
   }

   @Deprecated
   private boolean isItemFrame(Entity entity) {
      return entity instanceof ItemFrame;
   }

   @Deprecated
   private void handleFurnitureInteraction(PlayerInteractEntityEvent event, Entity entity) {
      Grave grave = this.oraxen.getGrave(entity);
      if (grave != null) {
         event.setCancelled(this.plugin.getGraveManager().openGrave(event.getPlayer(), entity.getLocation(), grave));
      }
   }
}
