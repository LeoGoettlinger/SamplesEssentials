package com.artillexstudios.axtrade.libs.axapi.config.adapters.primitive;

import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;

public final class BooleanAdapter implements TypeAdapter<Boolean, Boolean> {
   public Boolean deserialize(TypeAdapterHolder registry, Object input, Type type) {
      if (input instanceof Boolean bool) {
         return bool;
      } else if (input instanceof String str) {
         if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("on") || str.equalsIgnoreCase("enabled")) {
            return Boolean.TRUE;
         } else if (!str.equalsIgnoreCase("false") && !str.equalsIgnoreCase("off") && !str.equalsIgnoreCase("disabled")) {
            throw new IllegalArgumentException("Not a boolean type: " + str);
         } else {
            return Boolean.FALSE;
         }
      } else {
         throw new IllegalArgumentException("Not a boolean type: " + input.getClass());
      }
   }

   public Boolean serialize(TypeAdapterHolder registry, Boolean value, Type type) {
      return value;
   }
}
