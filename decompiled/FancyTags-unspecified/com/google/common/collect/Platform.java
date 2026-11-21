package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.J2ktIncompatible;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   emulated = true
)
final class Platform {
   private static final Logger logger = Logger.getLogger(Platform.class.getName());

   static <K, V> Map<K, V> newHashMapWithExpectedSize(int expectedSize) {
      return Maps.newHashMapWithExpectedSize(expectedSize);
   }

   static <K, V> Map<K, V> newLinkedHashMapWithExpectedSize(int expectedSize) {
      return Maps.newLinkedHashMapWithExpectedSize(expectedSize);
   }

   static <E> Set<E> newHashSetWithExpectedSize(int expectedSize) {
      return Sets.newHashSetWithExpectedSize(expectedSize);
   }

   static <E> Set<E> newConcurrentHashSet() {
      return ConcurrentHashMap.newKeySet();
   }

   static <E> Set<E> newLinkedHashSetWithExpectedSize(int expectedSize) {
      return Sets.newLinkedHashSetWithExpectedSize(expectedSize);
   }

   static <K, V> Map<K, V> preservesInsertionOrderOnPutsMap() {
      return Maps.newLinkedHashMap();
   }

   static <E> Set<E> preservesInsertionOrderOnAddsSet() {
      return Sets.newLinkedHashSet();
   }

   static <T> T[] newArray(T[] reference, int length) {
      T[] empty = (T[])(reference.length == 0 ? reference : Arrays.copyOf(reference, 0));
      return (T[])Arrays.copyOf(empty, length);
   }

   static <T> T[] copy(Object[] source, int from, int to, T[] arrayOfType) {
      return (T[])Arrays.copyOfRange(source, from, to, (Class<? extends T[]>)arrayOfType.getClass());
   }

   @J2ktIncompatible
   static MapMaker tryWeakKeys(MapMaker mapMaker) {
      return mapMaker.weakKeys();
   }

   static <E extends Enum<E>> Class<E> getDeclaringClassOrObjectForJ2cl(E e) {
      return e.getDeclaringClass();
   }

   static int reduceIterationsIfGwt(int iterations) {
      return iterations;
   }

   static int reduceExponentIfGwt(int exponent) {
      return exponent;
   }

   private Platform() {
   }
}
