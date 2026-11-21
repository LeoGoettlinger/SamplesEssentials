package com.sk89q.worldedit.function.pattern;

import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.lang.ref.SoftReference;

public class WaterloggedRemover extends AbstractExtentPattern {
   private static SoftReference<BlockState[]> cache = new SoftReference<>(null);
   private final BlockState[] remap = this.getRemap();

   private synchronized BlockState[] getRemap() {
      BlockState[] remap = cache.get();
      if (remap != null) {
         return remap;
      } else {
         cache = new SoftReference<>(remap = new BlockState[BlockTypesCache.states.length]);

         for (int i = 0; i < remap.length; i++) {
            BlockState state = BlockTypesCache.states[i];
            BlockType type = state.getBlockType();
            if (type.hasProperty(PropertyKey.WATERLOGGED) && state.getState(PropertyKey.WATERLOGGED) == Boolean.TRUE) {
               remap[i] = state.with(PropertyKey.WATERLOGGED, false);
            }
         }

         return remap;
      }
   }

   public WaterloggedRemover(Extent extent) {
      super(extent);
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BaseBlock block = this.getExtent().getFullBlock(position);
      BlockState newState = this.remap[block.getOrdinal()];
      return newState != null ? newState.toBaseBlock(block.getNbtData()) : BlockTypes.AIR.getDefaultState().toBaseBlock();
   }
}
