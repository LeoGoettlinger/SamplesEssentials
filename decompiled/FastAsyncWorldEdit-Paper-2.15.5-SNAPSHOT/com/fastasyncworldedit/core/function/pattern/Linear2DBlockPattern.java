package com.fastasyncworldedit.core.function.pattern;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.Arrays;

@Deprecated(
   forRemoval = true,
   since = "2.9.2"
)
public class Linear2DBlockPattern extends AbstractPattern {
   private final Pattern[] patternsArray;
   private final int xScale;
   private final int zScale;

   public Linear2DBlockPattern(Pattern[] patterns, int xScale, int zScale) {
      this.patternsArray = patterns;
      this.xScale = xScale;
      this.zScale = zScale;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      int index = (position.x() / this.xScale + position.z() / this.zScale) % this.patternsArray.length;
      if (index < 0) {
         index += this.patternsArray.length;
      }

      return this.patternsArray[index].applyBlock(position);
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      int index = (Math.floorDiv(get.x(), this.xScale) + Math.floorDiv(get.z(), this.zScale)) % this.patternsArray.length;
      if (index < 0) {
         index += this.patternsArray.length;
      }

      return this.patternsArray[index].apply(extent, get, set);
   }

   @Override
   public Pattern fork() {
      Pattern[] forked = Arrays.stream(this.patternsArray).map(Pattern::fork).toArray(Pattern[]::new);
      return new Linear2DBlockPattern(forked, this.xScale, this.zScale);
   }
}
