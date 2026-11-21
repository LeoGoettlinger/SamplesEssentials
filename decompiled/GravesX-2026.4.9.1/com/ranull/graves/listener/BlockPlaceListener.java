package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {
   private final Graves plugin;

   public BlockPlaceListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Block block = event.getBlock();
      Grave grave = this.plugin.getCacheManager().getGrave(block);
      if (!this.isGraveBlock(grave) && !this.isTokenItem(event)) {
         if (this.plugin.getGraveManager().isNearGrave(block.getLocation())) {
            event.setCancelled(true);
         }
      } else {
         event.setCancelled(true);
      }
   }

   private boolean isGraveBlock(Grave grave) {
      return grave != null;
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   private boolean isTokenItem(BlockPlaceEvent event) {
      return this.plugin.getRecipeManager() != null && this.plugin.getRecipeManager().isToken(event.getItemInHand());
   }
}
