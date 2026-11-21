package com.magmaguy.elitemobs.magmacore.util;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemStackGenerator {
   private ItemStackGenerator() {
   }

   public static ItemStack generateSkullItemStack(String username, String name, List<String> lore) {
      ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta skullMeta = (SkullMeta)itemStack.getItemMeta();
      skullMeta.setDisplayName(ChatColorConverter.convert(name));
      skullMeta.setLore(ChatColorConverter.convert(lore));
      itemStack.setItemMeta(skullMeta);
      return itemStack;
   }

   public static ItemStack generateItemStack(ItemStack itemStack, String name, List<String> lore) {
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColorConverter.convert(name));
      itemMeta.setLore(ChatColorConverter.convert(lore));
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public static ItemStack generateItemStack(Material material, String name, List<String> lore, int customModelID) {
      ItemStack itemStack = generateItemStack(material, ChatColorConverter.convert(name));
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setLore(ChatColorConverter.convert(lore));
      if (customModelID > 0) {
         itemMeta.setCustomModelData(customModelID);
      }

      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public static ItemStack generateItemStack(Material material, String name, List<String> lore, String namespacedKey) {
      ItemStack itemStack = generateItemStack(material, ChatColorConverter.convert(name));
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setLore(ChatColorConverter.convert(lore));
      if (!VersionChecker.serverVersionOlderThan(21, 4) && namespacedKey != null) {
         try {
            itemMeta.setItemModel(NamespacedKey.fromString(namespacedKey));
         } catch (Exception var7) {
            var7.printStackTrace();
            System.out.println("Failed to set item model for " + namespacedKey);
         }
      }

      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public static ItemStack generateItemStack(Material material, String name, List<String> lore) {
      ItemStack itemStack = generateItemStack(material, ChatColorConverter.convert(name));
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setLore(ChatColorConverter.convert(lore));
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public static ItemStack generateItemStack(Material material, String name) {
      ItemStack itemStack = generateItemStack(material);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(ChatColorConverter.convert(name));
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public static ItemStack generateItemStack(Material material) {
      if (material == null) {
         material = Material.AIR;
      }

      ItemStack itemStack = new ItemStack(material);
      if (material.equals(Material.AIR)) {
         return itemStack;
      } else {
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName("");
         itemStack.setItemMeta(itemMeta);
         return itemStack;
      }
   }

   public static ItemStack generateFlaglessItemStack(Material material, String name, List<String> loreList) {
      ItemStack itemStack = generateItemStack(material, name, loreList);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }
}
