package com.artillexstudios.axrankmenu.libs.axapi.config.adapters.primitive;

import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axrankmenu.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;

public final class StringAdapter implements TypeAdapter<String, String> {
   public String deserialize(TypeAdapterHolder holder, Object input, Type type) {
      return input instanceof String str ? str : input.toString();
   }

   public String serialize(TypeAdapterHolder holder, String value, Type type) {
      return value;
   }
}
