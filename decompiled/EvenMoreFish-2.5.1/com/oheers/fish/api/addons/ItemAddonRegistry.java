package com.oheers.fish.api.addons;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemAddonRegistry implements EMFRegistry<ItemAddon> {
   private static final ItemAddonRegistry instance = new ItemAddonRegistry();
   private final Map<String, ItemAddon> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

   private ItemAddonRegistry() {
   }

   @NonNull
   public static ItemAddonRegistry getInstance() {
      return instance;
   }

   @Override
   public boolean isEmpty() {
      return this.registry.isEmpty();
   }

   @Override
   public void clear() {
      this.registry.clear();
   }

   @NonNull
   @Override
   public Map<String, ItemAddon> getRegistry() {
      return Map.copyOf(this.registry);
   }

   @Nullable
   public ItemAddon get(@NonNull String key) {
      return this.registry.get(key);
   }

   @Nullable
   public ItemStack getItem(@NonNull String prefix, @NonNull String id) {
      ItemAddon addon = this.registry.get(prefix);
      return addon == null ? null : addon.getItemStack(id);
   }

   @NonNull
   public ItemAddon getOrDefault(@NonNull String key, @NonNull ItemAddon defaultValue) {
      return this.registry.getOrDefault(key, defaultValue);
   }

   @Override
   public boolean unregister(@NonNull String key) {
      ItemAddon value = this.registry.remove(key);
      if (value == null) {
         return false;
      } else {
         HandlerList.unregisterAll(value);
         return true;
      }
   }

   public boolean register(@NonNull ItemAddon value, boolean force) {
      if (!value.canLoad()) {
         return false;
      } else if (!force && this.registry.containsKey(value.getKey())) {
         return false;
      } else {
         this.registry.put(value.getKey(), value);
         Bukkit.getPluginManager().registerEvents(value, EMFPlugin.getInstance());
         EMFPlugin.getInstance().debug("Registered " + value.getKey() + " ItemAddon");
         return true;
      }
   }
}
