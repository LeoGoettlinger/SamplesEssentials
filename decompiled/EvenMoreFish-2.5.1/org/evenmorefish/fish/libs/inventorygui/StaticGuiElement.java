package org.evenmorefish.fish.libs.inventorygui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public class StaticGuiElement extends GuiElement {
   private ItemStack item;
   private int number;
   private String[] text;

   public StaticGuiElement(char slotChar, ItemStack item, int number, GuiElement.Action action, String... text) throws IllegalArgumentException {
      super(slotChar, action);
      this.item = item;
      this.text = text;
      this.setNumber(number);
   }

   public StaticGuiElement(char slotChar, ItemStack item, GuiElement.Action action, String... text) {
      this(slotChar, item, item != null ? item.getAmount() : 1, action, text);
   }

   public StaticGuiElement(char slotChar, ItemStack item, String... text) {
      this(slotChar, item, item != null ? item.getAmount() : 1, null, text);
   }

   public void setItem(ItemStack item) {
      this.item = item;
   }

   public ItemStack getRawItem() {
      return this.item;
   }

   @Override
   public ItemStack getItem(HumanEntity who, int slot) {
      if (this.item == null) {
         return null;
      } else {
         ItemStack clone = this.item.clone();
         this.gui.setItemText(who, clone, this.getText());
         if (this.number > 0 && this.number <= 64) {
            clone.setAmount(this.number);
         }

         return clone;
      }
   }

   public void setText(String... text) {
      this.text = text;
   }

   public String[] getText() {
      return this.text;
   }

   public boolean setNumber(int number) {
      if (number >= 1 && number <= 64) {
         this.number = number;
         return true;
      } else {
         this.number = 1;
         return false;
      }
   }

   public int getNumber() {
      return this.number;
   }
}
