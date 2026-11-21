package com.lishid.openinv.internal.paper26_2.container.slot;

import com.lishid.openinv.internal.paper26_2.container.slot.placeholder.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.jetbrains.annotations.NotNull;

public class ContentEquipment implements Content {
   private EntityEquipment equipment;
   private final ItemStack placeholder;
   private final EquipmentSlot equipmentSlot;

   public ContentEquipment(ServerPlayer holder, EquipmentSlot equipmentSlot) {
      this.setHolder(holder);

      this.placeholder = switch (equipmentSlot) {
         case HEAD -> Placeholders.emptyHelmet;
         case CHEST -> Placeholders.emptyChestplate;
         case LEGS -> Placeholders.emptyLeggings;
         case FEET -> Placeholders.emptyBoots;
         default -> Placeholders.emptyOffHand;
      };
      this.equipmentSlot = equipmentSlot;
   }

   @Override
   public void setHolder(@NotNull ServerPlayer holder) {
      this.equipment = holder.getInventory().equipment;
   }

   @Override
   public ItemStack get() {
      return this.equipment.get(this.equipmentSlot);
   }

   @Override
   public ItemStack remove() {
      return this.equipment.set(this.equipmentSlot, ItemStack.EMPTY);
   }

   @Override
   public ItemStack removePartial(int amount) {
      ItemStack current = this.get();
      return !current.isEmpty() && amount > 0 ? current.split(amount) : ItemStack.EMPTY;
   }

   @Override
   public void set(ItemStack itemStack) {
      this.equipment.set(this.equipmentSlot, itemStack);
   }

   @Override
   public Slot asSlot(Container container, int slot, int x, int y) {
      return new ContentEquipment.SlotEquipment(container, slot, x, y);
   }

   @Override
   public SlotType getSlotType() {
      return SlotType.ARMOR;
   }

   public class SlotEquipment extends SlotPlaceholder {
      private ServerPlayer viewer;

      SlotEquipment(Container container, int index, int x, int y) {
         super(container, index, x, y);
      }

      @Override
      public ItemStack getOrDefault() {
         ItemStack itemStack = this.getItem();
         return !itemStack.isEmpty() ? itemStack : ContentEquipment.this.placeholder;
      }

      public EquipmentSlot getEquipmentSlot() {
         return ContentEquipment.this.equipmentSlot;
      }

      public void onlyEquipmentFor(ServerPlayer viewer) {
         this.viewer = viewer;
      }

      public boolean mayPlace(@NotNull ItemStack itemStack) {
         return this.viewer == null
            ? true
            : ContentEquipment.this.equipmentSlot == EquipmentSlot.OFFHAND
               || this.viewer.getEquipmentSlotForItem(itemStack) == ContentEquipment.this.equipmentSlot;
      }
   }
}
