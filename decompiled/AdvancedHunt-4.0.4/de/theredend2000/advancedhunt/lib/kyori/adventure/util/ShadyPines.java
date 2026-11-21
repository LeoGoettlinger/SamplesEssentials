package de.theredend2000.advancedhunt.lib.kyori.adventure.util;

public final class ShadyPines {
   private ShadyPines() {
   }

   public static boolean equals(final double a, final double b) {
      return Double.doubleToLongBits(a) == Double.doubleToLongBits(b);
   }

   public static boolean equals(final float a, final float b) {
      return Float.floatToIntBits(a) == Float.floatToIntBits(b);
   }
}
