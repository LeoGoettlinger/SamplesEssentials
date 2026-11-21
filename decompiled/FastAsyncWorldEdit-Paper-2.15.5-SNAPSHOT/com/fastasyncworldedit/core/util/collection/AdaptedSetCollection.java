package com.fastasyncworldedit.core.util.collection;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class AdaptedSetCollection<T, V> implements Set<V> {
   private final Collection<V> adapted;
   private final Collection<T> original;

   public AdaptedSetCollection(Collection<T> collection, Function<T, V> adapter) {
      this.original = collection;
      this.adapted = Collections2.transform(collection, adapter);
   }

   public Collection<T> getOriginal() {
      return this.original;
   }

   @Override
   public int size() {
      return this.adapted.size();
   }

   @Override
   public boolean isEmpty() {
      return this.adapted.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      return this.adapted.contains(o);
   }

   @Nonnull
   @Override
   public Iterator<V> iterator() {
      return this.adapted.iterator();
   }

   @Nonnull
   @Override
   public Object[] toArray() {
      return this.adapted.toArray();
   }

   @Nonnull
   @Override
   public <V> V[] toArray(@Nonnull V[] a) {
      return this.adapted.toArray(a);
   }

   @Override
   public boolean add(V v) {
      return this.adapted.add(v);
   }

   @Override
   public boolean remove(Object o) {
      return this.adapted.remove(o);
   }

   @Override
   public boolean containsAll(@Nonnull Collection<?> c) {
      return this.adapted.containsAll(c);
   }

   @Override
   public boolean addAll(@Nonnull Collection<? extends V> c) {
      return this.adapted.addAll(c);
   }

   @Override
   public boolean removeAll(@Nonnull Collection<?> c) {
      return this.adapted.removeAll(c);
   }

   @Override
   public boolean removeIf(Predicate<? super V> filter) {
      return this.adapted.removeIf(filter);
   }

   @Override
   public boolean retainAll(@Nonnull Collection<?> c) {
      return this.adapted.retainAll(c);
   }

   @Override
   public void clear() {
      this.adapted.clear();
   }

   @Override
   public boolean equals(Object o) {
      return this.adapted.equals(o);
   }

   @Override
   public int hashCode() {
      return this.adapted.hashCode();
   }

   @Override
   public Spliterator<V> spliterator() {
      return this.adapted.spliterator();
   }

   @Override
   public Stream<V> stream() {
      return this.adapted.stream();
   }

   @Override
   public Stream<V> parallelStream() {
      return this.adapted.parallelStream();
   }

   @Override
   public void forEach(Consumer<? super V> action) {
      this.adapted.forEach(action);
   }
}
