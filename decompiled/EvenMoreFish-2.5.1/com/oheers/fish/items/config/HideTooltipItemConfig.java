package com.oheers.fish.items.config;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HideTooltipItemConfig extends ItemConfig<Boolean> {
   public HideTooltipItemConfig(@NonNull Section section) {
      super(section);
   }

   public HideTooltipItemConfig(@NonNull HideTooltipItemConfig base) {
      super(base);
   }

   public Boolean getConfiguredValue() {
      return this.section.getBoolean("hide-tooltip", false);
   }

   @Override
   protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> item.editMeta(meta -> meta.setHideTooltip(value));
   }

   @NonNull
   public HideTooltipItemConfig createCopy() {
      return new HideTooltipItemConfig(this);
   }
}
