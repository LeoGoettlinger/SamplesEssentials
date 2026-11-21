package com.fastasyncworldedit.core.util.task;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class RunnableVal<T> implements Runnable, Supplier<T>, Consumer<T> {
   public T value;

   public RunnableVal() {
   }

   public RunnableVal(T value) {
      this.value = value;
   }

   @Override
   public final void run() {
      this.run(this.value);
   }

   public final T runAndGet() {
      this.run();
      return this.value;
   }

   public abstract void run(T value);

   @Override
   public T get() {
      return this.runAndGet();
   }

   @Override
   public void accept(T t) {
      this.run(this.value);
   }
}
