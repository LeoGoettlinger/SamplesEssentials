package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class ItemEnchantmentPrevention implements Listener {
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onResultGeneration(PrepareAnvilEvent event) {
      if (ItemTagger.isEliteItem(event.getInventory().getItem(0)) || ItemTagger.isEliteItem(event.getInventory().getItem(1))) {
         event.setResult(ItemStackGenerator.generateItemStack(Material.AIR));
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onResultGeneration(PrepareItemEnchantEvent event) {
      if (ItemTagger.isEliteItem(event.getInventory().getItem(0)) || ItemTagger.isEliteItem(event.getInventory().getItem(1))) {
         event.setCancelled(true);
      }
   }
}
