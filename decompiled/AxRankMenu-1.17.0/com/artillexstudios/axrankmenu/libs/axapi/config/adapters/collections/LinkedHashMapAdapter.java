package com.artillexstudios.axrankmenu.libs.axapi.config.adapters.collections;

import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public final class LinkedHashMapAdapter implements TypeAdapter<LinkedHashMap<String, Object>, LinkedHashMap<String, Object>> {
   public LinkedHashMap<String, Object> deserialize(TypeAdapterHolder holder, Object input, Type type) {
      Type t = null;
      if (type instanceof ParameterizedType parameterizedType) {
         t = parameterizedType.getActualTypeArguments()[1];
      }

      if (!(input instanceof LinkedHashMap<?, ?> map)) {
         throw new RuntimeException();
      } else {
         LinkedHashMap<String, Object> returning = new LinkedHashMap<>();

         for (Entry<?, ?> entry : map.entrySet()) {
            returning.put((String)entry.getKey(), holder.deserialize(entry.getValue(), t));
         }

         return returning;
      }
   }

   public LinkedHashMap<String, Object> serialize(TypeAdapterHolder holder, LinkedHashMap<String, Object> value, Type type) {
      LinkedHashMap<String, Object> returning = new LinkedHashMap<>();
      Type t = null;
      boolean hasParameterizedType = false;
      if (type instanceof ParameterizedType parameterizedType) {
         t = parameterizedType.getActualTypeArguments()[1];
         hasParameterizedType = true;
      }

      for (Entry<?, ?> entry : value.entrySet()) {
         if (!hasParameterizedType) {
            Object value1 = entry.getValue();
            if (value1 != null) {
               t = value1.getClass();
            }
         }

         returning.put((String)entry.getKey(), holder.serialize(entry.getValue(), t));
      }

      return returning;
   }
}
