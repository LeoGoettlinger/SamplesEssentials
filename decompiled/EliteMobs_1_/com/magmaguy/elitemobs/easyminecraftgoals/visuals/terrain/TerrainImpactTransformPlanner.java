package com.magmaguy.elitemobs.easyminecraftgoals.visuals.terrain;

final class TerrainImpactTransformPlanner {
   private static final double MINIMUM_BASELINE_SCALE_DELTA = 0.012;
   private static final double VARIABLE_BASELINE_SCALE_DELTA = 0.028;
   private static final double MAXIMUM_TILT_DEGREES = 4.0;
   private static final double MINIMUM_BASELINE_LIFT = 0.006;
   private static final double VARIABLE_BASELINE_LIFT = 0.018;

   private TerrainImpactTransformPlanner() {
   }

   static TerrainImpactPlan.Transform plan(long seed, int x, int y, int z, double radialFalloff, double intensity) {
      double boundedFalloff = clamp(radialFalloff, 0.0, 1.0);
      double boundedIntensity = clamp(intensity, 0.0, 1.0);
      double radialInfluence = 0.35 + 0.65 * boundedFalloff;
      long coordinateSeed = mix(seed ^ x * 7146057691288625177L ^ y * -7046029288634856825L ^ z * -4417276706812531889L);
      double scaleNoise = unit(mix(coordinateSeed ^ 2611923443488327891L));
      double tiltXNoise = signedUnit(mix(coordinateSeed ^ 1376283091369227076L));
      double tiltZNoise = signedUnit(mix(coordinateSeed ^ -6626703657320631856L));
      double liftNoise = unit(mix(coordinateSeed ^ 589684135938649225L));
      double scale = 1.0 + boundedIntensity * (0.012 + 0.028 * radialInfluence * scaleNoise);
      double maximumTilt = 4.0 * radialInfluence * boundedIntensity;
      double tiltX = tiltXNoise * maximumTilt;
      double tiltZ = tiltZNoise * maximumTilt;
      double lift = boundedIntensity * (0.006 + 0.018 * radialInfluence * liftNoise);
      TerrainImpactPlan.Transform uncentered = new TerrainImpactPlan.Transform(scale, tiltX, tiltZ, lift, 0.0, 0.0, 0.0);
      TerrainImpactPlan.Point transformedPivot = uncentered.applyTo(0.5, 0.5, 0.5);
      return new TerrainImpactPlan.Transform(
         scale, tiltX, tiltZ, lift, 0.5 - transformedPivot.x(), 0.5 + lift - transformedPivot.y(), 0.5 - transformedPivot.z()
      );
   }

   private static long mix(long value) {
      value ^= value >>> 30;
      value *= -4658895280553007687L;
      value ^= value >>> 27;
      value *= -7723592293110705685L;
      return value ^ value >>> 31;
   }

   private static double unit(long value) {
      return (value >>> 11) * 1.110223E-16F;
   }

   private static double signedUnit(long value) {
      return unit(value) * 2.0 - 1.0;
   }

   private static double clamp(double value, double minimum, double maximum) {
      return !Double.isFinite(value) ? minimum : Math.max(minimum, Math.min(maximum, value));
   }
}
