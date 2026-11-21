package com.earth2me.essentials.libs.configurate.serialize;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.Predicate;

final class BooleanSerializer extends ScalarSerializer<Boolean> {
   BooleanSerializer() {
      super(Boolean.class);
   }

   public Boolean deserialize(final Type type, final Object value) throws SerializationException {
      if (value instanceof Number) {
         return !value.equals(0);
      } else {
         String potential = value.toString().toLowerCase(Locale.ROOT);
         if (potential.equals("true") || potential.equals("t") || potential.equals("yes") || potential.equals("y") || potential.equals("1")) {
            return true;
         } else if (!potential.equals("false") && !potential.equals("f") && !potential.equals("no") && !potential.equals("n") && !potential.equals("0")) {
            throw new CoercionFailedException(type, value, "boolean");
         } else {
            return false;
         }
      }
   }

   public Object serialize(final Boolean item, final Predicate<Class<?>> typeSupported) {
      return typeSupported.test(Integer.class) ? item ? 1 : 0 : item.toString();
   }
}
