package com.oheers.fish.addons.external.item;

import com.oheers.fish.api.addons.ItemAddon;
import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItems;
import com.willfp.ecoitems.items.ItemUtilsKt;
import java.util.Locale;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EcoItemsItemAddon extends ItemAddon {
   @Override
   public String getPluginName() {
      return "EcoItems";
   }

   @Override
   public String getAuthor() {
      return "FireML";
   }

   @Override
   public String getIdentifier() {
      return "ecoitems";
   }

   @Override
   public String getVersion() {
      return "1.0.0";
   }

   @Override
   public ItemStack getItemStack(String id) {
      EcoItem item = EcoItems.INSTANCE.getByID(id);
      if (item == null) {
         this.getLogger().info(() -> String.format("Could not obtain EcoItems item %s", id));
         return null;
      } else {
         return item.getItemStack();
      }
   }

   @Nullable
   @Override
   public String convertToString(@NonNull ItemStack item) {
      EcoItem ecoItem = ItemUtilsKt.getEcoItem(item);
      return ecoItem == null ? null : "ecoitems:" + ecoItem.getID().toLowerCase(Locale.ROOT);
   }
}
