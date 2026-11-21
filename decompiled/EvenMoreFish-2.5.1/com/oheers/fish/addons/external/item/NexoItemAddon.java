package com.oheers.fish.addons.external.item;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import com.nexomc.nexo.items.ItemBuilder;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class NexoItemAddon extends ItemAddon {
   @Override
   public String getIdentifier() {
      return "nexo";
   }

   @Override
   public String getPluginName() {
      return "Nexo";
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
      if (!NexoItems.exists(id)) {
         this.getLogger().warning(() -> "Nexo item with id %s doesn't exist.".formatted(id));
         return null;
      } else {
         ItemBuilder item = NexoItems.itemFromId(id);
         if (item == null) {
            this.getLogger().info(() -> String.format("Could not obtain Nexo item %s", id));
            return null;
         } else {
            return item.build();
         }
      }
   }

   @Nullable
   @Override
   public String convertToString(@NonNull ItemStack item) {
      String id = NexoItems.idFromItem(item);
      return id == null ? null : "nexo:" + id;
   }

   @EventHandler
   public void onItemsLoad(NexoItemsLoadedEvent event) {
      this.getLogger().info("Detected that Nexo has finished loading all items...");
      this.getLogger().info("Reloading EMF.");
      EMFPlugin.getInstance().reload(null);
   }
}
