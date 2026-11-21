package com.oheers.fish.items.config;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class QuantityItemConfig extends ItemConfig<Integer> {
   public QuantityItemConfig(@NonNull Section section) {
      super(section);
   }

   public QuantityItemConfig(@NonNull QuantityItemConfig base) {
      super(base);
   }

   @NonNull
   public Integer getConfiguredValue() {
      return this.section.getInt("quantity", 1);
   }

   @Override
   protected BiConsumer<ItemStack, Integer> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return ItemStack::setAmount;
   }

   @NonNull
   public QuantityItemConfig createCopy() {
      return new QuantityItemConfig(this);
   }
}
