package com.ranull.graves.compatibility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;

public class CompatibilityInventoryView {
   private CompatibilityInventoryView() {
   }

   public static Inventory getTopInventory(InventoryEvent event) {
      try {
         Object view = event.getView();
         Method getTopInventory = view.getClass().getMethod("getTopInventory");
         getTopInventory.setAccessible(true);
         return (Inventory)getTopInventory.invoke(view);
      } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException var3) {
         throw new RuntimeException(var3);
      }
   }

   public static Inventory getBottomInventory(InventoryEvent event) {
      try {
         Object view = event.getView();
         Method getBottomInventory = view.getClass().getMethod("getBottomInventory");
         getBottomInventory.setAccessible(true);
         return (Inventory)getBottomInventory.invoke(view);
      } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException var3) {
         throw new RuntimeException(var3);
      }
   }

   public static Inventory getTopInventory(Object inventoryView) {
      try {
         Method getTopInventory = inventoryView.getClass().getMethod("getTopInventory");
         getTopInventory.setAccessible(true);
         return (Inventory)getTopInventory.invoke(inventoryView);
      } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException var2) {
         throw new RuntimeException(var2);
      }
   }

   public static Inventory getBottomInventory(Object inventoryView) {
      try {
         Method getBottomInventory = inventoryView.getClass().getMethod("getBottomInventory");
         getBottomInventory.setAccessible(true);
         return (Inventory)getBottomInventory.invoke(inventoryView);
      } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException var2) {
         throw new RuntimeException(var2);
      }
   }
}
