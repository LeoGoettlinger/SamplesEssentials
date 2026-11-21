package de.theredend2000.advancedhunt.listeners;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.managers.PlaceModeManager;
import de.theredend2000.advancedhunt.managers.TreasureManager;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.util.BlockUtils;
import de.theredend2000.advancedhunt.util.ItemsAdderAdapter;
import de.theredend2000.advancedhunt.util.MaterialUtils;
import de.theredend2000.advancedhunt.util.RewardPresetUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceModeListener implements Listener {
   private final Main plugin;
   private final PlaceModeManager placeModeManager;
   private final TreasureManager treasureManager;

   public PlaceModeListener(Main plugin) {
      this.plugin = plugin;
      this.placeModeManager = plugin.getPlaceModeManager();
      this.treasureManager = plugin.getTreasureManager();
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      Player player = event.getPlayer();
      if (this.placeModeManager.isInPlaceMode(player)) {
         UUID collectionId = this.placeModeManager.getCollectionId(player);
         if (collectionId != null) {
            Block block = event.getBlock();
            ItemStack placedItem = event.getItemInHand();
            if (!ItemsAdderAdapter.isCustomFurniture(placedItem)) {
               if (!MaterialUtils.isAir(block.getType())) {
                  if (this.treasureManager.getTreasureCoreAt(block.getLocation()) == null) {
                     if (!ItemsAdderAdapter.isCustomBlockItem(placedItem)) {
                        this.handlePlacedBlock(player, collectionId, block.getLocation(), placedItem);
                     }
                  }
               }
            }
         }
      }
   }

   private void handlePlacedBlock(Player player, UUID collectionId, Location location, ItemStack placedItem) {
      if (player != null && collectionId != null && location != null) {
         if (this.placeModeManager.isInPlaceMode(player)) {
            Block block = location.getBlock();
            if (!MaterialUtils.isAir(block.getType())) {
               if (this.treasureManager.getTreasureCoreAt(location) == null) {
                  String nbtData;
                  try {
                     nbtData = (String)NBT.get(block.getState(), ReadableNBT::toString);
                  } catch (Exception var11) {
                     nbtData = null;
                  }

                  String material;
                  String blockState;
                  if (ItemsAdderAdapter.isItemsAdderBlock(block)) {
                     material = "ITEMS_ADDER";
                     blockState = ItemsAdderAdapter.getCustomBlockId(block);
                     if ((blockState == null || blockState.isEmpty()) && placedItem != null) {
                        blockState = ItemsAdderAdapter.getCustomBlockId(placedItem);
                     }
                  } else if (placedItem != null && ItemsAdderAdapter.isCustomBlockItem(placedItem)) {
                     material = "ITEMS_ADDER";
                     blockState = ItemsAdderAdapter.getCustomBlockId(placedItem);
                  } else {
                     material = block.getType().name();
                     blockState = BlockUtils.getBlockStateString(block);
                  }

                  if (!"ITEMS_ADDER".equalsIgnoreCase(material) || blockState != null && !blockState.isEmpty()) {
                     List<Reward> rewards = new ArrayList<>(RewardPresetUtils.getDefaultTreasureRewards(this.plugin, collectionId));
                     Treasure treasure = new Treasure(
                        this.treasureManager.generateUniqueTreasureId(), collectionId, location, rewards, nbtData, material, blockState
                     );
                     this.treasureManager.addTreasure(treasure);
                     player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.added"));
                     this.plugin.getSoundManager().playTreasurePlaced(player);
                  } else {
                     this.plugin.getLogger().warning("Failed to resolve ItemsAdder block ID for placed block at " + location);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.placeModeManager.removePlaceMode(event.getPlayer());
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (this.placeModeManager.isInPlaceMode(event.getPlayer())) {
         Treasure treasure = this.treasureManager.getTreasureAt(event.getBlock().getLocation());
         if (treasure != null) {
            this.treasureManager.deleteTreasure(treasure);
            event.getPlayer().sendMessage(this.plugin.getMessageManager().getMessage("treasure.removed"));
            this.plugin.getSoundManager().playTreasureBroken(event.getPlayer());
         } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(this.plugin.getMessageManager().getMessage("command.place_mode.no_break"));
            this.plugin.getSoundManager().playPlaceModeBreakDeny(event.getPlayer());
         }
      }
   }
}
