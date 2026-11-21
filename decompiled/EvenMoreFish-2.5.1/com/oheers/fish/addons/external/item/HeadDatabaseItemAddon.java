package com.oheers.fish.addons.external.item;

import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HeadDatabaseItemAddon extends ItemAddon {
   private HeadDatabaseAPI api = null;

   @Override
   public String getIdentifier() {
      return "headdb";
   }

   @Override
   public String getPluginName() {
      return "HeadDatabase";
   }

   @Override
   public String getAuthor() {
      return "EvenMoreFish";
   }

   @Override
   public String getVersion() {
      return "1.0.0";
   }

   @Override
   public ItemStack getItemStack(String id) {
      if (this.api == null) {
         return null;
      } else if (!this.api.isHead(id)) {
         this.getLogger().warning(() -> String.format("No such head with the id %s", id));
         return null;
      } else {
         return this.api.getItemHead(id);
      }
   }

   @Nullable
   @Override
   public String convertToString(@NonNull ItemStack item) {
      if (this.api == null) {
         return null;
      } else {
         String id = this.api.getItemID(item);
         return id == null ? null : "headdb:" + id;
      }
   }

   @EventHandler
   public void onItemsLoad(DatabaseLoadEvent event) {
      this.getLogger().info("Detected that HeadDatabase has finished loading...");
      this.getLogger().info("Reloading EMF.");
      this.api = new HeadDatabaseAPI();
      EMFPlugin.getInstance().reload(null);
   }
}
