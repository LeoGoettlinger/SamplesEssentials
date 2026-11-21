package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Ray;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class RotatingRay extends Ray {
   private final Vector initialDirection;
   private final Vector pitchAxis;
   private final double length;
   private Vector direction;

   public RotatingRay(
      boolean ignoresSolidBlocks,
      double pointRadius,
      Location target,
      final Location target2,
      double pitchPreRotation,
      double yawPreRotation,
      final double pitchRotation,
      final double yawRotation,
      final int animationDuration
   ) {
      super(ignoresSolidBlocks, pointRadius, target, target2);
      validateEndpoints(target, target2, pointRadius, animationDuration);
      if (Double.isFinite(pitchPreRotation) && Double.isFinite(yawPreRotation) && Double.isFinite(pitchRotation) && Double.isFinite(yawRotation)) {
         this.length = target.distance(target2);
         if (this.length == 0.0) {
            throw new IllegalArgumentException("Rotating ray endpoints must differ");
         } else {
            this.centerLocation = target.clone();
            this.direction = target2.toVector().subtract(target.toVector()).normalize();
            this.direction.rotateAroundY(Math.toRadians(yawPreRotation));
            this.direction.rotateAroundAxis(perpendicular(this.direction), Math.toRadians(pitchPreRotation));
            this.initialDirection = this.direction.clone();
            this.pitchAxis = perpendicular(this.direction);
            this.locations = this.drawLine(this.centerLocation, target2);
            if (animationDuration > 0) {
               (new BukkitRunnable() {
                     private int frame;

                     public void run() {
                        try {
                           double progress = (double)(++this.frame) / animationDuration;
                           RotatingRay.this.direction = RotatingRay.this.initialDirection
                              .clone()
                              .rotateAroundAxis(RotatingRay.this.pitchAxis, Math.toRadians(pitchRotation * progress))
                              .rotateAroundY(Math.toRadians(yawRotation * progress));
                           RotatingRay.this.locations = RotatingRay.this.drawLine(RotatingRay.this.centerLocation, target2);
                           if (this.frame == animationDuration) {
                              this.cancel();
                           }
                        } catch (RuntimeException var3) {
                           this.cancel();
                           Logger.warn("Stopped rotating ray after geometry failure: " + var3);
                        }
                     }
                  })
                  .runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
            }
         }
      } else {
         throw new IllegalArgumentException("Ray rotations must be finite");
      }
   }

   static void validateEndpoints(Location first, Location second, double radius, int duration) {
      if (first != null && second != null && first.getWorld() != null && first.getWorld().equals(second.getWorld())) {
         first.checkFinite();
         second.checkFinite();
         if (!Double.isFinite(first.distanceSquared(second))
            || !Double.isFinite(radius)
            || radius <= 0.0
            || !Double.isFinite(radius * radius)
            || !Double.isFinite(radius * 2.0)
            || duration < 0) {
            throw new IllegalArgumentException("Ray radius must be finite and positive; duration must be nonnegative");
         }
      } else {
         throw new IllegalArgumentException("Ray endpoints must be in the same loaded world");
      }
   }

   private static Vector perpendicular(Vector direction) {
      Vector axis = new Vector(direction.getZ(), 0.0, -direction.getX());
      return axis.lengthSquared() < 1.0E-20 ? new Vector(1, 0, 0) : axis.normalize();
   }

   @Override
   protected List<Location> drawLine(Location source, Location unusedTarget) {
      this.currentSource = source.clone();
      this.currentTarget = source.clone().add(this.direction.clone().normalize().multiply(this.length));
      this.currentTarget.checkFinite();
      List<Location> samples = new ArrayList<>();
      samples.add(this.currentSource.clone());
      double step = this.thickness * 2.0;

      for (int index = 1; index <= 500.0 && index * step <= this.length; index++) {
         Location sample = source.clone().add(this.direction.clone().multiply(index * step));
         sample.checkFinite();
         if (!this.ignoresSolidBlocks && sample.getBlock().getType().isSolid()) {
            break;
         }

         samples.add(sample);
      }

      return samples;
   }
}
