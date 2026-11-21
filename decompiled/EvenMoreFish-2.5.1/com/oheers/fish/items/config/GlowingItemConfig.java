package com.oheers.fish.items.config;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GlowingItemConfig extends ItemConfig<Boolean> {
   public GlowingItemConfig(@NonNull Section section) {
      super(section);
   }

   public GlowingItemConfig(@NonNull GlowingItemConfig base) {
      super(base);
   }

   @NonNull
   public Boolean getConfiguredValue() {
      return this.section.getBoolean("glowing", false);
   }

   @Override
   protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> item.editMeta(meta -> meta.setEnchantmentGlintOverride(value));
   }

   @NonNull
   public GlowingItemConfig createCopy() {
      return new GlowingItemConfig(this);
   }
}
