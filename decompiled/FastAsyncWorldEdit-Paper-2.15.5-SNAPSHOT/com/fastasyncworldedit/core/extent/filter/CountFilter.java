package com.fastasyncworldedit.core.extent.filter;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.internal.simd.VectorFacade;
import com.fastasyncworldedit.core.internal.simd.VectorizedFilter;
import jdk.incubator.vector.VectorMask;

public class CountFilter extends ForkedFilter<CountFilter> implements VectorizedFilter {
   private int total;

   public CountFilter() {
      super(null);
   }

   private CountFilter(CountFilter root) {
      super(root);
   }

   public CountFilter init() {
      return new CountFilter(this);
   }

   public void join(CountFilter filter) {
      this.total = this.total + filter.getTotal();
   }

   @Override
   public final void applyBlock(FilterBlock block) {
      this.total++;
   }

   public int getTotal() {
      return this.total;
   }

   @Override
   public void applyVector(final VectorFacade get, final VectorFacade set, final VectorMask<Short> mask) {
      this.total = this.total + mask.trueCount();
   }
}
