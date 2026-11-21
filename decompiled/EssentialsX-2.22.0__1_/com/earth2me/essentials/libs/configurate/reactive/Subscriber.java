package com.earth2me.essentials.libs.configurate.reactive;

@FunctionalInterface
public interface Subscriber<V> {
   void submit(V item);

   default void onError(Throwable thrown) {
      Thread t = Thread.currentThread();
      t.getUncaughtExceptionHandler().uncaughtException(t, thrown);
   }

   default void onClose() {
   }
}
