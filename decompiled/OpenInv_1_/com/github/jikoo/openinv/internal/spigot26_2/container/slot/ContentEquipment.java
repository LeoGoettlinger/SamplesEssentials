package com.github.jikoo.openinv.internal.spigot26_2.container.slot;

import com.github.jikoo.openinv.internal.spigot26_2.container.slot.placeholder.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class ContentEquipment implements Content {
   private PlayerInventory equipment;
   private final ItemStack placeholder;
   private final EquipmentSlot equipmentSlot;

   public ContentEquipment(ServerPlayer holder, net.minecraft.world.entity.EquipmentSlot equipmentSlot) {
      this.setHolder(holder);

      this.placeholder = switch (equipmentSlot) {
         case HEAD -> Placeholders.emptyHelmet;
         case CHEST -> Placeholders.emptyChestplate;
         case LEGS -> Placeholders.emptyLeggings;
         case FEET -> Placeholders.emptyBoots;
         default -> Placeholders.emptyOffHand;
      };
      this.equipmentSlot = CraftEquipmentSlot.getSlot(equipmentSlot);
   }

   @Override
   public void setHolder(@NotNull ServerPlayer holder) {
      this.equipment = holder.getBukkitEntity().getInventory();
   }

   @Override
   public ItemStack get() {
      return CraftItemStack.asNMSCopy(this.equipment.getItem(this.equipmentSlot));
   }

   @Override
   public ItemStack remove() {
      org.bukkit.inventory.ItemStack old = this.equipment.getItem(this.equipmentSlot);
      this.equipment.setItem(this.equipmentSlot, null);
      return CraftItemStack.asNMSCopy(old);
   }

   @Override
   public ItemStack removePartial(int amount) {
      if (amount <= 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack current = this.get();
         if (current.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            ItemStack split = current.split(amount);
            this.set(current);
            return split;
         }
      }
   }

   @Override
   public void set(ItemStack itemStack) {
      this.equipment.setItem(this.equipmentSlot, CraftItemStack.asCraftMirror(itemStack));
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

      public net.minecraft.world.entity.EquipmentSlot getEquipmentSlot() {
         return CraftEquipmentSlot.getNMS(ContentEquipment.this.equipmentSlot);
      }

      public void onlyEquipmentFor(ServerPlayer viewer) {
         this.viewer = viewer;
      }

      public boolean mayPlace(@NotNull ItemStack itemStack) {
         return this.viewer == null
            ? true
            : ContentEquipment.this.equipmentSlot == EquipmentSlot.OFF_HAND
               || this.viewer.getEquipmentSlotForItem(itemStack) == CraftEquipmentSlot.getNMS(ContentEquipment.this.equipmentSlot);
      }
   }
}
