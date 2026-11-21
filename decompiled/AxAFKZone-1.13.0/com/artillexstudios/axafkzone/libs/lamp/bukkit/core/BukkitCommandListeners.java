package com.artillexstudios.axafkzone.libs.lamp.bukkit.core;

import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

final class BukkitCommandListeners implements Listener {
   private final BukkitCommandHandler handler;

   public BukkitCommandListeners(BukkitCommandHandler handler) {
      this.handler = handler;
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onPluginDisable(PluginDisableEvent event) {
      if (event.getPlugin().equals(this.handler.getPlugin())) {
         this.handler.unregisterAllCommands();
      }
   }
}
