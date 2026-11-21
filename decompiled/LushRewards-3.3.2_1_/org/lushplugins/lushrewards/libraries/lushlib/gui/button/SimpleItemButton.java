package org.lushplugins.lushrewards.libraries.lushlib.gui.button;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.lushrewards.libraries.lushlib.utils.DisplayItemStack;

public class SimpleItemButton extends ItemButton {
   private DisplayItemStack item;

   public SimpleItemButton(DisplayItemStack item, Consumer<InventoryClickEvent> onClick) {
      super(onClick);
      this.item = item;
   }

   public DisplayItemStack getItem() {
      return this.item;
   }

   public void setItem(DisplayItemStack item) {
      this.item = item;
   }

   @Override
   public ItemStack getItemStack(Player player) {
      return this.item.asItemStack();
   }
}
