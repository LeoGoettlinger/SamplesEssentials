package com.magmaguy.elitemobs.magmacore.visuals.terrain;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;

public record TerrainImpactRequest(Location center, double radius, double intensity, int durationTicks, double viewRange, long seed) {
   private static final double DEFAULT_RADIUS = 4.5;
   private static final double MIN_RADIUS = 0.5;
   private static final double MAX_RADIUS = 6.0;
   private static final double DEFAULT_INTENSITY = 1.0;
   private static final int DEFAULT_DURATION_TICKS = 35;
   private static final int MAX_DURATION_TICKS = 200;
   private static final double DEFAULT_VIEW_RANGE = 48.0;
   private static final double MIN_VIEW_RANGE = 1.0;
   private static final double MAX_VIEW_RANGE = 96.0;

   public TerrainImpactRequest(Location center, double radius, double intensity, int durationTicks, double viewRange, long seed) {
      center = Objects.requireNonNull(center, "center").clone();
      radius = bounded(radius, 4.5, 0.5, 6.0);
      intensity = bounded(intensity, 1.0, 0.0, 1.0);
      durationTicks = Math.max(1, Math.min(200, durationTicks));
      viewRange = bounded(viewRange, 48.0, 1.0, 96.0);
      this.center = center;
      this.radius = radius;
      this.intensity = intensity;
      this.durationTicks = durationTicks;
      this.viewRange = viewRange;
      this.seed = seed;
   }

   public TerrainImpactRequest(Location center) {
      this(center, 4.5, 1.0, 35, 48.0, defaultSeed(center));
   }

   public Location center() {
      return this.center.clone();
   }

   private static double bounded(double value, double fallback, double minimum, double maximum) {
      return !Double.isFinite(value) ? fallback : Math.max(minimum, Math.min(maximum, value));
   }

   private static long defaultSeed(Location center) {
      Objects.requireNonNull(center, "center");
      long seed = -7046029254386353131L;
      if (center.getWorld() != null) {
         UUID worldId = center.getWorld().getUID();
         if (worldId != null) {
            seed ^= worldId.getMostSignificantBits() ^ worldId.getLeastSignificantBits();
         }
      }

      seed ^= center.getBlockX() * 7146057691288625177L;
      seed ^= center.getBlockY() * -7046029288634856825L;
      seed ^= center.getBlockZ() * -4417276706812531889L;
      seed ^= seed >>> 30;
      seed *= -4658895280553007687L;
      seed ^= seed >>> 27;
      seed *= -7723592293110705685L;
      return seed ^ seed >>> 31;
   }
}
