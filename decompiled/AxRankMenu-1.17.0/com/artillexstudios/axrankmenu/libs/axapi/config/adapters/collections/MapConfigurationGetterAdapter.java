package com.artillexstudios.axrankmenu.libs.axapi.config.adapters.collections;

import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.MapConfigurationGetter;
import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axrankmenu.libs.axapi.utils.UncheckedUtils;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class MapConfigurationGetterAdapter implements TypeAdapter<MapConfigurationGetter, Map<String, Object>> {
   public MapConfigurationGetter deserialize(TypeAdapterHolder holder, Object input, Type type) {
      Type t = null;
      if (type instanceof ParameterizedType parameterizedType) {
         t = parameterizedType.getActualTypeArguments()[1];
      }

      if (!(input instanceof Map<?, ?> map)) {
         if (input instanceof MapConfigurationGetter getter) {
            return getter;
         } else {
            throw new RuntimeException();
         }
      } else {
         HashMap<String, Object> returning = new HashMap<>();

         for (Entry<?, ?> entry : map.entrySet()) {
            returning.put((String)entry.getKey(), holder.deserialize(entry.getValue(), t));
         }

         return new MapConfigurationGetter(UncheckedUtils.unsafeCast(returning));
      }
   }

   public Map<String, Object> serialize(TypeAdapterHolder holder, MapConfigurationGetter value, Type type) {
      HashMap<String, Object> returning = new HashMap<>();
      Type t = null;
      boolean hasParameterizedType = false;
      if (type instanceof ParameterizedType parameterizedType) {
         t = parameterizedType.getActualTypeArguments()[1];
         hasParameterizedType = true;
      }

      for (Entry<?, ?> entry : value.wrapped().entrySet()) {
         if (!hasParameterizedType) {
            t = entry.getValue().getClass();
         }

         returning.put((String)entry.getKey(), holder.serialize(entry.getValue(), t));
      }

      return returning;
   }
}
