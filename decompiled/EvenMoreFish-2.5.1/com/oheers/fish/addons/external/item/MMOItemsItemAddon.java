package com.oheers.fish.addons.external.item;

import com.oheers.fish.api.addons.ItemAddon;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MMOItemsItemAddon extends ItemAddon {
   @Override
   public ItemStack getItemStack(String id) {
      String[] splitMaterialValue = id.split(":");
      if (splitMaterialValue.length != 2) {
         this.getLogger().severe(() -> String.format("Incorrect format for MMOItemsItemAddon, use %s:type:id. Got %s", this.getIdentifier(), id));
         return null;
      } else {
         MMOItems plugin = MMOItems.plugin;
         Type type = plugin.getTypes().get(splitMaterialValue[0]);
         if (type == null) {
            this.getLogger().info(() -> String.format("Could not obtain MMOItems item %s", id));
            return null;
         } else {
            MMOItem item = plugin.getMMOItem(type, splitMaterialValue[1]);
            if (item == null) {
               this.getLogger().info(() -> String.format("Could not obtain MMOItems item %s", id));
               return null;
            } else {
               return item.newBuilder().build();
            }
         }
      }
   }

   @Nullable
   @Override
   public String convertToString(@NonNull ItemStack item) {
      NBTItem nbtItem = NBTItem.get(item);
      String type = nbtItem.getType();
      if (type == null) {
         return null;
      } else {
         String id = nbtItem.getString("MMOITEMS_ITEM_ID");
         return id == null ? null : "mmoitems:" + type + ":" + id;
      }
   }

   @Override
   public String getPluginName() {
      return "MMOItems";
   }

   @Override
   public String getAuthor() {
      return "FireML";
   }

   @Override
   public String getIdentifier() {
      return "MMOITEMS";
   }
}
