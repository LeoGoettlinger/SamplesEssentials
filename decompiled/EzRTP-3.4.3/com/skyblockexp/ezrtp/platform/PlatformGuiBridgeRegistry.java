package com.skyblockexp.ezrtp.platform;

import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.compat.ItemMetaCompat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public final class PlatformGuiBridgeRegistry {
   private static final PlatformGuiBridge DEFAULT_BRIDGE = new PlatformGuiBridgeRegistry.DefaultPlatformGuiBridge();
   private static final AtomicReference<PlatformGuiBridge> BRIDGE = new AtomicReference<>(DEFAULT_BRIDGE);
   private static final List<PlatformGuiBridgeProvider> PROVIDERS = new CopyOnWriteArrayList<>();

   private PlatformGuiBridgeRegistry() {
   }

   public static void register(PlatformGuiBridge bridge) {
      BRIDGE.set(bridge != null ? bridge : DEFAULT_BRIDGE);
   }

   public static void unregister() {
      BRIDGE.set(DEFAULT_BRIDGE);
   }

   public static PlatformGuiBridge get() {
      PlatformGuiBridge bridge = BRIDGE.get();
      return bridge != null ? bridge : DEFAULT_BRIDGE;
   }

   public static void registerProvider(PlatformGuiBridgeProvider provider) {
      if (provider != null) {
         if (!hasProviderClass(provider.getClass())) {
            PROVIDERS.add(provider);
         }
      }
   }

   static boolean hasProviderClass(Class<?> providerClass) {
      if (providerClass == null) {
         return false;
      } else {
         for (PlatformGuiBridgeProvider provider : PROVIDERS) {
            if (provider.getClass().equals(providerClass)) {
               return true;
            }
         }

         return false;
      }
   }

   static int providerCount() {
      return PROVIDERS.size();
   }

   public static void clearProviders() {
      PROVIDERS.clear();
   }

   public static boolean loadAndRegister(Plugin plugin, Logger logger) {
      PlatformGuiBridgeProvider selected = null;

      for (PlatformGuiBridgeProvider provider : PROVIDERS) {
         if (provider.supports(plugin) && (selected == null || provider.priority() > selected.priority())) {
            selected = provider;
         }
      }

      if (selected == null) {
         register(DEFAULT_BRIDGE);
         return false;
      } else {
         PlatformGuiBridge bridge = selected.create(plugin);
         if (bridge == null) {
            register(DEFAULT_BRIDGE);
            return false;
         } else {
            register(bridge);
            if (logger != null) {
               logger.fine("Registered PlatformGuiBridge provider: " + selected.getClass().getName());
            }

            return true;
         }
      }
   }

   private static final class DefaultPlatformGuiBridge implements PlatformGuiBridge {
      @Override
      public Inventory createInventory(InventoryHolder holder, int size, Component title) {
         try {
            return Bukkit.createInventory(holder, size, title);
         } catch (NoClassDefFoundError | ClassCastException | NoSuchMethodError var6) {
            String legacyTitle = MessageUtil.componentToLegacy(title);
            return Bukkit.createInventory(holder, size, this.trimTitle(legacyTitle));
         }
      }

      @Override
      public void setDisplayName(ItemMeta meta, Component displayName) {
         if (meta != null && displayName != null) {
            ItemMetaCompat.setDisplayName(meta, displayName);
         }
      }

      @Override
      public void setLore(ItemMeta meta, List<Component> lore) {
         if (meta != null) {
            List<Component> safeLore = lore != null ? lore : List.of();
            ItemMetaCompat.setLore(meta, safeLore);
         }
      }

      @Override
      public void applyItemMeta(ItemStack icon, ItemMeta meta) {
         if (icon != null && meta != null) {
            icon.setItemMeta(meta);
         }
      }

      private String trimTitle(String title) {
         if (title == null) {
            return "";
         } else {
            return title.length() > 32 ? title.substring(0, 32) : title;
         }
      }
   }
}
