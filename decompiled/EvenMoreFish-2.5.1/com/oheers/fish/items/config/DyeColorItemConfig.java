package com.oheers.fish.items.config;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DyeColorItemConfig extends ItemConfig<Color> {
   public DyeColorItemConfig(@NonNull Section section) {
      super(section);
   }

   public DyeColorItemConfig(@NonNull DyeColorItemConfig base) {
      super(base);
   }

   public Color getConfiguredValue() {
      String colorString = this.section.getString("dye-colour", this.section.getString("dye-color"));
      if (colorString == null) {
         return null;
      } else {
         try {
            java.awt.Color color = java.awt.Color.decode(colorString);
            return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
         } catch (NumberFormatException var3) {
            return null;
         }
      }
   }

   @Override
   protected BiConsumer<ItemStack, Color> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> {
         if (value != null) {
            item.editMeta(LeatherArmorMeta.class, meta -> {
               meta.setColor(value);
               meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_DYE});
            });
         }
      };
   }

   @NonNull
   public DyeColorItemConfig createCopy() {
      return new DyeColorItemConfig(this);
   }
}
