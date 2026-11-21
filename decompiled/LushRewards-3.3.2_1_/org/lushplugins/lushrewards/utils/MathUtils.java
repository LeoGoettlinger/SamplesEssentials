package org.lushplugins.lushrewards.utils;

import org.jetbrains.annotations.Nullable;

public class MathUtils {
   @Nullable
   public static Integer findFirstNumInSequence(int start, int increment, int lowerBound) {
      if (increment <= 0) {
         return start > lowerBound ? start : null;
      } else {
         return (int)(start + increment * Math.ceil((double)(lowerBound - start) / increment));
      }
   }
}
