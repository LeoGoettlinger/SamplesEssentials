package com.viaversion.viaversion.util;

public final class MathUtil {
   public static final float EPSILON = 1.0E-5F;

   public static int ceil(float value) {
      int intValue = (int)value;
      return value > intValue ? intValue + 1 : intValue;
   }

   public static int ceilLog2(int i) {
      return i > 0 ? 32 - Integer.numberOfLeadingZeros(i - 1) : 0;
   }

   public static long ceilLong(double d) {
      long l = (long)d;
      return d > l ? l + 1L : l;
   }

   public static int clamp(int i, int min, int max) {
      if (i < min) {
         return min;
      } else {
         return i > max ? max : i;
      }
   }

   public static double clamp(double d, double min, double max) {
      if (d < min) {
         return min;
      } else {
         return d > max ? max : d;
      }
   }

   public static long clamp(long l, long min, long max) {
      if (l < min) {
         return min;
      } else {
         return l > max ? max : l;
      }
   }
}
