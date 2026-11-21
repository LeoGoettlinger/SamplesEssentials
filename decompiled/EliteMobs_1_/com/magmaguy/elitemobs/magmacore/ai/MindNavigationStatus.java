package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Objects;
import java.util.Optional;

public record MindNavigationStatus(
   Optional<MindPosition> destination, boolean navigationInProgress, Optional<MindNavigationProgress> lastProgress, int consecutiveStuckTicks
) {
   private static final MindNavigationStatus IDLE = new MindNavigationStatus(Optional.empty(), false, Optional.empty(), 0);

   public MindNavigationStatus(
      Optional<MindPosition> destination, boolean navigationInProgress, Optional<MindNavigationProgress> lastProgress, int consecutiveStuckTicks
   ) {
      destination = Objects.requireNonNull(destination, "destination");
      lastProgress = Objects.requireNonNull(lastProgress, "lastProgress");
      if (consecutiveStuckTicks < 0) {
         throw new IllegalArgumentException("consecutiveStuckTicks cannot be negative");
      } else if (!destination.isEmpty() || !navigationInProgress && !lastProgress.isPresent() && consecutiveStuckTicks == 0) {
         this.destination = destination;
         this.navigationInProgress = navigationInProgress;
         this.lastProgress = lastProgress;
         this.consecutiveStuckTicks = consecutiveStuckTicks;
      } else {
         throw new IllegalArgumentException("Idle navigation cannot retain progress or stuck state");
      }
   }

   public static MindNavigationStatus idle() {
      return IDLE;
   }

   public boolean destinationActive() {
      return this.destination.isPresent();
   }
}
