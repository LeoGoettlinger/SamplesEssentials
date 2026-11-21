package de.oliver.fancylib.gui.inventoryClick;

import de.oliver.fancylib.FancyLib;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

public interface InventoryItemClick {
   NamespacedKey ON_CLICK_KEY = new NamespacedKey(FancyLib.getInstance().getPlugin(), "onclick");
   InventoryItemClick EMPTY = new InventoryItemClick() {
      @Override
      public String getId() {
         return "";
      }

      @Override
      public void onClick(InventoryClickEvent event, Player player) {
      }
   };

   static boolean hasKeys(ItemStack item, List<NamespacedKey> keys) {
      PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();

      for (NamespacedKey key : keys) {
         if (!data.has(key)) {
            return false;
         }
      }

      return true;
   }

   String getId();

   void onClick(InventoryClickEvent var1, Player var2);

   default void register() {
      InventoryClickRegistry.registerInventoryItemClick(this);
   }
}
