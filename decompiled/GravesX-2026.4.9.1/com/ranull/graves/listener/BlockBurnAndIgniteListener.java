package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockBurnAndIgniteListener implements Listener {
   private final Graves plugin;

   public BlockBurnAndIgniteListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockSpread(BlockSpreadEvent event) {
      if (this.plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getBlock())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBurn(BlockBurnEvent event) {
      boolean near = this.plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getIgnitingBlock())
         || this.plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getBlock());
      if (near) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockIgnite(BlockIgniteEvent event) {
      boolean near = this.plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getIgnitingBlock())
         || this.plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getBlock());
      if (near) {
         event.setCancelled(true);
      }
   }
}
