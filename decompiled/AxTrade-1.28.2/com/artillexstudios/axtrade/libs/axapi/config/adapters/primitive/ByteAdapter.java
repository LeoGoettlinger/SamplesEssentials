package com.artillexstudios.axtrade.libs.axapi.config.adapters.primitive;

import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;
import java.math.BigInteger;

public final class ByteAdapter implements TypeAdapter<Byte, Byte> {
   public Byte deserialize(TypeAdapterHolder registry, Object input, Type type) {
      if (input instanceof Number num) {
         return this.validate(num);
      } else if (input instanceof String str) {
         return this.validate(Double.parseDouble(str));
      } else {
         throw new IllegalArgumentException("Can't make a byte from a " + input.getClass());
      }
   }

   private Byte validate(Number number) {
      long longValue = number instanceof BigInteger bigInteger ? bigInteger.longValueExact() : number.longValue();
      if (longValue <= 127L && longValue >= -128L) {
         return (byte)longValue;
      } else {
         throw new IllegalArgumentException(
            "Byte value is not actually a byte! Should be between %s and %s, but was %s".formatted((byte)-128, (byte)127, longValue)
         );
      }
   }

   public Byte serialize(TypeAdapterHolder registry, Byte value, Type type) {
      return value;
   }
}
