package org.evenmorefish.fish.libs.daisylib.events.move;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class MoveEventListener implements Listener {
   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Location to = event.getTo();
      Location from = event.getFrom();
      if (!to.getBlock().equals(from.getBlock())) {
         new PlayerMoveBlockEvent(event.getPlayer(), to, from).callEvent();
      }

      if (!to.getChunk().equals(from.getChunk())) {
         new PlayerMoveChunkEvent(event.getPlayer(), to, from).callEvent();
      }
   }
}
