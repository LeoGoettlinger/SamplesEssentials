package com.magmaguy.elitemobs.easyminecraftgoals.internal;

public final class DamageIndicatorClamp {
   private static volatile int maxParticles = 0;

   private DamageIndicatorClamp() {
   }

   public static int getMaxParticles() {
      return maxParticles;
   }

   public static void setMaxParticles(int max) {
      maxParticles = max;
   }

   public static boolean isEnabled() {
      return maxParticles > 0;
   }
}
