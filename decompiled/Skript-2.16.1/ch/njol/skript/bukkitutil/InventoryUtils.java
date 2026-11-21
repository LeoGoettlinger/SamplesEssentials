package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;

public class InventoryUtils {
   @Nullable
   private static final MethodHandle GET_TITLE;
   @Nullable
   private static final MethodHandle GET_INVENTORY;
   @Nullable
   private static final MethodHandle CONVERT_SLOT;
   @Nullable
   private static final MethodHandle GET_TOP_INVENTORY;
   @Nullable
   private static final MethodHandle GET_BOTTOM_INVENTORY;

   @Nullable
   public static String getTitle(InventoryView inventoryView) {
      if (GET_TITLE == null) {
         return inventoryView.getTitle();
      } else {
         try {
            return (String)GET_TITLE.invoke((InventoryView)inventoryView);
         } catch (Throwable var2) {
            return null;
         }
      }
   }

   @Nullable
   public static Inventory getInventory(InventoryView inventoryView, int rawSlot) {
      if (GET_INVENTORY == null) {
         return inventoryView.getInventory(rawSlot);
      } else {
         try {
            return (Inventory)GET_INVENTORY.invoke((InventoryView)inventoryView, (int)rawSlot);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   @Nullable
   public static Integer convertSlot(InventoryView inventoryView, int rawSlot) {
      if (CONVERT_SLOT == null) {
         return inventoryView.convertSlot(rawSlot);
      } else {
         try {
            return (Integer)CONVERT_SLOT.invoke((InventoryView)inventoryView, (int)rawSlot);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   @Nullable
   public static Inventory getTopInventory(InventoryView inventoryView) {
      if (GET_TOP_INVENTORY == null) {
         return inventoryView.getTopInventory();
      } else {
         try {
            return (Inventory)GET_TOP_INVENTORY.invoke((InventoryView)inventoryView);
         } catch (Throwable var2) {
            return null;
         }
      }
   }

   @Nullable
   public static Inventory getBottomInventory(InventoryView inventoryView) {
      if (GET_BOTTOM_INVENTORY == null) {
         return inventoryView.getBottomInventory();
      } else {
         try {
            return (Inventory)GET_BOTTOM_INVENTORY.invoke((InventoryView)inventoryView);
         } catch (Throwable var2) {
            return null;
         }
      }
   }

   static {
      MethodHandle getTitle = null;
      MethodHandle getInventory = null;
      MethodHandle convertSlot = null;
      MethodHandle getTopInventory = null;
      MethodHandle getBottomInventory = null;
      if (!InventoryView.class.isInterface()) {
         try {
            Lookup lookup = MethodHandles.lookup();
            getTitle = lookup.findVirtual(InventoryView.class, "getTitle", MethodType.methodType(String.class));
            getInventory = lookup.findVirtual(InventoryView.class, "getInventory", MethodType.methodType(Inventory.class, int.class));
            convertSlot = lookup.findVirtual(InventoryView.class, "convertSlot", MethodType.methodType(int.class, int.class));
            getTopInventory = lookup.findVirtual(InventoryView.class, "getTopInventory", MethodType.methodType(Inventory.class));
            getBottomInventory = lookup.findVirtual(InventoryView.class, "getBottomInventory", MethodType.methodType(Inventory.class));
         } catch (IllegalAccessException | NoSuchMethodException var6) {
            Skript.exception(var6, "Failed to load old inventory view support.");
         }
      }

      GET_TITLE = getTitle;
      GET_INVENTORY = getInventory;
      CONVERT_SLOT = convertSlot;
      GET_TOP_INVENTORY = getTopInventory;
      GET_BOTTOM_INVENTORY = getBottomInventory;
   }
}
