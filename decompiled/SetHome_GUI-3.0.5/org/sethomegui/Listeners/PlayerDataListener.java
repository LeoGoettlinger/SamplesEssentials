package org.sethomegui.Listeners;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.sethomegui.SetHomeGUI;

public class PlayerDataListener implements Listener {
   private final SetHomeGUI plugin;

   public PlayerDataListener(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onJoin(PlayerJoinEvent event) {
      UUID uuid = event.getPlayer().getUniqueId();
      if (this.plugin.getStorageManager().isExternalBackend()) {
         Bukkit.getAsyncScheduler().runNow(this.plugin, task -> this.plugin.getStorageManager().preload(uuid));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      this.plugin.getStorageManager().invalidate(event.getPlayer().getUniqueId());
   }
}
