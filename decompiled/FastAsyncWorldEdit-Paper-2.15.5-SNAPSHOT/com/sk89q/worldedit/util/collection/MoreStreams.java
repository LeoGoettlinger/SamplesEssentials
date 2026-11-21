package com.sk89q.worldedit.util.collection;

import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MoreStreams {
   public static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<T> predicate) {
      return takeUntil(stream, predicate.negate());
   }

   public static <T> Stream<T> takeUntil(Stream<T> stream, Predicate<T> predicate) {
      final Spliterator<T> spliterator = stream.spliterator();
      Iterator<T> iter = new AbstractIterator<T>() {
         private Iterator<T> source = Spliterators.iterator(spliterator);

         protected T computeNext() {
            Iterator<T> src = Objects.requireNonNull(this.source);
            if (!src.hasNext()) {
               return (T)this.done();
            } else {
               T next = src.next();
               return (T)(predicate.test(next) ? this.done() : next);
            }
         }

         private T done() {
            this.source = null;
            return (T)this.endOfData();
         }
      };
      int chars = spliterator.characteristics();
      chars &= -16449;
      return StreamSupport.stream(Spliterators.spliterator(iter, spliterator.estimateSize(), chars), stream.isParallel()).onClose(stream::close);
   }

   private MoreStreams() {
   }
}
