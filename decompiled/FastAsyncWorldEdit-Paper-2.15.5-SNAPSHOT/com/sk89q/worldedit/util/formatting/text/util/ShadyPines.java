package com.sk89q.worldedit.util.formatting.text.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ShadyPines {
   private ShadyPines() {
   }

   @SafeVarargs
   @NonNull
   public static <E extends Enum<E>> Set<E> enumSet(final Class<E> type, @NonNull final E... constants) {
      Set<E> set = EnumSet.noneOf(type);
      Collections.addAll(set, constants);
      return Collections.unmodifiableSet(set);
   }

   @NonNull
   public static String toString(@NonNull final Object object, @NonNull final Consumer<Map<String, Object>> consumer) {
      Map<String, Object> builder = new LinkedHashMap<>();
      consumer.accept(builder);
      return toString(object, builder);
   }

   @NonNull
   public static String toString(@NonNull final Object object, @NonNull final Map<String, Object> builder) {
      StringJoiner joiner = new StringJoiner(", ", object.getClass().getSimpleName() + "{", "}");
      builder.forEach((key, value) -> joiner.add(key + "=" + value));
      return joiner.toString();
   }
}
