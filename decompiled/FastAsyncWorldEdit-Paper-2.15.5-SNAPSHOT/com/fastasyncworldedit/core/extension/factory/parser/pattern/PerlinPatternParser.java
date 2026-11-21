package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.noise.PerlinNoise;

public class PerlinPatternParser extends NoisePatternParser {
   private static final String PERLIN_NAME = "perlin";

   public PerlinPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "perlin", PerlinNoise::new);
   }
}
