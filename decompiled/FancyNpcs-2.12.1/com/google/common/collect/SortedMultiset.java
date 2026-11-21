package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   emulated = true
)
public interface SortedMultiset<E> extends SortedMultisetBridge<E>, SortedIterable<E> {
   @Override
   Comparator<? super E> comparator();

   @CheckForNull
   Multiset.Entry<E> firstEntry();

   @CheckForNull
   Multiset.Entry<E> lastEntry();

   @CheckForNull
   Multiset.Entry<E> pollFirstEntry();

   @CheckForNull
   Multiset.Entry<E> pollLastEntry();

   NavigableSet<E> elementSet();

   @Override
   Set<Multiset.Entry<E>> entrySet();

   @Override
   Iterator<E> iterator();

   SortedMultiset<E> descendingMultiset();

   SortedMultiset<E> headMultiset(@ParametricNullness E upperBound, BoundType boundType);

   SortedMultiset<E> subMultiset(@ParametricNullness E lowerBound, BoundType lowerBoundType, @ParametricNullness E upperBound, BoundType upperBoundType);

   SortedMultiset<E> tailMultiset(@ParametricNullness E lowerBound, BoundType boundType);
}
