package ac.grim.grimac.utils.math;

import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import lombok.Generated;

public final class ModernVanillaMath {
   private static final float[] SIN = new float[65536];

   @Contract(
      pure = true
   )
   public static float sin(double value) {
      return SIN[(int)((long)(value * 10430.378350470453) & 65535L)];
   }

   @Contract(
      pure = true
   )
   public static float cos(double value) {
      return SIN[(int)((long)(value * 10430.378350470453 + 16384.0) & 65535L)];
   }

   @Generated
   private ModernVanillaMath() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      for (int i = 0; i < SIN.length; i++) {
         SIN[i] = (float)StrictMath.sin(i / 10430.378350470453);
      }
   }
}
