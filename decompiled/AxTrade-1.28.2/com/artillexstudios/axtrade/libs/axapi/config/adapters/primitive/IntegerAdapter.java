package com.artillexstudios.axtrade.libs.axapi.config.adapters.primitive;

import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;
import java.math.BigInteger;

public final class IntegerAdapter implements TypeAdapter<Integer, Integer> {
   public Integer deserialize(TypeAdapterHolder registry, Object input, Type type) {
      if (input instanceof Number num) {
         return this.validate(num);
      } else if (input instanceof String str) {
         return this.validate(Double.parseDouble(str));
      } else {
         throw new IllegalArgumentException("Can't make an int from a " + input.getClass());
      }
   }

   private Integer validate(Number number) {
      long longValue = number instanceof BigInteger bigInteger ? bigInteger.longValueExact() : number.longValue();
      if (longValue <= 2147483647L && longValue >= -2147483648L) {
         return (int)longValue;
      } else {
         throw new IllegalArgumentException(
            "Int value is not actually an int! Should be between %s and %s, but was %s".formatted(Integer.MIN_VALUE, Integer.MAX_VALUE, longValue)
         );
      }
   }

   public Integer serialize(TypeAdapterHolder registry, Integer value, Type type) {
      return value;
   }
}
