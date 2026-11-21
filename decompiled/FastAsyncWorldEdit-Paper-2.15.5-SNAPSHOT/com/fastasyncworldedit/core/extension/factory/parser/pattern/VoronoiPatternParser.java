package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.noise.VoronoiNoise;

public class VoronoiPatternParser extends NoisePatternParser {
   private static final String VORONOI_NAME = "voronoi";

   public VoronoiPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "voronoi", VoronoiNoise::new);
   }
}
