package com.magmaguy.elitemobs.menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EliteMenu implements Listener {
   protected static void validateSlots(int size, Collection<Integer> slots) {
      Set<Integer> occupied = new HashSet<>();

      for (Integer slot : slots) {
         if (slot == null || slot < 0 || slot >= size || !occupied.add(slot)) {
            throw new IllegalArgumentException("Menu slots must be distinct and between 0 and " + (size - 1) + ": " + slots);
         }
      }
   }

   public static void createEliteMenu(Inventory inventory, Set<Inventory> inventories) {
      inventories.add(inventory);
   }

   public static boolean isEliteMenu(InventoryClickEvent event, Set<Inventory> inventories) {
      return inventories.contains(event.getInventory());
   }

   public static boolean isTopMenu(InventoryClickEvent event) {
      return event.getClickedInventory() == null ? false : event.getClickedInventory().equals(event.getView().getTopInventory());
   }

   public static boolean isBottomMenu(InventoryClickEvent event) {
      return !isTopMenu(event);
   }

   public static void cancel(HumanEntity player, Inventory eliteInventory, Inventory playerInventory, List<Integer> inputSlots) {
      for (int slot : inputSlots) {
         if (eliteInventory.getItem(slot) != null) {
            HashMap<Integer, ItemStack> leftOvers = playerInventory.addItem(new ItemStack[]{eliteInventory.getItem(slot)});
            eliteInventory.clear(slot);
            if (!leftOvers.isEmpty()) {
               leftOvers.values().forEach(itemStack -> player.getWorld().dropItem(player.getLocation(), itemStack));
            }
         }
      }
   }

   public static void moveOneItemUp(int targetSlot, InventoryClickEvent event) {
      if (event.getCurrentItem() != null) {
         ItemStack newItemStack = event.getCurrentItem().clone();
         newItemStack.setAmount(1);
         event.getView().getTopInventory().setItem(targetSlot, newItemStack);
         event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
      }
   }

   public static boolean moveItemDown(Inventory inventoryToClear, int slotToClear, HumanEntity player, boolean canDrop) {
      boolean itemLoss = false;
      if (inventoryToClear.getItem(slotToClear) == null) {
         return itemLoss;
      } else {
         HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(new ItemStack[]{inventoryToClear.getItem(slotToClear)});
         if (!leftovers.isEmpty() && canDrop) {
            leftovers.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
         } else if (!leftovers.isEmpty()) {
            itemLoss = true;
         }

         inventoryToClear.setItem(slotToClear, null);
         return itemLoss;
      }
   }

   public static void moveItemDown(Inventory inventoryToClear, int slotToClear, HumanEntity player) {
      moveItemDown(inventoryToClear, slotToClear, player, true);
   }

   protected record RecipeLayout(int item, int consumable, int output, int itemInfo, int consumableInfo, int outputInfo, int info, int cancel, int confirm) {
      protected RecipeLayout(int item, int consumable, int output, int itemInfo, int consumableInfo, int outputInfo, int info, int cancel, int confirm) {
         EliteMenu.validateSlots(54, List.of(item, consumable, output, itemInfo, consumableInfo, outputInfo, info, cancel, confirm));
         this.item = item;
         this.consumable = consumable;
         this.output = output;
         this.itemInfo = itemInfo;
         this.consumableInfo = consumableInfo;
         this.outputInfo = outputInfo;
         this.info = info;
         this.cancel = cancel;
         this.confirm = confirm;
      }
   }

   protected record StoreLayout(List<Integer> inputs, int info, int cancel, int confirm) {
      protected StoreLayout(List<Integer> inputs, int info, int cancel, int confirm) {
         inputs = List.copyOf(inputs);
         ArrayList<Integer> slots = new ArrayList<>(inputs);
         slots.addAll(List.of(info, cancel, confirm));
         EliteMenu.validateSlots(54, slots);
         this.inputs = inputs;
         this.info = info;
         this.cancel = cancel;
         this.confirm = confirm;
      }
   }
}
