package net.milkbowl.vault;

import net.milkbowl.vault.papi.EconomyPlaceholders;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginEnableListener implements Listener {
   @EventHandler
   public void onEvent(final PluginEnableEvent event) {
      if (event.getPlugin().getDescription().getName().equalsIgnoreCase("placeholderapi")) {
         new EconomyPlaceholders().register();
      }
   }
}
