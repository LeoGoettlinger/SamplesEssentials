package com.earth2me.essentials.libs.configurate.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<V, E extends Throwable> {
   void accept(V value) throws E;

   static <V> CheckedConsumer<V, RuntimeException> from(Consumer<V> consumer) {
      return consumer::accept;
   }
}
