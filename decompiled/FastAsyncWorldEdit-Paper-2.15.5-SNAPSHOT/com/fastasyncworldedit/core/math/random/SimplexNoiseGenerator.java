package com.fastasyncworldedit.core.math.random;

import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.noise.NoiseGenerator;

public class SimplexNoiseGenerator implements NoiseGenerator {
   @Override
   public float noise(Vector2 position) {
      return this.convert(SimplexNoise.noise(position.x(), position.z()));
   }

   @Override
   public float noise(Vector3 position) {
      return this.convert(SimplexNoise.noise(position.x(), position.y(), position.z()));
   }

   private float convert(double d) {
      return (float)((d + 1.0) * 0.5);
   }
}
