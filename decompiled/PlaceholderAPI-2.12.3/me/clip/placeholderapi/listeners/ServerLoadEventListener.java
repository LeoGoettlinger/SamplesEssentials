package me.clip.placeholderapi.listeners;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

public final class ServerLoadEventListener implements Listener {
   @NotNull
   private final PlaceholderAPIPlugin plugin;

   public ServerLoadEventListener(@NotNull PlaceholderAPIPlugin plugin) {
      this.plugin = plugin;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   @EventHandler
   public void onServerLoad(@NotNull ServerLoadEvent event) {
      HandlerList.unregisterAll(this);
      this.plugin.getLocalExpansionManager().load(Bukkit.getConsoleSender());
   }
}
