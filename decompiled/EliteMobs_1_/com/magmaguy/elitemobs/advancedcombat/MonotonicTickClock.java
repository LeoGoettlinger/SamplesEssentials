package com.magmaguy.elitemobs.advancedcombat;

public final class MonotonicTickClock {
   private static final long NANOS_PER_TICK = 50000000L;

   private MonotonicTickClock() {
   }

   public static long currentTick() {
      return System.nanoTime() / 50000000L;
   }
}
