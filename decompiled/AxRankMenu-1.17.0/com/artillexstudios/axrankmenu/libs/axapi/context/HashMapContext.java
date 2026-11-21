package com.artillexstudios.axrankmenu.libs.axapi.context;

import com.artillexstudios.axrankmenu.libs.axapi.utils.UncheckedUtils;
import java.util.HashMap;
import java.util.Objects;
import java.util.Map.Entry;

public class HashMapContext {
   protected final HashMap<ContextKey<?>, Object> values;

   public HashMapContext() {
      this(new HashMap<>(1));
   }

   public HashMapContext(HashMapContext context) {
      this(new HashMap<>(context.values));
   }

   public HashMapContext(HashMap<ContextKey<?>, Object> contents) {
      this.values = contents;
   }

   public static HashMapContext create() {
      return new HashMapContext();
   }

   public <T> HashMapContext with(ContextKey<T> key, T value) {
      this.values.put(key, value);
      return this;
   }

   public <T> T getByClass(Class<T> clazz) {
      for (Entry<ContextKey<?>, Object> entry : this.values.entrySet()) {
         if (entry.getKey().type().equals(clazz)) {
            return clazz.cast(entry.getValue());
         }
      }

      return null;
   }

   public <T> T getByName(String name) {
      for (Entry<ContextKey<?>, Object> entry : this.values.entrySet()) {
         if (entry.getKey().name().equals(name)) {
            return UncheckedUtils.unsafeCast(entry.getValue());
         }
      }

      return null;
   }

   public <T> T get(ContextKey<T> key) {
      return key.type().cast(this.values.get(key));
   }

   public HashMapContext copy() {
      return new HashMapContext(this);
   }

   public HashMapContext merge(HashMapContext other) {
      for (Entry<ContextKey<?>, Object> entry : other.values.entrySet()) {
         if (!this.values.containsKey(entry.getKey())) {
            this.values.put(entry.getKey(), entry.getValue());
         }
      }

      return this;
   }

   @Override
   public final boolean equals(Object object) {
      return object instanceof HashMapContext context ? Objects.equals(this.values, context.values) : false;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.values);
   }
}
