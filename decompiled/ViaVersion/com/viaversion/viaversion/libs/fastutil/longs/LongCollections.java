package com.viaversion.viaversion.libs.fastutil.longs;

import com.viaversion.viaversion.libs.fastutil.longs.LongCollections.SynchronizedCollection;
import com.viaversion.viaversion.libs.fastutil.longs.LongCollections.UnmodifiableCollection;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectArrays;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class LongCollections {
   private LongCollections() {
   }

   public static LongCollection synchronize(LongCollection c) {
      return new SynchronizedCollection(c);
   }

   public static LongCollection synchronize(LongCollection c, Object sync) {
      return new SynchronizedCollection(c, sync);
   }

   public static LongCollection unmodifiable(LongCollection c) {
      return new UnmodifiableCollection(c);
   }

   public static LongCollection asCollection(LongIterable iterable) {
      return (LongCollection)(iterable instanceof LongCollection ? (LongCollection)iterable : new LongCollections.IterableCollection(iterable));
   }

   public abstract static class EmptyCollection extends AbstractLongCollection {
      protected EmptyCollection() {
      }

      @Override
      public boolean contains(long k) {
         return false;
      }

      @Override
      public Object[] toArray() {
         return ObjectArrays.EMPTY_ARRAY;
      }

      @Override
      public <T> T[] toArray(T[] array) {
         if (array.length > 0) {
            array[0] = null;
         }

         return array;
      }

      public LongBidirectionalIterator iterator() {
         return LongIterators.EMPTY_ITERATOR;
      }

      @Override
      public LongSpliterator spliterator() {
         return LongSpliterators.EMPTY_SPLITERATOR;
      }

      @Override
      public int size() {
         return 0;
      }

      @Override
      public void clear() {
      }

      @Override
      public int hashCode() {
         return 0;
      }

      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else {
            return !(o instanceof Collection) ? false : ((Collection)o).isEmpty();
         }
      }

      @Deprecated
      @Override
      public void forEach(Consumer<? super Long> action) {
      }

      @Override
      public boolean containsAll(Collection<?> c) {
         return c.isEmpty();
      }

      @Override
      public boolean addAll(Collection<? extends Long> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Deprecated
      @Override
      public boolean removeIf(Predicate<? super Long> filter) {
         return false;
      }

      @Override
      public long[] toLongArray() {
         return LongArrays.EMPTY_ARRAY;
      }

      @Deprecated
      @Override
      public long[] toLongArray(long[] a) {
         return a;
      }

      @Override
      public void forEach(java.util.function.LongConsumer action) {
      }

      @Override
      public boolean containsAll(LongCollection c) {
         return c.isEmpty();
      }

      @Override
      public boolean addAll(LongCollection c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(LongCollection c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(LongCollection c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeIf(java.util.function.LongPredicate filter) {
         return false;
      }
   }

   public static class IterableCollection extends AbstractLongCollection implements Serializable {
      private static final long serialVersionUID = -7046029254386353129L;
      protected final LongIterable iterable;

      protected IterableCollection(LongIterable iterable) {
         this.iterable = Objects.requireNonNull(iterable);
      }

      @Override
      public int size() {
         long size = this.iterable.spliterator().getExactSizeIfKnown();
         if (size >= 0L) {
            return (int)Math.min(2147483647L, size);
         } else {
            int c = 0;

            for (LongIterator iterator = this.iterator(); iterator.hasNext(); c++) {
               iterator.nextLong();
            }

            return c;
         }
      }

      @Override
      public boolean isEmpty() {
         return !this.iterable.iterator().hasNext();
      }

      @Override
      public LongIterator iterator() {
         return this.iterable.iterator();
      }

      @Override
      public LongSpliterator spliterator() {
         return this.iterable.spliterator();
      }

      @Override
      public LongIterator longIterator() {
         return this.iterable.longIterator();
      }

      @Override
      public LongSpliterator longSpliterator() {
         return this.iterable.longSpliterator();
      }
   }

   static class SizeDecreasingSupplier<C extends LongCollection> implements Supplier<C> {
      static final int RECOMMENDED_MIN_SIZE = 8;
      final AtomicInteger suppliedCount = new AtomicInteger(0);
      final int expectedFinalSize;
      final IntFunction<C> builder;

      SizeDecreasingSupplier(int expectedFinalSize, IntFunction<C> builder) {
         this.expectedFinalSize = expectedFinalSize;
         this.builder = builder;
      }

      public C get() {
         int expectedNeededNextSize = 1 + (this.expectedFinalSize - 1) / this.suppliedCount.incrementAndGet();
         if (expectedNeededNextSize < 0) {
            expectedNeededNextSize = 8;
         }

         return this.builder.apply(expectedNeededNextSize);
      }
   }
}
