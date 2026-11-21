package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

public class NoYPattern extends AbstractPattern {
   private final Pattern pattern;
   private final MutableBlockVector3 mutable = new MutableBlockVector3();

   public NoYPattern(Pattern pattern) {
      this.pattern = pattern;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 pos) {
      this.mutable.mutX(pos.x());
      this.mutable.mutZ(pos.z());
      return this.pattern.applyBlock(this.mutable);
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      this.mutable.mutX(get.x());
      this.mutable.mutZ(get.z());
      return this.pattern.apply(extent, this.mutable, set);
   }

   @Override
   public Pattern fork() {
      return new NoYPattern(this.pattern.fork());
   }
}
