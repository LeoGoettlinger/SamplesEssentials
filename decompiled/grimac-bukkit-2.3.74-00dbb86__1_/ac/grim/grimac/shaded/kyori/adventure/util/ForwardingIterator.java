package ac.grim.grimac.shaded.kyori.adventure.util;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Supplier;

public final class ForwardingIterator<T> implements Iterable<T> {
   private final Supplier<Iterator<T>> iterator;
   private final Supplier<Spliterator<T>> spliterator;

   public ForwardingIterator(@NotNull final Supplier<Iterator<T>> iterator, @NotNull final Supplier<Spliterator<T>> spliterator) {
      this.iterator = Objects.requireNonNull(iterator, "iterator");
      this.spliterator = Objects.requireNonNull(spliterator, "spliterator");
   }

   @NotNull
   @Override
   public Iterator<T> iterator() {
      return this.iterator.get();
   }

   @NotNull
   @Override
   public Spliterator<T> spliterator() {
      return this.spliterator.get();
   }
}
