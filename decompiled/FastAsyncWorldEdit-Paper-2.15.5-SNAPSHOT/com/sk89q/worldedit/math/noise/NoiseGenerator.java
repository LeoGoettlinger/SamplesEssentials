package com.sk89q.worldedit.math.noise;

import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;

public interface NoiseGenerator {
   float noise(Vector2 position);

   float noise(Vector3 position);
}
