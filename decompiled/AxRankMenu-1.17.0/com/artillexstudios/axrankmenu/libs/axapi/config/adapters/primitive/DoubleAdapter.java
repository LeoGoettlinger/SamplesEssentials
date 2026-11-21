package com.artillexstudios.axrankmenu.libs.axapi.config.adapters.primitive;

import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;

public final class DoubleAdapter implements TypeAdapter<Double, Double> {
   public Double deserialize(TypeAdapterHolder registry, Object input, Type type) {
      if (input instanceof Number num) {
         return num.doubleValue();
      } else if (input instanceof String str) {
         return Double.parseDouble(str);
      } else {
         throw new IllegalArgumentException("Can't make a short from a " + input.getClass());
      }
   }

   public Double serialize(TypeAdapterHolder registry, Double value, Type type) {
      return value;
   }
}
