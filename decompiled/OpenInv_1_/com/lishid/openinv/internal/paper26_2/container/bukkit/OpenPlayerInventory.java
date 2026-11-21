package com.lishid.openinv.internal.paper26_2.container.bukkit;

import com.google.common.base.Preconditions;
import com.lishid.openinv.internal.paper26_2.container.BaseOpenInventory;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenPlayerInventory extends CraftInventory implements PlayerInventory {
   public OpenPlayerInventory(@NotNull BaseOpenInventory inventory) {
      super(inventory);
   }

   @NotNull
   public BaseOpenInventory getInventory() {
      return (BaseOpenInventory)super.getInventory();
   }

   @NotNull
   public org.bukkit.inventory.ItemStack[] getContents() {
      return this.asCraftMirror(this.getInventory().getOwnerHandle().getInventory().getContents());
   }

   public void setContents(org.bukkit.inventory.ItemStack[] items) {
      Inventory internal = this.getInventory().getOwnerHandle().getInventory();
      int size = internal.getContainerSize();
      Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);

      for (int index = 0; index < size; index++) {
         if (index < items.length) {
            internal.setItem(index, CraftItemStack.asNMSCopy(items[index]));
         } else {
            internal.setItem(index, ItemStack.EMPTY);
         }
      }
   }

   @NotNull
   public org.bukkit.inventory.ItemStack[] getStorageContents() {
      return this.asCraftMirror(this.getInventory().getOwnerHandle().getInventory().getNonEquipmentItems());
   }

   public void setStorageContents(org.bukkit.inventory.ItemStack[] items) throws IllegalArgumentException {
      NonNullList<ItemStack> list = this.getInventory().getOwnerHandle().getInventory().getNonEquipmentItems();
      int size = list.size();
      Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);

      for (int index = 0; index < items.length; index++) {
         list.set(index, CraftItemStack.asNMSCopy(items[index]));
      }
   }

   @NotNull
   public InventoryType getType() {
      return InventoryType.PLAYER;
   }

   @NotNull
   public Player getHolder() {
      return this.getInventory().getOwner();
   }

   @NotNull
   public org.bukkit.inventory.ItemStack[] getArmorContents() {
      return this.asCraftMirror(this.getInventory().getOwnerHandle().getInventory().getArmorContents());
   }

   public void setArmorContents(@NotNull org.bukkit.inventory.ItemStack[] items) {
      int size = Inventory.EQUIPMENT_SLOTS_SORTED_BY_INDEX.length;
      Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);

      for (int index = 0; index < items.length; index++) {
         this.getInventory()
            .getOwnerHandle()
            .getInventory()
            .equipment
            .set(Inventory.EQUIPMENT_SLOTS_SORTED_BY_INDEX[index], CraftItemStack.asNMSCopy(items[index]));
      }
   }

   @NotNull
   public org.bukkit.inventory.ItemStack[] getExtraContents() {
      return this.asCraftMirror(List.of(this.getInventory().getOwnerHandle().getInventory().equipment.get(EquipmentSlot.OFFHAND)));
   }

   public void setExtraContents(@NotNull org.bukkit.inventory.ItemStack[] items) {
      Preconditions.checkArgument(items.length <= 1, "items.length must be <= 1");

      for (org.bukkit.inventory.ItemStack item : items) {
         this.getInventory().getOwnerHandle().getInventory().equipment.set(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(item));
      }
   }

   @NotNull
   public org.bukkit.inventory.ItemStack getHelmet() {
      return CraftItemStack.asCraftMirror(this.getInventory().getOwnerHandle().getInventory().equipment.get(EquipmentSlot.HEAD));
   }

   public void setHelmet(@Nullable org.bukkit.inventory.ItemStack helmet) {
      this.getInventory().getOwnerHandle().getInventory().equipment.set(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(helmet));
   }

   @NotNull
   public org.bukkit.inventory.ItemStack getChestplate() {
      return CraftItemStack.asCraftMirror(this.getInventory().getOwnerHandle().getInventory().equipment.get(EquipmentSlot.CHEST));
   }

   public void setChestplate(@Nullable org.bukkit.inventory.ItemStack chestplate) {
      this.getInventory().getOwnerHandle().getInventory().equipment.set(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(chestplate));
   }

   @NotNull
   public org.bukkit.inventory.ItemStack getLeggings() {
      return CraftItemStack.asCraftMirror(this.getInventory().getOwnerHandle().getInventory().equipment.get(EquipmentSlot.LEGS));
   }

   public void setLeggings(@Nullable org.bukkit.inventory.ItemStack leggings) {
      this.getInventory().getOwnerHandle().getInventory().equipment.set(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(leggings));
   }

   @NotNull
   public org.bukkit.inventory.ItemStack getBoots() {
      return CraftItemStack.asCraftMirror(this.getInventory().getOwnerHandle().getInventory().equipment.get(EquipmentSlot.FEET));
   }

   public void setBoots(@Nullable org.bukkit.inventory.ItemStack boots) {
      this.getInventory().getOwnerHandle().getInventory().equipment.set(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(boots));
   }

   @NotNull
   public org.bukkit.inventory.ItemStack getItemInMainHand() {
      Inventory internal = this.getInventory().getOwnerHandle().getInventory();
      return CraftItemStack.asCraftMirror(internal.getSelectedItem());
   }

   public void setItemInMainHand(@Nullable org.bukkit.inventory.ItemStack item) {
      Inventory internal = this.getInventory().getOwnerHandle().getInventory();
      internal.setSelectedItem(CraftItemStack.asNMSCopy(item));
   }

   @NotNull
   public org.bukkit.inventory.ItemStack getItemInOffHand() {
      return CraftItemStack.asCraftMirror(this.getInventory().getOwnerHandle().getInventory().equipment.get(EquipmentSlot.OFFHAND));
   }

   public void setItemInOffHand(@Nullable org.bukkit.inventory.ItemStack item) {
      this.getInventory().getOwnerHandle().getInventory().equipment.set(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(item));
   }

   @Deprecated
   @NotNull
   public org.bukkit.inventory.ItemStack getItemInHand() {
      return this.getItemInMainHand();
   }

   @Deprecated
   public void setItemInHand(@Nullable org.bukkit.inventory.ItemStack stack) {
      this.setItemInMainHand(stack);
   }

   public int getHeldItemSlot() {
      Inventory internal = this.getInventory().getOwnerHandle().getInventory();
      return internal.getNonEquipmentItems().size() - 9 + internal.getSelectedSlot();
   }

   public void setHeldItemSlot(int slot) {
      slot %= 9;
      this.getInventory().getOwnerHandle().getInventory().setSelectedSlot(slot);
   }

   @NotNull
   public org.bukkit.inventory.ItemStack getItem(@NotNull org.bukkit.inventory.EquipmentSlot slot) {
      return switch (slot) {
         case HAND -> this.getItemInMainHand();
         case OFF_HAND -> this.getItemInOffHand();
         case FEET -> this.getBoots();
         case LEGS -> this.getLeggings();
         case CHEST -> this.getChestplate();
         case HEAD -> this.getHelmet();
         default -> throw new IllegalArgumentException("Unsupported EquipmentSlot " + slot);
      };
   }

   public void setItem(@NotNull org.bukkit.inventory.EquipmentSlot slot, @Nullable org.bukkit.inventory.ItemStack item) {
      switch (slot) {
         case HAND:
            this.setItemInMainHand(item);
            break;
         case OFF_HAND:
            this.setItemInOffHand(item);
            break;
         case FEET:
            this.setBoots(item);
            break;
         case LEGS:
            this.setLeggings(item);
            break;
         case CHEST:
            this.setChestplate(item);
            break;
         case HEAD:
            this.setHelmet(item);
            break;
         default:
            throw new IllegalArgumentException("Unsupported EquipmentSlot " + slot);
      }
   }
}
