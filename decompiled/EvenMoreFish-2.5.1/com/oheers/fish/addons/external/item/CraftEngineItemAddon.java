package com.oheers.fish.addons.external.item;

import com.oheers.fish.api.addons.ItemAddon;
import java.util.Locale;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CraftEngineItemAddon extends ItemAddon {
   @Override
   public String getIdentifier() {
      return "craftengine";
   }

   @Override
   public String getPluginName() {
      return "CraftEngine";
   }

   @Override
   public String getVersion() {
      return "2.0.0";
   }

   @Override
   public String getAuthor() {
      return "FireML";
   }

   @Override
   public ItemStack getItemStack(String id) {
      BukkitItemDefinition itemDef = CraftEngineItems.byId(id);
      if (itemDef == null) {
         this.getLogger().warning(() -> "CraftEngine item with id %s doesn't exist.".formatted(id));
         return null;
      } else {
         ItemStack item = itemDef.buildBukkitItem();
         if (item == null) {
            this.getLogger().info(() -> String.format("Could not obtain CraftEngine item %s", id));
            return null;
         } else {
            return item;
         }
      }
   }

   @Nullable
   @Override
   public String convertToString(@NonNull ItemStack item) {
      BukkitItemDefinition itemDef = CraftEngineItems.byItemStack(item);
      return itemDef == null ? null : "craftengine:" + itemDef.id().asString().toLowerCase(Locale.ROOT);
   }
}
