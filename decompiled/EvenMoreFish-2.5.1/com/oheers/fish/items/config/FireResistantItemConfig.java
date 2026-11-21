package com.oheers.fish.items.config;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FireResistantItemConfig extends ItemConfig<Boolean> {
   public FireResistantItemConfig(@NonNull Section section) {
      super(section);
   }

   public FireResistantItemConfig(@NonNull FireResistantItemConfig base) {
      super(base);
   }

   public Boolean getConfiguredValue() {
      return this.section.getBoolean("fire-resistant", false);
   }

   @Override
   protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> item.editMeta(meta -> meta.setFireResistant(value));
   }

   @NonNull
   public FireResistantItemConfig createCopy() {
      return new FireResistantItemConfig(this);
   }
}
