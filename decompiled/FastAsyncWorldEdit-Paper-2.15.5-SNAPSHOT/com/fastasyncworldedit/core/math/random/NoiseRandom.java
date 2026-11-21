package com.fastasyncworldedit.core.math.random;

import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.noise.NoiseGenerator;

public record NoiseRandom(NoiseGenerator generator, double scale) implements SimpleRandom {
   @Override
   public double nextDouble(int x, int y, int z) {
      return this.cap(this.generator.noise(Vector3.at(x, y, z).multiply(this.scale)));
   }

   private double cap(double d) {
      return d >= 1.0 ? 0.9999999999999999 : d;
   }
}
