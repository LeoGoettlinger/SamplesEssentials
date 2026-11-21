package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
   private final Graves plugin;

   public PlayerQuitListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();

      try {
         this.plugin.getCacheManager().clearAllGraveViewersFor(player.getUniqueId());
      } catch (Throwable var4) {
      }

      this.removeLastSolidLocation(player);
   }

   private void removeLastSolidLocation(Player player) {
      this.plugin.getLocationManager().removeLastSolidLocation(player);
   }
}
