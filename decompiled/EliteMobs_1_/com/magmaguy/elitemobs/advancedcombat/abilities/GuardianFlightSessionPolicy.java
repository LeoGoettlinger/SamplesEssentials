package com.magmaguy.elitemobs.advancedcombat.abilities;

final class GuardianFlightSessionPolicy {
   static final int STALLED_TICKS_BEFORE_OBSTRUCTION = 3;

   private GuardianFlightSessionPolicy() {
   }

   static GuardianFlightSessionPolicy.Decision evaluate(GuardianFlightSessionPolicy.Frame frame) {
      if (!frame.casterValid()) {
         return GuardianFlightSessionPolicy.Decision.CASTER_INVALID;
      } else if (!frame.targetValid() || !Double.isFinite(frame.targetDistance()) || frame.targetDistance() < 0.0) {
         return GuardianFlightSessionPolicy.Decision.TARGET_INVALID;
      } else if (frame.elapsedTicks() >= frame.maximumTicks()) {
         return GuardianFlightSessionPolicy.Decision.EXPIRED;
      } else if (frame.targetDistance() > frame.maximumTargetDistance()) {
         return GuardianFlightSessionPolicy.Decision.OUT_OF_RANGE;
      } else if (frame.targetDistance() <= frame.arrivalDistance()) {
         return GuardianFlightSessionPolicy.Decision.ARRIVED;
      } else {
         return !frame.pathObstructed() && frame.stalledTicks() < 3
            ? GuardianFlightSessionPolicy.Decision.CONTINUE
            : GuardianFlightSessionPolicy.Decision.OBSTRUCTED;
      }
   }

   static enum Decision {
      CONTINUE,
      ARRIVED,
      OBSTRUCTED,
      TARGET_INVALID,
      CASTER_INVALID,
      OUT_OF_RANGE,
      EXPIRED;
   }

   record Frame(
      boolean casterValid,
      boolean targetValid,
      int elapsedTicks,
      int maximumTicks,
      double targetDistance,
      double maximumTargetDistance,
      double arrivalDistance,
      boolean pathObstructed,
      int stalledTicks
   ) {
      Frame(
         boolean casterValid,
         boolean targetValid,
         int elapsedTicks,
         int maximumTicks,
         double targetDistance,
         double maximumTargetDistance,
         double arrivalDistance,
         boolean pathObstructed,
         int stalledTicks
      ) {
         if (elapsedTicks < 0 || maximumTicks < 1 || stalledTicks < 0) {
            throw new IllegalArgumentException("Guardian Flight tick counts must be valid");
         } else if (Double.isFinite(maximumTargetDistance) && !(maximumTargetDistance <= 0.0) && Double.isFinite(arrivalDistance) && !(arrivalDistance < 0.0)) {
            this.casterValid = casterValid;
            this.targetValid = targetValid;
            this.elapsedTicks = elapsedTicks;
            this.maximumTicks = maximumTicks;
            this.targetDistance = targetDistance;
            this.maximumTargetDistance = maximumTargetDistance;
            this.arrivalDistance = arrivalDistance;
            this.pathObstructed = pathObstructed;
            this.stalledTicks = stalledTicks;
         } else {
            throw new IllegalArgumentException("Guardian Flight distances must be valid");
         }
      }
   }
}
