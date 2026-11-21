package com.artillexstudios.axafkzone.libs.axapi.config.adapters.other;

import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapterHolder;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

public final class PatternAdapter implements TypeAdapter<Pattern, String> {
   public Pattern deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input instanceof String str) {
         return Pattern.compile(str);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public String serialize(TypeAdapterHolder holder, Pattern value, Type type) {
      return value.pattern();
   }
}
