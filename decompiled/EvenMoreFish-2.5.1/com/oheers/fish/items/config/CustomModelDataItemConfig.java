package com.oheers.fish.items.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.evenmorefish.fish.libs.daisylib.utils.CommonUtils;
import org.evenmorefish.fish.libs.daisylib.utils.ReflectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CustomModelDataItemConfig extends ItemConfig<Number> {
   private static final Method MODERN_GETTER_METHOD;
   private static final Method SET_FLOATS;
   private static final Method MODERN_SETTER_METHOD;

   public CustomModelDataItemConfig(@NonNull Section section) {
      super(section);
   }

   public CustomModelDataItemConfig(@NonNull CustomModelDataItemConfig base) {
      super(base);
   }

   @Nullable
   public Integer getConfiguredValue() {
      String str = this.section.getString("custom-model-data");
      return str == null ? null : CommonUtils.getInt(str);
   }

   @Override
   protected BiConsumer<ItemStack, Number> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> item.editMeta(meta -> {
         if (MODERN_GETTER_METHOD != null) {
            this.applyModern(meta, value);
         } else {
            this.applyLegacy(meta, value.intValue());
         }
      });
   }

   private void applyModern(@NonNull ItemMeta meta, @NonNull Number value) {
      try {
         Object component = MODERN_GETTER_METHOD.invoke(meta);
         SET_FLOATS.invoke(component, List.of(value.floatValue()));
         MODERN_SETTER_METHOD.invoke(meta, component);
      } catch (IllegalAccessException | InvocationTargetException var4) {
         DaisyLib.get().getLogging().warn("Failed to apply modern CustomModelData.", var4);
         this.applyLegacy(meta, value.intValue());
      }
   }

   private void applyLegacy(@NonNull ItemMeta meta, int value) {
      meta.setCustomModelData(value);
   }

   @NonNull
   public CustomModelDataItemConfig createCopy() {
      return new CustomModelDataItemConfig(this);
   }

   static {
      Class<?> clazz = ReflectionUtils.getClassOrNull("org.bukkit.inventory.meta.components.CustomModelDataComponent");
      Method getter = null;
      Method setFloats = null;
      Method setter = null;
      if (clazz != null) {
         getter = ReflectionUtils.getMethodOrNull(ItemMeta.class, "getCustomModelDataComponent");
         setFloats = ReflectionUtils.getMethodOrNull(clazz, "setFloats", List.class);
         setter = ReflectionUtils.getMethodOrNull(ItemMeta.class, "setCustomModelDataComponent", clazz);
      }

      MODERN_GETTER_METHOD = getter;
      SET_FLOATS = setFloats;
      MODERN_SETTER_METHOD = setter;
   }
}
