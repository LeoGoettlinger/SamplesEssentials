package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class PlayerBucketListener implements Listener {
   private final Graves plugin;

   public PlayerBucketListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
      Block block = this.getTargetBlock(event);
      if (this.isGraveBlock(block)) {
         this.preventBucketUsage(event, block);
      } else if (this.plugin.getGraveManager().isNearGrave(block.getLocation(), block)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerBucketFill(PlayerBucketFillEvent event) {
      Block block = event.getBlockClicked();
      if (this.isGraveBlock(block)) {
         this.preventBucketUsage(event, block);
      } else if (this.plugin.getGraveManager().isNearGrave(block.getLocation(), block)) {
         event.setCancelled(true);
      }
   }

   private Block getTargetBlock(PlayerBucketEmptyEvent event) {
      return event.getBlockClicked().getRelative(event.getBlockFace());
   }

   private boolean isGraveBlock(Block block) {
      return this.plugin.getBlockManager().getGraveFromBlock(block) != null;
   }

   private void preventBucketUsage(Event event, Block block) {
      block.getState().update();
      if (event instanceof PlayerBucketEmptyEvent e) {
         e.setCancelled(true);
      } else if (event instanceof PlayerBucketFillEvent e) {
         e.setCancelled(true);
      }
   }
}
