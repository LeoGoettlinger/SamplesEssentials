package com.sk89q.worldedit.function.util;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.math.BlockVector2;

public class FlatRegionOffset implements FlatRegionFunction {
   private BlockVector2 offset;
   private final FlatRegionFunction function;

   public FlatRegionOffset(BlockVector2 offset, FlatRegionFunction function) {
      Preconditions.checkNotNull(function);
      this.setOffset(offset);
      this.function = function;
   }

   public BlockVector2 getOffset() {
      return this.offset;
   }

   public void setOffset(BlockVector2 offset) {
      Preconditions.checkNotNull(offset);
      this.offset = offset;
   }

   @Override
   public boolean apply(BlockVector2 position) throws WorldEditException {
      return this.function.apply(position.add(this.offset));
   }
}
