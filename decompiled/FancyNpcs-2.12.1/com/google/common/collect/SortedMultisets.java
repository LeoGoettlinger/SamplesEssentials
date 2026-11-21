package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.j2objc.annotations.Weak;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   emulated = true
)
final class SortedMultisets {
   private SortedMultisets() {
   }

   private static <E> E getElementOrThrow(@CheckForNull Multiset.Entry<E> entry) {
      if (entry == null) {
         throw new NoSuchElementException();
      } else {
         return entry.getElement();
      }
   }

   @CheckForNull
   private static <E> E getElementOrNull(@CheckForNull Multiset.Entry<E> entry) {
      return entry == null ? null : entry.getElement();
   }

   static class ElementSet<E> extends Multisets.ElementSet<E> implements SortedSet<E> {
      @Weak
      private final SortedMultiset<E> multiset;

      ElementSet(SortedMultiset<E> multiset) {
         this.multiset = multiset;
      }

      final SortedMultiset<E> multiset() {
         return this.multiset;
      }

      @Override
      public Iterator<E> iterator() {
         return Multisets.elementIterator(this.multiset().entrySet().iterator());
      }

      @Override
      public Comparator<? super E> comparator() {
         return this.multiset().comparator();
      }

      @Override
      public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
         return this.multiset().subMultiset(fromElement, BoundType.CLOSED, toElement, BoundType.OPEN).elementSet();
      }

      @Override
      public SortedSet<E> headSet(@ParametricNullness E toElement) {
         return this.multiset().headMultiset(toElement, BoundType.OPEN).elementSet();
      }

      @Override
      public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
         return this.multiset().tailMultiset(fromElement, BoundType.CLOSED).elementSet();
      }

      @ParametricNullness
      @Override
      public E first() {
         return SortedMultisets.getElementOrThrow(this.multiset().firstEntry());
      }

      @ParametricNullness
      @Override
      public E last() {
         return SortedMultisets.getElementOrThrow(this.multiset().lastEntry());
      }
   }

   @GwtIncompatible
   static class NavigableElementSet<E> extends SortedMultisets.ElementSet<E> implements NavigableSet<E> {
      NavigableElementSet(SortedMultiset<E> multiset) {
         super(multiset);
      }

      @CheckForNull
      @Override
      public E lower(@ParametricNullness E e) {
         return SortedMultisets.getElementOrNull(this.multiset().headMultiset(e, BoundType.OPEN).lastEntry());
      }

      @CheckForNull
      @Override
      public E floor(@ParametricNullness E e) {
         return SortedMultisets.getElementOrNull(this.multiset().headMultiset(e, BoundType.CLOSED).lastEntry());
      }

      @CheckForNull
      @Override
      public E ceiling(@ParametricNullness E e) {
         return SortedMultisets.getElementOrNull(this.multiset().tailMultiset(e, BoundType.CLOSED).firstEntry());
      }

      @CheckForNull
      @Override
      public E higher(@ParametricNullness E e) {
         return SortedMultisets.getElementOrNull(this.multiset().tailMultiset(e, BoundType.OPEN).firstEntry());
      }

      @Override
      public NavigableSet<E> descendingSet() {
         return new SortedMultisets.NavigableElementSet<>(this.multiset().descendingMultiset());
      }

      @Override
      public Iterator<E> descendingIterator() {
         return this.descendingSet().iterator();
      }

      @CheckForNull
      @Override
      public E pollFirst() {
         return SortedMultisets.getElementOrNull(this.multiset().pollFirstEntry());
      }

      @CheckForNull
      @Override
      public E pollLast() {
         return SortedMultisets.getElementOrNull(this.multiset().pollLastEntry());
      }

      @Override
      public NavigableSet<E> subSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
         return new SortedMultisets.NavigableElementSet<>(
            this.multiset().subMultiset(fromElement, BoundType.forBoolean(fromInclusive), toElement, BoundType.forBoolean(toInclusive))
         );
      }

      @Override
      public NavigableSet<E> headSet(@ParametricNullness E toElement, boolean inclusive) {
         return new SortedMultisets.NavigableElementSet<>(this.multiset().headMultiset(toElement, BoundType.forBoolean(inclusive)));
      }

      @Override
      public NavigableSet<E> tailSet(@ParametricNullness E fromElement, boolean inclusive) {
         return new SortedMultisets.NavigableElementSet<>(this.multiset().tailMultiset(fromElement, BoundType.forBoolean(inclusive)));
      }
   }
}
