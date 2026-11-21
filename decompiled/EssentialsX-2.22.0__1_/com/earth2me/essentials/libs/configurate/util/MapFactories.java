package com.earth2me.essentials.libs.configurate.util;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class MapFactories {
   private MapFactories() {
   }

   public static MapFactory unordered() {
      return MapFactories.DefaultFactory.UNORDERED;
   }

   public static MapFactory sorted(final Comparator<Object> comparator) {
      return new MapFactories.SortedMapFactory(Objects.requireNonNull(comparator, "comparator"));
   }

   public static MapFactory sortedNatural() {
      return MapFactories.DefaultFactory.SORTED_NATURAL;
   }

   public static MapFactory insertionOrdered() {
      return MapFactories.DefaultFactory.INSERTION_ORDERED;
   }

   private static enum DefaultFactory implements MapFactory {
      UNORDERED {
         public <K, V> ConcurrentMap<K, V> create() {
            return new ConcurrentHashMap<>();
         }
      },
      SORTED_NATURAL {
         public <K, V> ConcurrentMap<K, V> create() {
            return new ConcurrentSkipListMap<>();
         }
      },
      INSERTION_ORDERED {
         @Override
         public <K, V> Map<K, V> create() {
            return new LinkedHashMap<>();
         }
      };

      private DefaultFactory() {
      }
   }

   private static final class SortedMapFactory implements MapFactory {
      private final Comparator<Object> comparator;

      private SortedMapFactory(final Comparator<Object> comparator) {
         this.comparator = comparator;
      }

      public <K, V> ConcurrentMap<K, V> create() {
         return new ConcurrentSkipListMap<>(this.comparator);
      }

      @Override
      public boolean equals(final Object obj) {
         return obj instanceof MapFactories.SortedMapFactory && this.comparator.equals(((MapFactories.SortedMapFactory)obj).comparator);
      }

      @Override
      public int hashCode() {
         return this.comparator.hashCode();
      }

      @Override
      public String toString() {
         return "SortedMapFactory{comparator=" + this.comparator + '}';
      }
   }
}
