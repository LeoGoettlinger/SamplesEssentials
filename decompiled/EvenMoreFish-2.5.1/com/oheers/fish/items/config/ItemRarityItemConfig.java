package com.oheers.fish.items.config;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.utils.CommonUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemRarityItemConfig extends ItemConfig<String> {
   public ItemRarityItemConfig(@NonNull Section section) {
      super(section);
   }

   public ItemRarityItemConfig(@NonNull ItemRarityItemConfig base) {
      super(base);
   }

   public String getConfiguredValue() {
      return this.section.getString("item-rarity");
   }

   @Override
   protected BiConsumer<ItemStack, String> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> {
         ItemRarity rarity = CommonUtils.getEnumValue(ItemRarity.class, value);
         if (rarity != null) {
            item.editMeta(meta -> meta.setRarity(rarity));
         }
      };
   }

   @NonNull
   public ItemRarityItemConfig createCopy() {
      return new ItemRarityItemConfig(this);
   }
}
