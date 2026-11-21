package dev.cwhead.GravesX.listener.integration.itemsadder;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class FurnitureBreakListener implements Listener {
   private final Graves plugin;

   public FurnitureBreakListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onFurnitureBreak(FurnitureBreakEvent event) {
      Player player = event.getPlayer();
      if (!this.plugin.getConfigManager().getConfigSection("grave.break", player).getBoolean("grave.break")) {
         Grave grave = this.plugin.getEntityDataManager().getGrave(event.getBukkitEntity());
         String furnitureId = event.getNamespacedID();
         if (grave != null
            && this.plugin.getConfigManager().getConfigSection("itemsadder.furniture.enabled", player).getBoolean("itemsadder.furniture.enabled")
            && furnitureId.equals(this.plugin.getConfigManager().getConfigSection("itemsadder.furniture.name", player).getString("itemsadder.furniture.name"))) {
            event.setCancelled(true);
         }
      }
   }
}
