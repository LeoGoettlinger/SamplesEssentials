package com.lishid.openinv.internal.paper26_2.container.bukkit;

import net.minecraft.world.Container;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenDummyPlayerInventory extends OpenDummyInventory implements PlayerInventory {
   public OpenDummyPlayerInventory(Container inventory) {
      super(inventory, InventoryType.PLAYER);
   }

   public HumanEntity getHolder() {
      return (HumanEntity)super.getHolder();
   }

   @NotNull
   public ItemStack[] getArmorContents() {
      return new ItemStack[4];
   }

   @NotNull
   public ItemStack[] getExtraContents() {
      return new ItemStack[4];
   }

   @Nullable
   public ItemStack getHelmet() {
      return null;
   }

   @Nullable
   public ItemStack getChestplate() {
      return null;
   }

   @Nullable
   public ItemStack getLeggings() {
      return null;
   }

   @Nullable
   public ItemStack getBoots() {
      return null;
   }

   public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item) {
   }

   @NotNull
   public ItemStack getItem(@NotNull EquipmentSlot slot) {
      return new ItemStack(Material.AIR);
   }

   public void setArmorContents(@NotNull ItemStack[] items) {
   }

   public void setExtraContents(@NotNull ItemStack[] items) {
   }

   public void setHelmet(@Nullable ItemStack helmet) {
   }

   public void setChestplate(@Nullable ItemStack chestplate) {
   }

   public void setLeggings(@Nullable ItemStack leggings) {
   }

   public void setBoots(@Nullable ItemStack boots) {
   }

   @NotNull
   public ItemStack getItemInMainHand() {
      return new ItemStack(Material.AIR);
   }

   public void setItemInMainHand(@Nullable ItemStack item) {
   }

   @NotNull
   public ItemStack getItemInOffHand() {
      return new ItemStack(Material.AIR);
   }

   public void setItemInOffHand(@Nullable ItemStack item) {
   }

   @Deprecated
   @NotNull
   public ItemStack getItemInHand() {
      return new ItemStack(Material.AIR);
   }

   @Deprecated
   public void setItemInHand(@Nullable ItemStack stack) {
   }

   public int getHeldItemSlot() {
      return 0;
   }

   public void setHeldItemSlot(int slot) {
   }
}
