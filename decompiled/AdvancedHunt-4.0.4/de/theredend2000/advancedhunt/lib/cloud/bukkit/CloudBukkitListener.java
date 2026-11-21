package de.theredend2000.advancedhunt.lib.cloud.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

final class CloudBukkitListener<C> implements Listener {
   private final BukkitCommandManager<C> bukkitCommandManager;

   CloudBukkitListener(@NonNull final BukkitCommandManager<C> bukkitCommandManager) {
      this.bukkitCommandManager = bukkitCommandManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   void onPlayerLogin(@NonNull final AsyncPlayerPreLoginEvent event) {
      this.bukkitCommandManager.lockIfBrigadierCapable();
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   void onPluginDisable(@NonNull final PluginDisableEvent event) {
      if (event.getPlugin().equals(this.bukkitCommandManager.owningPlugin())) {
         this.bukkitCommandManager.rootCommands().forEach(this.bukkitCommandManager::deleteRootCommand);
      }
   }
}
