package org.jdbi.v3.core.internal;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class JdbiOptionals {
   private JdbiOptionals() {
      throw new UtilityClassException();
   }

   @SafeVarargs
   public static <T> Optional<T> findFirstPresent(Supplier<Optional<T>>... suppliers) {
      return Stream.of(suppliers).flatMap(supplier -> supplier.get().stream()).findFirst();
   }
}
