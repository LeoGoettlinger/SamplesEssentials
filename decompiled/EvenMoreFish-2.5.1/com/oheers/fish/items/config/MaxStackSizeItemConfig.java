package com.oheers.fish.items.config;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.utils.CommonUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MaxStackSizeItemConfig extends ItemConfig<Integer> {
   public MaxStackSizeItemConfig(@NonNull Section section) {
      super(section);
   }

   public MaxStackSizeItemConfig(@NonNull MaxStackSizeItemConfig base) {
      super(base);
   }

   @Nullable
   public Integer getConfiguredValue() {
      String str = this.section.getString("max-stack-size");
      return str == null ? null : CommonUtils.getInt(str);
   }

   @Override
   protected BiConsumer<ItemStack, Integer> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> {
         if (value != null) {
            int finalValue = Math.clamp((long)value.intValue(), 1, 99);
            item.editMeta(meta -> meta.setMaxStackSize(finalValue));
         }
      };
   }

   @NonNull
   public MaxStackSizeItemConfig createCopy() {
      return new MaxStackSizeItemConfig(this);
   }
}
