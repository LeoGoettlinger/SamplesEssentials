package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.NonNull;
import java.lang.reflect.Type;
import java.util.function.Predicate;

final class CharSerializer extends ScalarSerializer<Character> {
   CharSerializer() {
      super(Character.class);
   }

   public Character deserialize(final Type type, final Object val) throws SerializationException {
      if (val instanceof String) {
         String strVal = (String)val;
         if (strVal.length() == 1) {
            return strVal.charAt(0);
         } else {
            throw new SerializationException(type, "Only single character expected, but received " + strVal);
         }
      } else if (val instanceof Number) {
         return (char)((Number)val).shortValue();
      } else {
         throw new CoercionFailedException(type, val, "char");
      }
   }

   public Object serialize(@NonNull final Character item, final Predicate<Class<?>> typeSupported) {
      return typeSupported.test(char.class) ? item : item.toString();
   }
}
