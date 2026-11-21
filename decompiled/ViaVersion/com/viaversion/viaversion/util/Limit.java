package com.viaversion.viaversion.util;

public final class Limit {
   public static int max(int value, int max) {
      if (value > max) {
         throw new IllegalArgumentException("Value " + value + " is higher than the maximum " + max);
      } else {
         return value;
      }
   }
}
