package com.lishid.openinv.internal.paper26_2.container.menu;

import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.InternalOwned;
import com.lishid.openinv.internal.paper26_2.container.bukkit.OpenDummyInventory;
import com.lishid.openinv.internal.paper26_2.container.slot.SlotViewOnly;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class OpenChestMenu<T extends Container & ISpecialInventory & InternalOwned<ServerPlayer>> extends AbstractContainerMenu {
   protected static final int BOTTOM_INVENTORY_SIZE = 36;
   protected final T container;
   protected final ServerPlayer viewer;
   protected final boolean viewOnly;
   protected final boolean ownContainer;
   protected final int topSize;
   @Nullable
   private CraftInventoryView<OpenChestMenu<T>, Inventory> bukkitEntity;

   protected OpenChestMenu(MenuType<ChestMenu> type, int containerCounter, T container, ServerPlayer viewer, boolean viewOnly) {
      super(type, containerCounter);
      this.container = container;
      this.viewer = viewer;
      this.viewOnly = viewOnly;
      this.ownContainer = ((ServerPlayer)container.getOwnerHandle()).equals(viewer);
      this.topSize = this.getTopSize(viewer);
      this.preSlotSetup();
      int upperRows = this.topSize / 9;

      for (int row = 0; row < upperRows; row++) {
         for (int col = 0; col < 9; col++) {
            int x = 8 + col * 18;
            int y = 18 + row * 18;
            int index = row * 9 + col;
            if (index >= container.getContainerSize()) {
               this.addSlot(new SlotViewOnly(container, index, x, y));
            } else {
               Slot slot = this.getUpperSlot(index, x, y);
               this.addSlot(slot);
            }
         }
      }

      int playerInvPad = (upperRows - 4) * 18;

      for (int row = 0; row < 3; row++) {
         for (int colx = 0; colx < 9; colx++) {
            int x = 8 + colx * 18;
            int y = playerInvPad + row * 18 + 103;
            this.addSlot(new Slot(viewer.getInventory(), row * 9 + colx + 9, x, y));
         }
      }

      for (int colx = 0; colx < 9; colx++) {
         int x = 8 + colx * 18;
         int y = playerInvPad + 161;
         this.addSlot(new Slot(viewer.getInventory(), colx, x, y));
      }
   }

   public static MenuType<ChestMenu> getChestMenuType(int inventorySize) {
      inventorySize = (int)Math.ceil(inventorySize / 9.0) * 9;

      return switch (inventorySize) {
         case 9 -> MenuType.GENERIC_9x1;
         case 18 -> MenuType.GENERIC_9x2;
         case 27 -> MenuType.GENERIC_9x3;
         case 36 -> MenuType.GENERIC_9x4;
         case 45 -> MenuType.GENERIC_9x5;
         case 54 -> MenuType.GENERIC_9x6;
         default -> throw new IllegalArgumentException("Inventory size unsupported: " + inventorySize);
      };
   }

   protected void preSlotSetup() {
   }

   protected Slot getUpperSlot(int index, int x, int y) {
      Slot slot = new Slot(this.container, index, x, y);
      return (Slot)(this.viewOnly ? SlotViewOnly.wrap(slot) : slot);
   }

   public boolean isViewOnly() {
      return this.viewOnly;
   }

   public final CraftInventoryView<OpenChestMenu<T>, Inventory> getBukkitView() {
      if (this.bukkitEntity == null) {
         this.bukkitEntity = this.createBukkitEntity();
      }

      return this.bukkitEntity;
   }

   protected CraftInventoryView<OpenChestMenu<T>, Inventory> createBukkitEntity() {
      Inventory top;
      if (this.viewOnly) {
         top = new OpenDummyInventory(this.container, this.container.getBukkitType());
      } else {
         top = this.container.getBukkitInventory();
      }

      return new CraftInventoryView<OpenChestMenu<T>, Inventory>(this.viewer.getBukkitEntity(), top, this) {
         @Nullable
         public Inventory getInventory(int rawSlot) {
            return OpenChestMenu.this.viewOnly ? null : super.getInventory(rawSlot);
         }

         public int convertSlot(int rawSlot) {
            return OpenChestMenu.this.viewOnly ? -999 : super.convertSlot(rawSlot);
         }

         public SlotType getSlotType(int slot) {
            return OpenChestMenu.this.viewOnly ? SlotType.OUTSIDE : super.getSlotType(slot);
         }
      };
   }

   private int getTopSize(ServerPlayer viewer) {
      MenuType<?> menuType = this.getType();
      if (menuType == MenuType.GENERIC_9x1) {
         return 9;
      } else if (menuType == MenuType.GENERIC_9x2) {
         return 18;
      } else if (menuType == MenuType.GENERIC_9x3) {
         return 27;
      } else if (menuType == MenuType.GENERIC_9x4) {
         return 36;
      } else if (menuType == MenuType.GENERIC_9x5) {
         return 45;
      } else {
         return menuType == MenuType.GENERIC_9x6 ? 54 : menuType.create(-1, viewer.getInventory()).slots.size() - 36;
      }
   }

   protected boolean moveItemStackTo(ItemStack itemStack, int rangeLow, int rangeHigh, boolean topDown) {
      boolean modified = false;
      boolean stackable = itemStack.isStackable();
      Slot firstEmpty = null;

      for (int index = topDown ? rangeHigh - 1 : rangeLow; !itemStack.isEmpty() && (topDown ? index >= rangeLow : index < rangeHigh); index += topDown ? -1 : 1) {
         Slot slot = (Slot)this.slots.get(index);
         if (!slot.isFake() && slot.mayPlace(itemStack)) {
            if (slot.hasItem()) {
               if (stackable) {
                  modified |= addToExistingStack(itemStack, slot);
               }
            } else {
               if (firstEmpty == null) {
                  firstEmpty = slot;
               }

               if (!stackable) {
                  break;
               }
            }
         }
      }

      if (!itemStack.isEmpty() && firstEmpty != null) {
         firstEmpty.setByPlayer(itemStack.split(Math.min(itemStack.getCount(), firstEmpty.getMaxStackSize(itemStack))));
         firstEmpty.setChanged();
         modified = true;
      }

      return modified;
   }

   private static boolean addToExistingStack(ItemStack itemStack, Slot slot) {
      ItemStack existing = slot.getItem();
      if (!ItemStack.isSameItemSameComponents(itemStack, existing)) {
         return false;
      } else {
         int max = slot.getMaxStackSize(existing);
         int existingCount = existing.getCount();
         if (existingCount >= max) {
            return false;
         } else {
            int total = existingCount + itemStack.getCount();
            if (total <= max) {
               itemStack.setCount(0);
               existing.setCount(total);
               slot.setChanged();
               return true;
            } else {
               itemStack.shrink(max - existingCount);
               existing.setCount(max);
               slot.setChanged();
               return true;
            }
         }
      }
   }

   public boolean stillValid(Player player) {
      return true;
   }
}
