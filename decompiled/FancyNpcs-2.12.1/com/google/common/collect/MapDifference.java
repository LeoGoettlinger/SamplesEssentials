package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import javax.annotation.CheckForNull;

@DoNotMock("Use Maps.difference")
@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface MapDifference<K, V> {
   boolean areEqual();

   Map<K, V> entriesOnlyOnLeft();

   Map<K, V> entriesOnlyOnRight();

   Map<K, V> entriesInCommon();

   Map<K, MapDifference.ValueDifference<V>> entriesDiffering();

   @Override
   boolean equals(@CheckForNull Object object);

   @Override
   int hashCode();

   @DoNotMock("Use Maps.difference")
   public interface ValueDifference<V> {
      @ParametricNullness
      V leftValue();

      @ParametricNullness
      V rightValue();

      @Override
      boolean equals(@CheckForNull Object other);

      @Override
      int hashCode();
   }
}
