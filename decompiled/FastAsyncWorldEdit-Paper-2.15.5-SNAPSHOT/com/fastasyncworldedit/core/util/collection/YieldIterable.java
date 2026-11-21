package com.fastasyncworldedit.core.util.collection;

import java.io.Closeable;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class YieldIterable<T> implements Iterable<T>, Consumer<T>, Closeable {
   private static final Object END_MARKER = new Object();
   private final LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
   private Future<?> future;

   public YieldIterable(@Nullable Future<?> task) {
      this.future = task;
   }

   public YieldIterable() {
      this(null);
   }

   public void setFuture(Future<?> future) {
      this.future = future;
   }

   @Override
   public Iterator<T> iterator() {
      return new Iterator<T>() {
         private boolean interrupted;
         private T buffer;

         @Override
         public boolean hasNext() {
            try {
               while (this.buffer == null && !this.interrupted && (YieldIterable.this.future == null || !YieldIterable.this.future.isCancelled())) {
                  this.buffer = YieldIterable.this.queue.poll(50L, TimeUnit.MILLISECONDS);
                  if (this.buffer == YieldIterable.END_MARKER) {
                     this.interrupted = true;
                     this.buffer = null;
                     return false;
                  }
               }
            } catch (InterruptedException var2) {
               this.interrupted = true;
               this.buffer = null;
            }

            return this.buffer != null;
         }

         @Override
         public T next() {
            this.hasNext();
            T result = this.buffer;
            this.buffer = null;
            return result;
         }
      };
   }

   @Override
   public void accept(T t) {
      this.queue.add(t);
   }

   @Override
   public void close() {
      this.queue.add((T)END_MARKER);
   }
}
