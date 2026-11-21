package com.fastasyncworldedit.core.world.block;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import javax.annotation.Nonnull;

public final class BlanketBaseBlock extends BaseBlock {
   public BlanketBaseBlock(BlockState blockState) {
      super(blockState);
   }

   public BlanketBaseBlock(BlockState blockState, @Nonnull CompoundTag tile) {
      super(blockState, tile);
   }

   @Override
   public BaseBlock toBaseBlock(CompoundTag compoundTag) {
      return (BaseBlock)(compoundTag != null ? new BaseBlock(this.toImmutableState(), compoundTag) : this);
   }
}
