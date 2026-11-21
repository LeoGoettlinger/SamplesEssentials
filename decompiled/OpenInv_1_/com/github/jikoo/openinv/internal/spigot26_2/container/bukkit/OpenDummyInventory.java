package com.github.jikoo.openinv.internal.spigot26_2.container.bukkit;

import com.lishid.openinv.internal.ViewOnly;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import net.minecraft.world.Container;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenDummyInventory extends CraftInventory implements ViewOnly {
   private final InventoryType type;

   public OpenDummyInventory(Container inventory, InventoryType type) {
      super(inventory);
      this.type = type;
   }

   @NotNull
   public InventoryType getType() {
      return this.type;
   }

   @Nullable
   public ItemStack getItem(int index) {
      return null;
   }

   public void setItem(int index, @Nullable ItemStack item) {
   }

   @NotNull
   public HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) throws IllegalArgumentException {
      return arrayToHashMap(items);
   }

   @NotNull
   public HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) throws IllegalArgumentException {
      return arrayToHashMap(items);
   }

   @NotNull
   private static HashMap<Integer, ItemStack> arrayToHashMap(@NotNull ItemStack[] items) {
      HashMap<Integer, ItemStack> ignored = new HashMap<>();

      for (int index = 0; index < items.length; index++) {
         ignored.put(index, items[index]);
      }

      return ignored;
   }

   @NotNull
   public ItemStack[] getContents() {
      return new ItemStack[this.getSize()];
   }

   public void setContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
   }

   @NotNull
   public ItemStack[] getStorageContents() {
      return new ItemStack[this.getSize()];
   }

   public void setStorageContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
   }

   public boolean contains(@NotNull Material material) throws IllegalArgumentException {
      return false;
   }

   public boolean contains(@Nullable ItemStack item) {
      return false;
   }

   public boolean contains(@NotNull Material material, int amount) throws IllegalArgumentException {
      return false;
   }

   public boolean contains(@Nullable ItemStack item, int amount) {
      return false;
   }

   public boolean containsAtLeast(@Nullable ItemStack item, int amount) {
      return false;
   }

   @NotNull
   public HashMap<Integer, ItemStack> all(@NotNull Material material) throws IllegalArgumentException {
      return new HashMap<>();
   }

   @NotNull
   public HashMap<Integer, ItemStack> all(@Nullable ItemStack item) {
      return new HashMap<>();
   }

   public int first(@NotNull Material material) throws IllegalArgumentException {
      return -1;
   }

   public int first(@NotNull ItemStack item) {
      return -1;
   }

   public int firstEmpty() {
      return -1;
   }

   public boolean isEmpty() {
      return true;
   }

   public void remove(@NotNull Material material) throws IllegalArgumentException {
   }

   public void remove(@NotNull ItemStack item) {
   }

   public void clear(int index) {
   }

   public void clear() {
   }

   @NotNull
   public ListIterator<ItemStack> iterator() {
      return Collections.emptyListIterator();
   }

   @NotNull
   public ListIterator<ItemStack> iterator(int index) {
      return Collections.emptyListIterator();
   }
}
