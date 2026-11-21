package com.earth2me.essentials.libs.configurate.serialize;

import java.lang.reflect.Type;
import java.util.function.Predicate;

final class StringSerializer extends ScalarSerializer<String> {
   StringSerializer() {
      super(String.class);
   }

   public String deserialize(final Type type, final Object obj) {
      return obj.toString();
   }

   public Object serialize(final String item, final Predicate<Class<?>> typeSupported) {
      return item;
   }
}
