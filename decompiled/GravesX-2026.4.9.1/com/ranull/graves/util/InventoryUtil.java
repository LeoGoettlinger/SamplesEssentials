package com.ranull.graves.util;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import de.tr7zw.nbtapi.NBTItem;
import dev.cwhead.GravesX.provider.CustomItemStorageProvider;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class InventoryUtil {
   private InventoryUtil() {
   }

   public static int getInventorySize(int size) {
      if (size <= 9) {
         return 9;
      } else if (size <= 18) {
         return 18;
      } else if (size <= 27) {
         return 27;
      } else if (size <= 36) {
         return 36;
      } else {
         return size <= 45 ? 45 : 54;
      }
   }

   public static void equipArmor(Inventory inventory, Player player) {
      List<ItemStack> itemList = Arrays.asList(inventory.getContents());
      Collections.reverse(itemList);

      for (ItemStack itemStack : itemList) {
         if (itemStack != null) {
            if (player.getInventory().getHelmet() == null && isHelmet(itemStack)) {
               player.getInventory().setHelmet(itemStack);
               playArmorEquipSound(player, itemStack);
               inventory.removeItem(new ItemStack[]{itemStack});
            } else if (player.getInventory().getChestplate() == null && isChestplate(itemStack)) {
               player.getInventory().setChestplate(itemStack);
               playArmorEquipSound(player, itemStack);
               inventory.removeItem(new ItemStack[]{itemStack});
            } else if (player.getInventory().getLeggings() == null && isLeggings(itemStack)) {
               player.getInventory().setLeggings(itemStack);
               playArmorEquipSound(player, itemStack);
               inventory.removeItem(new ItemStack[]{itemStack});
            } else if (player.getInventory().getBoots() == null && isBoots(itemStack)) {
               player.getInventory().setBoots(itemStack);
               playArmorEquipSound(player, itemStack);
               inventory.removeItem(new ItemStack[]{itemStack});
            }
         }
      }
   }

   public static void equipItems(Inventory inventory, Player player) {
      List<ItemStack> itemStackList = new ArrayList<>();

      for (ItemStack itemStack : (ItemStack[])inventory.getContents().clone()) {
         if (itemStack != null && !MaterialUtil.isAir(itemStack.getType())) {
            itemStackList.add(itemStack);
         }
      }

      inventory.clear();

      for (ItemStack itemStackx : itemStackList) {
         player.getInventory()
            .addItem(new ItemStack[]{itemStackx})
            .forEach(
               (key, value) -> inventory.addItem(new ItemStack[]{value})
                  .forEach((dropKey, dropValue) -> player.getWorld().dropItem(player.getLocation(), dropValue))
            );
      }
   }

   public static void playArmorEquipSound(Player player, ItemStack itemStack) {
      try {
         String type = itemStack.getType().name();
         if (type.startsWith("NETHERITE")) {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0F, 1.0F);
         } else if (type.startsWith("DIAMOND")) {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1.0F, 1.0F);
         } else if (type.startsWith("GOLD")) {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 1.0F, 1.0F);
         } else if (type.startsWith("COPPER")) {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_COPPER, 1.0F, 1.0F);
         } else if (type.startsWith("CHAIN")) {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1.0F, 1.0F);
         } else if (type.startsWith("IRON")) {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1.0F, 1.0F);
         } else if (type.startsWith("LEATHER")) {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
         } else if (type.startsWith("ELYTRA")) {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_ELYTRA, 1.0F, 1.0F);
         } else if (type.startsWith("TURTLE")) {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1.0F, 1.0F);
         } else {
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
         }
      } catch (NoSuchFieldError var3) {
      }
   }

   public static boolean isArmor(ItemStack itemStack) {
      return isHelmet(itemStack) || isChestplate(itemStack) || isLeggings(itemStack) || isBoots(itemStack);
   }

   public static boolean isHelmet(ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else {
         String type = itemStack.getType().name().toUpperCase(Locale.ROOT);
         return type.contains("HELMET")
            || type.equals("CARVED_PUMPKIN")
            || type.equals("PUMPKIN")
            || type.equals("PLAYER_HEAD")
            || type.equals("SKELETON_SKULL")
            || type.equals("WITHER_SKELETON_SKULL")
            || type.equals("ZOMBIE_HEAD")
            || type.equals("CREEPER_HEAD")
            || type.equals("DRAGON_HEAD")
            || type.equals("PIGLIN_HEAD");
      }
   }

   public static boolean isChestplate(ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else {
         String type = itemStack.getType().name().toUpperCase(Locale.ROOT);
         return type.contains("CHESTPLATE") || type.equals("ELYTRA");
      }
   }

   public static boolean isLeggings(ItemStack itemStack) {
      return itemStack == null ? false : itemStack.getType().name().toUpperCase(Locale.ROOT).contains("LEGGINGS");
   }

   public static boolean isBoots(ItemStack itemStack) {
      return itemStack == null ? false : itemStack.getType().name().toUpperCase(Locale.ROOT).contains("BOOTS");
   }

   public static String inventoryToString(Inventory inventory) {
      return inventoryToString(inventory, null);
   }

   public static String inventoryToString(Inventory inventory, Graves plugin) {
      List<String> stringList = new ArrayList<>();
      if (inventory == null) {
         return "";
      } else {
         for (ItemStack itemStack : inventory.getContents()) {
            stringList.add(itemStackToString(itemStack, plugin, inventory.getHolder()));
         }

         return String.join("|", stringList);
      }
   }

   public static String equipmentMapToString(Map<EquipmentSlot, ItemStack> equipmentMap, Graves plugin, Grave grave) {
      Map<String, String> serializedEquipmentMap = new HashMap<>();
      if (equipmentMap != null) {
         for (Entry<EquipmentSlot, ItemStack> entry : equipmentMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue().getType() != Material.AIR) {
               serializedEquipmentMap.put(entry.getKey().name(), itemStackToString(entry.getValue(), plugin, grave));
            }
         }
      }

      return Base64Util.objectToBase64(serializedEquipmentMap);
   }

   public static Map<EquipmentSlot, ItemStack> stringToEquipmentMap(String string, Graves plugin, Grave grave) {
      Map<EquipmentSlot, ItemStack> equipmentMap = new EnumMap<>(EquipmentSlot.class);
      if (string != null && !string.isBlank()) {
         try {
            if (!(Base64Util.base64ToObject(string) instanceof Map<?, ?> rawMap)) {
               return equipmentMap;
            }

            for (Entry<?, ?> entry : rawMap.entrySet()) {
               EquipmentSlot equipmentSlot = parseEquipmentSlot(entry.getKey());
               if (equipmentSlot != null && entry.getValue() != null) {
                  ItemStack itemStack = null;
                  if (entry.getValue() instanceof ItemStack legacyItemStack) {
                     itemStack = legacyItemStack;
                  } else if (entry.getValue() instanceof String itemString) {
                     itemStack = stringToItemStack(itemString, plugin, grave, -1, true);
                  }

                  if (itemStack != null && itemStack.getType() != Material.AIR) {
                     equipmentMap.put(equipmentSlot, itemStack);
                  }
               }
            }
         } catch (Exception var13) {
            Bukkit.getLogger().warning("Exception during equipment Base64 conversion: " + var13.getMessage());
            Bukkit.getLogger().warning("Stack Trace:");
            var13.printStackTrace();
         }

         return equipmentMap;
      } else {
         return equipmentMap;
      }
   }

   public static Inventory stringToInventory(InventoryHolder inventoryHolder, String string, String title, Graves plugin) {
      String[] strings = string != null ? string.split("\\|") : new String[0];
      if (strings.length > 0 && !strings[0].equals("")) {
         Inventory inventory = plugin.getServer().createInventory(inventoryHolder, getInventorySize(strings.length), title);
         int counter = 0;

         for (String itemString : strings) {
            inventory.setItem(counter, stringToItemStack(itemString, plugin, inventoryHolder, counter, false));
            counter++;
         }

         return inventory;
      } else {
         return plugin.getServer().createInventory(inventoryHolder, strings.length, title);
      }
   }

   private static String itemStackToString(ItemStack itemStack, Graves plugin, InventoryHolder inventoryHolder) {
      ItemStack serializableItemStack = normalizeItemStackForStorage(itemStack);

      try {
         String base64 = Base64Util.objectToBase64(serializableItemStack);
         InventoryUtil.CustomSerializedItem customSerializedItem = getCustomSerializedItem(itemStack, plugin, inventoryHolder);
         return customSerializedItem != null ? customSerializedItem.providerId() + ":" + encodeString(customSerializedItem.itemId()) + ":" + base64 : base64;
      } catch (Exception var6) {
         Bukkit.getLogger().warning("Exception during Base64 conversion for: " + itemStack + " - " + var6.getMessage());
         Bukkit.getLogger().severe("NBT Data: " + itemStack);
         Bukkit.getLogger()
            .warning("Removed problematic item " + itemStack + " from grave. While the grave will still generate, this is likely a Spigot/Paper bug.");
         Bukkit.getLogger().warning("Stack Trace:");
         var6.printStackTrace();
         return Base64Util.objectToBase64(new ItemStack(Material.AIR));
      }
   }

   private static ItemStack normalizeItemStackForStorage(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         Plugin nbtAPI = Bukkit.getPluginManager().getPlugin("NBTAPI");
         if (nbtAPI != null && nbtAPI.isEnabled()) {
            NBTItem nbtItem = new NBTItem(itemStack);
            return nbtItem.getItem();
         } else {
            return itemStack;
         }
      } else {
         return new ItemStack(Material.AIR);
      }
   }

   private static ItemStack stringToItemStack(String itemString, Graves plugin, InventoryHolder inventoryHolder, int slot, boolean equipment) {
      InventoryUtil.SerializedItem serializedItem = parseSerializedItem(itemString, plugin);

      try {
         Object object = Base64Util.base64ToObject(serializedItem.itemString());
         ItemStack itemStack = object instanceof ItemStack ? (ItemStack)object : new ItemStack(Material.AIR);
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            Plugin nbtAPI = Bukkit.getPluginManager().getPlugin("NBTAPI");
            if (nbtAPI != null && nbtAPI.isEnabled()) {
               NBTItem nbtItem = new NBTItem(itemStack);
               itemStack = nbtItem.getItem();
            }
         }

         return restoreCustomItem(serializedItem.providerId(), serializedItem.customItemId(), itemStack, plugin, inventoryHolder);
      } catch (Exception var10) {
         if (var10.getMessage() == null || !var10.getMessage().contains("ItemStack can't be null/air/amount of 0!")) {
            String location = equipment ? "equipment item" : "item at slot " + slot;
            Bukkit.getLogger().warning("Exception during Base64 conversion for " + location + ": " + itemString + " - " + var10.getMessage());
            Bukkit.getLogger().severe("NBT Data: " + itemString);
            Bukkit.getLogger().warning("Removed problematic " + location + ". While the grave will still generate, this is likely a Spigot/Paper bug.");
            Bukkit.getLogger().warning("Stack Trace:");
            var10.printStackTrace();
         }

         return new ItemStack(Material.AIR);
      }
   }

   private static InventoryUtil.CustomSerializedItem getCustomSerializedItem(ItemStack itemStack, Graves plugin, InventoryHolder inventoryHolder) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         for (CustomItemStorageProvider provider : getCustomItemStorageProviders(plugin)) {
            String itemId = provider.getCustomItemId(itemStack);
            if (itemId != null && !itemId.isBlank()) {
               return new InventoryUtil.CustomSerializedItem(provider.getProviderId(), itemId);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static ItemStack restoreCustomItem(
      String providerId, String customItemId, ItemStack fallbackItemStack, Graves plugin, InventoryHolder inventoryHolder
   ) {
      if (providerId != null && customItemId != null) {
         CustomItemStorageProvider provider = getCustomItemStorageProvider(plugin, providerId);
         if (provider == null) {
            return fallbackItemStack;
         } else {
            ItemStack fallback = fallbackItemStack != null ? fallbackItemStack : new ItemStack(Material.AIR);
            String currentId = provider.getCustomItemId(fallback);
            if (customItemId.equalsIgnoreCase(currentId)) {
               return fallback;
            } else {
               ItemStack rebuilt = provider.rebuildCustomItem(customItemId, Math.max(1, fallback.getAmount()));
               return rebuilt != null ? rebuilt : fallback;
            }
         }
      } else {
         return fallbackItemStack;
      }
   }

   private static List<CustomItemStorageProvider> getCustomItemStorageProviders(Graves plugin) {
      if (plugin != null && plugin.getIntegrationManager() != null) {
         List<CustomItemStorageProvider> providers = plugin.getIntegrationManager().getCustomItemStorageProviders();
         return providers != null ? providers : Collections.emptyList();
      } else {
         return Collections.emptyList();
      }
   }

   private static CustomItemStorageProvider getCustomItemStorageProvider(Graves plugin, String providerId) {
      if (providerId != null && !providerId.isBlank()) {
         for (CustomItemStorageProvider provider : getCustomItemStorageProviders(plugin)) {
            if (provider.getProviderId().equalsIgnoreCase(providerId)) {
               return provider;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static InventoryUtil.SerializedItem parseSerializedItem(String itemString, Graves plugin) {
      if (itemString == null) {
         return new InventoryUtil.SerializedItem(null, null, null);
      } else {
         for (CustomItemStorageProvider provider : getCustomItemStorageProviders(plugin)) {
            String providerId = provider.getProviderId();
            if (providerId != null && !providerId.isBlank()) {
               String prefix = providerId + ":";
               if (itemString.startsWith(prefix)) {
                  int separatorIndex = itemString.indexOf(58, prefix.length());
                  if (separatorIndex <= prefix.length()) {
                     return new InventoryUtil.SerializedItem(null, null, itemString);
                  }

                  String encodedItemId = itemString.substring(prefix.length(), separatorIndex);
                  String base64Item = itemString.substring(separatorIndex + 1);

                  try {
                     return new InventoryUtil.SerializedItem(providerId, decodeString(encodedItemId), base64Item);
                  } catch (IllegalArgumentException var10) {
                     return new InventoryUtil.SerializedItem(null, null, itemString);
                  }
               }
            }
         }

         return new InventoryUtil.SerializedItem(null, null, itemString);
      }
   }

   private static String encodeString(String string) {
      return Base64.getUrlEncoder().withoutPadding().encodeToString(string.getBytes(StandardCharsets.UTF_8));
   }

   private static String decodeString(String string) {
      return new String(Base64.getUrlDecoder().decode(string), StandardCharsets.UTF_8);
   }

   private static EquipmentSlot parseEquipmentSlot(Object object) {
      if (object instanceof EquipmentSlot equipmentSlot) {
         return equipmentSlot;
      } else {
         if (object instanceof String string) {
            try {
               return EquipmentSlot.valueOf(string);
            } catch (IllegalArgumentException var3) {
            }
         }

         return null;
      }
   }

   private record CustomSerializedItem(String providerId, String itemId) {
   }

   private record SerializedItem(String providerId, String customItemId, String itemString) {
   }
}
