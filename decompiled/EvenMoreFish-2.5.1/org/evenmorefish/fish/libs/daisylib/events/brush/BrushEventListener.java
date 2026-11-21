package org.evenmorefish.fish.libs.daisylib.events.brush;

import org.bukkit.block.BrushableBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class BrushEventListener implements Listener {
   @EventHandler
   public void onBlockChange(EntityChangeBlockEvent event) {
      if (event.getEntity() instanceof Player player) {
         if (event.getBlock().getState() instanceof BrushableBlock brushable) {
            boolean cancelled = !new BlockBrushEvent(player, brushable).callEvent();
            event.setCancelled(cancelled);
         }
      }
   }
}
