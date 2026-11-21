package ac.grim.grimac.utils.math;

import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import lombok.Generated;

public final class LegacyFastMath {
   private static final float[] SIN_TABLE_FAST = new float[4096];

   @Contract(
      pure = true
   )
   public static float sin(float value) {
      return SIN_TABLE_FAST[(int)(value * 651.8986F) & 4095];
   }

   @Contract(
      pure = true
   )
   public static float cos(float value) {
      return SIN_TABLE_FAST[(int)((value + (float) (Math.PI / 2)) * 651.8986F) & 4095];
   }

   @Generated
   private LegacyFastMath() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      for (int i = 0; i < 4096; i++) {
         SIN_TABLE_FAST[i] = (float)Math.sin((i + 0.5F) / 4096.0F * (float) (Math.PI * 2));
      }

      for (int i = 0; i < 360; i += 90) {
         SIN_TABLE_FAST[(int)(i * 11.377778F) & 4095] = (float)Math.sin(GrimMath.radians(i));
      }
   }
}
