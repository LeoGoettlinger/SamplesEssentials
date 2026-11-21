package com.earth2me.essentials.libs.configurate.reactive;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import com.earth2me.essentials.libs.configurate.util.CheckedSupplier;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public interface Publisher<V> {
   static <V, E extends Exception> Publisher<V> execute(CheckedSupplier<V, E> action) {
      return execute(action, ForkJoinPool.commonPool());
   }

   static <V, E extends Exception> Publisher<V> execute(CheckedSupplier<V, E> action, Executor executor) {
      return new ExecutePublisher<>(Objects.requireNonNull(action, "action"), Objects.requireNonNull(executor, "executor"));
   }

   Disposable subscribe(Subscriber<? super V> subscriber);

   boolean hasSubscribers();

   default <R> Publisher<R> map(CheckedFunction<? super V, ? extends R, TransactionFailedException> mapper) {
      return new MappedProcessor<>(mapper, this);
   }

   default Publisher.Cached<V> cache() {
      return this.cache(null);
   }

   default Publisher.Cached<V> cache(@Nullable V initialValue) {
      return new CachedPublisher<>(this, initialValue);
   }

   Executor executor();

   public interface Cached<V> extends Publisher<V> {
      V get();

      void submit(V value);
   }
}
