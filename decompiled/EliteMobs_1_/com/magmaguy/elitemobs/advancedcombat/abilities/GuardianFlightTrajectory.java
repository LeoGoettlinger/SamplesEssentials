package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.bukkit.util.Vector;

final class GuardianFlightTrajectory {
   private static final double STEERING_ACCELERATION = 0.22;
   private static final double MINIMUM_TRAVEL_SPEED = 0.32;
   private static final double MAXIMUM_TRAVEL_SPEED = 1.3;
   private static final double FORWARD_CANCEL_MINIMUM_SPEED = 0.65;
   private static final double FORWARD_CANCEL_MAXIMUM_SPEED = 1.45;
   private static final double FORWARD_CANCEL_SPEED_PER_BLOCK = 0.045;
   private static final double UPWARD_CANCEL_BASE_SPEED = 0.78;
   private static final double UPWARD_CANCEL_MAXIMUM_BONUS = 0.32;

   private GuardianFlightTrajectory() {
   }

   static Vector steer(Vector currentVelocity, Vector displacement) {
      Vector current = finite(currentVelocity) ? currentVelocity.clone() : new Vector();
      Vector remaining = finite(displacement) ? displacement.clone() : new Vector();
      double distance = remaining.length();
      if (distance <= 1.0E-9) {
         return current;
      } else {
         double desiredSpeed = clamp(distance * 0.28, 0.32, 1.3);
         Vector desired = remaining.multiply(1.0 / distance).multiply(desiredSpeed);
         Vector steering = desired.subtract(current);
         if (steering.lengthSquared() > 0.0484) {
            steering.normalize().multiply(0.22);
         }

         Vector next = current.clone().add(steering);
         double currentSpeed = current.length();
         double nextSpeed = next.length();
         double allowedSpeed = Math.max(1.3, currentSpeed);
         if (nextSpeed > allowedSpeed && nextSpeed > 1.0E-9) {
            next.multiply(allowedSpeed / nextSpeed);
         }

         return next;
      }
   }

   static Vector forwardCancel(Vector viewDirection, double travelledDistance) {
      Vector direction = finite(viewDirection) ? viewDirection.clone() : new Vector();
      if (direction.lengthSquared() <= 1.0E-9) {
         direction.setZ(1.0);
      }

      double speed = clamp(0.65 + Math.max(0.0, travelledDistance) * 0.045, 0.65, 1.45);
      return direction.normalize().multiply(speed);
   }

   static Vector upwardCancel(Vector currentVelocity, double travelledDistance) {
      Vector momentum = finite(currentVelocity) ? currentVelocity.clone() : new Vector();
      momentum.setX(momentum.getX() * 0.35);
      momentum.setZ(momentum.getZ() * 0.35);
      momentum.setY(0.78 + Math.min(0.32, Math.max(0.0, travelledDistance) * 0.02));
      return momentum;
   }

   private static double clamp(double value, double minimum, double maximum) {
      return Math.max(minimum, Math.min(maximum, value));
   }

   private static boolean finite(Vector vector) {
      return vector != null && Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
   }
}
