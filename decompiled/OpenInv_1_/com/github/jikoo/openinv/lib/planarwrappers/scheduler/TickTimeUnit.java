package com.github.jikoo.openinv.lib.planarwrappers.scheduler;

import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public enum TickTimeUnit {
   private static final long MILLIS_PER_TICK = 50L;

   public static long toTicks(long sourceDuration, @NotNull TimeUnit sourceUnit) {
      return TimeUnit.MILLISECONDS.convert(sourceDuration, sourceUnit) / 50L;
   }

   public static long toTime(long ticks, @NotNull TimeUnit destinationUnit) {
      long millis = ticks * 50L;
      return destinationUnit == TimeUnit.MILLISECONDS ? millis : destinationUnit.convert(millis, TimeUnit.MILLISECONDS);
   }
}
