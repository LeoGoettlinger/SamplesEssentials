package de.theredend2000.advancedhunt.util;

import de.theredend2000.advancedhunt.util.itemsadder.ItemsAdderBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ItemsAdderAdapter {
   private static volatile ItemsAdderBridge bridge;

   private static ItemsAdderBridge getBridge() {
      ItemsAdderBridge local = bridge;
      if (local != null) {
         return local;
      } else {
         synchronized (ItemsAdderAdapter.class) {
            if (bridge != null) {
               return bridge;
            } else {
               ItemsAdderBridge var10000;
               try {
                  Plugin itemsAdderPlugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");
                  if (itemsAdderPlugin == null || !itemsAdderPlugin.isEnabled()) {
                     return null;
                  }

                  Class<?> impl = Class.forName(
                     "de.theredend2000.advancedhunt.util.itemsadder.ApiItemsAdderBridge", true, ItemsAdderAdapter.class.getClassLoader()
                  );
                  bridge = (ItemsAdderBridge)impl.getDeclaredConstructor().newInstance();
                  var10000 = bridge;
               } catch (Throwable var5) {
                  return null;
               }

               return var10000;
            }
         }
      }
   }

   public static boolean isEnabled() {
      return getBridge() != null;
   }

   public static boolean isItemsAdderBlock(Block block) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? false : bridge.isItemsAdderBlock(block);
   }

   public static String getCustomBlockId(Block block) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? null : bridge.getCustomBlockId(block);
   }

   public static boolean isCustomBlockItem(ItemStack item) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? false : bridge.isCustomBlockItem(item);
   }

   public static String getCustomBlockId(ItemStack item) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? null : bridge.getCustomBlockId(item);
   }

   public static boolean isCustomFurniture(ItemStack item) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? false : bridge.isCustomFurniture(item);
   }

   public static boolean isItemsAdderFurniture(Entity entity) {
      return getCustomFurnitureId(entity) != null;
   }

   public static String getCustomFurnitureId(Entity entity) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? null : bridge.getCustomFurnitureId(entity);
   }

   public static String getCustomFurnitureId(ItemStack item) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? null : bridge.getCustomFurnitureId(item);
   }

   public static ItemStack getCustomItem(String id) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? null : bridge.getCustomItem(id);
   }

   public static BlockData getCustomBlockData(String namespacedId) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? null : bridge.getCustomBlockData(namespacedId);
   }

   public static boolean isCustomFurnitureId(String namespacedId) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? false : bridge.isCustomFurnitureId(namespacedId);
   }

   public static boolean placeCustomBlock(String namespacedId, Location location) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? false : bridge.placeCustomBlock(namespacedId, location);
   }

   public static boolean removeCustomBlock(Location location) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? false : bridge.removeCustomBlock(location);
   }

   public static boolean spawnCustomFurniture(String namespacedId, Location location) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? false : bridge.spawnCustomFurniture(namespacedId, location);
   }

   public static boolean removeCustomFurniture(Location location) {
      ItemsAdderBridge bridge = getBridge();
      return bridge == null ? false : bridge.removeCustomFurniture(location);
   }
}
