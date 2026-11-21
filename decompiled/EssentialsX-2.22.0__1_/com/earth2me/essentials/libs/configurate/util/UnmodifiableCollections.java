package com.earth2me.essentials.libs.configurate.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.function.Consumer;

public final class UnmodifiableCollections {
   private UnmodifiableCollections() {
   }

   public static <E> List<E> copyOf(final List<E> original) {
      switch (original.size()) {
         case 0:
            return Collections.emptyList();
         case 1:
            return Collections.singletonList(original.get(0));
         default:
            return Collections.unmodifiableList(new ArrayList<>(original));
      }
   }

   public static <E> Set<E> copyOf(final Set<E> original) {
      switch (original.size()) {
         case 0:
            return Collections.emptySet();
         case 1:
            return Collections.singleton(original.iterator().next());
         default:
            return Collections.unmodifiableSet(new LinkedHashSet<>(original));
      }
   }

   public static <K, V> Map<K, V> copyOf(final Map<K, V> original) {
      switch (original.size()) {
         case 0:
            return Collections.emptyMap();
         case 1:
            Entry<K, V> entry = original.entrySet().iterator().next();
            return Collections.singletonMap(entry.getKey(), entry.getValue());
         default:
            return original instanceof LinkedHashMap
               ? Collections.unmodifiableMap(new LinkedHashMap<>(original))
               : Collections.unmodifiableMap(new HashMap<>(original));
      }
   }

   @SafeVarargs
   public static <E> List<E> toList(final E... original) {
      switch (original.length) {
         case 0:
            return Collections.emptyList();
         case 1:
            return Collections.singletonList(original[0]);
         default:
            return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(original)));
      }
   }

   @SafeVarargs
   public static <E> Set<E> toSet(final E... original) {
      switch (original.length) {
         case 0:
            return Collections.emptySet();
         case 1:
            return Collections.singleton(original[0]);
         default:
            return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(original)));
      }
   }

   public static <K, V> Map<K, V> buildMap(final Consumer<Map<K, V>> handler) {
      Map<K, V> builder = new LinkedHashMap<>();
      Objects.requireNonNull(handler, "handler").accept(builder);
      return Collections.unmodifiableMap(builder);
   }

   public static <K, V> Entry<K, V> immutableMapEntry(final K key, final V value) {
      return new SimpleImmutableEntry<>(key, value);
   }
}
