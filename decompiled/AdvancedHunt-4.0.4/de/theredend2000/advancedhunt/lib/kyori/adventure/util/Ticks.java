package de.theredend2000.advancedhunt.lib.kyori.adventure.util;

import java.time.Duration;

public interface Ticks {
   int TICKS_PER_SECOND = 20;
   long SINGLE_TICK_DURATION_MS = 50L;

   static Duration duration(final long ticks) {
      return Duration.ofMillis(ticks * 50L);
   }
}
