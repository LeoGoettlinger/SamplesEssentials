package com.artillexstudios.axtrade.libs.gui.components;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public interface GuiContainer {
   @NotNull
   Component title();

   void title(@NotNull Component var1);

   @NotNull
   Inventory createInventory(@NotNull InventoryHolder var1);

   @NotNull
   GuiType guiType();

   int inventorySize();

   int rows();

   public static class Chest implements GuiContainer {
      private final InventoryProvider.Chest inventoryProvider;
      private int rows;
      private Component title;

      public Chest(@NotNull Component title, @NotNull InventoryProvider.Chest inventoryProvider, int rows) {
         this.inventoryProvider = inventoryProvider;
         this.title = title;
         this.rows = rows;
      }

      @NotNull
      @Override
      public Component title() {
         return this.title;
      }

      @Override
      public void title(@NotNull Component title) {
         this.title = title;
      }

      @Override
      public int inventorySize() {
         return this.rows * 9;
      }

      @NotNull
      @Override
      public GuiType guiType() {
         return GuiType.CHEST;
      }

      @Override
      public int rows() {
         return this.rows;
      }

      public void rows(int rows) {
         this.rows = rows;
      }

      @NotNull
      @Override
      public Inventory createInventory(@NotNull InventoryHolder inventoryHolder) {
         return this.inventoryProvider.getInventory(this.title, inventoryHolder, this.inventorySize());
      }
   }

   public static class Typed implements GuiContainer {
      private final InventoryProvider.Typed inventoryProvider;
      private final GuiType guiType;
      private Component title;

      public Typed(@NotNull Component title, @NotNull InventoryProvider.Typed inventoryProvider, @NotNull GuiType guiType) {
         this.inventoryProvider = inventoryProvider;
         this.title = title;
         this.guiType = guiType;
      }

      @NotNull
      @Override
      public Component title() {
         return this.title;
      }

      @Override
      public void title(@NotNull Component title) {
         this.title = title;
      }

      @Override
      public int inventorySize() {
         return this.guiType.getLimit();
      }

      @NotNull
      @Override
      public GuiType guiType() {
         return this.guiType;
      }

      @Override
      public int rows() {
         return 1;
      }

      @NotNull
      @Override
      public Inventory createInventory(@NotNull InventoryHolder inventoryHolder) {
         return this.inventoryProvider.getInventory(this.title, inventoryHolder, this.guiType.getInventoryType());
      }
   }
}
