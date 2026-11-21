package dev.cwhead.GravesX.listener.integration.itemsadder;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.integration.ItemsAdder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public final class ItemsAdderDisableListener implements Listener {
   private final Graves plugin;
   private final ItemsAdder integration;

   public ItemsAdderDisableListener(Graves plugin, ItemsAdder integration) {
      this.plugin = plugin;
      this.integration = integration;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDisable(PluginDisableEvent event) {
      if (event.getPlugin().getName().equalsIgnoreCase("ItemsAdder")) {
         this.integration.setReady(false);
         this.plugin.debugMessage("ItemsAdder disabled. Integration is now gated.", 1);
      }
   }
}
