package com.fastasyncworldedit.core.function.pattern;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.Arrays;

public class LinearBlockPattern extends AbstractPattern implements ResettablePattern {
   private final Pattern[] patternsArray;
   private transient int index;

   public LinearBlockPattern(Pattern[] patterns) {
      this.patternsArray = patterns;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      this.index = (this.index + 1) % this.patternsArray.length;
      return this.patternsArray[this.index].applyBlock(position);
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      this.index = (this.index + 1) % this.patternsArray.length;
      return this.patternsArray[this.index].apply(extent, get, set);
   }

   @Override
   public void reset() {
      this.index = 0;
   }

   @Override
   public Pattern fork() {
      Pattern[] forked = Arrays.stream(this.patternsArray).map(Pattern::fork).toArray(Pattern[]::new);
      return new LinearBlockPattern(forked);
   }
}
