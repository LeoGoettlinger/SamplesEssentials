package com.fastasyncworldedit.core.extent.filter;

import com.fastasyncworldedit.core.extent.filter.block.DelegateFilter;
import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.internal.simd.SimdSupport;
import com.fastasyncworldedit.core.internal.simd.VectorFacade;
import com.fastasyncworldedit.core.internal.simd.VectorizedFilter;
import com.fastasyncworldedit.core.internal.simd.VectorizedMask;
import com.fastasyncworldedit.core.queue.Filter;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class MaskFilter<T extends Filter> extends DelegateFilter<T> {
   final Mask mask;
   final AtomicInteger changes;

   public MaskFilter(T other, Mask root) {
      this(other, root, new AtomicInteger());
   }

   public MaskFilter(T other, Mask root, AtomicInteger changes) {
      super(other);
      this.mask = root;
      this.changes = changes;
   }

   @Override
   public void applyBlock(FilterBlock block) {
      if (this.mask instanceof AbstractExtentMask) {
         if (((AbstractExtentMask)this.mask).test(block.getExtent(), block)) {
            this.getParent().applyBlock(block);
            this.changes.incrementAndGet();
         }
      } else if (this.mask.test(block)) {
         this.getParent().applyBlock(block);
         this.changes.incrementAndGet();
      }
   }

   public int getBlocksApplied() {
      return this.changes.get();
   }

   public MaskFilter<?> newInstance(Filter other) {
      return new MaskFilter<>(other, this.mask);
   }

   @Override
   public Filter fork() {
      return new MaskFilter<>(this.getParent().fork(), this.mask.copy(), this.changes);
   }

   public static class VectorizedMaskFilter<T extends VectorizedFilter> extends MaskFilter<T> implements VectorizedFilter {
      private final VectorizedMask vectorizedMask;

      public VectorizedMaskFilter(final T other, final Mask root) {
         super(other, root);
         this.vectorizedMask = Objects.requireNonNull(SimdSupport.vectorizedTargetMask(root), "invalid vectorizable mask");
      }

      public VectorizedMaskFilter(final T other, final Mask root, AtomicInteger changes) {
         super(other, root, changes);
         this.vectorizedMask = Objects.requireNonNull(SimdSupport.vectorizedTargetMask(root), "invalid vectorizable mask");
      }

      @Override
      public void applyVector(final VectorFacade get, final VectorFacade set, final VectorMask<Short> mask) {
         T parent = (T)((VectorizedFilter)this.getParent());
         VectorSpecies<Short> species = mask.vectorSpecies();
         VectorMask<Short> masked = this.vectorizedMask.compareVector(set, get, species);
         ShortVector before = set.getOrZero(masked.vectorSpecies());
         parent.applyVector(get, set, mask.and(masked));
         ShortVector after = set.getOrZero(masked.vectorSpecies());
         VectorMask<Short> changed = after.compare(VectorOperators.NE, before);
         this.changes.getAndAdd(changed.trueCount());
      }

      @Override
      public MaskFilter<?> newInstance(final Filter other) {
         return (MaskFilter<?>)(other instanceof VectorizedFilter o ? new MaskFilter.VectorizedMaskFilter<>(o, this.mask) : super.newInstance(other));
      }

      @Override
      public Filter fork() {
         return new MaskFilter.VectorizedMaskFilter((T)((VectorizedFilter)((VectorizedFilter)this.getParent()).fork()), this.mask.copy(), this.changes);
      }
   }
}
