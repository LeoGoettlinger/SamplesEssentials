package org.evenmorefish.fish.libs.inventorygui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class GuiElement {
   private final char slotChar;
   private GuiElement.Action action;
   protected int[] slots = new int[0];
   protected InventoryGui gui;

   public GuiElement(char slotChar, GuiElement.Action action) {
      this.slotChar = slotChar;
      this.setAction(action);
   }

   public GuiElement(char slotChar) {
      this(slotChar, null);
   }

   public char getSlotChar() {
      return this.slotChar;
   }

   public abstract ItemStack getItem(HumanEntity var1, int var2);

   public GuiElement.Action getAction(HumanEntity who) {
      return this.action;
   }

   public void setAction(GuiElement.Action action) {
      this.action = action;
   }

   public int[] getSlots() {
      return this.slots;
   }

   public void setSlots(int[] slots) {
      this.slots = slots;
   }

   public int getSlotIndex(int slot) {
      return this.getSlotIndex(slot, 0);
   }

   public int getSlotIndex(int slot, int pageNumber) {
      for (int i = 0; i < this.slots.length; i++) {
         if (this.slots[i] == slot) {
            return i + this.slots.length * pageNumber;
         }
      }

      return -1;
   }

   public void setGui(InventoryGui gui) {
      this.gui = gui;
   }

   public InventoryGui getGui() {
      return this.gui;
   }

   public interface Action {
      boolean onClick(GuiElement.Click var1);
   }

   public static class Click {
      private final InventoryGui gui;
      private final int slot;
      private final ClickType clickType;
      private ItemStack cursor;
      private final GuiElement element;
      private final InventoryInteractEvent event;

      public Click(InventoryGui gui, int slot, ClickType clickType, ItemStack cursor, GuiElement element, InventoryInteractEvent event) {
         this.gui = gui;
         this.slot = slot;
         this.clickType = clickType;
         this.cursor = cursor;
         this.element = element;
         this.event = event;
      }

      public int getSlot() {
         return this.slot;
      }

      public GuiElement getElement() {
         return this.element;
      }

      public ClickType getType() {
         return this.clickType;
      }

      public ItemStack getCursor() {
         return this.cursor;
      }

      public void setCursor(ItemStack cursor) {
         this.cursor = cursor;
      }

      public HumanEntity getWhoClicked() {
         return this.event.getWhoClicked();
      }

      public InventoryInteractEvent getRawEvent() {
         return this.event;
      }

      public InventoryGui getGui() {
         return this.gui;
      }
   }
}
