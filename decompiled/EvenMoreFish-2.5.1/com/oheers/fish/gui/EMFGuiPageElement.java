package com.oheers.fish.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.inventorygui.GuiPageElement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EMFGuiPageElement extends GuiPageElement {
   private final GuiPageElement.PageAction pageAction;
   private final ItemStack fallbackItem;

   public EMFGuiPageElement(
      char slotChar, @NonNull ItemStack item, @Nullable ItemStack fallbackItem, @NonNull GuiPageElement.PageAction pageAction, @Nullable String... text
   ) {
      super(slotChar, item, pageAction, text);
      this.pageAction = pageAction;
      this.fallbackItem = fallbackItem;
   }

   @Override
   public ItemStack getItem(HumanEntity who, int slot) {
      if ((this.pageAction == GuiPageElement.PageAction.FIRST || this.pageAction == GuiPageElement.PageAction.LAST) && this.gui.getPageAmount(who) < 3
         || this.pageAction == GuiPageElement.PageAction.NEXT && this.gui.getPageNumber(who) + 1 >= this.gui.getPageAmount(who)
         || this.pageAction == GuiPageElement.PageAction.PREVIOUS && this.gui.getPageNumber(who) == 0) {
         if (this.fallbackItem == null) {
            return this.gui.getFiller() != null ? this.gui.getFiller().getItem(who, slot) : null;
         } else {
            return this.fallbackItem.clone();
         }
      } else {
         if (this.pageAction == GuiPageElement.PageAction.PREVIOUS) {
            this.setNumber(this.gui.getPageNumber(who));
         } else if (this.pageAction == GuiPageElement.PageAction.NEXT) {
            this.setNumber(this.gui.getPageNumber(who) + 2);
         } else if (this.pageAction == GuiPageElement.PageAction.LAST) {
            this.setNumber(this.gui.getPageAmount(who));
         }

         return super.getItem(who, slot).clone();
      }
   }
}
