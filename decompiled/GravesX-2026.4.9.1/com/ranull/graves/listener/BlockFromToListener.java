package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class BlockFromToListener implements Listener {
   private final Graves plugin;

   public BlockFromToListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onBlockFromTo(BlockFromToEvent event) {
      if (this.isGraveBlock(event) || event.getToBlock().getType().name().contains("SKULL")) {
         event.setCancelled(true);
      }
   }

   private boolean isGraveBlock(BlockFromToEvent event) {
      return this.plugin.getCacheManager().getGrave(event.getToBlock()) != null;
   }
}
