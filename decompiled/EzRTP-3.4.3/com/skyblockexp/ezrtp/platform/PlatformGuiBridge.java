package com.skyblockexp.ezrtp.platform;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface PlatformGuiBridge {
   Inventory createInventory(InventoryHolder var1, int var2, Component var3);

   void setDisplayName(ItemMeta var1, Component var2);

   void setLore(ItemMeta var1, List<Component> var2);

   void applyItemMeta(ItemStack var1, ItemMeta var2);
}
