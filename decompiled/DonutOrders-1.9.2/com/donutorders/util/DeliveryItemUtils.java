package com.donutorders.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public final class DeliveryItemUtils {
   private DeliveryItemUtils() {
   }

   public static boolean isShulkerBox(ItemStack item) {
      return item != null && item.getType() != Material.AIR && Tag.SHULKER_BOXES.isTagged(item.getType());
   }

   public static boolean isDeliverablePlacement(ItemStack item, ItemStack template) {
      if (item == null || item.getType() == Material.AIR || template == null) {
         return false;
      } else {
         return item.isSimilar(template) ? true : shulkerContainsDeliverable(item, template);
      }
   }

   public static boolean shulkerContainsDeliverable(ItemStack shulker, ItemStack template) {
      return countInShulker(shulker, template) > 0;
   }

   public static int countInShulker(ItemStack shulker, ItemStack template) {
      Inventory inventory = getShulkerInventory(shulker);
      if (inventory != null && template != null) {
         int total = 0;

         for (ItemStack inner : inventory.getContents()) {
            if (inner != null && inner.isSimilar(template)) {
               total += inner.getAmount();
            }
         }

         return total;
      } else {
         return 0;
      }
   }

   public static int countAvailable(Player player, ItemStack[] guiSlots, ItemStack template) {
      int total = 0;
      if (guiSlots != null) {
         for (ItemStack slot : guiSlots) {
            if (slot != null && slot.getType() != Material.AIR) {
               if (slot.isSimilar(template)) {
                  total += slot.getAmount();
               } else if (isShulkerBox(slot)) {
                  total += countInShulker(slot, template);
               }
            }
         }
      }

      if (player != null) {
         total += countShulkersInInventory(player, template);
      }

      return total;
   }

   public static ItemStack[] extract(Player player, ItemStack[] guiSnapshot, ItemStack template, int needed) {
      if (needed > 0 && template != null) {
         List<ItemStack> result = new ArrayList<>();
         int remaining = needed;
         if (guiSnapshot != null) {
            remaining = extractLooseFromSlots(guiSnapshot, template, needed, result);
            if (remaining > 0) {
               remaining = extractFromShulkerSlots(guiSnapshot, template, remaining, result);
            }
         }

         if (remaining > 0 && player != null) {
            extractFromPlayerInventoryShulkers(player, template, remaining, result);
         }

         return result.toArray(new ItemStack[0]);
      } else {
         return new ItemStack[0];
      }
   }

   public static void returnSnapshotShulkers(Player player, ItemStack[] guiSnapshot) {
      if (player != null && guiSnapshot != null) {
         for (ItemStack slot : guiSnapshot) {
            if (slot != null && isShulkerBox(slot)) {
               HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack[]{slot.clone()});
               overflow.values().forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
            }
         }
      }
   }

   private static int countShulkersInInventory(Player player, ItemStack template) {
      int total = 0;

      for (ItemStack item : player.getInventory().getStorageContents()) {
         if (isShulkerBox(item)) {
            total += countInShulker(item, template);
         }
      }

      for (ItemStack itemx : player.getInventory().getArmorContents()) {
         if (isShulkerBox(itemx)) {
            total += countInShulker(itemx, template);
         }
      }

      ItemStack offhand = player.getInventory().getItemInOffHand();
      if (isShulkerBox(offhand)) {
         total += countInShulker(offhand, template);
      }

      return total;
   }

   private static int extractLooseFromSlots(ItemStack[] slots, ItemStack template, int remaining, List<ItemStack> result) {
      for (int i = 0; i < slots.length && remaining > 0; i++) {
         ItemStack slot = slots[i];
         if (slot != null && slot.isSimilar(template)) {
            int take = Math.min(slot.getAmount(), remaining);
            addToResult(result, slot, take);
            remaining -= take;
            if (take >= slot.getAmount()) {
               slots[i] = null;
            } else {
               slot.setAmount(slot.getAmount() - take);
            }
         }
      }

      return remaining;
   }

   private static int extractFromShulkerSlots(ItemStack[] slots, ItemStack template, int remaining, List<ItemStack> result) {
      for (ItemStack slot : slots) {
         if (remaining <= 0) {
            break;
         }

         if (isShulkerBox(slot)) {
            remaining = extractFromShulker(slot, template, remaining, result);
         }
      }

      return remaining;
   }

   private static void extractFromPlayerInventoryShulkers(Player player, ItemStack template, int remaining, List<ItemStack> result) {
      remaining = extractFromInventoryArray(player.getInventory().getStorageContents(), template, remaining, result);
      if (remaining > 0) {
         remaining = extractFromInventoryArray(player.getInventory().getArmorContents(), template, remaining, result);
         if (remaining > 0) {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (isShulkerBox(offhand) && remaining > 0) {
               extractFromShulker(offhand, template, remaining, result);
            }
         }
      }
   }

   private static int extractFromInventoryArray(ItemStack[] items, ItemStack template, int remaining, List<ItemStack> result) {
      for (ItemStack item : items) {
         if (remaining <= 0) {
            break;
         }

         if (isShulkerBox(item)) {
            remaining = extractFromShulker(item, template, remaining, result);
         }
      }

      return remaining;
   }

   private static int extractFromShulker(ItemStack shulker, ItemStack template, int remaining, List<ItemStack> result) {
      if (!(shulker.getItemMeta() instanceof BlockStateMeta blockStateMeta)) {
         return remaining;
      } else if (!(blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox)) {
         return remaining;
      } else {
         Inventory var11 = shulkerBox.getInventory();

         for (int i = 0; i < var11.getSize() && remaining > 0; i++) {
            ItemStack inner = var11.getItem(i);
            if (inner != null && inner.isSimilar(template)) {
               int take = Math.min(inner.getAmount(), remaining);
               addToResult(result, inner, take);
               remaining -= take;
               if (take >= inner.getAmount()) {
                  var11.setItem(i, null);
               } else {
                  inner.setAmount(inner.getAmount() - take);
               }
            }
         }

         blockStateMeta.setBlockState(shulkerBox);
         shulker.setItemMeta(blockStateMeta);
         return remaining;
      }
   }

   private static void addToResult(List<ItemStack> result, ItemStack source, int amount) {
      for (ItemStack existing : result) {
         if (existing.isSimilar(source) && existing.getAmount() + amount <= existing.getMaxStackSize()) {
            existing.setAmount(existing.getAmount() + amount);
            return;
         }
      }

      ItemStack copy = source.clone();
      copy.setAmount(amount);
      result.add(copy);
   }

   private static Inventory getShulkerInventory(ItemStack shulker) {
      if (!isShulkerBox(shulker)) {
         return null;
      } else if (shulker.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
         return blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox ? shulkerBox.getInventory() : null;
      } else {
         return null;
      }
   }
}
