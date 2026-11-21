package com.fastasyncworldedit.core.util.collection;

import java.io.IOException;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CleanableThreadLocal<T> extends ThreadLocal<T> implements AutoCloseable {
   private final Supplier<T> supplier;
   private final Function<T, T> modifier;
   private final LongAdder count = new LongAdder();

   public CleanableThreadLocal(Supplier<T> supplier) {
      this(supplier, Function.identity());
   }

   public CleanableThreadLocal(Supplier<T> supplier, Consumer<T> modifier) {
      this(supplier, t -> {
         modifier.accept(t);
         return t;
      });
   }

   public CleanableThreadLocal(Supplier<T> supplier, Function<T, T> modifier) {
      this.supplier = supplier;
      this.modifier = modifier;
   }

   @Override
   protected final T initialValue() {
      T value = this.modifier.apply(this.init());
      if (value != null) {
         this.count.increment();
      }

      return value;
   }

   public T init() {
      return this.supplier.get();
   }

   public void clean() {
      this.remove();
   }

   @Override
   public void close() throws IOException {
      this.clean();
   }
}
