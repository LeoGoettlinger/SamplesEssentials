package com.oheers.fish.items.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.evenmorefish.fish.libs.daisylib.utils.ReflectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class TooltipStyleItemConfig extends ItemConfig<NamespacedKey> {
   private static final Method SETTER = ReflectionUtils.getMethodOrNull(ItemMeta.class, "setTooltipStyle", NamespacedKey.class);

   public TooltipStyleItemConfig(@NonNull Section section) {
      super(section);
   }

   public TooltipStyleItemConfig(@NonNull TooltipStyleItemConfig base) {
      super(base);
   }

   @Nullable
   public NamespacedKey getConfiguredValue() {
      String keyStr = this.section.getString("tooltip-style");
      return keyStr == null ? null : NamespacedKey.fromString(keyStr);
   }

   @Override
   protected BiConsumer<ItemStack, NamespacedKey> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> {
         if (SETTER != null) {
            item.editMeta(meta -> {
               try {
                  SETTER.invoke(meta, value);
               } catch (InvocationTargetException | IllegalAccessException var3) {
                  DaisyLib.get().getLogging().warn("Failed to apply tooltip-style config", var3);
               }
            });
         }
      };
   }

   @NonNull
   public TooltipStyleItemConfig createCopy() {
      return new TooltipStyleItemConfig(this);
   }
}
