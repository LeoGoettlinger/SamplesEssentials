package de.mistrx.buildpaste.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PositionSelectorItem {
   public static ItemStack PositionSelector() {
      ItemStack item = new ItemStack(Material.STICK);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName("Position Selector");
      meta.addEnchant(Enchantment.VANISHING_CURSE, 10, true);
      item.setItemMeta(meta);
      return item;
   }

   public static boolean IsPositionSelector(ItemStack item) {
      if (!item.getType().equals(Material.STICK)) {
         return false;
      } else {
         ItemMeta meta = item.getItemMeta();
         return meta.getDisplayName().equals("Position Selector");
      }
   }
}
