package org.evenmorefish.fish.libs.dimensionfishing;

import java.util.Random;
import org.bukkit.Location;
import org.evenmorefish.fish.libs.dimensionfishing.common.TrackedHook;
import org.evenmorefish.fish.libs.dimensionfishing.util.DirectionUtil;
import org.evenmorefish.fish.libs.dimensionfishing.util.ParticleFactory;
import org.jetbrains.annotations.NotNull;

public class LureTracker {
   private static final Random RANDOM = new Random();
   private final TrackedHook hook;
   private final double distance;
   private final int yaw;
   private final Location startLocation;
   private Location currentLocation;
   private final ParticleFactory particles;

   public LureTracker(@NotNull TrackedHook hook) {
      this.hook = hook;
      this.distance = RANDOM.nextDouble(4.0, 6.0);
      this.yaw = RANDOM.nextInt(0, 359);
      Location hookLocation = hook.getFishHook().getLocation().clone();
      hookLocation.setYaw(this.yaw);
      this.startLocation = DirectionUtil.forwardFlat(hookLocation, this.distance);
      this.currentLocation = this.startLocation;
      this.particles = hook.getFishingState().getLureParticles();
   }

   public void tick() {
      if (this.particles != null) {
         this.particles.show(this.currentLocation);
      }

      this.updateLocation();
   }

   private void updateLocation() {
      int remainingTicks = this.hook.getRemainingLureTime();
      if (remainingTicks > 0) {
         Location hookLocation = this.hook.getFishHook().getLocation();
         hookLocation.setYaw(this.yaw);
         this.currentLocation = DirectionUtil.forwardFlat(hookLocation, remainingTicks / this.distance).add(0.0, 0.1, 0.0);
      }
   }
}
