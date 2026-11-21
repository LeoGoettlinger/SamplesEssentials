package com.github.jikoo.openinv.internal.spigot26_2.container.menu;

import com.github.jikoo.openinv.internal.spigot26_2.container.OpenInventory;
import com.github.jikoo.openinv.internal.spigot26_2.container.bukkit.OpenDummyPlayerInventory;
import com.github.jikoo.openinv.internal.spigot26_2.container.bukkit.OpenPlayerInventorySelf;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentDrop;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentEquipment;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.SlotViewOnly;
import com.google.common.base.Preconditions;
import com.lishid.openinv.util.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenInventoryMenu extends OpenSyncMenu<OpenInventory> {
   private int offset;

   public OpenInventoryMenu(OpenInventory inventory, ServerPlayer viewer, int i, boolean viewOnly) {
      super(getMenuType(inventory, viewer), i, inventory, viewer, viewOnly);
   }

   private static MenuType<ChestMenu> getMenuType(OpenInventory inventory, ServerPlayer viewer) {
      int size = inventory.getContainerSize();
      if (inventory.getOwnerHandle().equals(viewer)) {
         size -= viewer.getInventory().getNonEquipmentItems().size();
         size = (int)Math.ceil(size / 9.0) * 9;
      }

      return getChestMenuType(size);
   }

   @Override
   protected void preSlotSetup() {
      this.offset = this.ownContainer ? this.viewer.getInventory().getNonEquipmentItems().size() : 0;
   }

   @NotNull
   @Override
   protected Slot getUpperSlot(int index, int x, int y) {
      index += this.offset;
      Slot slot = this.container.getMenuSlot(index, x, y);
      if (slot.getClass().equals(SlotViewOnly.class)) {
         return slot;
      } else if (!(slot instanceof ContentDrop.SlotDrop) || !this.viewOnly && Permissions.INVENTORY_SLOT_DROP.hasPermission(this.viewer.getBukkitEntity())) {
         if (slot instanceof ContentEquipment.SlotEquipment equipment) {
            if (this.viewOnly) {
               return SlotViewOnly.wrap(slot);
            } else {
               Permissions perm = switch (equipment.getEquipmentSlot()) {
                  case HEAD -> Permissions.INVENTORY_SLOT_HEAD_ANY;
                  case CHEST -> Permissions.INVENTORY_SLOT_CHEST_ANY;
                  case LEGS -> Permissions.INVENTORY_SLOT_LEGS_ANY;
                  case FEET -> Permissions.INVENTORY_SLOT_FEET_ANY;
                  default -> null;
               };
               if (perm != null && !perm.hasPermission(this.viewer.getBukkitEntity())) {
                  equipment.onlyEquipmentFor(this.container.getOwnerHandle());
               }

               return slot;
            }
         } else if (this.ownContainer && !(slot instanceof ContentDrop.SlotDrop)) {
            return new SlotViewOnly(this.container, index, x, y);
         } else {
            return (Slot)(this.viewOnly ? SlotViewOnly.wrap(slot) : slot);
         }
      } else {
         return new SlotViewOnly(this.container, index, x, y);
      }
   }

   @NotNull
   @Override
   protected CraftInventoryView<OpenChestMenu<OpenInventory>, Inventory> createBukkitEntity() {
      Inventory bukkitInventory;
      if (this.viewOnly) {
         bukkitInventory = new OpenDummyPlayerInventory(this.container);
      } else if (this.ownContainer) {
         bukkitInventory = new OpenPlayerInventorySelf(this.container, this.offset);
      } else {
         bukkitInventory = this.container.getBukkitInventory();
      }

      return new CraftInventoryView<OpenChestMenu<OpenInventory>, Inventory>(this.viewer.getBukkitEntity(), bukkitInventory, this) {
         public ItemStack getItem(int index) {
            if (!OpenInventoryMenu.this.viewOnly && index >= 0) {
               Slot slot = (Slot)OpenInventoryMenu.this.slots.get(index);
               return CraftItemStack.asCraftMirror(slot.hasItem() ? slot.getItem() : net.minecraft.world.item.ItemStack.EMPTY);
            } else {
               return null;
            }
         }

         public boolean isInTop(int rawSlot) {
            return rawSlot < OpenInventoryMenu.this.topSize;
         }

         @Nullable
         public Inventory getInventory(int rawSlot) {
            if (OpenInventoryMenu.this.viewOnly) {
               return null;
            } else if (rawSlot != -999 && rawSlot != -1) {
               Preconditions.checkArgument(
                  rawSlot >= 0 && rawSlot < OpenInventoryMenu.this.topSize + OpenInventoryMenu.this.offset + 36, "Slot %s outside of inventory", rawSlot
               );
               if (rawSlot > OpenInventoryMenu.this.topSize) {
                  return this.getBottomInventory();
               } else {
                  Slot slot = (Slot)OpenInventoryMenu.this.slots.get(rawSlot);
                  return slot.isFake() ? null : this.getTopInventory();
               }
            } else {
               return null;
            }
         }

         public int convertSlot(int rawSlot) {
            if (OpenInventoryMenu.this.viewOnly) {
               return -999;
            } else if (rawSlot < 0) {
               return rawSlot;
            } else if (rawSlot < OpenInventoryMenu.this.topSize) {
               Slot slot = (Slot)OpenInventoryMenu.this.slots.get(rawSlot);
               return slot.isFake() ? -999 : rawSlot;
            } else {
               int slot = rawSlot - OpenInventoryMenu.this.topSize;
               if (slot >= 27) {
                  slot -= 27;
               } else {
                  slot += 9;
               }

               return slot;
            }
         }

         @NotNull
         public SlotType getSlotType(int slot) {
            if (OpenInventoryMenu.this.viewOnly || slot < 0) {
               return SlotType.OUTSIDE;
            } else if (slot >= OpenInventoryMenu.this.topSize) {
               slot -= OpenInventoryMenu.this.topSize;
               return slot >= 27 ? SlotType.QUICKBAR : SlotType.CONTAINER;
            } else {
               return OpenInventoryMenu.this.container.getSlotType(OpenInventoryMenu.this.offset + slot);
            }
         }

         public int countSlots() {
            return OpenInventoryMenu.this.topSize + 36;
         }
      };
   }

   @NotNull
   public net.minecraft.world.item.ItemStack quickMoveStack(@NotNull Player player, int index) {
      if (this.viewOnly) {
         return net.minecraft.world.item.ItemStack.EMPTY;
      } else {
         Slot slot = (Slot)this.slots.get(index);
         if (slot.hasItem() && !slot.isFake()) {
            net.minecraft.world.item.ItemStack itemStack = slot.getItem();
            net.minecraft.world.item.ItemStack originalStack = itemStack.copy();
            if (index < this.topSize) {
               if (!this.moveItemStackTo(itemStack, this.topSize, this.slots.size(), true)) {
                  return net.minecraft.world.item.ItemStack.EMPTY;
               }
            } else {
               EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(itemStack);

               boolean movedGear = switch (equipmentSlot) {
                  case HEAD, CHEST, LEGS, FEET, OFFHAND -> {
                     for (int extra = this.container.getOwnerHandle().getInventory().getNonEquipmentItems().size() - this.offset; extra < this.topSize; extra++) {
                        if (this.getSlot(extra) instanceof ContentEquipment.SlotEquipment equipSlot && equipSlot.getEquipmentSlot() == equipmentSlot) {
                           yield this.moveItemStackTo(itemStack, extra, extra + 1, false);
                        }
                     }

                     yield false;
                  }
                  default -> false;
               };
               if (this.offset != 0) {
                  if (!movedGear) {
                     return net.minecraft.world.item.ItemStack.EMPTY;
                  }
               } else if (!movedGear && !this.moveItemStackTo(itemStack, 0, this.container.getOwnerHandle().getInventory().getNonEquipmentItems().size(), true)
                  )
                {
                  return net.minecraft.world.item.ItemStack.EMPTY;
               }
            }

            if (itemStack.isEmpty()) {
               slot.setByPlayer(net.minecraft.world.item.ItemStack.EMPTY);
            } else {
               slot.setChanged();
            }

            return originalStack;
         } else {
            return net.minecraft.world.item.ItemStack.EMPTY;
         }
      }
   }
}
