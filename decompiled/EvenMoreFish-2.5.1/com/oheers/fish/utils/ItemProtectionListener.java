package com.oheers.fish.utils;

import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.events.FishEatEvent;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.Material;
import org.bukkit.block.Crafter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class ItemProtectionListener implements Listener {
   @EventHandler
   public void onCraft(CraftItemEvent event) {
      if (MainConfig.getInstance().preventCrafting()) {
         for (ItemStack craftItem : event.getInventory().getMatrix()) {
            if (craftItem != null && (FishManager.getInstance().isFish(craftItem) || BaitManager.getInstance().isBait(craftItem))) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onCrafterCraft(CrafterCraftEvent event) {
      if (MainConfig.getInstance().preventCrafting()) {
         Crafter crafter = (Crafter)event.getBlock().getState();

         for (ItemStack craftItem : crafter.getInventory().getContents()) {
            if (craftItem != null && (FishManager.getInstance().isFish(craftItem) || BaitManager.getInstance().isBait(craftItem))) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onConsume(PlayerItemConsumeEvent event) {
      if (!FishEatEvent.getInstance().checkEatEvent(event) && MainConfig.getInstance().preventConsume()) {
         ItemStack item = event.getItem();
         if (FishManager.getInstance().isFish(item) || BaitManager.getInstance().isBait(item)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onFurnaceBurn(FurnaceBurnEvent event) {
      if (MainConfig.getInstance().preventFurnaceBurn()) {
         ItemStack item = event.getFuel();
         if (FishManager.getInstance().isFish(item) || BaitManager.getInstance().isBait(item)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onCook(BlockCookEvent event) {
      if (MainConfig.getInstance().preventCooking()) {
         ItemStack item = event.getSource();
         if (FishManager.getInstance().isFish(item) || BaitManager.getInstance().isBait(item)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPlace(BlockPlaceEvent event) {
      if (MainConfig.getInstance().preventPlacing()) {
         ItemStack item = event.getItemInHand();
         if (!item.getType().equals(Material.PLAYER_HEAD)) {
            if (FishManager.getInstance().isFish(item) || BaitManager.getInstance().isBait(item)) {
               event.setCancelled(true);
            }
         }
      }
   }
}
