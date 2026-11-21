package com.artillexstudios.axtrade.libs.axapi.config.adapters.other;

import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;
import java.util.UUID;

public final class UUIDAdapter implements TypeAdapter<UUID, String> {
   public UUID deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input instanceof String str) {
         return UUID.fromString(str);
      } else if (input instanceof UUID uuid) {
         return uuid;
      } else {
         throw new IllegalArgumentException("Can't convert %s into a UUID!".formatted(input.getClass()));
      }
   }

   public String serialize(TypeAdapterHolder holder, UUID value, Type type) {
      return value.toString();
   }
}
