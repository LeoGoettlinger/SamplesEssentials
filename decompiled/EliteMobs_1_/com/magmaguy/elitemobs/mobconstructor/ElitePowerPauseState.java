package com.magmaguy.elitemobs.mobconstructor;

import java.util.EnumSet;
import java.util.Objects;

public final class ElitePowerPauseState {
   private final EnumSet<ElitePowerPauseReason> activeReasons = EnumSet.noneOf(ElitePowerPauseReason.class);

   public boolean set(ElitePowerPauseReason reason, boolean paused) {
      Objects.requireNonNull(reason, "reason");
      boolean before = this.isPaused();
      if (paused) {
         this.activeReasons.add(reason);
      } else {
         this.activeReasons.remove(reason);
      }

      return before != this.isPaused();
   }

   public boolean isPaused() {
      return !this.activeReasons.isEmpty();
   }

   public boolean has(ElitePowerPauseReason reason) {
      return this.activeReasons.contains(Objects.requireNonNull(reason, "reason"));
   }
}
