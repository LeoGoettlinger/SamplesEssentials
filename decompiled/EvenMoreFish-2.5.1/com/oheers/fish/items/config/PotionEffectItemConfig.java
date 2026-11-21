package com.oheers.fish.items.config;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.evenmorefish.fish.libs.daisylib.config.serializer.PotionEffectSerializer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PotionEffectItemConfig extends ItemConfig<PotionEffect> {
   public PotionEffectItemConfig(@NonNull Section section) {
      super(section);
   }

   public PotionEffectItemConfig(@NonNull PotionEffectItemConfig base) {
      super(base);
   }

   public PotionEffect getConfiguredValue() {
      String potionSettings = this.section.getString("potion", this.section.getString("potion-effect"));
      return potionSettings == null ? null : PotionEffectSerializer.get().deserialize(potionSettings);
   }

   @Override
   public BiConsumer<ItemStack, PotionEffect> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> {
         if (value == null) {
            DaisyLib.get().getLogging().warn(this.section.getName() + " has an invalid potion effect.");
         } else {
            item.editMeta(PotionMeta.class, meta -> meta.addCustomEffect(value, true));
         }
      };
   }

   @NonNull
   public PotionEffectItemConfig createCopy() {
      return new PotionEffectItemConfig(this);
   }
}
