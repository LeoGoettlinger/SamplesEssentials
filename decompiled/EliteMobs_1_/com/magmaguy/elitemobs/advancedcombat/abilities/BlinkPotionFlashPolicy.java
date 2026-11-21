package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;

public final class BlinkPotionFlashPolicy {
   private static final double MINIMUM_DISTANCE_SQUARED = 1.0E-6;

   private BlinkPotionFlashPolicy() {
   }

   public static boolean shouldFlash(boolean successful, double distanceSquared) {
      return successful && Double.isFinite(distanceSquared) && distanceSquared > 1.0E-6;
   }

   public static BlinkPotionFlashPolicy.CleanupAction cleanup(
      long scheduledToken,
      long currentToken,
      boolean externallyChanged,
      boolean flashInstalled,
      BlinkPotionFlashPolicy.EffectState flash,
      BlinkPotionFlashPolicy.EffectState current,
      BlinkPotionFlashPolicy.EffectState previous
   ) {
      Objects.requireNonNull(flash, "flash");
      if (scheduledToken != currentToken || externallyChanged || !flashInstalled) {
         return BlinkPotionFlashPolicy.CleanupAction.NOOP;
      } else if (current != null && !current.sameIdentity(flash)) {
         return BlinkPotionFlashPolicy.CleanupAction.NOOP;
      } else if (previous != null) {
         return BlinkPotionFlashPolicy.CleanupAction.RESTORE_PREVIOUS;
      } else {
         return current == null ? BlinkPotionFlashPolicy.CleanupAction.NOOP : BlinkPotionFlashPolicy.CleanupAction.REMOVE_FLASH;
      }
   }

   public static int restoredDuration(int originalDuration, boolean infinite, long elapsedTicks) {
      if (infinite) {
         return originalDuration;
      } else {
         long remaining = originalDuration - Math.max(0L, elapsedTicks);
         return remaining <= 0L ? 0 : (int)Math.min(2147483647L, remaining);
      }
   }

   public static enum CleanupAction {
      NOOP,
      REMOVE_FLASH,
      RESTORE_PREVIOUS;
   }

   public record EffectState(int amplifier, int durationTicks, boolean ambient, boolean particles, boolean icon) {
      boolean sameIdentity(BlinkPotionFlashPolicy.EffectState other) {
         return other != null
            && this.amplifier == other.amplifier
            && this.ambient == other.ambient
            && this.particles == other.particles
            && this.icon == other.icon;
      }
   }
}
