package com.sk89q.worldedit.function.pattern;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.Arrays;

public class ExtentBufferedCompositePattern extends AbstractExtentPattern {
   private final Pattern[] patterns;

   public ExtentBufferedCompositePattern(Extent extent, Pattern... patterns) {
      super(extent);
      Preconditions.checkArgument(patterns.length != 0, "patterns cannot be empty");
      this.patterns = patterns;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BaseBlock lastBlock = null;

      for (Pattern pattern : this.patterns) {
         lastBlock = pattern.applyBlock(position);

         try {
            this.getExtent().setBlock(position, lastBlock);
         } catch (WorldEditException var8) {
         }
      }

      return lastBlock;
   }

   @Override
   public Pattern fork() {
      Pattern[] forkedPatterns = Arrays.stream(this.patterns).map(Pattern::fork).toArray(Pattern[]::new);
      return new ExtentBufferedCompositePattern(this.getExtent(), forkedPatterns);
   }
}
