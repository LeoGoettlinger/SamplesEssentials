package com.earth2me.essentials.libs.configurate.reactive;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.MonotonicNonNull;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

class CachedPublisher<V> implements Publisher.Cached<V>, AutoCloseable {
   private final Publisher<V> parent;
   private final Set<Subscriber<? super V>> subscribers = ConcurrentHashMap.newKeySet();
   @MonotonicNonNull
   private volatile V value;
   private final Disposable closer;

   CachedPublisher(final Publisher<V> parent, @Nullable final V initialValue) {
      this.parent = parent;
      if (initialValue != null) {
         this.value = initialValue;
      }

      this.closer = this.parent.subscribe(next -> this.value = (V)next);
   }

   @Override
   public Disposable subscribe(final Subscriber<? super V> subscriber) {
      Disposable disp = this.parent.subscribe(subscriber);
      if (disp != NoOpDisposable.INSTANCE) {
         this.subscribers.add(subscriber);
         V value = this.value;
         if (value != null) {
            subscriber.submit(value);
         }

         return () -> {
            this.subscribers.remove(subscriber);
            disp.dispose();
         };
      } else {
         return disp;
      }
   }

   @Override
   public boolean hasSubscribers() {
      return !this.subscribers.isEmpty();
   }

   @Override
   public Publisher.Cached<V> cache() {
      return this;
   }

   @Override
   public Publisher.Cached<V> cache(@Nullable final V initialValue) {
      if (this.value == null && initialValue != null) {
         this.value = initialValue;
      }

      return this;
   }

   @Override
   public Executor executor() {
      return this.parent.executor();
   }

   @Override
   public V get() {
      return this.value;
   }

   @Override
   public void submit(final V value) {
      this.value = value;
      this.subscribers.forEach(it -> it.submit(value));
   }

   @Override
   public void close() {
      this.closer.dispose();
   }
}
