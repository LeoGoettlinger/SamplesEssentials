package org.enginehub.piston.util;

import java.util.Optional;
import javax.annotation.Nullable;

public interface ValueProvider<C, T> {
   static <C, T> ValueProvider<C, T> constant(@Nullable T value) {
      Optional<T> opt = Optional.ofNullable(value);
      return context -> opt;
   }

   Optional<T> value(C var1);
}
