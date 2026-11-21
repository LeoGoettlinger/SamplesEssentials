package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.SoulbindConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EliteScroll {
   public static final NamespacedKey eliteScrollNamespacedKey = new NamespacedKey(MetadataHandler.PLUGIN, "elite_scroll");
   public static Material scrollMaterial = Material.PAPER;

   private EliteScroll() {
   }

   public static ItemStack generateScroll(int scrollLevel, Player player) {
      ItemStack itemStack = ItemStackGenerator.generateItemStack(scrollMaterial, ItemSettingsConfig.getEliteItemScrollName());
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta == null) {
         return itemStack;
      } else {
         itemMeta.getPersistentDataContainer().set(eliteScrollNamespacedKey, PersistentDataType.INTEGER, scrollLevel);
         CustomModelAdder.addCustomModel(itemMeta, ItemSettingsConfig.getEliteItemModel());
         itemStack.setItemMeta(itemMeta);
         ItemTagger.registerEliteItem(itemStack);
         SoulbindEnchantment.addEnchantment(itemStack, player);
         itemMeta = itemStack.getItemMeta();
         List<String> lore = new ArrayList<>();

         for (String line : ItemSettingsConfig.getEliteItemScrollLore()) {
            lore.add(ChatColorConverter.convert(line.replace("$level", String.valueOf(scrollLevel))));
         }

         if (player != null && SoulbindEnchantment.itemHasSoulbindEnchantment(itemMeta)) {
            lore.add(
               ChatColorConverter.convert(SoulbindConfig.loreStrings.replace("$player", player.getDisplayName()).replace("$playerName", player.getName()))
            );
         }

         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         return itemStack;
      }
   }

   public static boolean isEliteScroll(ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else {
         return !itemStack.hasItemMeta()
            ? false
            : itemStack.getItemMeta().getPersistentDataContainer().has(eliteScrollNamespacedKey, PersistentDataType.INTEGER);
      }
   }

   public static int getEliteScrollLevel(ItemStack itemStack) {
      return !isEliteScroll(itemStack)
         ? 0
         : (Integer)itemStack.getItemMeta().getPersistentDataContainer().get(eliteScrollNamespacedKey, PersistentDataType.INTEGER);
   }

   public static boolean canUseScroll(Player player, ItemStack scrollItemStack) {
      if (player != null && isEliteScroll(scrollItemStack) && scrollItemStack.getAmount() > 0) {
         PersistentDataContainer data = scrollItemStack.getItemMeta().getPersistentDataContainer();
         return !data.has(SoulbindEnchantment.SOULBIND_KEY)
            ? true
            : data.has(SoulbindEnchantment.SOULBIND_KEY, PersistentDataType.STRING)
               && player.getUniqueId().toString().equals(data.get(SoulbindEnchantment.SOULBIND_KEY, PersistentDataType.STRING));
      } else {
         return false;
      }
   }

   public static ItemStack convertVanillaItem(ItemStack originalItem, ItemStack scrollItemStack, Player player) {
      if (canUseScroll(player, scrollItemStack)
         && originalItem != null
         && !originalItem.getType().isAir()
         && originalItem.getAmount() > 0
         && !EliteItemManager.isEliteMobsItem(originalItem)) {
         ItemStack newItem = originalItem.clone();
         int level = getEliteScrollLevel(scrollItemStack);
         EliteItemManager.setEliteLevel(newItem, level);
         ItemMeta itemMeta = newItem.getItemMeta();
         if (itemMeta == null) {
            return newItem;
         } else {
            if (ItemSettingsConfig.isHideEnchants()) {
               itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            }

            HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>(itemMeta.getEnchants());
            ItemTagger.registerEnchantments(itemMeta, enchantmentMap);
            ItemTagger.registerItemSource(null, itemMeta);
            newItem.setItemMeta(itemMeta);
            new EliteItemLore(newItem, false);
            return newItem;
         }
      } else {
         return null;
      }
   }
}
