package com.github.retrooper.packetevents.protocol.component;

import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemAttributeModifiers;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEnchantments;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemRarity;
import com.github.retrooper.packetevents.util.mappings.GlobalRegistryHolder;
import com.github.retrooper.packetevents.util.mappings.IRegistryHolder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StaticComponentMap implements IComponentMap {
   public static final StaticComponentMap EMPTY = new StaticComponentMap(Collections.emptyMap());
   @Obsolete
   public static final StaticComponentMap SHARED_ITEM_COMPONENTS = builder()
      .set(ComponentTypes.MAX_STACK_SIZE, 64)
      .set(ComponentTypes.LORE, ItemLore.EMPTY)
      .set(ComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY)
      .set(ComponentTypes.REPAIR_COST, 0)
      .set(ComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
      .set(ComponentTypes.RARITY, ItemRarity.COMMON)
      .build();
   private final boolean empty;
   final Map<ComponentType<?>, ?> delegate;
   final IRegistryHolder registries;

   @Deprecated
   public StaticComponentMap(Map<ComponentType<?>, ?> delegate) {
      this(delegate, GlobalRegistryHolder.INSTANCE);
   }

   public StaticComponentMap(Map<ComponentType<?>, ?> delegate, IRegistryHolder registries) {
      this.empty = delegate.isEmpty();
      this.delegate = this.empty ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(delegate));
      this.registries = registries;
   }

   public static StaticComponentMap.Builder builder() {
      return new StaticComponentMap.Builder();
   }

   @Override
   public boolean has(ComponentType<?> type) {
      return !this.empty && this.delegate.containsKey(type);
   }

   @Nullable
   @Override
   public <T> T get(ComponentType<T> type) {
      Object v = this.delegate.get(type);
      if (v instanceof ComponentValueRef) {
         v = ((ComponentValueRef)v).resolve(this.registries);
      }

      return (T)v;
   }

   @Override
   public <T> void set(ComponentType<T> type, Optional<T> value) {
      throw new UnsupportedOperationException();
   }

   public StaticComponentMap withRegistries(IRegistryHolder registries) {
      return this.registries != registries ? new StaticComponentMap(this.delegate, registries) : this;
   }

   public StaticComponentMap merge(StaticComponentMap prioritizedMap) {
      return builder().setAll(this).setAll(prioritizedMap).setRegistries(this.registries).build();
   }

   public Set<ComponentType<?>> getKeys() {
      return this.delegate.keySet();
   }

   @Deprecated
   public Map<ComponentType<?>, ?> getDelegate() {
      return this.delegate;
   }

   public boolean isEmpty() {
      return this.empty;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof StaticComponentMap)) {
         return false;
      } else {
         StaticComponentMap that = (StaticComponentMap)obj;
         return this.delegate.equals(that.delegate);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.delegate);
   }

   @Override
   public String toString() {
      return "Components" + this.delegate;
   }

   public static class Builder {
      private final Map<ComponentType<?>, Object> map = new HashMap<>();
      private IRegistryHolder registries = GlobalRegistryHolder.INSTANCE;

      public StaticComponentMap build() {
         return new StaticComponentMap(this.map, this.registries);
      }

      public StaticComponentMap.Builder setRegistries(IRegistryHolder registries) {
         this.registries = registries;
         return this;
      }

      public StaticComponentMap.Builder setAll(StaticComponentMap.Builder map) {
         return this.setAll(map.map);
      }

      public StaticComponentMap.Builder setAll(StaticComponentMap map) {
         return this.setAll(map.delegate);
      }

      public StaticComponentMap.Builder setAll(Map<ComponentType<?>, ?> map) {
         for (Entry<ComponentType<?>, ?> entry : map.entrySet()) {
            this.set(entry.getKey(), entry.getValue());
         }

         return this;
      }

      public <T> StaticComponentMap.Builder set(ComponentType<T> type, Optional<T> value) {
         return this.set(type, value.orElse(null));
      }

      public <T> StaticComponentMap.Builder set(ComponentType<T> type, @Nullable T value) {
         if (value == null) {
            this.map.remove(type);
         } else {
            this.map.put(type, value);
         }

         return this;
      }

      public <T> StaticComponentMap.Builder set(ComponentType<T> type, @Nullable ComponentValueRef<T> ref) {
         if (ref == null) {
            this.map.remove(type);
         } else {
            this.map.put(type, ref);
         }

         return this;
      }
   }
}
