package com.artillexstudios.axtrade.libs.gui.guis;

import com.artillexstudios.axtrade.libs.gui.components.GuiContainer;
import com.artillexstudios.axtrade.libs.gui.components.InteractionModifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StorageGui extends BaseGui {
   public StorageGui(@NotNull GuiContainer guiContainer, @NotNull Set<InteractionModifier> interactionModifiers) {
      super(guiContainer, interactionModifiers);
   }

   @NotNull
   public Map<Integer, ItemStack> addItem(@NotNull ItemStack... items) {
      return Collections.unmodifiableMap(this.getInventory().addItem(items));
   }

   public Map<Integer, ItemStack> addItem(@NotNull List<ItemStack> items) {
      return this.addItem(items.toArray(new ItemStack[0]));
   }

   @Override
   public void open(@NotNull HumanEntity player) {
      if (!player.isSleeping()) {
         this.populateGui();
         player.openInventory(this.getInventory());
      }
   }
}
