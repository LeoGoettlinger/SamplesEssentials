package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.serialization.standard;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.serialization.YamlSerializer;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.utils.supplier.MapSupplier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StandardSerializer implements YamlSerializer {
   public static final String DEFAULT_SERIALIZED_TYPE_KEY = "==";
   private static final StandardSerializer defaultSerializer = new StandardSerializer("==");
   private final Map<Class<?>, TypeAdapter<?>> adapters = new HashMap<>();
   private final Map<String, Class<?>> aliases = new HashMap<>();
   private final Object serializedTypeKey;

   public StandardSerializer(@NotNull Object serializedTypeKey) {
      this.serializedTypeKey = serializedTypeKey;
   }

   public <T> void register(@NotNull Class<T> clazz, @NotNull TypeAdapter<T> adapter) {
      this.adapters.put(clazz, adapter);
      this.aliases.put(clazz.getCanonicalName(), clazz);
   }

   public <T> void register(@NotNull String alias, @NotNull Class<T> clazz) {
      if (!this.adapters.containsKey(clazz)) {
         throw new IllegalStateException("Cannot register an alias for yet unregistered type!");
      } else {
         this.aliases.put(alias, clazz);
      }
   }

   @Nullable
   @Override
   public Object deserialize(@NotNull Map<Object, Object> map) {
      if (!map.containsKey(this.serializedTypeKey)) {
         return null;
      } else {
         Class<?> type = this.aliases.get(map.get(this.serializedTypeKey).toString());
         return type == null ? null : this.adapters.get(type).deserialize(map);
      }
   }

   @Nullable
   @Override
   public <T> Map<Object, Object> serialize(@NotNull T object, @NotNull MapSupplier supplier) {
      if (!this.adapters.containsKey(object.getClass())) {
         return null;
      } else {
         Map<Object, Object> serialized = supplier.supply(1);
         serialized.putAll(((TypeAdapter<T>)this.adapters.get(object.getClass())).serialize(object));
         serialized.computeIfAbsent(this.serializedTypeKey, k -> object.getClass().getCanonicalName());
         return serialized;
      }
   }

   @NotNull
   @Override
   public Set<Class<?>> getSupportedClasses() {
      return this.adapters.keySet();
   }

   @NotNull
   @Override
   public Set<Class<?>> getSupportedParentClasses() {
      return Collections.emptySet();
   }

   public static StandardSerializer getDefault() {
      return defaultSerializer;
   }
}
