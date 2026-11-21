package com.artillexstudios.axtrade.libs.axapi.config.adapters.primitive;

import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;
import java.math.BigInteger;

public final class LongAdapter implements TypeAdapter<Long, Long> {
   public Long deserialize(TypeAdapterHolder registry, Object input, Type type) {
      if (input instanceof Number num) {
         return this.getValue(num);
      } else if (input instanceof String str) {
         return this.getValue(Double.parseDouble(str));
      } else {
         throw new IllegalArgumentException("Can't make a long from a " + input.getClass());
      }
   }

   private Long getValue(Number number) {
      return number instanceof BigInteger bigInteger ? bigInteger.longValueExact() : number.longValue();
   }

   public Long serialize(TypeAdapterHolder registry, Long value, Type type) {
      return value;
   }
}
