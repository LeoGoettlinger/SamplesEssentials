package dev.cwhead.GravesX.listener.integration.nexo;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.integration.Nexo;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener implements Listener {
   private final Graves plugin;
   private final Nexo nexo;

   public PlayerInteractEntityListener(Graves plugin, Nexo nexo) {
      this.plugin = plugin;
      this.nexo = nexo;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onFurnitureInteract(PlayerInteractEntityEvent event) {
      Entity entity = event.getRightClicked();
      if (this.isItemFrame(entity)) {
         this.handleFurnitureInteraction(event, entity);
      }
   }

   private boolean isItemFrame(Entity entity) {
      return entity instanceof ItemFrame;
   }

   private void handleFurnitureInteraction(PlayerInteractEntityEvent event, Entity entity) {
      Grave grave = this.nexo.getGrave(entity);
      if (grave != null) {
         event.setCancelled(true);
         this.plugin.getGraveManager().openGrave(event.getPlayer(), entity.getLocation(), grave);
      }
   }
}
