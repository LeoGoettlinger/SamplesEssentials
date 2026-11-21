package org.skriptlang.skript.bukkit.particles.particleeffects;

import com.google.common.base.Function;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public class ScalableEffect extends ParticleEffect {
   private double scale = 1.0;
   private final ScalableEffect.ScalingFunction scalingFunction;

   private ScalableEffect.ScalingFunction getScalingFunction(@NotNull Particle particle) {
      return switch (particle) {
         case SWEEP_ATTACK -> ScalableEffect.ScalingFunction.SWEEP;
         case EXPLOSION -> ScalableEffect.ScalingFunction.EXPLOSION;
         default -> throw new IllegalArgumentException("Particle " + particle.name() + " is not a scalable effect.");
      };
   }

   @Internal
   public ScalableEffect(Particle particle) {
      super(particle);
      this.scalingFunction = this.getScalingFunction(particle);
   }

   public boolean hasScale() {
      return this.count() == 0;
   }

   public ParticleEffect scale(double scale) {
      this.scale = scale;
      this.count(0);
      this.offset(this.scalingFunction.apply(scale), 0.0, 0.0);
      return this;
   }

   public double scale() {
      return this.scale;
   }

   public ScalableEffect copy() {
      ScalableEffect copy = (ScalableEffect)super.copy();
      copy.scale = this.scale;
      return copy;
   }

   private static enum ScalingFunction {
      SWEEP(scale -> 2.0 - 2.0 * scale),
      EXPLOSION(scale -> 2.0 - scale);

      private final Function<Double, Double> scaleToOffsetX;

      private ScalingFunction(Function<Double, Double> scalingFunction) {
         this.scaleToOffsetX = scalingFunction;
      }

      public double apply(double scale) {
         return (Double)this.scaleToOffsetX.apply(scale);
      }
   }
}
