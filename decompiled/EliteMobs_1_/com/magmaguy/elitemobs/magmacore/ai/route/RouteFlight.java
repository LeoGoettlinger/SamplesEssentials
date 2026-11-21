package com.magmaguy.elitemobs.magmacore.ai.route;

import com.magmaguy.elitemobs.magmacore.ai.MindContext;
import org.bukkit.util.Vector;

public final class RouteFlight {
   private final CurvedRoute route;
   private final double maximumSpeed;
   private final double acceleration;
   private double distance;
   private double speed;

   public RouteFlight(CurvedRoute route, double blocksPerSecond, double accelerationPerSecond) {
      if (Double.isFinite(blocksPerSecond) && !(blocksPerSecond <= 0.0) && Double.isFinite(accelerationPerSecond) && !(accelerationPerSecond <= 0.0)) {
         this.route = route;
         this.maximumSpeed = blocksPerSecond / 20.0;
         this.acceleration = accelerationPerSecond / 400.0;
      } else {
         throw new IllegalArgumentException("Flight speed and acceleration must be finite and positive");
      }
   }

   public double distance() {
      return this.distance;
   }

   public RouteFlight.Status tick(MindContext context) {
      if (this.distance >= this.route.length()) {
         context.actuator().stopMoving();
         return RouteFlight.Status.ARRIVED;
      } else {
         this.speed = Math.min(this.maximumSpeed, this.speed + this.acceleration);
         this.distance = Math.min(this.route.length(), this.distance + this.speed);
         Vector velocity = this.route.at(this.distance).subtract(context.entity().getLocation().toVector());
         return context.actuator().steerFlight(velocity) ? RouteFlight.Status.FLYING : RouteFlight.Status.UNSUPPORTED;
      }
   }

   public static enum Status {
      FLYING,
      ARRIVED,
      UNSUPPORTED;
   }
}
