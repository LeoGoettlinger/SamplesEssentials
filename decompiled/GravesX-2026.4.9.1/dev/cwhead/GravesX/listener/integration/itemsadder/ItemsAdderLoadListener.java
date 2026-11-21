package dev.cwhead.GravesX.listener.integration.itemsadder;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.integration.ItemsAdder;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ItemsAdderLoadListener implements Listener {
   private final Graves plugin;
   private final ItemsAdder integration;

   public ItemsAdderLoadListener(Graves plugin, ItemsAdder integration) {
      this.plugin = plugin;
      this.integration = integration;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onItemsAdderLoad(ItemsAdderLoadDataEvent event) {
      this.plugin.getSchedulerManager().runTask(() -> this.plugin.getSchedulerManager().runTaskLater(() -> {
         this.integration.setReady(true);
         this.plugin.debugMessage("ItemsAdder content loaded. ItemsAdder integration is now ready.", 1);
      }, 1L));
   }
}
