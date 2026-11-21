package com.oheers.fish.items.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.pairs.ReadOnlyPair;
import org.evenmorefish.fish.libs.daisylib.utils.CommonUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EnchantmentsItemConfig extends ItemConfig<Map<Enchantment, Integer>> {
   public EnchantmentsItemConfig(@NonNull Section section) {
      super(section);
   }

   public EnchantmentsItemConfig(@NonNull EnchantmentsItemConfig base) {
      super(base);
   }

   public Map<Enchantment, Integer> getConfiguredValue() {
      List<String> strings = this.section.getStringList("enchantments");
      if (strings.isEmpty()) {
         return null;
      } else {
         Map<Enchantment, Integer> enchantments = new HashMap<>();

         for (String string : strings) {
            ReadOnlyPair<Enchantment, Integer> parsed = this.parseEnchantment(string);
            enchantments.put(parsed.left(), parsed.right());
         }

         return enchantments;
      }
   }

   private ReadOnlyPair<Enchantment, Integer> parseEnchantment(@NonNull String string) {
      String[] split = string.split(",");
      String name = split[0];
      Enchantment enchantment = CommonUtils.getEnchantment(name);
      if (split.length == 1) {
         return new ReadOnlyPair<>(enchantment, 1);
      } else {
         Integer level = CommonUtils.getIntOrDefault(split[1], 1);
         return new ReadOnlyPair<>(enchantment, level);
      }
   }

   @Override
   protected BiConsumer<ItemStack, Map<Enchantment, Integer>> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return ItemStack::addUnsafeEnchantments;
   }

   @NonNull
   public EnchantmentsItemConfig createCopy() {
      return new EnchantmentsItemConfig(this);
   }
}
