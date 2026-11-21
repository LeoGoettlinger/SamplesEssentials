package com.earth2me.essentials.libs.configurate.serialize;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.function.Predicate;

final class UuidSerializer extends ScalarSerializer<UUID> {
   private static final char DASH = '-';

   UuidSerializer() {
      super(UUID.class);
   }

   public UUID deserialize(final Type type, final Object obj) throws SerializationException {
      if (obj instanceof long[]) {
         long[] arr = (long[])obj;
         if (arr.length == 2) {
            return new UUID(arr[0], arr[1]);
         }
      }

      String uuidStr = obj.toString();
      if (uuidStr.length() == 32) {
         uuidStr = new StringBuilder(36)
            .append(uuidStr, 0, 8)
            .append('-')
            .append(uuidStr, 8, 12)
            .append('-')
            .append(uuidStr, 12, 16)
            .append('-')
            .append(uuidStr, 16, 20)
            .append('-')
            .append(uuidStr, 20, 32)
            .toString();
      }

      try {
         return UUID.fromString(uuidStr);
      } catch (IllegalArgumentException var5) {
         throw new CoercionFailedException(obj, "UUID");
      }
   }

   public Object serialize(final UUID item, final Predicate<Class<?>> typeSupported) {
      return item.toString();
   }
}
