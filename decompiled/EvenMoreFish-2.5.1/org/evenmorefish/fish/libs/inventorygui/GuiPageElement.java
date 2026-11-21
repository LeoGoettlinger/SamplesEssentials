package org.evenmorefish.fish.libs.inventorygui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public class GuiPageElement extends StaticGuiElement {
   private GuiPageElement.PageAction pageAction;
   private boolean silent = false;

   public GuiPageElement(char slotChar, ItemStack item, GuiPageElement.PageAction pageAction, String... text) {
      super(slotChar, item, text);
      this.setAction(click -> {
         switch (pageAction) {
            case NEXT:
               if (click.getGui().getPageNumber(click.getWhoClicked()) + 1 < click.getGui().getPageAmount(click.getWhoClicked())) {
                  if (!this.isSilent()) {
                     click.getGui().playClickSound();
                  }

                  click.getGui().setPageNumber(click.getWhoClicked(), click.getGui().getPageNumber(click.getWhoClicked()) + 1);
               }
               break;
            case PREVIOUS:
               if (click.getGui().getPageNumber(click.getWhoClicked()) > 0) {
                  if (!this.isSilent()) {
                     click.getGui().playClickSound();
                  }

                  click.getGui().setPageNumber(click.getWhoClicked(), click.getGui().getPageNumber(click.getWhoClicked()) - 1);
               }
               break;
            case FIRST:
               if (!this.isSilent()) {
                  click.getGui().playClickSound();
               }

               click.getGui().setPageNumber(click.getWhoClicked(), 0);
               break;
            case LAST:
               if (!this.isSilent()) {
                  click.getGui().playClickSound();
               }

               click.getGui().setPageNumber(click.getWhoClicked(), click.getGui().getPageAmount(click.getWhoClicked()) - 1);
         }

         return true;
      });
      this.pageAction = pageAction;
   }

   public boolean isSilent() {
      return this.silent;
   }

   public void setSilent(boolean silent) {
      this.silent = silent;
   }

   @Override
   public ItemStack getItem(HumanEntity who, int slot) {
      if ((this.pageAction == GuiPageElement.PageAction.FIRST || this.pageAction == GuiPageElement.PageAction.LAST) && this.gui.getPageAmount(who) < 3
         || this.pageAction == GuiPageElement.PageAction.NEXT && this.gui.getPageNumber(who) + 1 >= this.gui.getPageAmount(who)
         || this.pageAction == GuiPageElement.PageAction.PREVIOUS && this.gui.getPageNumber(who) == 0) {
         return this.gui.getFiller() != null ? this.gui.getFiller().getItem(who, slot) : null;
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

   public static enum PageAction {
      NEXT,
      PREVIOUS,
      FIRST,
      LAST;
   }
}
