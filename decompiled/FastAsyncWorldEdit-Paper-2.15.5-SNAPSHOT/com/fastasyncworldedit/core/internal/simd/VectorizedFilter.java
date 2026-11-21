package com.fastasyncworldedit.core.internal.simd;

import com.fastasyncworldedit.core.queue.Filter;
import jdk.incubator.vector.VectorMask;

public interface VectorizedFilter extends Filter {
   void applyVector(VectorFacade get, VectorFacade set, VectorMask<Short> mask);
}
