package com.sk89q.worldedit.function.mask;

import com.fastasyncworldedit.core.extent.filter.MaskFilter;
import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.function.mask.InverseMask;
import com.fastasyncworldedit.core.internal.simd.SimdSupport;
import com.fastasyncworldedit.core.internal.simd.VectorizedFilter;
import com.fastasyncworldedit.core.internal.simd.VectorizedMask;
import com.fastasyncworldedit.core.queue.Filter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public interface Mask {
   boolean test(BlockVector3 vector);

   @Nullable
   default Mask2D toMask2D() {
      return null;
   }

   @Nullable
   default Mask tryOptimize() {
      return null;
   }

   default <T extends Filter> MaskFilter<T> toFilter(T filter) {
      VectorizedMask mask = SimdSupport.vectorizedTargetMask(this);
      if (mask != null) {
         VectorizedFilter vectorizedFilter = null;
         if (filter instanceof VectorizedFilter vf) {
            vectorizedFilter = vf;
         } else if (filter instanceof Pattern p) {
            vectorizedFilter = SimdSupport.vectorizedPattern(p);
         }

         if (vectorizedFilter != null) {
            return (MaskFilter<T>)(new MaskFilter.VectorizedMaskFilter<>(vectorizedFilter, this));
         }
      }

      return new MaskFilter<>(filter, this);
   }

   default Mask tryCombine(Mask other) {
      return null;
   }

   default Mask tryOr(Mask other) {
      return null;
   }

   default Mask optimize() {
      Mask value = this.tryOptimize();
      return value == null ? this : value;
   }

   default Mask inverse() {
      if (this instanceof Masks.AlwaysTrue) {
         return Masks.ALWAYS_FALSE;
      } else if (this instanceof Masks.AlwaysFalse) {
         return Masks.ALWAYS_TRUE;
      } else {
         return (Mask)(this instanceof Masks.NegatedMask ? ((Masks.NegatedMask)this).mask() : new InverseMask(this));
      }
   }

   default Filter toFilter(Consumer<FilterBlock> run) {
      return new Filter() {
         @Override
         public void applyBlock(FilterBlock block) {
            if (Mask.this.test(block)) {
               run.accept(block);
            }
         }
      };
   }

   default boolean replacesAir() {
      return false;
   }

   Mask copy();
}
