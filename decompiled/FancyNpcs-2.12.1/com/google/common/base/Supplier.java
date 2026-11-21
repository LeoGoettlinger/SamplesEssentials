package com.google.common.base;

import com.google.common.annotations.GwtCompatible;

@FunctionalInterface
@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface Supplier<T> extends java.util.function.Supplier<T> {
   @ParametricNullness
   @Override
   T get();
}
