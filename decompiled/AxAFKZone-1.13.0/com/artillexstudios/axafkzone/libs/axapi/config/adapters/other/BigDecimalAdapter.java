package com.artillexstudios.axafkzone.libs.axapi.config.adapters.other;

import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;
import java.math.BigDecimal;

public final class BigDecimalAdapter implements TypeAdapter<BigDecimal, String> {
   public BigDecimal deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input instanceof String str) {
         return new BigDecimal(str);
      } else if (input instanceof Number number) {
         return BigDecimal.valueOf(number.doubleValue());
      } else {
         throw new IllegalArgumentException("Can't convert %s into a BigDecimal!".formatted(input.getClass()));
      }
   }

   public String serialize(TypeAdapterHolder holder, BigDecimal value, Type type) {
      return value.toString();
   }
}
