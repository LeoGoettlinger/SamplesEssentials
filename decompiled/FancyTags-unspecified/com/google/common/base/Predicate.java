package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.CheckForNull;

@FunctionalInterface
@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface Predicate<T> extends java.util.function.Predicate<T> {
   boolean apply(@ParametricNullness T input);

   @Override
   boolean equals(@CheckForNull Object object);

   @Override
   default boolean test(@ParametricNullness T input) {
      return this.apply(input);
   }
}
