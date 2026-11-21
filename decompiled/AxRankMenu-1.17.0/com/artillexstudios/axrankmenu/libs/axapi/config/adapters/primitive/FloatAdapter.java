package com.artillexstudios.axrankmenu.libs.axapi.config.adapters.primitive;

import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;

public final class FloatAdapter implements TypeAdapter<Float, Float> {
   public Float deserialize(TypeAdapterHolder registry, Object input, Type type) {
      if (input instanceof Number num) {
         return this.validate(num);
      } else if (input instanceof String str) {
         return this.validate(Double.parseDouble(str));
      } else {
         throw new IllegalArgumentException("Can't make a float from a " + input.getClass());
      }
   }

   private Float validate(Number number) {
      double doubleValue = number.doubleValue();
      if (!(doubleValue > Float.MAX_VALUE) && !(doubleValue < -Float.MAX_VALUE)) {
         return (float)doubleValue;
      } else {
         throw new IllegalArgumentException(
            "Float value is not actually a float! Should be between %s and %s, but was %s".formatted(-Float.MAX_VALUE, Float.MAX_VALUE, doubleValue)
         );
      }
   }

   public Float serialize(TypeAdapterHolder registry, Float value, Type type) {
      return value;
   }
}
