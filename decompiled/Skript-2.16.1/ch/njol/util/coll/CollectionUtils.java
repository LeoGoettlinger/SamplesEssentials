package ch.njol.util.coll;

import ch.njol.util.Pair;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

public abstract class CollectionUtils {
   private static final Random random = new Random();

   private CollectionUtils() {
   }

   public static <T> int indexOf(@Nullable T[] array, @Nullable T t) {
      if (array == null) {
         return -1;
      } else {
         for (int i = 0; i < array.length; i++) {
            if (array[i] == null ? t == null : array[i].equals(t)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static <T> int lastIndexOf(@Nullable T[] array, @Nullable T t) {
      if (array == null) {
         return -1;
      } else {
         for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == null ? t == null : array[i].equals(t)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static <T> int indexOf(@Nullable T[] array, @Nullable T t, int start, int end) {
      if (array == null) {
         return -1;
      } else {
         for (int i = start; i < end; i++) {
            if (Objects.equals(array[i], t)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static <T> boolean contains(@Nullable T[] array, @Nullable T o) {
      return indexOf(array, o) != -1;
   }

   public static <T> boolean containsAny(@Nullable T[] array, @Nullable T... os) {
      if (array != null && os != null) {
         for (T o : os) {
            if (indexOf(array, o) != -1) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static <T> boolean containsAll(@Nullable T[] array, @Nullable T... os) {
      if (array != null && os != null) {
         for (T o : os) {
            if (indexOf(array, o) == -1) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static <T> boolean check(@Nullable T[] array, Predicate<T> predicate, boolean and) {
      if (array == null) {
         return false;
      } else {
         for (T value : array) {
            boolean result = predicate.test(value);
            if (and && !result) {
               return false;
            }

            if (!and && result) {
               return true;
            }
         }

         return and;
      }
   }

   public static int indexOf(@Nullable int[] array, int num) {
      return array == null ? -1 : indexOf(array, num, 0, array.length);
   }

   public static int indexOf(@Nullable int[] array, int num, int start) {
      return array == null ? -1 : indexOf(array, num, start, array.length);
   }

   public static int indexOf(@Nullable int[] array, int num, int start, int end) {
      if (array == null) {
         return -1;
      } else {
         for (int i = start; i < end; i++) {
            if (array[i] == num) {
               return i;
            }
         }

         return -1;
      }
   }

   public static boolean contains(@Nullable int[] array, int num) {
      return indexOf(array, num) != -1;
   }

   public static int indexOfIgnoreCase(@Nullable String[] array, @Nullable String s) {
      if (array == null) {
         return -1;
      } else {
         int i = 0;

         for (String a : array) {
            if (a == null ? s == null : a.equalsIgnoreCase(s)) {
               return i;
            }

            i++;
         }

         return -1;
      }
   }

   public static boolean containsIgnoreCase(@Nullable String[] array, @Nullable String s) {
      return indexOfIgnoreCase(array, s) != -1;
   }

   public static <T> int indexOf(@Nullable Iterable<T> iter, @Nullable T o) {
      if (iter == null) {
         return -1;
      } else {
         int i = 0;

         for (T a : iter) {
            if (a == null ? o == null : a.equals(o)) {
               return i;
            }

            i++;
         }

         return -1;
      }
   }

   public static int indexOfIgnoreCase(@Nullable Iterable<String> iter, @Nullable String s) {
      if (iter == null) {
         return -1;
      } else {
         int i = 0;

         for (String a : iter) {
            if (a == null ? s == null : a.equalsIgnoreCase(s)) {
               return i;
            }

            i++;
         }

         return -1;
      }
   }

   @Nullable
   public static <T, U> Entry<T, U> containsKey(@Nullable Map<T, U> map, @Nullable T key) {
      if (map == null) {
         return null;
      } else {
         return map.containsKey(key) ? new Pair<>(key, map.get(key)) : null;
      }
   }

   @Nullable
   public static <U> Entry<String, U> containsKeyIgnoreCase(@Nullable Map<String, U> map, @Nullable String key) {
      if (key == null) {
         return containsKey(map, null);
      } else if (map == null) {
         return null;
      } else {
         for (Entry<String, U> e : map.entrySet()) {
            if (key.equalsIgnoreCase(e.getKey())) {
               return e;
            }
         }

         return null;
      }
   }

   public static boolean containsSuperclass(@Nullable Class<?>[] classes, @Nullable Class<?> c) {
      if (classes != null && c != null) {
         for (Class<?> cl : classes) {
            if (cl != null && cl.isAssignableFrom(c)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean containsAnySuperclass(@Nullable Class<?>[] classes, @Nullable Class<?>... cs) {
      if (classes != null && cs != null) {
         for (Class<?> cl : classes) {
            if (cl != null) {
               for (Class<?> c : cs) {
                  if (cl.isAssignableFrom(c)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isAnyInstanceOf(Object object, Class<?>... classes) {
      for (Class<?> clazz : classes) {
         if (clazz.isInstance(object)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public static <T> T getRandom(@Nullable T[] os) {
      return os != null && os.length != 0 ? os[random.nextInt(os.length)] : null;
   }

   @Nullable
   public static <T> T getRandom(@Nullable T[] os, int start) {
      return os != null && os.length != 0 ? os[random.nextInt(os.length - start) + start] : null;
   }

   @Nullable
   public static <T> T getRandom(@Nullable List<T> os) {
      return os != null && !os.isEmpty() ? os.get(random.nextInt(os.size())) : null;
   }

   public static boolean isSubset(@Nullable Object[] set, @Nullable Object[] sub) {
      if (set != null && sub != null) {
         for (Object s : set) {
            if (!contains(sub, s)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static <E> Set<E> intersection(@Nullable Set<E>... sets) {
      if (sets != null && sets.length != 0) {
         if (sets.length == 1 && sets[0] != null) {
            return sets[0];
         } else {
            Set<E> l = new HashSet<>(sets[0]);

            for (int i = 1; i < sets.length; i++) {
               if (sets[i] != null) {
                  l.retainAll(sets[i]);
               }
            }

            return l;
         }
      } else {
         return Collections.emptySet();
      }
   }

   public static <E> Set<E> union(@Nullable Set<E>... sets) {
      if (sets != null && sets.length != 0) {
         if (sets.length == 1 && sets[0] != null) {
            return sets[0];
         } else {
            Set<E> l = new HashSet<>(sets[0]);

            for (int i = 1; i < sets.length; i++) {
               if (sets[i] != null) {
                  l.addAll(sets[i]);
               }
            }

            return l;
         }
      } else {
         return Collections.emptySet();
      }
   }

   @SafeVarargs
   public static <T> T[] array(T... array) {
      return array;
   }

   public static <T> Class<T[]> arrayType(Class<T> c) {
      return (Class<T[]>)Array.newInstance(c, 0).getClass();
   }

   public static <T> T[] subarray(T[] array, int startIndex, int endIndex) {
      Class<T> componentType = (Class<T>)array.getClass().getComponentType();
      startIndex = Math.max(startIndex, 0);
      endIndex = Math.min(Math.max(endIndex, 0), array.length);
      if (startIndex >= endIndex) {
         return (T[])((Object[])Array.newInstance(componentType, 0));
      } else {
         int size = endIndex - startIndex;
         T[] subarray = (T[])Array.newInstance(componentType, size);
         System.arraycopy(array, startIndex, subarray, 0, size);
         return subarray;
      }
   }

   public static int[] permutation(int start, int end) {
      if (start > end) {
         return new int[0];
      } else {
         int length = end - start + 1;
         int[] r = new int[length];

         for (int i = 0; i < length; i++) {
            r[i] = start + i;
         }

         for (int i = length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int b = r[i];
            r[i] = r[j];
            r[j] = b;
         }

         return r;
      }
   }

   public static byte[] permutation(byte start, byte end) {
      if (start > end) {
         return new byte[0];
      } else {
         int length = end - start + 1;
         byte[] r = new byte[length];

         for (byte i = 0; i < length; i++) {
            r[i] = (byte)(start + i);
         }

         for (int i = length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            byte b = r[i];
            r[i] = r[j];
            r[j] = b;
         }

         return r;
      }
   }

   public static int[] permutation(int length) {
      return permutation(0, length - 1);
   }

   public static int[] toArray(@Nullable Collection<Integer> ints) {
      if (ints == null) {
         return new int[0];
      } else {
         int[] r = new int[ints.size()];
         int i = 0;

         for (Integer n : ints) {
            r[i++] = n;
         }

         if (i != r.length) {
            assert false : ints;

            return Arrays.copyOfRange(r, 0, i);
         } else {
            return r;
         }
      }
   }

   public static float[] toFloats(@Nullable double[] doubles) {
      if (doubles == null) {
         return new float[0];
      } else {
         float[] floats = new float[doubles.length];

         for (int i = 0; i < floats.length; i++) {
            floats[i] = (float)doubles[i];
         }

         return floats;
      }
   }

   public static Double[] wrap(double[] primitive) {
      Double[] wrapped = new Double[primitive.length];

      for (int i = 0; i < primitive.length; i++) {
         wrapped[i] = primitive[i];
      }

      return wrapped;
   }
}
