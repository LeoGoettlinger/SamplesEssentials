package com.sk89q.worldedit.util.formatting.text.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class NameMap<E extends Enum<E>> {
   private final Map<String, E> nameToValue;
   private final Map<E, String> valueToName;

   private NameMap(final Map<String, E> nameToValue, final Map<E, String> valueToName) {
      this.nameToValue = nameToValue;
      this.valueToName = valueToName;
   }

   @NonNull
   public static <E extends Enum<E>> NameMap<E> create(final Class<E> type, @NonNull final Function<E, String> namer) {
      E[] constants = (E[])type.getEnumConstants();
      int length = constants.length;
      Map<String, E> nameToValue = new HashMap<>(length);
      Map<E, String> valueToName = new EnumMap<>(type);

      for (int i = 0; i < length; i++) {
         E constant = constants[i];
         String name = namer.apply(constant);
         nameToValue.put(name, constant);
         valueToName.put(constant, name);
      }

      return new NameMap<>(Collections.unmodifiableMap(nameToValue), Collections.unmodifiableMap(valueToName));
   }

   @Deprecated
   @NonNull
   public static <E extends Enum<E>> NameMap<E> create(@NonNull final E[] constants, @NonNull final Function<E, String> namer) {
      int length = constants.length;
      Map<String, E> nameToValue = new HashMap<>(length);
      Map<E, String> valueToName = new HashMap<>(length);

      for (int i = 0; i < length; i++) {
         E constant = constants[i];
         String name = namer.apply(constant);
         nameToValue.put(name, constant);
         valueToName.put(constant, name);
      }

      return new NameMap<>(Collections.unmodifiableMap(nameToValue), Collections.unmodifiableMap(valueToName));
   }

   @NonNull
   public String name(@NonNull final E value) {
      return this.valueToName.get(value);
   }

   @NonNull
   public Optional<E> value(@NonNull final String name) {
      return Optional.ofNullable(this.nameToValue.get(name));
   }

   @Deprecated
   @NonNull
   public Optional<E> get(@NonNull final String name) {
      return this.value(name);
   }
}
