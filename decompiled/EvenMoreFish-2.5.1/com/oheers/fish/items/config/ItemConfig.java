package com.oheers.fish.items.config;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public abstract class ItemConfig<T> {
   protected T def;
   protected Function<T, T> transformer;
   protected final Section section;
   protected boolean enabled = true;

   public ItemConfig(@NonNull Section section) {
      this.section = section;
   }

   protected ItemConfig(@NonNull ItemConfig<T> base) {
      this.def = base.def;
      this.transformer = base.transformer;
      this.section = base.section;
      this.enabled = base.enabled;
   }

   @Nullable
   public T getActualValue() {
      T configured = this.getConfiguredValue();
      return configured == null ? this.def : configured;
   }

   public void apply(@NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      if (this.enabled) {
         T value = this.transformer == null ? this.getActualValue() : this.transformer.apply(this.getActualValue());
         if (value != null) {
            this.applyToItem(player, replacements).accept(item, value);
         }
      }
   }

   @Nullable
   public abstract T getConfiguredValue();

   protected abstract BiConsumer<ItemStack, T> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements);

   public void setDefault(@Nullable T def) {
      this.def = def;
   }

   public void setTransformer(@Nullable Function<T, T> transformer) {
      this.transformer = transformer;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   @NonNull
   public abstract ItemConfig<T> createCopy();
}
