package com.earth2me.essentials.libs.configurate.transformation;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.NodePath;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.util.function.Supplier;

@FunctionalInterface
public interface TransformAction {
   static TransformAction remove() {
      return (path, value) -> {
         value.raw(null);
         return null;
      };
   }

   static TransformAction rename(Object newKey) {
      return (path, value) -> {
         Object[] arr = path.array();
         if (arr.length == 0) {
            throw new ConfigurateException(value, "The root node cannot be renamed!");
         } else {
            arr[arr.length - 1] = newKey;
            return arr;
         }
      };
   }

   static <V> TransformAction set(TypeToken<V> type, @Nullable V value) {
      return (path, node) -> {
         node.set(type, value);
         return null;
      };
   }

   static <V> TransformAction set(TypeToken<V> type, Supplier<V> valueSupplier) {
      return (path, value) -> {
         value.set(type, valueSupplier.get());
         return null;
      };
   }

   static <V> TransformAction set(Class<V> type, Supplier<V> valueSupplier) {
      return (path, value) -> {
         value.set(type, valueSupplier.get());
         return null;
      };
   }

   @Nullable
   Object[] visitPath(NodePath path, ConfigurationNode value) throws ConfigurateException;
}
