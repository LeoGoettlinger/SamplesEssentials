package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.CheckForNull;

@FunctionalInterface
@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface Function<F, T> extends java.util.function.Function<F, T> {
   @ParametricNullness
   @Override
   T apply(@ParametricNullness F input);

   @Override
   boolean equals(@CheckForNull Object object);
}
