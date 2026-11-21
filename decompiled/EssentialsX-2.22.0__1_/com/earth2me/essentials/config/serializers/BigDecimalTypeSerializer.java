package com.earth2me.essentials.config.serializers;

import com.earth2me.essentials.libs.configurate.serialize.ScalarSerializer;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.function.Predicate;

public class BigDecimalTypeSerializer extends ScalarSerializer<BigDecimal> {
   public BigDecimalTypeSerializer() {
      super(BigDecimal.class);
   }

   public BigDecimal deserialize(Type type, Object obj) throws SerializationException {
      if (obj instanceof Double) {
         return BigDecimal.valueOf((Double)obj);
      } else if (obj instanceof Integer) {
         return BigDecimal.valueOf((long)((Integer)obj).intValue());
      } else if (obj instanceof Long) {
         return BigDecimal.valueOf((Long)obj);
      } else if (obj instanceof BigInteger) {
         return new BigDecimal((BigInteger)obj);
      } else if (obj instanceof String) {
         try {
            return new BigDecimal((String)obj, MathContext.DECIMAL128);
         } catch (ArithmeticException | NumberFormatException var4) {
            throw new SerializationException(type, "Failed to coerce input value of type " + obj.getClass() + " to BigDecimal", var4);
         }
      } else {
         throw new SerializationException(type, "Failed to coerce input value of type " + obj.getClass() + " to BigDecimal");
      }
   }

   protected Object serialize(BigDecimal item, Predicate<Class<?>> typeSupported) {
      return item.toString();
   }
}
