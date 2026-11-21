package com.sk89q.worldedit.util.lifecycle;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public class LifecycledCallbackHandler<T> implements Lifecycled.Events<T> {
   private final Lifecycled<T> lifecycled;
   private final Lock lock = new ReentrantLock();
   private final Map<Object, BiConsumer<?, ? super Lifecycled<T>>> onInvalidatedCallbacks = new WeakHashMap<>();
   private final Map<Object, BiConsumer<?, ? super Lifecycled<T>>> onNewValueCallbacks = new WeakHashMap<>();

   public LifecycledCallbackHandler(Lifecycled<T> lifecycled) {
      this.lifecycled = lifecycled;
   }

   @Override
   public <O> void onInvalidated(O owner, BiConsumer<O, ? super Lifecycled<T>> callback) {
      this.lock.lock();

      try {
         this.onInvalidatedCallbacks.put(owner, callback);
         if (!this.lifecycled.isValid()) {
            callback.accept(owner, this.lifecycled);
         }
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public <O> void onNewValue(O owner, BiConsumer<O, ? super Lifecycled<T>> callback) {
      this.lock.lock();

      try {
         this.onNewValueCallbacks.put(owner, callback);
         if (this.lifecycled.isValid()) {
            callback.accept(owner, this.lifecycled);
         }
      } finally {
         this.lock.unlock();
      }
   }

   public void fireInvalidated() {
      this.lock.lock();

      try {
         for (Entry<Object, BiConsumer<?, ? super Lifecycled<T>>> callback : this.onInvalidatedCallbacks.entrySet()) {
            Object owner = callback.getKey();
            if (owner != null) {
               BiConsumer<Object, ? super Lifecycled<T>> cast = (BiConsumer<Object, ? super Lifecycled<T>>)callback.getValue();
               cast.accept(owner, this.lifecycled);
            }
         }
      } finally {
         this.lock.unlock();
      }
   }

   public void fireOnNewValue() {
      this.lock.lock();

      try {
         for (Entry<Object, BiConsumer<?, ? super Lifecycled<T>>> callback : this.onNewValueCallbacks.entrySet()) {
            Object owner = callback.getKey();
            if (owner != null) {
               BiConsumer<Object, ? super Lifecycled<T>> cast = (BiConsumer<Object, ? super Lifecycled<T>>)callback.getValue();
               cast.accept(owner, this.lifecycled);
            }
         }
      } finally {
         this.lock.unlock();
      }
   }
}
