package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;

public class CustomModelAdder {
   public static ItemStack addCustomModel(ItemStack itemStack, String customModelData) {
      if (itemStack != null && itemStack.getItemMeta() != null && customModelData != null && !customModelData.isEmpty()) {
         itemStack.setItemMeta(addCustomModel(itemStack.getItemMeta(), customModelData));
         return itemStack;
      } else {
         return itemStack;
      }
   }

   public static ItemMeta addCustomModel(ItemMeta itemMeta, String customModelData) {
      if (!CustomModelsConfig.useModels) {
         return itemMeta;
      } else if (!DefaultConfig.useResourcePackModels()) {
         return itemMeta;
      } else if (!VersionChecker.serverVersionOlderThan(21, 4) && itemMeta != null) {
         itemMeta.setItemModel(NamespacedKey.fromString(customModelData));
         return itemMeta;
      } else {
         return itemMeta;
      }
   }

   public static ItemStack addEquippableModel(ItemStack itemStack, String equipmentModelId) {
      if (itemStack == null || itemStack.getItemMeta() == null) {
         return itemStack;
      } else if (!CustomModelsConfig.useModels) {
         return itemStack;
      } else if (!DefaultConfig.useResourcePackModels()) {
         return itemStack;
      } else if (VersionChecker.serverVersionOlderThan(21, 4)) {
         return itemStack;
      } else {
         EquipmentSlot slot = getArmorSlot(itemStack.getType());
         if (slot == null) {
            return itemStack;
         } else {
            ItemMeta itemMeta = itemStack.getItemMeta();
            EquippableComponent equippable = itemMeta.getEquippable();
            equippable.setSlot(slot);
            if (slot != EquipmentSlot.HEAD && equipmentModelId != null && !equipmentModelId.isEmpty()) {
               equippable.setModel(NamespacedKey.fromString(equipmentModelId));
            }

            itemMeta.setEquippable(equippable);
            itemStack.setItemMeta(itemMeta);
            return itemStack;
         }
      }
   }

   @Deprecated
   public static ItemMeta addEquippableModel(ItemMeta itemMeta, String equipmentModelId) {
      if (!CustomModelsConfig.useModels) {
         return itemMeta;
      } else if (!DefaultConfig.useResourcePackModels()) {
         return itemMeta;
      } else if (!VersionChecker.serverVersionOlderThan(21, 4) && itemMeta != null) {
         EquippableComponent equippable = itemMeta.getEquippable();
         equippable.setModel(NamespacedKey.fromString(equipmentModelId));
         itemMeta.setEquippable(equippable);
         return itemMeta;
      } else {
         return itemMeta;
      }
   }

   private static EquipmentSlot getArmorSlot(Material material) {
      String name = material.name();
      if (name.endsWith("_HELMET") || name.equals("CARVED_PUMPKIN") || name.endsWith("_HEAD") || name.endsWith("_SKULL")) {
         return EquipmentSlot.HEAD;
      } else if (name.endsWith("_CHESTPLATE") || name.equals("ELYTRA")) {
         return EquipmentSlot.CHEST;
      } else if (name.endsWith("_LEGGINGS")) {
         return EquipmentSlot.LEGS;
      } else {
         return name.endsWith("_BOOTS") ? EquipmentSlot.FEET : null;
      }
   }
}
