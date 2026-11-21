package com.oheers.fish.items.config;

import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DurabilityItemConfig extends ItemConfig<Integer> {
   @NonNull
   public static Supplier<Boolean> FALLBACK_TO_RANDOM_DURABILITY = () -> false;
   private static final Random RANDOM = new Random();

   public DurabilityItemConfig(@NonNull Section section) {
      super(section);
   }

   public DurabilityItemConfig(@NonNull DurabilityItemConfig base) {
      super(base);
   }

   @NonNull
   public Integer getConfiguredValue() {
      return this.section.getInt("durability");
   }

   @Override
   protected BiConsumer<ItemStack, Integer> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> {
         int maxDurability = item.getType().getMaxDurability();
         item.editMeta(Damageable.class, meta -> {
            if (value >= 0 && value <= 100) {
               int finalDurability = value / 100 * maxDurability;
               meta.setDamage(finalDurability);
            } else if (FALLBACK_TO_RANDOM_DURABILITY.get()) {
               meta.setDamage(RANDOM.nextInt() * (maxDurability + 1));
            }
         });
      };
   }

   @NonNull
   public DurabilityItemConfig createCopy() {
      return new DurabilityItemConfig(this);
   }
}
