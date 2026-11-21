package com.sk89q.worldedit.world.registry;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.item.ItemType;
import javax.annotation.Nullable;

public class BundledItemRegistry implements ItemRegistry {
   private BundledItemData.ItemEntry getEntryById(ItemType itemType) {
      return BundledItemData.getInstance().findById(itemType.id());
   }

   @Override
   public Component getRichName(ItemType itemType) {
      BundledItemData.ItemEntry itemEntry = this.getEntryById(itemType);
      return (Component)(itemEntry != null && !itemEntry.localizedName.equals("Air")
         ? TextComponent.of(itemEntry.localizedName)
         : Caption.of(TranslationManager.makeTranslationKey("item", itemType.id())));
   }

   @Nullable
   @Deprecated
   @Override
   public String getName(ItemType itemType) {
      BundledItemData.ItemEntry itemEntry = this.getEntryById(itemType);
      if (itemEntry != null) {
         String localized = itemEntry.localizedName;
         if (localized.equals("Air")) {
            String id = itemType.id();
            int c = id.indexOf(58);
            return c < 0 ? id : id.substring(c + 1);
         } else {
            return localized;
         }
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public ItemMaterial getMaterial(ItemType itemType) {
      return new PassthroughItemMaterial(BundledItemData.getInstance().getMaterialById(itemType.id()));
   }
}
