package com.sk89q.worldedit.extent.cache;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class LastAccessExtentCache extends AbstractDelegateExtent {
   private LastAccessExtentCache.CachedBlock<BlockState> lastBlock;
   private LastAccessExtentCache.CachedBlock<BaseBlock> lastFullBlock;

   public LastAccessExtentCache(Extent extent) {
      super(extent);
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      LastAccessExtentCache.CachedBlock<BlockState> lastBlock = this.lastBlock;
      if (lastBlock != null && lastBlock.position.equals(position)) {
         return lastBlock.block;
      } else {
         BlockState block = super.getBlock(position);
         this.lastBlock = new LastAccessExtentCache.CachedBlock<>(position, block);
         return block;
      }
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      LastAccessExtentCache.CachedBlock<BaseBlock> lastFullBlock = this.lastFullBlock;
      if (lastFullBlock != null && lastFullBlock.position.equals(position)) {
         return lastFullBlock.block;
      } else {
         BaseBlock block = super.getFullBlock(position);
         this.lastFullBlock = new LastAccessExtentCache.CachedBlock<>(position, block);
         return block;
      }
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
      if (super.setBlock(location, block)) {
         if (this.lastFullBlock != null && this.lastFullBlock.position.equals(location)) {
            this.lastFullBlock = new LastAccessExtentCache.CachedBlock<>(location, block.toBaseBlock());
         }

         if (this.lastBlock != null && this.lastBlock.position.equals(location)) {
            this.lastBlock = new LastAccessExtentCache.CachedBlock<>(location, block.toImmutableState());
         }

         return true;
      } else {
         return false;
      }
   }

   private static class CachedBlock<B extends BlockStateHolder<B>> {
      private final BlockVector3 position;
      private final B block;

      private CachedBlock(BlockVector3 position, B block) {
         this.position = position;
         this.block = block;
      }
   }
}
