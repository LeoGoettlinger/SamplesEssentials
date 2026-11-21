package com.magmaguy.elitemobs.powerstances;

import org.bukkit.util.Vector;

public class MinorPowerStanceMath {
   public static final int NUMBER_OF_POINTS_PER_FULL_ROTATION = 30;
   private static final double X = 1.0;
   private static final double Z = 0.0;
   public static Vector[][] cachedVectors = new Vector[MinorPowerPowerStance.trackAmount][30];
   private static double Y = 0.0;
   private static double a;
   private static double b;
   private static double c;

   public static void initializeVectorCache() {
      for (int i = 0; i < MinorPowerPowerStance.trackAmount; i++) {
         if (i == 0) {
            a = -0.6;
            Y = 0.5;
         } else {
            a = 0.6;
            Y = -0.5;
         }

         b = 1.0;
         c = 0.0;

         for (int j = 0; j < 30; j++) {
            Vector rotationVector = GenericRotationMatrixMath.applyRotation(a, b, c, 30.0, 1.0, Y, 0.0, j);
            cachedVectors[i][j] = rotationVector;
         }
      }
   }
}
