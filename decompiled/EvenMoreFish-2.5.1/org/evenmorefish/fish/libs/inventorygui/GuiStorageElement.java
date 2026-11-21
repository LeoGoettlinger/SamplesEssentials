package org.evenmorefish.fish.libs.inventorygui;

import java.util.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiStorageElement extends GuiElement {
   private final Inventory storage;
   private final int invSlot;
   private Runnable applyStorage;
   private Function<GuiStorageElement.ValidatorInfo, Boolean> placeValidator;
   private Function<GuiStorageElement.ValidatorInfo, Boolean> takeValidator;

   public GuiStorageElement(char slotChar, Inventory storage) {
      this(slotChar, storage, -1);
   }

   public GuiStorageElement(char slotChar, Inventory storage, int invSlot) {
      this(slotChar, storage, invSlot, null, null);
   }

   public GuiStorageElement(
      char slotChar, Inventory storage, int invSlot, Runnable applyStorage, Function<GuiStorageElement.ValidatorInfo, Boolean> placeValidator
   ) {
      this(slotChar, storage, invSlot, applyStorage, placeValidator, null);
   }

   public GuiStorageElement(
      char slotChar,
      Inventory storage,
      int invSlot,
      Runnable applyStorage,
      Function<GuiStorageElement.ValidatorInfo, Boolean> placeValidator,
      Function<GuiStorageElement.ValidatorInfo, Boolean> takeValidator
   ) {
      super(slotChar, null);
      this.invSlot = invSlot;
      this.applyStorage = applyStorage;
      this.placeValidator = placeValidator;
      this.takeValidator = takeValidator;
      this.setAction(
         click -> {
            if (this.getStorageSlot(click.getWhoClicked(), click.getSlot()) < 0) {
               return true;
            } else {
               ItemStack storageItem = this.getStorageItem(click.getWhoClicked(), click.getSlot());
               ItemStack slotItem = click.getRawEvent().getView().getTopInventory().getItem(click.getSlot());
               if (click.getType() != ClickType.RIGHT
                  || (click.getCursor() == null || !click.getCursor().getType().getKey().getKey().contains("bundle"))
                     && (storageItem == null || !storageItem.getType().getKey().getKey().contains("bundle"))
                     && (slotItem == null || !slotItem.getType().getKey().getKey().contains("bundle"))) {
                  if ((slotItem != null || storageItem == null || storageItem.getType() == Material.AIR)
                     && (storageItem != null || slotItem == null || slotItem.getType() == Material.AIR)
                     && (storageItem == null || storageItem.equals(slotItem))) {
                     if (!(click.getRawEvent() instanceof InventoryClickEvent)) {
                        return true;
                     } else {
                        InventoryClickEvent event = (InventoryClickEvent)click.getRawEvent();
                        ItemStack movedItem = null;
                        switch (event.getAction()) {
                           case NOTHING:
                           case CLONE_STACK:
                              return !this.validateItemTake(click.getSlot(), slotItem);
                           case MOVE_TO_OTHER_INVENTORY:
                              if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                                 if (!this.validateItemTake(click.getSlot(), slotItem)) {
                                    return true;
                                 }

                                 if (click.getRawEvent().getView().getBottomInventory().firstEmpty() == -1) {
                                    return true;
                                 }

                                 movedItem = null;
                              } else {
                                 if (event.getView().getTopInventory().firstEmpty() == -1) {
                                    return true;
                                 }

                                 movedItem = event.getCurrentItem();
                              }

                              this.gui.runTask(this.gui::draw);
                              break;
                           case HOTBAR_MOVE_AND_READD:
                           case HOTBAR_SWAP:
                              if (!this.validateItemTake(click.getSlot(), slotItem)) {
                                 return true;
                              }

                              int button = event.getHotbarButton();
                              if (button < 0) {
                                 return true;
                              }

                              ItemStack hotbarItem = event.getView().getBottomInventory().getItem(button);
                              if (hotbarItem != null) {
                                 movedItem = hotbarItem.clone();
                              }
                              break;
                           case PICKUP_ONE:
                           case DROP_ONE_SLOT:
                              if (!this.validateItemTake(click.getSlot(), slotItem)) {
                                 return true;
                              }

                              if (event.getCurrentItem() != null) {
                                 movedItem = event.getCurrentItem().clone();
                                 movedItem.setAmount(movedItem.getAmount() - 1);
                              }
                              break;
                           case DROP_ALL_SLOT:
                              if (!this.validateItemTake(click.getSlot(), slotItem)) {
                                 return true;
                              }

                              movedItem = null;
                              break;
                           case PICKUP_HALF:
                              if (!this.validateItemTake(click.getSlot(), slotItem)) {
                                 return true;
                              }

                              if (event.getCurrentItem() != null) {
                                 movedItem = event.getCurrentItem().clone();
                                 movedItem.setAmount(movedItem.getAmount() / 2);
                              }
                              break;
                           case PLACE_SOME:
                              if (event.getCurrentItem() == null) {
                                 if (event.getCursor() != null) {
                                    movedItem = event.getCursor().clone();
                                 }
                              } else {
                                 movedItem = event.getCurrentItem().clone();
                                 int newAmount = movedItem.getAmount() + (event.getCursor() != null ? event.getCursor().getAmount() : 0);
                                 if (newAmount < movedItem.getMaxStackSize()) {
                                    movedItem.setAmount(newAmount);
                                 } else {
                                    movedItem.setAmount(movedItem.getMaxStackSize());
                                 }
                              }
                              break;
                           case PLACE_ONE:
                              if (event.getCursor() != null) {
                                 if (event.getCurrentItem() == null) {
                                    movedItem = event.getCursor().clone();
                                    movedItem.setAmount(1);
                                 } else {
                                    movedItem = event.getCursor().clone();
                                    movedItem.setAmount(event.getCurrentItem().getAmount() + 1);
                                 }
                              }
                              break;
                           case PLACE_ALL:
                              if (event.getCursor() != null) {
                                 movedItem = event.getCursor().clone();
                                 if (event.getCurrentItem() != null && event.getCurrentItem().getAmount() > 0) {
                                    movedItem.setAmount(event.getCurrentItem().getAmount() + movedItem.getAmount());
                                 }
                              }
                              break;
                           case PICKUP_ALL:
                           case SWAP_WITH_CURSOR:
                              if (!this.validateItemTake(click.getSlot(), slotItem)) {
                                 return true;
                              }

                              if (event.getCursor() != null) {
                                 movedItem = event.getCursor().clone();
                              }
                              break;
                           case COLLECT_TO_CURSOR:
                              if (event.getCursor() != null && (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)) {
                                 if (!this.validateItemTake(click.getSlot(), slotItem)) {
                                    return true;
                                 }

                                 this.gui.simulateCollectToCursor(click);
                                 return false;
                              }

                              return true;
                           default:
                              click.getRawEvent()
                                 .getWhoClicked()
                                 .sendMessage(ChatColor.RED + "The action " + event.getAction() + " is not supported! Sorry about that :(");
                              return true;
                        }

                        return !this.setStorageItem(click.getWhoClicked(), click.getSlot(), movedItem);
                     }
                  } else {
                     this.gui.draw(click.getWhoClicked(), false);
                     return true;
                  }
               } else {
                  this.gui.draw(click.getWhoClicked(), false);
                  return true;
               }
            }
         }
      );
      this.storage = storage;
   }

   @Override
   public ItemStack getItem(HumanEntity who, int slot) {
      int index = this.getStorageSlot(who, slot);
      return index > -1 && index < this.storage.getSize() ? this.storage.getItem(index) : null;
   }

   public Inventory getStorage() {
      return this.storage;
   }

   private int getStorageSlot(HumanEntity player, int slot) {
      int index = this.invSlot != -1 ? this.invSlot : this.getSlotIndex(slot, this.gui.getPageNumber(player));
      return index >= 0 && index < this.storage.getSize() ? index : -1;
   }

   @Deprecated
   public ItemStack getStorageItem(int slot) {
      return this.getStorageItem(null, slot);
   }

   public ItemStack getStorageItem(HumanEntity player, int slot) {
      int index = this.getStorageSlot(player, slot);
      return index == -1 ? null : this.storage.getItem(index);
   }

   @Deprecated
   public boolean setStorageItem(int slot, ItemStack item) {
      return this.setStorageItem(null, slot, item);
   }

   public boolean setStorageItem(HumanEntity player, int slot, ItemStack item) {
      int index = this.getStorageSlot(player, slot);
      if (index == -1) {
         return false;
      } else if (!this.validateItemPlace(slot, item)) {
         return false;
      } else {
         this.storage.setItem(index, item);
         if (this.applyStorage != null) {
            this.applyStorage.run();
         }

         return true;
      }
   }

   public Runnable getApplyStorage() {
      return this.applyStorage;
   }

   public void setApplyStorage(Runnable applyStorage) {
      this.applyStorage = applyStorage;
   }

   @Deprecated
   public Function<GuiStorageElement.ValidatorInfo, Boolean> getItemValidator() {
      return this.getPlaceValidator();
   }

   public Function<GuiStorageElement.ValidatorInfo, Boolean> getPlaceValidator() {
      return this.placeValidator;
   }

   public Function<GuiStorageElement.ValidatorInfo, Boolean> getTakeValidator() {
      return this.takeValidator;
   }

   @Deprecated
   public void setItemValidator(Function<GuiStorageElement.ValidatorInfo, Boolean> placeValidator) {
      this.placeValidator = placeValidator;
   }

   public void setPlaceValidator(Function<GuiStorageElement.ValidatorInfo, Boolean> placeValidator) {
      this.placeValidator = placeValidator;
   }

   public void setTakeValidator(Function<GuiStorageElement.ValidatorInfo, Boolean> takeValidator) {
      this.takeValidator = takeValidator;
   }

   @Deprecated
   public boolean validateItem(int slot, ItemStack item) {
      return this.validateItemPlace(slot, item);
   }

   public boolean validateItemPlace(int slot, ItemStack item) {
      return this.placeValidator == null || this.placeValidator.apply(new GuiStorageElement.ValidatorInfo(this, slot, item));
   }

   public boolean validateItemTake(int slot, ItemStack item) {
      return this.takeValidator == null || this.takeValidator.apply(new GuiStorageElement.ValidatorInfo(this, slot, item));
   }

   public static class ValidatorInfo {
      private final GuiElement element;
      private final int slot;
      private final ItemStack item;

      public ValidatorInfo(GuiElement element, int slot, ItemStack item) {
         this.item = item;
         this.slot = slot;
         this.element = element;
      }

      public GuiElement getElement() {
         return this.element;
      }

      public int getSlot() {
         return this.slot;
      }

      public ItemStack getItem() {
         return this.item;
      }
   }
}
