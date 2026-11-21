package org.apache.commons.lang3.builder;

import java.lang.reflect.Field;
import java.util.Objects;

final class Reflection {
   static Object getUnchecked(Field field, Object obj) {
      try {
         return Objects.requireNonNull(field, "field").get(obj);
      } catch (IllegalAccessException var3) {
         throw new IllegalArgumentException(var3);
      }
   }
}
