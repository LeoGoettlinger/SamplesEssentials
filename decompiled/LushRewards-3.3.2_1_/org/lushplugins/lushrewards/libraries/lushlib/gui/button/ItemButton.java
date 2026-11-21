package org.lushplugins.lushrewards.libraries.lushlib.gui.button;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ItemButton extends Button {
   public ItemButton(Consumer<InventoryClickEvent> onClick) {
      super(onClick);
   }

   public abstract ItemStack getItemStack(Player var1);
}
