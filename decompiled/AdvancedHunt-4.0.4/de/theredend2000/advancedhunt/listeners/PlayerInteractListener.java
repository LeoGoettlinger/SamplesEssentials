package de.theredend2000.advancedhunt.listeners;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.managers.CollectionManager;
import de.theredend2000.advancedhunt.managers.TreasureInteractionHandler;
import de.theredend2000.advancedhunt.managers.TreasureManager;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.platform.PlatformAccess;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {
   private final Main plugin;
   private final TreasureManager treasureManager;
   private final TreasureInteractionHandler treasureInteractionHandler;
   private final CollectionManager collectionManager;

   public PlayerInteractListener(Main plugin) {
      this.plugin = plugin;
      this.treasureManager = plugin.getTreasureManager();
      this.treasureInteractionHandler = TreasureInteractionHandler.getInstance(plugin);
      this.collectionManager = plugin.getCollectionManager();
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         if (PlatformAccess.get().isMainHandInteract(event)) {
            Block block = event.getClickedBlock();
            if (block != null) {
               if (!this.isItemsAdderBlock(block)) {
                  TreasureCore treasureCore = this.treasureManager.getTreasureCoreAt(block.getLocation());
                  if (treasureCore != null) {
                     Player player = event.getPlayer();
                     if (player.isSneaking()) {
                        event.setCancelled(true);
                        this.treasureInteractionHandler.handleSneakRewardsEditor(player, treasureCore);
                     } else if (this.plugin.getTreasureVisibilityManager().shouldHideFoundForPlayer(treasureCore, player)) {
                        event.setCancelled(true);
                        this.plugin.getTreasureVisibilityManager().hideFoundTreasureForPlayer(player, treasureCore);
                        this.collectionManager.getCollectionById(treasureCore.getCollectionId()).ifPresent(col -> {
                           if (col.isSinglePlayerFind()) {
                              player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.already_claimed_global"));
                              this.plugin.getSoundManager().playTreasureClaimedByOther(player);
                           } else {
                              player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.already_found"));
                              this.plugin.getSoundManager().playTreasureAlreadyFound(player);
                           }
                        });
                     } else {
                        this.treasureInteractionHandler.handleTreasureCollect(player, treasureCore);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isItemsAdderBlock(Block block) {
      try {
         return Bukkit.getPluginManager().getPlugin("ItemsAdder") == null ? false : CustomBlock.byAlreadyPlaced(block) != null;
      } catch (Exception | NoClassDefFoundError var3) {
         return false;
      }
   }
}
