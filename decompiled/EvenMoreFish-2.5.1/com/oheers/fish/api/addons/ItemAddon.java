package com.oheers.fish.api.addons;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class ItemAddon implements Listener, RegistryItem {
   public abstract ItemStack getItemStack(String var1);

   @Nullable
   public abstract String convertToString(@NonNull ItemStack var1);

   public abstract String getPluginName();

   public abstract String getAuthor();

   public abstract String getIdentifier();

   @NonNull
   @Override
   public String getKey() {
      return this.getIdentifier();
   }

   public String getVersion() {
      return "0.0.0";
   }

   public boolean canLoad() {
      return this.getPluginName() == null || Bukkit.getPluginManager().isPluginEnabled(this.getPluginName());
   }

   public boolean register() {
      return EMFRegistry.ITEM_ADDON.register(this);
   }

   @Override
   public final String toString() {
      return String.format("ItemAddon[prefix: %s, author: %s]", this.getIdentifier(), this.getAuthor());
   }

   protected Logger getLogger() {
      return EMFPlugin.getInstance().getLogger();
   }

   @Deprecated(
      since = "2.1.0"
   )
   public static Map<String, ItemAddon> getLoadedAddons() {
      return EMFRegistry.ITEM_ADDON.getRegistry();
   }

   @Deprecated(
      since = "2.1.0"
   )
   public static void unregisterAll() {
      EMFRegistry.ITEM_ADDON.clear();
   }

   @Deprecated(
      since = "2.1.0"
   )
   @Nullable
   public static ItemAddon get(@NonNull String prefix) {
      return EMFRegistry.ITEM_ADDON.get(prefix);
   }

   @Deprecated(
      since = "2.1.0"
   )
   @Nullable
   public static ItemStack getItem(@NonNull String prefix, @NonNull String id) {
      return EMFRegistry.ITEM_ADDON.getItem(prefix, id);
   }
}
