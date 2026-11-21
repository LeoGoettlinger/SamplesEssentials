package com.magmaguy.elitemobs.powerstances;

import org.bukkit.util.Vector;

public class MajorPowerStanceMath {
   public static final int NUMBER_OF_POINTS_PER_FULL_ROTATION = 30;
   private static final double X = 0.0;
   private static final double Y = 1.0;
   private static final double Z = 0.0;
   public static Vector[][] cachedVectors = new Vector[MajorPowerPowerStance.trackAmount][30];
   private static double a;
   private static double b;
   private static double c;

   public static void initializeVectorCache() {
      for (int i = 0; i < MajorPowerPowerStance.trackAmount; i++) {
         a = Math.cos(i * 2 * Math.PI / MajorPowerPowerStance.trackAmount);
         b = 0.0;
         c = Math.sin(i * 2 * Math.PI / MajorPowerPowerStance.trackAmount);

         for (int j = 0; j < 30; j++) {
            Vector rotationVector = GenericRotationMatrixMath.applyRotation(a, b, c, 30.0, 0.0, 1.0, 0.0, j);
            cachedVectors[i][j] = rotationVector;
         }
      }
   }
}
