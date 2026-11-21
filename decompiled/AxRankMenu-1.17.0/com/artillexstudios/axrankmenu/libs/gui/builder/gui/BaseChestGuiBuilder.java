package com.artillexstudios.axrankmenu.libs.gui.builder.gui;

import com.artillexstudios.axrankmenu.libs.gui.components.GuiContainer;
import com.artillexstudios.axrankmenu.libs.gui.components.InventoryProvider;
import com.artillexstudios.axrankmenu.libs.gui.components.util.Legacy;
import com.artillexstudios.axrankmenu.libs.gui.guis.BaseGui;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class BaseChestGuiBuilder<G extends BaseGui, B extends BaseChestGuiBuilder<G, B>> extends BaseGuiBuilder<G, B> {
   private int rows = 1;
   private InventoryProvider.Chest inventoryProvider = (title, owner, rows) -> Bukkit.createInventory(owner, rows, Legacy.SERIALIZER.serialize(title));

   @NotNull
   @Contract("_ -> this")
   public B rows(int rows) {
      this.rows = rows;
      return (B)this;
   }

   public B inventory(@NotNull InventoryProvider.Chest inventoryProvider) {
      this.inventoryProvider = inventoryProvider;
      return (B)this;
   }

   protected int getRows() {
      return this.rows;
   }

   @NotNull
   protected InventoryProvider.Chest getInventoryProvider() {
      return this.inventoryProvider;
   }

   @NotNull
   protected GuiContainer.Chest createContainer() {
      return new GuiContainer.Chest(this.getTitle(), this.inventoryProvider, this.getRows());
   }
}
