package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

final class MapConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> implements ConfigValue<N, A> {
   private final A holder;
   volatile Map<Object, A> values;

   MapConfigValue(final A holder) {
      this.holder = holder;
      this.values = this.newMap();
   }

   private Map<Object, A> newMap() {
      Map<Object, A> ret = this.holder.options().mapFactory().create();
      return !(ret instanceof ConcurrentMap) ? Collections.synchronizedMap(ret) : ret;
   }

   @Override
   public Object get() {
      Map<Object, Object> value = new LinkedHashMap<>();

      for (Entry<Object, A> ent : this.values.entrySet()) {
         value.put(ent.getKey(), ent.getValue().raw());
      }

      return value;
   }

   public Map<Object, N> unwrapped() {
      Map<Object, N> unwrapped = new LinkedHashMap<>();
      this.values.forEach((k, v) -> unwrapped.put(k, v.self()));
      return Collections.unmodifiableMap(unwrapped);
   }

   @Override
   public void set(@Nullable final Object value) {
      if (value instanceof Map) {
         Map<Object, A> newValue = this.newMap();

         for (Entry<?, ?> ent : ((Map)value).entrySet()) {
            if (ent.getValue() != null) {
               A child = this.holder.createNode(ent.getKey());
               newValue.put(ent.getKey(), child);
               child.attached = true;
               child.raw(ent.getValue());
            }
         }

         synchronized (this) {
            Map<Object, A> oldMap = this.values;
            this.values = newValue;
            this.detachChildren(oldMap);
         }
      } else {
         throw new IllegalArgumentException("Map configuration values can only be set to values of type Map");
      }
   }

   @Nullable
   @Override
   public A putChild(final Object key, @Nullable final A value) {
      return value == null ? this.values.remove(key) : this.values.put(key, value);
   }

   @Nullable
   @Override
   public A putChildIfAbsent(final Object key, @Nullable final A value) {
      return value == null ? this.values.remove(key) : this.values.putIfAbsent(key, value);
   }

   @Nullable
   @Override
   public A child(@Nullable final Object key) {
      return this.values.get(key);
   }

   @Override
   public Iterable<A> iterateChildren() {
      return this.values.values();
   }

   public MapConfigValue<N, A> copy(final A holder) {
      MapConfigValue<N, A> copy = new MapConfigValue<>(holder);

      for (Entry<Object, A> ent : this.values.entrySet()) {
         copy.values.put(ent.getKey(), ent.getValue().copy(holder));
      }

      return copy;
   }

   @Override
   public boolean isEmpty() {
      return this.values.isEmpty();
   }

   private void detachChildren(final Map<Object, ? extends AbstractConfigurationNode<?, ?>> map) {
      for (AbstractConfigurationNode<?, ?> value : map.values()) {
         value.attached = false;
         if (Objects.equals(value.parent(), this.holder)) {
            value.clear();
         }
      }
   }

   @Override
   public void clear() {
      synchronized (this) {
         Map<Object, A> oldMap = this.values;
         this.values = this.newMap();
         this.detachChildren(oldMap);
      }
   }

   @Override
   public boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof MapConfigValue)) {
         return false;
      } else {
         MapConfigValue<?, ?> that = (MapConfigValue<?, ?>)other;
         return Objects.equals(this.values, that.values);
      }
   }

   @Override
   public int hashCode() {
      return this.values.hashCode();
   }

   @Override
   public String toString() {
      return "MapConfigValue{values=" + this.values + '}';
   }
}
