package com.sk89q.worldedit.util.task.progress;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.List;

public class ProgressIterator<V> implements Iterator<V>, ProgressObservable {
   private final Iterator<V> iterator;
   private final int count;
   private int visited = 0;

   private ProgressIterator(Iterator<V> iterator, int count) {
      Preconditions.checkNotNull(iterator);
      this.iterator = iterator;
      this.count = count;
   }

   @Override
   public boolean hasNext() {
      return this.iterator.hasNext();
   }

   @Override
   public V next() {
      V value = this.iterator.next();
      this.visited++;
      return value;
   }

   @Override
   public void remove() {
      this.iterator.remove();
   }

   @Override
   public Progress getProgress() {
      return Progress.of(this.count > 0 ? Math.min(1.0, Math.max(0.0, (double)this.visited / this.count)) : 1.0);
   }

   public static <V> ProgressIterator<V> create(Iterator<V> iterator, int count) {
      return new ProgressIterator<>(iterator, count);
   }

   public static <V> ProgressIterator<V> create(List<V> list) {
      return create(list.iterator(), list.size());
   }
}
