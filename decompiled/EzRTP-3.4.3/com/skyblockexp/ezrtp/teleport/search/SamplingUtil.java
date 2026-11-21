package com.skyblockexp.ezrtp.teleport.search;

import java.util.concurrent.ThreadLocalRandom;

public final class SamplingUtil {
   private SamplingUtil() {
   }

   public static int randomIntInclusive(int min, int max) {
      return min >= max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
   }

   public static double randomAngle() {
      return ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
   }

   public static double randomDouble() {
      return ThreadLocalRandom.current().nextDouble();
   }
}
