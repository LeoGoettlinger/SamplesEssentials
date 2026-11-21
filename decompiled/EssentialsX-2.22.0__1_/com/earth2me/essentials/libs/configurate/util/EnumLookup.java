package com.earth2me.essentials.libs.configurate.util;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public final class EnumLookup {
   private static final Map<Class<? extends Enum<?>>, Map<String, Enum<?>>> ENUM_FIELD_CACHE = new WeakHashMap<>();

   private EnumLookup() {
   }

   private static String processKey(final String key) {
      return "\ud83c\udf38" + key.toLowerCase(Locale.ROOT).replace("_", "");
   }

   @Nullable
   public static <T extends Enum<T>> T lookupEnum(final Class<T> clazz, final String key) {
      Map<String, Enum<?>> vals = ENUM_FIELD_CACHE.computeIfAbsent(Objects.requireNonNull(clazz), c2 -> {
         Map<String, Enum<?>> ret = new HashMap<>();

         for (Enum<?> field : c2.getEnumConstants()) {
            ret.put(field.name(), field);
            ret.putIfAbsent(processKey(field.name()), field);
         }

         return Collections.unmodifiableMap(ret);
      });
      Enum<?> possibleRet = vals.get(Objects.requireNonNull(key, "key"));
      return (T)(possibleRet != null ? possibleRet : vals.get(processKey(key)));
   }
}
