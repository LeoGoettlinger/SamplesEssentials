package com.viaversion.viaversion.libs.fastutil.longs;

import com.viaversion.viaversion.libs.fastutil.longs.LongSortedSets.SynchronizedSortedSet;
import com.viaversion.viaversion.libs.fastutil.longs.LongSortedSets.UnmodifiableSortedSet;
import java.io.Serializable;
import java.util.NoSuchElementException;

public final class LongSortedSets {
   public static final LongSortedSets.EmptySet EMPTY_SET = new LongSortedSets.EmptySet();

   private LongSortedSets() {
   }

   public static LongSortedSet singleton(long element) {
      return new LongSortedSets.Singleton(element);
   }

   public static LongSortedSet singleton(long element, LongComparator comparator) {
      return new LongSortedSets.Singleton(element, comparator);
   }

   public static LongSortedSet singleton(Object element) {
      return new LongSortedSets.Singleton((Long)element);
   }

   public static LongSortedSet singleton(Object element, LongComparator comparator) {
      return new LongSortedSets.Singleton((Long)element, comparator);
   }

   public static LongSortedSet synchronize(LongSortedSet s) {
      return new SynchronizedSortedSet(s);
   }

   public static LongSortedSet synchronize(LongSortedSet s, Object sync) {
      return new SynchronizedSortedSet(s, sync);
   }

   public static LongSortedSet unmodifiable(LongSortedSet s) {
      return new UnmodifiableSortedSet(s);
   }

   public static class EmptySet extends LongSets.EmptySet implements LongSortedSet, Serializable, Cloneable {
      private static final long serialVersionUID = -7046029254386353129L;

      protected EmptySet() {
      }

      @Override
      public LongBidirectionalIterator iterator(long from) {
         return LongIterators.EMPTY_ITERATOR;
      }

      @Override
      public LongSortedSet subSet(long from, long to) {
         return LongSortedSets.EMPTY_SET;
      }

      @Override
      public LongSortedSet headSet(long from) {
         return LongSortedSets.EMPTY_SET;
      }

      @Override
      public LongSortedSet tailSet(long to) {
         return LongSortedSets.EMPTY_SET;
      }

      @Override
      public long firstLong() {
         throw new NoSuchElementException();
      }

      @Override
      public long lastLong() {
         throw new NoSuchElementException();
      }

      @Override
      public LongComparator comparator() {
         return null;
      }

      @Deprecated
      @Override
      public LongSortedSet subSet(Long from, Long to) {
         return LongSortedSets.EMPTY_SET;
      }

      @Deprecated
      @Override
      public LongSortedSet headSet(Long from) {
         return LongSortedSets.EMPTY_SET;
      }

      @Deprecated
      @Override
      public LongSortedSet tailSet(Long to) {
         return LongSortedSets.EMPTY_SET;
      }

      @Deprecated
      @Override
      public Long first() {
         throw new NoSuchElementException();
      }

      @Deprecated
      @Override
      public Long last() {
         throw new NoSuchElementException();
      }

      @Override
      public Object clone() {
         return LongSortedSets.EMPTY_SET;
      }

      private Object readResolve() {
         return LongSortedSets.EMPTY_SET;
      }
   }

   public static class Singleton extends LongSets.Singleton implements LongSortedSet, Serializable, Cloneable {
      private static final long serialVersionUID = -7046029254386353129L;
      final LongComparator comparator;

      protected Singleton(long element, LongComparator comparator) {
         super(element);
         this.comparator = comparator;
      }

      Singleton(long element) {
         this(element, null);
      }

      final int compare(long k1, long k2) {
         return this.comparator == null ? Long.compare(k1, k2) : this.comparator.compare(k1, k2);
      }

      @Override
      public LongBidirectionalIterator iterator(long from) {
         LongBidirectionalIterator i = this.iterator();
         if (this.compare(this.element, from) <= 0) {
            i.nextLong();
         }

         return i;
      }

      @Override
      public LongComparator comparator() {
         return this.comparator;
      }

      @Override
      public LongSpliterator spliterator() {
         return LongSpliterators.singleton(this.element, this.comparator);
      }

      @Override
      public LongSortedSet subSet(long from, long to) {
         return (LongSortedSet)(this.compare(from, this.element) <= 0 && this.compare(this.element, to) < 0 ? this : LongSortedSets.EMPTY_SET);
      }

      @Override
      public LongSortedSet headSet(long to) {
         return (LongSortedSet)(this.compare(this.element, to) < 0 ? this : LongSortedSets.EMPTY_SET);
      }

      @Override
      public LongSortedSet tailSet(long from) {
         return (LongSortedSet)(this.compare(from, this.element) <= 0 ? this : LongSortedSets.EMPTY_SET);
      }

      @Override
      public long firstLong() {
         return this.element;
      }

      @Override
      public long lastLong() {
         return this.element;
      }

      @Deprecated
      @Override
      public LongSortedSet subSet(Long from, Long to) {
         return this.subSet(from.longValue(), to.longValue());
      }

      @Deprecated
      @Override
      public LongSortedSet headSet(Long to) {
         return this.headSet(to.longValue());
      }

      @Deprecated
      @Override
      public LongSortedSet tailSet(Long from) {
         return this.tailSet(from.longValue());
      }

      @Deprecated
      @Override
      public Long first() {
         return this.element;
      }

      @Deprecated
      @Override
      public Long last() {
         return this.element;
      }
   }
}
