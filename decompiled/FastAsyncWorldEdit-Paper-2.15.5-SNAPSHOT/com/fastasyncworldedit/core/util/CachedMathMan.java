package com.fastasyncworldedit.core.util;

public class CachedMathMan {
   private static final int ATAN2_BITS = 7;
   private static final int ATAN2_BITS2 = 14;
   private static final int ATAN2_MASK = 16383;
   private static final int ATAN2_COUNT = 16384;
   private static final int ATAN2_DIM = (int)Math.sqrt(16384.0);
   private static final float INV_ATAN2_DIM_MINUS_1 = 1.0F / (ATAN2_DIM - 1);
   private static final float[] atan2 = new float[16384];
   private static final float[] ANGLES;
   private static final char[] SQRT;

   protected static int usqrt(int i) {
      return SQRT[i];
   }

   protected static float sinInexact(double paramFloat) {
      return ANGLES[(int)(paramFloat * 10430.378F) & 65535];
   }

   protected static float cosInexact(double paramFloat) {
      return ANGLES[(int)(paramFloat * 10430.378F + 16384.0) & 65535];
   }

   protected static float atan2(float i, float j) {
      float add;
      float mul;
      if (j < 0.0F) {
         if (i < 0.0F) {
            j = -j;
            i = -i;
            mul = 1.0F;
         } else {
            j = -j;
            mul = -1.0F;
         }

         add = (float) -Math.PI;
      } else {
         if (i < 0.0F) {
            i = -i;
            mul = -1.0F;
         } else {
            mul = 1.0F;
         }

         add = 0.0F;
      }

      float invDiv = 1.0F / (Math.max(j, i) * INV_ATAN2_DIM_MINUS_1);
      int xi = (int)(j * invDiv);
      int yi = (int)(i * invDiv);
      return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
   }

   static {
      for (int i = 0; i < ATAN2_DIM; i++) {
         for (int j = 0; j < ATAN2_DIM; j++) {
            float x0 = (float)i / ATAN2_DIM;
            float y0 = (float)j / ATAN2_DIM;
            atan2[j * ATAN2_DIM + i] = (float)Math.atan2(y0, x0);
         }
      }

      ANGLES = new float[65536];

      for (int i = 0; i < 65536; i++) {
         ANGLES[i] = (float)Math.sin(i * Math.PI * 2.0 / 65536.0);
      }

      SQRT = new char[65536];

      for (int i = 0; i < SQRT.length; i++) {
         SQRT[i] = (char)Math.round(Math.sqrt(i));
      }
   }
}
