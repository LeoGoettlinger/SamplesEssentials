package com.oheers.fish.addons.external.item;

import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class OraxenItemAddon extends ItemAddon {
   @Override
   public String getIdentifier() {
      return "oraxen";
   }

   @Override
   public String getPluginName() {
      return "Oraxen";
   }

   @Override
   public String getAuthor() {
      return "FireML";
   }

   @Override
   public String getVersion() {
      return "1.0.0";
   }

   @Override
   public ItemStack getItemStack(String id) {
      if (!OraxenItems.exists(id)) {
         this.getLogger().warning(() -> "Oraxen item with id %s doesn't exist.".formatted(id));
         return null;
      } else {
         ItemBuilder item = OraxenItems.getItemById(id);
         if (item == null) {
            this.getLogger().info(() -> "Could not obtain Oraxen item %s".formatted(id));
            return null;
         } else {
            return item.build();
         }
      }
   }

   @Nullable
   @Override
   public String convertToString(@NonNull ItemStack item) {
      String id = OraxenItems.getIdByItem(item);
      return id == null ? null : "oraxen:" + id;
   }

   @EventHandler
   public void onItemsLoad(OraxenItemsLoadedEvent event) {
      this.getLogger().info("Detected that Oraxen has finished loading all items...");
      this.getLogger().info("Reloading EMF.");
      EMFPlugin.getInstance().reload(null);
   }
}
