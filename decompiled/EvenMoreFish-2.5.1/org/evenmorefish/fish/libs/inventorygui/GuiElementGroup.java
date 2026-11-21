package org.evenmorefish.fish.libs.inventorygui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public class GuiElementGroup extends GuiElement {
   private List<GuiElement> elements = new ArrayList<>();
   private GuiElement filler = null;
   private GuiElementGroup.Alignment alignment = GuiElementGroup.Alignment.LEFT;

   public GuiElementGroup(char slotChar, GuiElement... elements) {
      super(slotChar, null);
      this.setAction(
         click -> {
            GuiElement element = this.getElement(click.getSlot(), click.getGui().getPageNumber(click.getWhoClicked()));
            return element != null && element.getAction(click.getRawEvent().getWhoClicked()) != null
               ? element.getAction(click.getWhoClicked()).onClick(click)
               : true;
         }
      );
      Collections.addAll(this.elements, elements);
   }

   @Override
   public ItemStack getItem(HumanEntity who, int slot) {
      GuiElement element = this.getElement(slot, this.gui.getPageNumber(who));
      return element != null ? element.getItem(who, slot) : null;
   }

   @Override
   public void setGui(InventoryGui gui) {
      super.setGui(gui);

      for (GuiElement element : this.elements) {
         if (element != null) {
            element.setGui(gui);
         }
      }

      if (this.filler != null) {
         this.filler.setGui(gui);
      }
   }

   @Override
   public void setSlots(int[] slots) {
      super.setSlots(slots);

      for (GuiElement element : this.elements) {
         if (element != null) {
            element.setSlots(slots);
         }
      }
   }

   public void addElement(GuiElement element) {
      this.elements.add(element);
      if (element != null) {
         element.setGui(this.gui);
         element.setSlots(this.slots);
      }
   }

   public void addElements(GuiElement... elements) {
      for (GuiElement element : elements) {
         this.addElement(element);
      }
   }

   public void addElements(Collection<GuiElement> elements) {
      for (GuiElement element : elements) {
         this.addElement(element);
      }
   }

   public GuiElement getElement(int slot) {
      return this.getElement(slot, 0);
   }

   public GuiElement getElement(int slot, int pageNumber) {
      if (this.elements.isEmpty()) {
         return null;
      } else {
         int index = this.getSlotIndex(slot, this.slots.length < this.elements.size() ? pageNumber : 0);
         if (index > -1) {
            if (this.alignment != GuiElementGroup.Alignment.LEFT) {
               int lineWidth = this.getLineWidth(slot);
               int linePosition = this.getLinePosition(slot);
               if (this.elements.size() - index > lineWidth - linePosition) {
                  return this.elements.get(index);
               }

               int rest = this.elements.size() - (index - linePosition);
               int blankBefore = this.alignment == GuiElementGroup.Alignment.CENTER ? (lineWidth - rest) / 2 : lineWidth - rest;
               if (linePosition >= blankBefore && index - blankBefore < this.elements.size()) {
                  return this.elements.get(index - blankBefore);
               }

               return this.filler;
            }

            if (index < this.elements.size()) {
               return this.elements.get(index);
            }
         }

         return this.filler;
      }
   }

   private int getLineWidth(int slot) {
      int width = this.gui.getWidth();
      int row = slot / width;
      int amount = 0;

      for (int s : this.slots) {
         if (s >= row * width && s < (row + 1) * width) {
            amount++;
         }
      }

      return amount;
   }

   private int getLinePosition(int slot) {
      int width = this.gui.getWidth();
      int row = slot / width;
      int position = -1;

      for (int s : this.slots) {
         if (s >= row * width && s < (row + 1) * width) {
            position++;
            if (s == slot) {
               return position;
            }
         }
      }

      return position;
   }

   public List<GuiElement> getElements() {
      return Collections.unmodifiableList(this.elements);
   }

   public void clearElements() {
      this.elements.clear();
   }

   public void setFiller(ItemStack item) {
      this.filler = new StaticGuiElement(' ', item, " ");
      this.filler.setGui(this.gui);
   }

   public void setFiller(GuiElement filler) {
      this.filler = filler;
      if (filler != null) {
         filler.setGui(this.gui);
      }
   }

   public GuiElement getFiller() {
      return this.filler;
   }

   public int size() {
      return this.elements.size();
   }

   public void setAlignment(GuiElementGroup.Alignment alignment) {
      this.alignment = alignment;
   }

   public GuiElementGroup.Alignment getAlignment() {
      return this.alignment;
   }

   public static enum Alignment {
      LEFT,
      CENTER,
      RIGHT;
   }
}
