package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.serialization.standard;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;

public interface TypeAdapter<T> {
   @NotNull
   Map<Object, Object> serialize(@NotNull T var1);

   @NotNull
   T deserialize(@NotNull Map<Object, Object> var1);

   @NotNull
   default Map<String, Object> toStringKeyedMap(@NotNull Map<?, ?> map) {
      Map<String, Object> newMap = new HashMap<>();

      for (Entry<?, ?> entry : map.entrySet()) {
         if (entry.getValue() instanceof Map) {
            newMap.put(entry.getKey().toString(), this.toStringKeyedMap((Map<?, ?>)entry.getValue()));
         } else {
            newMap.put(entry.getKey().toString(), entry.getValue());
         }
      }

      return newMap;
   }
}
