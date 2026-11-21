package com.earth2me.essentials.libs.configurate.util;

import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<V, E extends Throwable> {
   V get() throws E;

   static <V> CheckedSupplier<V, RuntimeException> from(Supplier<V> consumer) {
      return consumer::get;
   }
}
