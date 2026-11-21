package com.earth2me.essentials.craftbukkit;

import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class Inventories {
   private static final int HELM_SLOT = 39;
   private static final int CHEST_SLOT = 38;
   private static final int LEG_SLOT = 37;
   private static final int BOOT_SLOT = 36;
   private static final int BODY_SLOT = 41;
   private static final int SADDLE_SLOT = 42;
   private static final boolean HAS_OFFHAND = VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_9_R01);
   private static final int INVENTORY_SIZE = HAS_OFFHAND ? 41 : 40;

   private Inventories() {
   }

   public static ItemStack getItemInHand(final Player player) {
      if (!HAS_OFFHAND) {
         return player.getInventory().getItemInHand();
      } else {
         PlayerInventory inventory = player.getInventory();
         ItemStack main = inventory.getItemInMainHand();
         return !isEmpty(main) ? main : inventory.getItemInOffHand();
      }
   }

   public static ItemStack getItemInMainHand(final Player player) {
      return !HAS_OFFHAND ? player.getInventory().getItemInHand() : player.getInventory().getItemInMainHand();
   }

   public static void setItemInMainHand(final Player player, final ItemStack stack) {
      if (HAS_OFFHAND) {
         player.getInventory().setItemInMainHand(stack);
      } else {
         player.setItemInHand(stack);
      }
   }

   public static void setItemInMainHand(final EntityEquipment entityEquipment, final ItemStack stack) {
      if (HAS_OFFHAND) {
         entityEquipment.setItemInMainHand(stack);
      } else {
         entityEquipment.setItemInHand(stack);
      }
   }

   public static void setItemInMainHandDropChance(final EntityEquipment entityEquipment, final float chance) {
      if (HAS_OFFHAND) {
         entityEquipment.setItemInMainHandDropChance(chance);
      } else {
         entityEquipment.setItemInHandDropChance(chance);
      }
   }

   public static boolean containsAtLeast(final Player player, final ItemStack item, int amount) {
      for (ItemStack invItem : player.getInventory().getContents()) {
         if (!isEmpty(invItem) && invItem.isSimilar(item)) {
            amount -= invItem.getAmount();
            if (amount <= 0) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean hasSpace(final Player player, final int maxStack, final boolean includeArmor, ItemStack... items) {
      items = normalizeItems(cloneItems(items));
      InventoryData inventoryData = parseInventoryData(player.getInventory(), items, maxStack, includeArmor);
      List<Integer> emptySlots = inventoryData.getEmptySlots();

      label44:
      for (ItemStack item : items) {
         if (!isEmpty(item)) {
            int itemMax = Math.max(maxStack, item.getMaxStackSize());
            List<Integer> partialSlots = inventoryData.getPartialSlots().get(item);

            while (true) {
               while (partialSlots == null || partialSlots.isEmpty()) {
                  if (emptySlots.isEmpty()) {
                     return false;
                  }

                  emptySlots.remove(0);
                  if (item.getAmount() <= itemMax) {
                     continue label44;
                  }

                  item.setAmount(item.getAmount() - itemMax);
               }

               int slot = partialSlots.remove(0);
               ItemStack existing = player.getInventory().getItem(slot);
               if (isEmpty(existing)) {
                  existing = item.clone();
                  existing.setAmount(0);
               }

               int amount = item.getAmount();
               int existingAmount = existing.getAmount();
               if (amount + existingAmount <= itemMax) {
                  break;
               }

               item.setAmount(amount + existingAmount - itemMax);
            }
         }
      }

      return true;
   }

   public static Map<Integer, ItemStack> addItem(final Player player, final ItemStack... items) {
      return addItem(player, 0, false, items);
   }

   public static Map<Integer, ItemStack> addItem(final Player player, final int maxStack, final ItemStack... items) {
      return addItem(player, maxStack, false, items);
   }

   public static ItemStack addItem(final Player player, final int maxStack, ItemStack item, int slot) {
      int itemMax = Math.max(maxStack, item.getMaxStackSize());
      ItemStack existing = player.getInventory().getItem(slot);
      int existingAmount;
      if (isEmpty(existing)) {
         existing = item.clone();
         existingAmount = 0;
      } else {
         existingAmount = existing.getAmount();
      }

      if (!item.isSimilar(existing)) {
         return item;
      } else {
         int amount = item.getAmount();
         if (amount + existingAmount <= itemMax) {
            existing.setAmount(amount + existingAmount);
            player.getInventory().setItem(slot, existing);
            return null;
         } else {
            existing.setAmount(itemMax);
            player.getInventory().setItem(slot, existing);
            item.setAmount(amount + existingAmount - itemMax);
            return item;
         }
      }
   }

   public static Map<Integer, ItemStack> addItem(final Player player, final int maxStack, final boolean allowArmor, ItemStack... items) {
      items = normalizeItems(cloneItems(items));
      Map<Integer, ItemStack> leftover = new HashMap<>();
      InventoryData inventoryData = parseInventoryData(player.getInventory(), items, maxStack, allowArmor);
      List<Integer> emptySlots = inventoryData.getEmptySlots();

      label46:
      for (int i = 0; i < items.length; i++) {
         ItemStack item = items[i];
         if (!isEmpty(item)) {
            int itemMax = Math.max(maxStack, item.getMaxStackSize());
            List<Integer> partialSlots = inventoryData.getPartialSlots().get(item);

            while (true) {
               while (partialSlots == null || partialSlots.isEmpty()) {
                  if (emptySlots.isEmpty()) {
                     leftover.put(i, item);
                     continue label46;
                  }

                  int slot = emptySlots.remove(0);
                  if (item.getAmount() <= itemMax) {
                     player.getInventory().setItem(slot, item);
                     continue label46;
                  }

                  ItemStack split = item.clone();
                  split.setAmount(itemMax);
                  player.getInventory().setItem(slot, split);
                  item.setAmount(item.getAmount() - itemMax);
               }

               int slot = partialSlots.remove(0);
               ItemStack existing = player.getInventory().getItem(slot);
               if (isEmpty(existing)) {
                  existing = item.clone();
                  existing.setAmount(0);
               }

               int amount = item.getAmount();
               int existingAmount = existing.getAmount();
               if (amount + existingAmount <= itemMax) {
                  existing.setAmount(amount + existingAmount);
                  player.getInventory().setItem(slot, existing);
                  break;
               }

               existing.setAmount(itemMax);
               player.getInventory().setItem(slot, existing);
               item.setAmount(amount + existingAmount - itemMax);
            }
         }
      }

      return leftover;
   }

   public static ItemStack[] getInventory(final Player player, final boolean includeArmor) {
      ItemStack[] items = new ItemStack[INVENTORY_SIZE];

      for (int i = 0; i < items.length; i++) {
         if (!includeArmor && isArmorSlot(i)) {
            items[i] = null;
         } else {
            items[i] = player.getInventory().getItem(i);
         }
      }

      return items;
   }

   public static void removeItemExact(final Player player, final ItemStack toRemove, final boolean includeArmor) {
      removeItems(player, itemStack -> itemStack.equals(toRemove), includeArmor);
   }

   public static int removeItemSimilar(final Player player, final ItemStack toRemove, final boolean includeArmor) {
      return removeItems(player, itemStack -> itemStack.isSimilar(toRemove), includeArmor);
   }

   public static int removeItems(final Player player, final Predicate<ItemStack> removePredicate, final boolean includeArmor) {
      int removedAmount = 0;
      ItemStack[] items = player.getInventory().getContents();

      for (int i = 0; i < items.length; i++) {
         if (!isContortedSlot(i) && (includeArmor || !isArmorSlot(i))) {
            ItemStack item = items[i];
            if (!isEmpty(item) && removePredicate.test(item)) {
               removedAmount += item.getAmount();
               item.setAmount(0);
               player.getInventory().setItem(i, item);
            }
         }
      }

      return removedAmount;
   }

   public static boolean removeItemAmount(final Player player, final ItemStack toRemove, int amount) {
      if (amount < 0) {
         throw new IllegalArgumentException("Amount cannot be negative.");
      } else {
         List<Integer> clearSlots = new ArrayList<>();
         ItemStack[] items = player.getInventory().getContents();

         for (int i = 0; i < items.length; i++) {
            if (!isContortedSlot(i)) {
               ItemStack item = items[i];
               if (!isEmpty(item) && item.isSimilar(toRemove)) {
                  if (item.getAmount() >= amount) {
                     item.setAmount(item.getAmount() - amount);
                     player.getInventory().setItem(i, item);

                     for (int slot : clearSlots) {
                        clearSlot(player, slot);
                     }

                     return true;
                  }

                  amount -= item.getAmount();
                  clearSlots.add(i);
                  if (amount == 0) {
                     for (int slot : clearSlots) {
                        clearSlot(player, slot);
                     }

                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public static void clearSlot(final Player player, final int slot) {
      ItemStack item = player.getInventory().getItem(slot);
      if (!isEmpty(item)) {
         item.setAmount(0);
         player.getInventory().setItem(slot, item);
      }
   }

   public static void setSlot(final Player inventory, final int slot, final ItemStack item) {
      inventory.getInventory().setItem(slot, item);
   }

   private static ItemStack[] normalizeItems(final ItemStack[] items) {
      if (items.length <= 1) {
         return items;
      } else {
         ItemStack[] normalizedItems = new ItemStack[items.length];
         int nextNormalizedIndex = 0;

         label35:
         for (ItemStack item : items) {
            if (!isEmpty(item)) {
               for (int j = 0; j < nextNormalizedIndex; j++) {
                  ItemStack normalizedItem = normalizedItems[j];
                  if (!isEmpty(normalizedItem) && item.isSimilar(normalizedItem)) {
                     normalizedItem.setAmount(normalizedItem.getAmount() + item.getAmount());
                     continue label35;
                  }
               }

               normalizedItems[nextNormalizedIndex++] = item;
            }
         }

         return normalizedItems;
      }
   }

   private static ItemStack[] cloneItems(final ItemStack[] items) {
      ItemStack[] clonedItems = new ItemStack[items.length];

      for (int i = 0; i < items.length; i++) {
         ItemStack item = items[i];
         if (!isEmpty(item)) {
            clonedItems[i] = item.clone();
         }
      }

      return clonedItems;
   }

   private static InventoryData parseInventoryData(final Inventory inventory, final ItemStack[] items, final int maxStack, final boolean includeArmor) {
      ItemStack[] inventoryContents = inventory.getContents();
      List<Integer> emptySlots = new ArrayList<>();
      HashMap<ItemStack, List<Integer>> partialSlots = new HashMap<>();

      for (int i = 0; i < inventoryContents.length; i++) {
         if (!isContortedSlot(i) && (includeArmor || !isArmorSlot(i))) {
            ItemStack invItem = inventoryContents[i];
            if (isEmpty(invItem)) {
               emptySlots.add(i);
            } else {
               for (ItemStack newItem : items) {
                  if (invItem.getAmount() < Math.max(maxStack, invItem.getMaxStackSize()) && invItem.isSimilar(newItem)) {
                     partialSlots.computeIfAbsent(newItem, k -> new ArrayList<>()).add(i);
                  }
               }
            }
         }
      }

      if (includeArmor) {
         ItemStack helm = null;
         ItemStack chest = null;
         ItemStack legs = null;
         ItemStack boots = null;

         for (ItemStack item : items) {
            if (!isEmpty(item)) {
               if (helm == null && MaterialUtil.isHelmet(item.getType())) {
                  helm = item;
                  if (emptySlots.contains(39)) {
                     partialSlots.computeIfAbsent(item, k -> new ArrayList<>()).add(39);
                  }
               } else if (chest == null && MaterialUtil.isChestplate(item.getType())) {
                  chest = item;
                  if (emptySlots.contains(38)) {
                     partialSlots.computeIfAbsent(item, k -> new ArrayList<>()).add(38);
                  }
               } else if (legs == null && MaterialUtil.isLeggings(item.getType())) {
                  legs = item;
                  if (emptySlots.contains(37)) {
                     partialSlots.computeIfAbsent(item, k -> new ArrayList<>()).add(37);
                  }
               } else if (boots == null && MaterialUtil.isBoots(item.getType())) {
                  boots = item;
                  if (emptySlots.contains(36)) {
                     partialSlots.computeIfAbsent(item, k -> new ArrayList<>()).add(36);
                  }
               }
            }
         }

         emptySlots.remove(Integer.valueOf(39));
         emptySlots.remove(Integer.valueOf(38));
         emptySlots.remove(Integer.valueOf(37));
         emptySlots.remove(Integer.valueOf(36));
      }

      return new InventoryData(emptySlots, partialSlots);
   }

   private static boolean isEmpty(final ItemStack stack) {
      return stack == null || MaterialUtil.isAir(stack.getType());
   }

   public static boolean isContortedSlot(final int slot) {
      return slot == 41 || slot == 42;
   }

   private static boolean isArmorSlot(final int slot) {
      return slot == 39 || slot == 38 || slot == 37 || slot == 36;
   }

   public static boolean isBottomInventorySlot(final int slot) {
      return slot > 35;
   }
}
