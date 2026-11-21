package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.noise.RidgedMultiFractalNoise;

public class RidgedMultiFractalPatternParser extends NoisePatternParser {
   private static final String RIDGED_MULTI_FRACTAL_NAME = "rmf";

   public RidgedMultiFractalPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "rmf", RidgedMultiFractalNoise::new);
   }
}
