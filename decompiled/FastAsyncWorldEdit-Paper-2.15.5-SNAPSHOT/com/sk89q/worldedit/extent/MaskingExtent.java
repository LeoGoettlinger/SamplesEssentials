package com.sk89q.worldedit.extent;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.extent.filter.block.CharFilterBlock;
import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.internal.simd.SimdSupport;
import com.fastasyncworldedit.core.internal.simd.VectorizedMask;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.function.LongFunction;
import javax.annotation.Nullable;

public class MaskingExtent extends AbstractDelegateExtent implements IBatchProcessor, Filter {
   private final LongFunction<ChunkFilterBlock> getOrCreateFilterBlock;
   private Mask mask;
   @Nullable
   private VectorizedMask vectorizedMask;

   public MaskingExtent(Extent extent, Mask mask) {
      super(extent);
      Preconditions.checkNotNull(mask);
      this.mask = mask;
      this.vectorizedMask = SimdSupport.vectorizedTargetMask(mask);
      this.getOrCreateFilterBlock = FaweCache.INSTANCE.createMainThreadSafeCache(() -> new CharFilterBlock(this.getExtent()));
   }

   private MaskingExtent(Extent extent, Mask mask, LongFunction<ChunkFilterBlock> getOrCreateFilterBlock, @Nullable VectorizedMask vectorizedMask) {
      super(extent);
      Preconditions.checkNotNull(mask);
      this.mask = mask;
      this.vectorizedMask = vectorizedMask;
      this.getOrCreateFilterBlock = getOrCreateFilterBlock;
   }

   public Mask getMask() {
      return this.mask;
   }

   public void setMask(Mask mask) {
      Preconditions.checkNotNull(mask);
      this.mask = mask;
      this.vectorizedMask = SimdSupport.vectorizedTargetMask(mask);
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      return this.mask.test(location) && super.setBlock(location, block);
   }

   @Override
   public boolean setBiome(BlockVector3 location, BiomeType biome) {
      return this.mask.test(location) && super.setBiome(location, biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return this.mask.test(BlockVector3.at(x, y, z)) && super.setBiome(x, y, z, biome);
   }

   @Override
   public IChunkSet processSet(final IChunk chunk, final IChunkGet get, final IChunkSet set) {
      if (this.vectorizedMask != null) {
         this.vectorizedMask.processChunks(chunk, get, set);
         return set;
      } else {
         ChunkFilterBlock filter = this.getOrCreateFilterBlock.apply(Thread.currentThread().getId());
         filter.initChunk(chunk.getX(), chunk.getZ());
         return filter.filter(chunk, get, set, this);
      }
   }

   @Override
   public void applyBlock(final FilterBlock block) {
      if (!this.mask.test(block)) {
         block.setOrdinal(0);
      }
   }

   @Override
   public Extent construct(Extent child) {
      return child == this.getExtent() ? this : new MaskingExtent(child, this.mask.copy(), this.getOrCreateFilterBlock, this.vectorizedMask);
   }

   @Override
   public Filter fork() {
      return new MaskingExtent(this.getExtent(), this.mask.copy(), this.getOrCreateFilterBlock, this.vectorizedMask);
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.REMOVING_BLOCKS;
   }
}
