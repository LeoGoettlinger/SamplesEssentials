package com.fastasyncworldedit.core.extent.filter;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.internal.simd.VectorFacade;
import com.fastasyncworldedit.core.internal.simd.VectorizedFilter;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IChunk;
import com.sk89q.worldedit.regions.Region;
import jdk.incubator.vector.VectorMask;
import org.jetbrains.annotations.Nullable;

public sealed class LinkedFilter<L extends Filter, R extends Filter> implements Filter permits LinkedFilter.VectorizedLinkedFilter {
   private final L left;
   private final R right;

   public LinkedFilter(L left, R right) {
      this.left = left;
      this.right = right;
   }

   public static <L extends Filter, R extends Filter> LinkedFilter<? extends L, ? extends R> of(L left, R right) {
      return (LinkedFilter<? extends L, ? extends R>)(left instanceof VectorizedFilter l && right instanceof VectorizedFilter r
         ? new LinkedFilter.VectorizedLinkedFilter<>(l, r)
         : new LinkedFilter<>(left, right));
   }

   public L getLeft() {
      return this.left;
   }

   public R getRight() {
      return this.right;
   }

   @Override
   public <T extends IChunk> T applyChunk(T chunk, @Nullable Region region) {
      chunk = this.getLeft().applyChunk(chunk, region);
      return this.getRight().applyChunk(chunk, region);
   }

   @Override
   public void applyBlock(FilterBlock block) {
      this.getLeft().applyBlock(block);
      this.getRight().applyBlock(block);
   }

   @Override
   public void finishChunk(IChunk chunk) {
      this.getLeft().finishChunk(chunk);
      this.getRight().finishChunk(chunk);
   }

   @Override
   public Filter fork() {
      return new LinkedFilter<>(this.getLeft().fork(), this.getRight().fork());
   }

   @Override
   public void join() {
      this.getLeft().join();
      this.getRight().join();
   }

   private static final class VectorizedLinkedFilter<L extends VectorizedFilter, R extends VectorizedFilter>
      extends LinkedFilter<L, R>
      implements VectorizedFilter {
      public VectorizedLinkedFilter(final L left, final R right) {
         super(left, right);
      }

      @Override
      public void applyVector(final VectorFacade get, final VectorFacade set, final VectorMask<Short> mask) {
         ((VectorizedFilter)this.getLeft()).applyVector(get, set, mask);
         ((VectorizedFilter)this.getRight()).applyVector(get, set, mask);
      }

      @Override
      public Filter fork() {
         return new LinkedFilter.VectorizedLinkedFilter(
            (L)((VectorizedFilter)((VectorizedFilter)this.getLeft()).fork()), (R)((VectorizedFilter)((VectorizedFilter)this.getRight()).fork())
         );
      }
   }
}
