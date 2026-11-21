package com.artillexstudios.axrankmenu.libs.axapi.config.adapters.primitive;

import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;
import java.math.BigInteger;

public final class ShortAdapter implements TypeAdapter<Short, Short> {
   public Short deserialize(TypeAdapterHolder registry, Object input, Type type) {
      if (input instanceof Number num) {
         return this.validate(num);
      } else if (input instanceof String str) {
         return this.validate(Double.parseDouble(str));
      } else {
         throw new IllegalArgumentException("Can't make a short from a " + input.getClass());
      }
   }

   private Short validate(Number number) {
      long longValue = number instanceof BigInteger bigInteger ? bigInteger.longValueExact() : number.longValue();
      if (longValue <= 32767L && longValue >= -32768L) {
         return (short)longValue;
      } else {
         throw new IllegalArgumentException(
            "Short value is not actually a short! Should be between %s and %s, but was %s".formatted((short)-32768, (short)32767, longValue)
         );
      }
   }

   public Short serialize(TypeAdapterHolder registry, Short value, Type type) {
      return value;
   }
}
