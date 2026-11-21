package com.fastasyncworldedit.core.queue.implementation.blocks;

import com.fastasyncworldedit.core.queue.IBlocks;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.Arrays;

public abstract class CharGetBlocks extends CharBlocks implements IChunkGet {
   public CharGetBlocks(final int minSectionPosition, final int maxSectionPosition) {
      super(minSectionPosition, maxSectionPosition);
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      BlockState state = BlockTypesCache.states[this.get(x, y, z)];
      return state.toBaseBlock((IBlocks)this, x, y, z);
   }

   @Override
   public boolean trim(boolean aggressive) {
      for (int i = 0; i < this.sectionCount; i++) {
         synchronized (this.sectionLocks[i]) {
            this.blocks[i] = null;
         }
      }

      return true;
   }

   @Override
   public char[] update(int layer, char[] data, boolean aggressive) {
      if (data == null) {
         data = new char[4096];
      }

      Arrays.fill(data, '\u0001');
      return data;
   }

   @Override
   protected char defaultOrdinal() {
      return '\u0001';
   }

   @Override
   public boolean trim(boolean aggressive, int layer) {
      layer -= this.minSectionPosition;
      synchronized (this.sectionLocks[layer]) {
         this.blocks[layer] = null;
         return true;
      }
   }

   @Override
   public IChunkSet reset() {
      super.reset();
      return null;
   }
}
