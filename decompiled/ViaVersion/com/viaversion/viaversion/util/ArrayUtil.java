package com.viaversion.viaversion.util;

import java.lang.reflect.Array;
import java.util.Arrays;

public final class ArrayUtil {
   public static <T> T[] add(T[] array, T element) {
      int length = array.length;
      T[] newArray = (T[])Arrays.copyOf(array, length + 1);
      newArray[length] = element;
      return newArray;
   }

   @SafeVarargs
   public static <T> T[] add(T[] array, T... elements) {
      int length = array.length;
      T[] newArray = (T[])Arrays.copyOf(array, length + elements.length);
      System.arraycopy(elements, 0, newArray, length, elements.length);
      return newArray;
   }

   public static <T> T[] remove(T[] array, int index) {
      T[] newArray = (T[])Arrays.copyOf(array, array.length - 1);
      System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
      return newArray;
   }

   public static Float[] boxedArray(float[] array) {
      Float[] boxedArray = new Float[array.length];

      for (int i = 0; i < array.length; i++) {
         boxedArray[i] = array[i];
      }

      return boxedArray;
   }

   public static Integer[] boxedArray(int[] array) {
      Integer[] boxedArray = new Integer[array.length];

      for (int i = 0; i < array.length; i++) {
         boxedArray[i] = array[i];
      }

      return boxedArray;
   }

   public static Boolean[] boxedArray(boolean[] array) {
      Boolean[] boxedArray = new Boolean[array.length];

      for (int i = 0; i < array.length; i++) {
         boxedArray[i] = array[i];
      }

      return boxedArray;
   }

   public static String toString(Object o) {
      if (o != null && o.getClass().isArray()) {
         int length = Array.getLength(o);
         StringBuilder builder = new StringBuilder("[");

         for (int i = 0; i < length; i++) {
            if (i > 0) {
               builder.append(", ");
            }

            builder.append(Array.get(o, i));
         }

         builder.append("]");
         return builder.toString();
      } else {
         return String.valueOf(o);
      }
   }
}
