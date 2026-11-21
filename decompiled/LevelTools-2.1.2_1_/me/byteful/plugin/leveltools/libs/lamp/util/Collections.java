package me.byteful.plugin.leveltools.libs.lamp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Collections {
   private Collections() {
   }

   @SafeVarargs
   public static <T> LinkedList<T> linkedListOf(T... elements) {
      LinkedList<T> list = new LinkedList<>();
      java.util.Collections.addAll(list, elements);
      return list;
   }

   @SafeVarargs
   public static <T> List<T> listOf(T... elements) {
      List<T> list = new ArrayList<>();
      java.util.Collections.addAll(list, elements);
      return list;
   }

   @Contract(
      pure = true
   )
   @CheckReturnValue
   public static <L, K, V> Map<L, V> mapKeys(Map<K, V> map, Function<K, L> remap) {
      Map<L, V> remapped = (Map<L, V>)(new HashMap<>());

      for (Entry<K, V> e : map.entrySet()) {
         if (remapped.put(remap.apply(e.getKey()), e.getValue()) != null) {
            throw new IllegalStateException("Duplicate key");
         }
      }

      return remapped;
   }

   public static <T> T getOrNull(T[] array, int index) {
      return index >= 0 && index <= lastIndex(array) ? array[index] : null;
   }

   public static <T> T getOrNull(List<T> list, int index) {
      return index >= 0 && index <= list.size() - 1 ? list.get(index) : null;
   }

   private static int lastIndex(Object[] array) {
      return array.length - 1;
   }

   @Contract(
      pure = true,
      value = "null, _ -> fail; _, _ -> new"
   )
   @CheckReturnValue
   public static <T> Object[] insertAtBeginning(@NotNull T[] original, @Nullable T item) {
      Preconditions.notNull((T)original, "original array");
      int newSize = original.length + 1;
      Object[] newArr = new Object[newSize];
      newArr[0] = item;
      System.arraycopy(original, 0, newArr, 1, original.length);
      return newArr;
   }
}
