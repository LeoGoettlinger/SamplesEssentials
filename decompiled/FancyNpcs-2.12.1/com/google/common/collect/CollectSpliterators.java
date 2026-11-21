package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.j2objc.annotations.Weak;
import java.util.Comparator;
import java.util.Spliterator;
import java.util.Spliterator.OfDouble;
import java.util.Spliterator.OfInt;
import java.util.Spliterator.OfLong;
import java.util.Spliterator.OfPrimitive;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
final class CollectSpliterators {
   private CollectSpliterators() {
   }

   static <T> Spliterator<T> indexed(int size, int extraCharacteristics, IntFunction<T> function) {
      return indexed(size, extraCharacteristics, function, null);
   }

   static <T> Spliterator<T> indexed(int size, int extraCharacteristics, IntFunction<T> function, @CheckForNull Comparator<? super T> comparator) {
      if (comparator != null) {
         Preconditions.checkArgument((extraCharacteristics & 4) != 0);
      }

      class WithCharacteristics implements Spliterator<T> {
         private final OfInt delegate;

         WithCharacteristics(OfInt delegate) {
            this.delegate = delegate;
         }

         @Override
         public boolean tryAdvance(Consumer<? super T> action) {
            return this.delegate.tryAdvance(i -> action.accept(function.apply(i)));
         }

         @Override
         public void forEachRemaining(Consumer<? super T> action) {
            this.delegate.forEachRemaining(i -> action.accept(function.apply(i)));
         }

         @CheckForNull
         @Override
         public Spliterator<T> trySplit() {
            OfInt split = this.delegate.trySplit();
            return split == null ? null : new WithCharacteristics(split);
         }

         @Override
         public long estimateSize() {
            return this.delegate.estimateSize();
         }

         @Override
         public int characteristics() {
            return 16464 | extraCharacteristics;
         }

         @CheckForNull
         @Override
         public Comparator<? super T> getComparator() {
            if (this.hasCharacteristics(4)) {
               return comparator;
            } else {
               throw new IllegalStateException();
            }
         }
      }

      return new WithCharacteristics(IntStream.range(0, size).spliterator());
   }

   static <InElementT, OutElementT> Spliterator<OutElementT> map(
      Spliterator<InElementT> fromSpliterator, Function<? super InElementT, ? extends OutElementT> function
   ) {
      Preconditions.checkNotNull(fromSpliterator);
      Preconditions.checkNotNull(function);
      return new Spliterator<OutElementT>() {
         @Override
         public boolean tryAdvance(Consumer<? super OutElementT> action) {
            return fromSpliterator.tryAdvance(fromElement -> action.accept((OutElementT)function.apply(fromElement)));
         }

         @Override
         public void forEachRemaining(Consumer<? super OutElementT> action) {
            fromSpliterator.forEachRemaining(fromElement -> action.accept((OutElementT)function.apply(fromElement)));
         }

         @CheckForNull
         @Override
         public Spliterator<OutElementT> trySplit() {
            Spliterator<InElementT> fromSplit = fromSpliterator.trySplit();
            return fromSplit != null ? CollectSpliterators.map(fromSplit, function) : null;
         }

         @Override
         public long estimateSize() {
            return fromSpliterator.estimateSize();
         }

         @Override
         public int characteristics() {
            return fromSpliterator.characteristics() & -262;
         }
      };
   }

   static <T> Spliterator<T> filter(Spliterator<T> fromSpliterator, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(fromSpliterator);
      Preconditions.checkNotNull(predicate);

      class Splitr implements Spliterator<T>, Consumer<T> {
         @CheckForNull
         T holder = (T)null;

         @Override
         public void accept(@ParametricNullness T t) {
            this.holder = t;
         }

         @Override
         public boolean tryAdvance(Consumer<? super T> action) {
            while (fromSpliterator.tryAdvance(this)) {
               boolean var3;
               try {
                  T next = NullnessCasts.uncheckedCastNullableTToT(this.holder);
                  if (!predicate.test(next)) {
                     continue;
                  }

                  action.accept(next);
                  var3 = true;
               } finally {
                  this.holder = null;
               }

               return var3;
            }

            return false;
         }

         @CheckForNull
         @Override
         public Spliterator<T> trySplit() {
            Spliterator<T> fromSplit = fromSpliterator.trySplit();
            return fromSplit == null ? null : CollectSpliterators.filter(fromSplit, predicate);
         }

         @Override
         public long estimateSize() {
            return fromSpliterator.estimateSize() / 2L;
         }

         @CheckForNull
         @Override
         public Comparator<? super T> getComparator() {
            return fromSpliterator.getComparator();
         }

         @Override
         public int characteristics() {
            return fromSpliterator.characteristics() & 277;
         }
      }

      return new Splitr();
   }

   static <InElementT, OutElementT> Spliterator<OutElementT> flatMap(
      Spliterator<InElementT> fromSpliterator, Function<? super InElementT, Spliterator<OutElementT>> function, int topCharacteristics, long topSize
   ) {
      Preconditions.checkArgument((topCharacteristics & 16384) == 0, "flatMap does not support SUBSIZED characteristic");
      Preconditions.checkArgument((topCharacteristics & 4) == 0, "flatMap does not support SORTED characteristic");
      Preconditions.checkNotNull(fromSpliterator);
      Preconditions.checkNotNull(function);
      return new CollectSpliterators.FlatMapSpliteratorOfObject<>(null, fromSpliterator, function, topCharacteristics, topSize);
   }

   static <InElementT> OfInt flatMapToInt(
      Spliterator<InElementT> fromSpliterator, Function<? super InElementT, OfInt> function, int topCharacteristics, long topSize
   ) {
      Preconditions.checkArgument((topCharacteristics & 16384) == 0, "flatMap does not support SUBSIZED characteristic");
      Preconditions.checkArgument((topCharacteristics & 4) == 0, "flatMap does not support SORTED characteristic");
      Preconditions.checkNotNull(fromSpliterator);
      Preconditions.checkNotNull(function);
      return new CollectSpliterators.FlatMapSpliteratorOfInt<>(null, fromSpliterator, function, topCharacteristics, topSize);
   }

   static <InElementT> OfLong flatMapToLong(
      Spliterator<InElementT> fromSpliterator, Function<? super InElementT, OfLong> function, int topCharacteristics, long topSize
   ) {
      Preconditions.checkArgument((topCharacteristics & 16384) == 0, "flatMap does not support SUBSIZED characteristic");
      Preconditions.checkArgument((topCharacteristics & 4) == 0, "flatMap does not support SORTED characteristic");
      Preconditions.checkNotNull(fromSpliterator);
      Preconditions.checkNotNull(function);
      return new CollectSpliterators.FlatMapSpliteratorOfLong<>(null, fromSpliterator, function, topCharacteristics, topSize);
   }

   static <InElementT> OfDouble flatMapToDouble(
      Spliterator<InElementT> fromSpliterator, Function<? super InElementT, OfDouble> function, int topCharacteristics, long topSize
   ) {
      Preconditions.checkArgument((topCharacteristics & 16384) == 0, "flatMap does not support SUBSIZED characteristic");
      Preconditions.checkArgument((topCharacteristics & 4) == 0, "flatMap does not support SORTED characteristic");
      Preconditions.checkNotNull(fromSpliterator);
      Preconditions.checkNotNull(function);
      return new CollectSpliterators.FlatMapSpliteratorOfDouble<>(null, fromSpliterator, function, topCharacteristics, topSize);
   }

   abstract static class FlatMapSpliterator<InElementT, OutElementT, OutSpliteratorT extends Spliterator<OutElementT>> implements Spliterator<OutElementT> {
      @CheckForNull
      @Weak
      OutSpliteratorT prefix;
      final Spliterator<InElementT> from;
      final Function<? super InElementT, OutSpliteratorT> function;
      final CollectSpliterators.FlatMapSpliterator.Factory<InElementT, OutSpliteratorT> factory;
      int characteristics;
      long estimatedSize;

      FlatMapSpliterator(
         @CheckForNull OutSpliteratorT prefix,
         Spliterator<InElementT> from,
         Function<? super InElementT, OutSpliteratorT> function,
         CollectSpliterators.FlatMapSpliterator.Factory<InElementT, OutSpliteratorT> factory,
         int characteristics,
         long estimatedSize
      ) {
         this.prefix = prefix;
         this.from = from;
         this.function = function;
         this.factory = factory;
         this.characteristics = characteristics;
         this.estimatedSize = estimatedSize;
      }

      @Override
      public final boolean tryAdvance(Consumer<? super OutElementT> action) {
         while (this.prefix == null || !this.prefix.tryAdvance(action)) {
            this.prefix = null;
            if (!this.from.tryAdvance(fromElement -> this.prefix = this.function.apply(fromElement))) {
               return false;
            }
         }

         if (this.estimatedSize != Long.MAX_VALUE) {
            this.estimatedSize--;
         }

         return true;
      }

      @Override
      public final void forEachRemaining(Consumer<? super OutElementT> action) {
         if (this.prefix != null) {
            this.prefix.forEachRemaining(action);
            this.prefix = null;
         }

         this.from.forEachRemaining(fromElement -> {
            Spliterator<OutElementT> elements = this.function.apply(fromElement);
            if (elements != null) {
               elements.forEachRemaining(action);
            }
         });
         this.estimatedSize = 0L;
      }

      @CheckForNull
      @Override
      public final OutSpliteratorT trySplit() {
         Spliterator<InElementT> fromSplit = this.from.trySplit();
         if (fromSplit != null) {
            int splitCharacteristics = this.characteristics & -65;
            long estSplitSize = this.estimateSize();
            if (estSplitSize < Long.MAX_VALUE) {
               estSplitSize /= 2L;
               this.estimatedSize -= estSplitSize;
               this.characteristics = splitCharacteristics;
            }

            OutSpliteratorT result = this.factory.newFlatMapSpliterator(this.prefix, fromSplit, this.function, splitCharacteristics, estSplitSize);
            this.prefix = null;
            return result;
         } else if (this.prefix != null) {
            OutSpliteratorT result = this.prefix;
            this.prefix = null;
            return result;
         } else {
            return null;
         }
      }

      @Override
      public final long estimateSize() {
         if (this.prefix != null) {
            this.estimatedSize = Math.max(this.estimatedSize, this.prefix.estimateSize());
         }

         return Math.max(this.estimatedSize, 0L);
      }

      @Override
      public final int characteristics() {
         return this.characteristics;
      }

      @FunctionalInterface
      interface Factory<InElementT, OutSpliteratorT extends Spliterator<?>> {
         OutSpliteratorT newFlatMapSpliterator(
            @CheckForNull OutSpliteratorT prefix,
            Spliterator<InElementT> fromSplit,
            Function<? super InElementT, OutSpliteratorT> function,
            int splitCharacteristics,
            long estSplitSize
         );
      }
   }

   static final class FlatMapSpliteratorOfDouble<InElementT>
      extends CollectSpliterators.FlatMapSpliteratorOfPrimitive<InElementT, Double, DoubleConsumer, OfDouble>
      implements OfDouble {
      FlatMapSpliteratorOfDouble(
         @CheckForNull OfDouble prefix, Spliterator<InElementT> from, Function<? super InElementT, OfDouble> function, int characteristics, long estimatedSize
      ) {
         super(prefix, from, function, CollectSpliterators.FlatMapSpliteratorOfDouble::new, characteristics, estimatedSize);
      }
   }

   static final class FlatMapSpliteratorOfInt<InElementT>
      extends CollectSpliterators.FlatMapSpliteratorOfPrimitive<InElementT, Integer, IntConsumer, OfInt>
      implements OfInt {
      FlatMapSpliteratorOfInt(
         @CheckForNull OfInt prefix, Spliterator<InElementT> from, Function<? super InElementT, OfInt> function, int characteristics, long estimatedSize
      ) {
         super(prefix, from, function, CollectSpliterators.FlatMapSpliteratorOfInt::new, characteristics, estimatedSize);
      }
   }

   static final class FlatMapSpliteratorOfLong<InElementT>
      extends CollectSpliterators.FlatMapSpliteratorOfPrimitive<InElementT, Long, LongConsumer, OfLong>
      implements OfLong {
      FlatMapSpliteratorOfLong(
         @CheckForNull OfLong prefix, Spliterator<InElementT> from, Function<? super InElementT, OfLong> function, int characteristics, long estimatedSize
      ) {
         super(prefix, from, function, CollectSpliterators.FlatMapSpliteratorOfLong::new, characteristics, estimatedSize);
      }
   }

   static final class FlatMapSpliteratorOfObject<InElementT, OutElementT>
      extends CollectSpliterators.FlatMapSpliterator<InElementT, OutElementT, Spliterator<OutElementT>> {
      FlatMapSpliteratorOfObject(
         @CheckForNull Spliterator<OutElementT> prefix,
         Spliterator<InElementT> from,
         Function<? super InElementT, Spliterator<OutElementT>> function,
         int characteristics,
         long estimatedSize
      ) {
         super(prefix, from, function, CollectSpliterators.FlatMapSpliteratorOfObject::new, characteristics, estimatedSize);
      }
   }

   abstract static class FlatMapSpliteratorOfPrimitive<InElementT, OutElementT, OutConsumerT, OutSpliteratorT extends OfPrimitive<OutElementT, OutConsumerT, OutSpliteratorT>>
      extends CollectSpliterators.FlatMapSpliterator<InElementT, OutElementT, OutSpliteratorT>
      implements OfPrimitive<OutElementT, OutConsumerT, OutSpliteratorT> {
      FlatMapSpliteratorOfPrimitive(
         @CheckForNull OutSpliteratorT prefix,
         Spliterator<InElementT> from,
         Function<? super InElementT, OutSpliteratorT> function,
         CollectSpliterators.FlatMapSpliterator.Factory<InElementT, OutSpliteratorT> factory,
         int characteristics,
         long estimatedSize
      ) {
         super(prefix, from, function, factory, characteristics, estimatedSize);
      }

      @Override
      public final boolean tryAdvance(OutConsumerT action) {
         while (this.prefix == null || !this.prefix.tryAdvance(action)) {
            this.prefix = null;
            if (!this.from.tryAdvance(fromElement -> this.prefix = this.function.apply(fromElement))) {
               return false;
            }
         }

         if (this.estimatedSize != Long.MAX_VALUE) {
            this.estimatedSize--;
         }

         return true;
      }

      @Override
      public final void forEachRemaining(OutConsumerT action) {
         if (this.prefix != null) {
            this.prefix.forEachRemaining(action);
            this.prefix = null;
         }

         this.from.forEachRemaining(fromElement -> {
            OutSpliteratorT elements = this.function.apply(fromElement);
            if (elements != null) {
               elements.forEachRemaining(action);
            }
         });
         this.estimatedSize = 0L;
      }
   }
}
