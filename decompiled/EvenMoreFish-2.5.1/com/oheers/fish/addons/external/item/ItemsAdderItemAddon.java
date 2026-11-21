package com.oheers.fish.addons.external.item;

import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemsAdderItemAddon extends ItemAddon {
   @Override
   public String getIdentifier() {
      return "itemsadder";
   }

   @Override
   public String getPluginName() {
      return "ItemsAdder";
   }

   @Override
   public String getAuthor() {
      return "sarhatabaot";
   }

   @Override
   public String getVersion() {
      return "1.0.1";
   }

   @Override
   public ItemStack getItemStack(@NonNull String id) {
      String[] splitMaterialValue = id.split(":");
      if (!this.verifyItemsFormat(splitMaterialValue)) {
         this.getLogger().severe(() -> String.format("Incorrect format for ItemsAdderItemAddon, use %s:namespace:id. Got %s", this.getIdentifier(), id));
         return null;
      } else {
         String namespaceId = splitMaterialValue[0] + ":" + splitMaterialValue[1];
         CustomStack customStack = CustomStack.getInstance(namespaceId);
         if (customStack == null) {
            this.getLogger().info(() -> String.format("Could not obtain itemsadder item %s", namespaceId));
            return null;
         } else {
            return CustomStack.getInstance(namespaceId).getItemStack();
         }
      }
   }

   @Nullable
   @Override
   public String convertToString(@NonNull ItemStack item) {
      CustomStack customStack = CustomStack.byItemStack(item);
      return customStack == null ? null : "itemsadder:" + customStack.getNamespacedID();
   }

   @EventHandler
   public void onItemsLoad(ItemsAdderLoadDataEvent event) {
      this.getLogger().info("Detected that ItemsAdder has finished loading all items...");
      this.getLogger().info("Reloading EMF.");
      EMFPlugin.getInstance().reload(null);
   }

   public boolean verifyItemsFormat(final String[] splitMaterialValue) {
      return splitMaterialValue.length == 2;
   }
}
