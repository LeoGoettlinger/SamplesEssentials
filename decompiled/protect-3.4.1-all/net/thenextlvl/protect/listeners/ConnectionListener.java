package net.thenextlvl.protect.listeners;

import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ConnectionListener implements Listener {
   private final ProtectPlugin plugin;

   public ConnectionListener(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getPlayer());
      Boolean collides = area.getFlag(this.plugin.flags.collisions);
      this.plugin.collisionController().setCollidable(event.getPlayer(), collides);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.plugin.collisionController().remove(event.getPlayer());
   }
}
