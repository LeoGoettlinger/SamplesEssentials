package com.github.jikoo.openinv.internal.spigot26_2.container.bukkit;

import com.github.jikoo.openinv.internal.spigot26_2.container.OpenInventory;
import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenPlayerInventory extends CraftInventory implements PlayerInventory {
   public OpenPlayerInventory(@NotNull OpenInventory inventory) {
      super(inventory);
   }

   @NotNull
   public OpenInventory getInventory() {
      return (OpenInventory)super.getInventory();
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
      return this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().getArmorContents();
   }

   public void setArmorContents(@NotNull org.bukkit.inventory.ItemStack[] items) {
      this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().setArmorContents(items);
   }

   @NotNull
   public org.bukkit.inventory.ItemStack[] getExtraContents() {
      return this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().getExtraContents();
   }

   public void setExtraContents(@NotNull org.bukkit.inventory.ItemStack[] items) {
      this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().setExtraContents(items);
   }

   @Nullable
   public org.bukkit.inventory.ItemStack getHelmet() {
      return this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().getHelmet();
   }

   public void setHelmet(@Nullable org.bukkit.inventory.ItemStack helmet) {
      this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().setHelmet(helmet);
   }

   @Nullable
   public org.bukkit.inventory.ItemStack getChestplate() {
      return this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().getChestplate();
   }

   public void setChestplate(@Nullable org.bukkit.inventory.ItemStack chestplate) {
      this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().setChestplate(chestplate);
   }

   @Nullable
   public org.bukkit.inventory.ItemStack getLeggings() {
      return this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().getLeggings();
   }

   public void setLeggings(@Nullable org.bukkit.inventory.ItemStack leggings) {
      this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().setLeggings(leggings);
   }

   @Nullable
   public org.bukkit.inventory.ItemStack getBoots() {
      return this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().getBoots();
   }

   public void setBoots(@Nullable org.bukkit.inventory.ItemStack boots) {
      this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().setBoots(boots);
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
      return this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().getItemInOffHand();
   }

   public void setItemInOffHand(@Nullable org.bukkit.inventory.ItemStack item) {
      this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().setItemInOffHand(item);
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

   @Nullable
   public org.bukkit.inventory.ItemStack getItem(@NotNull EquipmentSlot slot) {
      return this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().getItem(slot);
   }

   public void setItem(@NotNull EquipmentSlot slot, @Nullable org.bukkit.inventory.ItemStack item) {
      this.getInventory().getOwnerHandle().getBukkitEntity().getInventory().setItem(slot, item);
   }
}
