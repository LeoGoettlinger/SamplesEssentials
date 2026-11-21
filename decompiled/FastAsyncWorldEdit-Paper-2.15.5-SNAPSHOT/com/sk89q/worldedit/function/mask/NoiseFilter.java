package com.sk89q.worldedit.function.mask;

import com.fastasyncworldedit.core.math.MutableVector3;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.noise.NoiseGenerator;
import javax.annotation.Nullable;

public class NoiseFilter extends AbstractMask {
   private MutableVector3 mutable;
   private NoiseGenerator noiseGenerator;
   private double density;

   public NoiseFilter(NoiseGenerator noiseGenerator, double density) {
      this.setNoiseGenerator(noiseGenerator);
      this.setDensity(density);
   }

   public NoiseGenerator getNoiseGenerator() {
      return this.noiseGenerator;
   }

   public void setNoiseGenerator(NoiseGenerator noiseGenerator) {
      Preconditions.checkNotNull(noiseGenerator);
      this.noiseGenerator = noiseGenerator;
      this.mutable = new MutableVector3();
   }

   public double getDensity() {
      return this.density;
   }

   public void setDensity(double density) {
      Preconditions.checkArgument(density >= 0.0, "density must be >= 0");
      Preconditions.checkArgument(density <= 1.0, "density must be <= 1");
      this.density = density;
      this.mutable = new MutableVector3();
   }

   @Override
   public boolean test(BlockVector3 vector) {
      return this.noiseGenerator.noise(this.mutable.setComponents(vector.x(), vector.y(), vector.z())) <= this.density;
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      return new NoiseFilter2D(this.getNoiseGenerator(), this.getDensity());
   }

   @Override
   public Mask copy() {
      return new NoiseFilter(this.noiseGenerator, this.density);
   }
}
