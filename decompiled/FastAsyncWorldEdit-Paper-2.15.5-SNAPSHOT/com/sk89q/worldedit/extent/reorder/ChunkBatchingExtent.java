package com.sk89q.worldedit.extent.reorder;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractBufferingExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.internal.util.RegionOptimizedVectorSorter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChunkBatchingExtent extends AbstractBufferingExtent {
   private final BlockMap<BaseBlock> blockMap = BlockMap.createForBaseBlock();
   private boolean enabled;

   public ChunkBatchingExtent(Extent extent) {
      this(extent, true);
   }

   public ChunkBatchingExtent(Extent extent, boolean enabled) {
      super(extent);
      this.enabled = enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean commitRequired() {
      return this.enabled;
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      if (!this.enabled) {
         return this.setDelegateBlock(location, block);
      } else {
         this.blockMap.put(location, block.toBaseBlock());
         return true;
      }
   }

   @Override
   protected BaseBlock getBufferedFullBlock(BlockVector3 position) {
      return this.blockMap.get(position);
   }

   @Override
   protected Operation commitBefore() {
      return !this.commitRequired() ? null : new Operation() {
         private Iterator<BlockVector3> iterator;

         @Override
         public Operation resume(RunContext run) throws WorldEditException {
            if (this.iterator == null) {
               List<BlockVector3> blockVectors = new ArrayList<>(ChunkBatchingExtent.this.blockMap.keySet());
               RegionOptimizedVectorSorter.sort(blockVectors);
               this.iterator = blockVectors.iterator();
            }

            while (this.iterator.hasNext()) {
               BlockVector3 position = this.iterator.next();
               BaseBlock block = ChunkBatchingExtent.this.blockMap.get(position);
               ChunkBatchingExtent.this.getExtent().setBlock(position, block);
            }

            ChunkBatchingExtent.this.blockMap.clear();
            return null;
         }

         @Override
         public void cancel() {
         }
      };
   }
}
