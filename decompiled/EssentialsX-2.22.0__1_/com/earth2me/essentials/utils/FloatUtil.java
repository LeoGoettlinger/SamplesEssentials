package com.earth2me.essentials.utils;

public final class FloatUtil {
   private FloatUtil() {
   }

   public static float parseFloat(final String s) throws NumberFormatException {
      float f = Float.parseFloat(s);
      if (Float.isNaN(f)) {
         throw new NumberFormatException("NaN is not valid");
      } else if (Float.isInfinite(f)) {
         throw new NumberFormatException("Infinity is not valid");
      } else {
         return f;
      }
   }

   public static double parseDouble(final String s) throws NumberFormatException {
      double d = Double.parseDouble(s);
      if (Double.isNaN(d)) {
         throw new NumberFormatException("NaN is not valid");
      } else if (Double.isInfinite(d)) {
         throw new NumberFormatException("Infinity is not valid");
      } else {
         return d;
      }
   }
}
