package com.artillexstudios.axafkzone.libs.axapi.config.adapters.other;

import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;
import java.math.BigInteger;

public final class BigIntegerAdapter implements TypeAdapter<BigInteger, String> {
   public BigInteger deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input instanceof String str) {
         return new BigInteger(str);
      } else if (input instanceof Number number) {
         return BigInteger.valueOf(number.longValue());
      } else {
         throw new IllegalArgumentException("Can't convert %s into a BigInteger!".formatted(input.getClass()));
      }
   }

   public String serialize(TypeAdapterHolder holder, BigInteger value, Type type) {
      return value.toString();
   }
}
