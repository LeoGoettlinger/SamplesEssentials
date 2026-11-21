package de.theredend2000.advancedhunt.lib.kyori.adventure.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Supplier;

public final class ForwardingIterator<T> implements Iterable<T> {
   private final Supplier<Iterator<T>> iterator;
   private final Supplier<Spliterator<T>> spliterator;

   public ForwardingIterator(final Supplier<Iterator<T>> iterator, final Supplier<Spliterator<T>> spliterator) {
      this.iterator = Objects.requireNonNull(iterator, "iterator");
      this.spliterator = Objects.requireNonNull(spliterator, "spliterator");
   }

   @Override
   public Iterator<T> iterator() {
      return this.iterator.get();
   }

   @Override
   public Spliterator<T> spliterator() {
      return this.spliterator.get();
   }
}
