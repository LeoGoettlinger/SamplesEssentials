package com.sk89q.worldedit.util;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

public record LocatedBlock(BlockVector3 location, BaseBlock block) {
   public LocatedBlock(BlockVector3 location, BaseBlock block) {
      Preconditions.checkNotNull(location);
      Preconditions.checkNotNull(block);
      this.location = location;
      this.block = block;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public BlockVector3 getLocation() {
      return this.location;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public BaseBlock getBlock() {
      return this.block;
   }
}
