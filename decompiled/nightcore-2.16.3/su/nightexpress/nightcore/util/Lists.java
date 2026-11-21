package su.nightexpress.nightcore.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Lists {
   public static int indexOf(Object[] array, @NonNull Object objectToFind) {
      return indexOf(array, objectToFind, 0);
   }

   public static int indexOf(Object[] array, @NonNull Object objectToFind, int startIndex) {
      if (array != null && array.length != 0) {
         if (!array.getClass().getComponentType().isInstance(objectToFind)) {
            return -1;
         } else {
            if (startIndex < 0) {
               startIndex = 0;
            }

            for (int index = startIndex; index < array.length; index++) {
               if (objectToFind.equals(array[index])) {
                  return index;
               }
            }

            return -1;
         }
      } else {
         return -1;
      }
   }

   public static boolean contains(Object[] array, Object objectToFind) {
      return indexOf(array, objectToFind) != -1;
   }

   public static int indexOf(int[] array, int valueToFind) {
      return indexOf(array, valueToFind, 0);
   }

   public static int indexOf(int[] array, int valueToFind, int startIndex) {
      if (array != null && array.length != 0) {
         if (startIndex < 0) {
            startIndex = 0;
         }

         for (int index = startIndex; index < array.length; index++) {
            if (valueToFind == array[index]) {
               return index;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static boolean contains(int[] array, int valueToFind) {
      return indexOf(array, valueToFind) != -1;
   }

   @NonNull
   public static <T, R> Set<R> modify(@NonNull Set<T> set, @NonNull Function<T, R> function) {
      return set.stream().map(function).collect(Collectors.toCollection(HashSet::new));
   }

   @NonNull
   public static <T, R> List<R> modify(@NonNull List<T> set, @NonNull Function<T, R> function) {
      return set.stream().map(function).collect(Collectors.toCollection(ArrayList::new));
   }

   @NonNull
   public static <T> List<List<T>> split(@NonNull List<T> list, int targetSize) {
      List<List<T>> lists = new ArrayList<>();
      if (targetSize <= 0) {
         return lists;
      } else {
         for (int index = 0; index < list.size(); index += targetSize) {
            lists.add(list.subList(index, Math.min(index + targetSize, list.size())));
         }

         return lists;
      }
   }

   @NonNull
   public static List<String> replace(@NonNull List<String> origin, @NonNull String var, String... with) {
      return replace(origin, var, Arrays.asList(with));
   }

   @NonNull
   public static List<String> replace(@NonNull List<String> origin, @NonNull String var, @NonNull List<String> with) {
      List<String> replaced = new ArrayList<>();

      for (String line : origin) {
         if (line.equalsIgnoreCase(var)) {
            replaced.addAll(with);
         } else {
            replaced.add(line);
         }
      }

      return replaced;
   }

   @Deprecated
   @NonNull
   public static List<String> stripEmpty(@NonNull List<String> original) {
      List<String> stripped = new ArrayList<>();

      for (int index = 0; index < original.size(); index++) {
         String line = original.get(index);
         if (line.isEmpty()) {
            String last = stripped.isEmpty() ? null : stripped.getLast();
            if (last == null || last.isEmpty() || index == original.size() - 1) {
               continue;
            }
         }

         stripped.add(line);
      }

      return stripped;
   }

   @NonNull
   public static List<String> getSequentialMatches(@NonNull List<String> results, @NonNull String input) {
      if (input.isBlank()) {
         return results;
      } else {
         char[] inputChars = input.toCharArray();
         List<String> goods = new ArrayList<>();

         label41:
         for (String resultItem : results) {
            int itemLength = resultItem.length();
            if (input.length() <= itemLength) {
               int lastIndex = -1;

               for (char letter : inputChars) {
                  int nextIndex = lastIndex;
                  if (lastIndex < 0) {
                     nextIndex = 0;
                  } else if (lastIndex < itemLength - 1) {
                     nextIndex = lastIndex + 1;
                  }

                  letter = Character.toLowerCase(letter);
                  int index = resultItem.toLowerCase().indexOf(letter, nextIndex);
                  if (index <= lastIndex) {
                     continue label41;
                  }

                  lastIndex = index;
               }

               goods.add(resultItem);
            }
         }

         return goods;
      }
   }

   @NonNull
   public static <K, V extends Comparable<? super V>> Map<K, V> sortAscent(@NonNull Map<K, V> map) {
      return sort(map, Entry.comparingByValue());
   }

   @NonNull
   public static <K, V extends Comparable<? super V>> Map<K, V> sortDescent(@NonNull Map<K, V> map) {
      return sort(map, Collections.reverseOrder(Entry.comparingByValue()));
   }

   @SafeVarargs
   @NonNull
   public static <T> List<T> newList(T... values) {
      List<T> list = new ArrayList<>();
      Collections.addAll(list, values);
      return list;
   }

   @SafeVarargs
   @NonNull
   public static <T> Set<T> newSet(T... values) {
      Set<T> list = new HashSet<>();
      Collections.addAll(list, values);
      return list;
   }

   @NonNull
   public static <K, V extends Comparable<? super V>> Map<K, V> sort(@NonNull Map<K, V> map, @NonNull Comparator<Entry<K, V>> comparator) {
      return new LinkedList<>(map.entrySet())
         .stream()
         .sorted(comparator)
         .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (old, nev) -> nev, LinkedHashMap::new));
   }

   @Deprecated
   @NonNull
   public static List<String> getEnums(@NonNull Class<? extends Enum<?>> clazz) {
      return Enums.getNames(clazz);
   }

   @NonNull
   public static <T extends Enum<T>> T next(@NonNull Enum<T> numeration) {
      return shifted(numeration, 1);
   }

   @NonNull
   public static <T extends Enum<T>> T next(@NonNull Enum<T> numeration, @NonNull Predicate<T> predicate) {
      return shifted(numeration, 1, predicate);
   }

   @NonNull
   public static <T extends Enum<T>> T previous(@NonNull Enum<T> numeration) {
      return shifted(numeration, -1);
   }

   @NonNull
   public static <T extends Enum<T>> T previous(@NonNull Enum<T> numeration, @NonNull Predicate<T> predicate) {
      return shifted(numeration, -1, predicate);
   }

   @NonNull
   public static <T extends Enum<T>> T shifted(@NonNull Enum<T> numeration, int shift) {
      return shifted(numeration, shift, null);
   }

   @NonNull
   private static <T extends Enum<T>> T shifted(@NonNull Enum<T> numeration, int shift, @Nullable Predicate<T> predicate) {
      T[] values = (T[])numeration.getDeclaringClass().getEnumConstants();
      return shifted(values, numeration, shift, predicate);
   }

   @NonNull
   private static <T extends Enum<T>> T shifted(T[] values, @NonNull Enum<T> origin, int shift, @Nullable Predicate<T> predicate) {
      if (predicate != null) {
         T source = (T)origin.getDeclaringClass().cast(origin);
         List<T> filtered = new ArrayList<>(Arrays.asList(values));
         filtered.removeIf(num -> !predicate.test((T)num) && num != source);
         int currentIndex = filtered.indexOf(source);
         return currentIndex < 0 | filtered.isEmpty() ? source : shifted(filtered, currentIndex, shift);
      } else {
         return shifted(values, origin.ordinal(), shift);
      }
   }

   @NonNull
   public static <T> T shifted(T[] values, int currentIndex, int shift) {
      int index = currentIndex + shift;
      return values[index < values.length && index >= 0 ? index : 0];
   }

   @NonNull
   public static <T> T shifted(@NonNull List<T> values, int currentIndex, int shift) {
      int index = currentIndex + shift;
      return index < 0 ? values.getLast() : values.get(index >= values.size() ? 0 : index);
   }
}
