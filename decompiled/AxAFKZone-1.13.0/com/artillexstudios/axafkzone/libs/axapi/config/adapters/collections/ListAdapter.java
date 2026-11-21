package com.artillexstudios.axafkzone.libs.axapi.config.adapters.collections;

import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class ListAdapter implements TypeAdapter<List<Object>, List<Object>> {
   public List<Object> deserialize(TypeAdapterHolder holder, Object input, Type type) {
      Type t = null;
      if (type instanceof ParameterizedType parameterizedType) {
         t = parameterizedType.getActualTypeArguments()[0];
      }

      if (!(input instanceof List<?> list)) {
         throw new RuntimeException();
      } else {
         List<Object> returning = new ArrayList<>(list.size());

         for (Object o : list) {
            returning.add(holder.deserialize(o, t));
         }

         return returning;
      }
   }

   public List<Object> serialize(TypeAdapterHolder holder, List<Object> value, Type type) {
      if (type instanceof ParameterizedType parameterizedType) {
         Type var9 = parameterizedType.getActualTypeArguments()[0];
         ArrayList var10 = new ArrayList(value.size());

         for (Object o : value) {
            var10.add(holder.serialize(o, var9));
         }

         return var10;
      } else {
         List<Object> returning = new ArrayList<>(value.size());

         for (Object o : value) {
            returning.add(holder.serialize(o, o.getClass()));
         }

         return returning;
      }
   }
}
