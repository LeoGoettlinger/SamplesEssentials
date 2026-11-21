package com.skyblockexp.ezrtp.platform;

import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class BukkitPlatformGuiBridge implements PlatformGuiBridge {
   private static final int MAX_TITLE_LENGTH = 32;

   @Override
   public Inventory createInventory(InventoryHolder holder, int size, Component title) {
      String legacyTitle = MessageUtil.componentToLegacy(title);
      String safeTitle = legacyTitle == null ? "" : legacyTitle;
      if (safeTitle.length() > 32) {
         safeTitle = safeTitle.substring(0, 32);
      }

      return Bukkit.createInventory(holder, size, safeTitle);
   }

   @Override
   public void setDisplayName(ItemMeta meta, Component displayName) {
      if (meta != null && displayName != null) {
         String legacy = MessageUtil.componentToLegacy(displayName);
         meta.setDisplayName(legacy == null ? "" : legacy);
      }
   }

   @Override
   public void setLore(ItemMeta meta, List<Component> lore) {
      if (meta != null) {
         List<Component> safeLore = lore != null ? lore : List.of();
         List<String> legacy = new ArrayList<>(safeLore.size());

         for (Component component : safeLore) {
            legacy.add(MessageUtil.componentToLegacy(component));
         }

         meta.setLore(legacy);
      }
   }

   @Override
   public void applyItemMeta(ItemStack icon, ItemMeta meta) {
      if (icon != null && meta != null) {
         icon.setItemMeta(meta);
      }
   }
}
