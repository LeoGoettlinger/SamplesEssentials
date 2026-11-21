package org.enginehub.piston.converter;

import java.util.function.Supplier;

public class FailedConversionMapper {
   public static <X extends Throwable> X mapOnto(Supplier<X> newThrowable, FailedConversion<?> conversion) {
      X error = (X)newThrowable.get();
      error.initCause(conversion.getError());
      conversion.getOtherFailures().forEach(f -> error.addSuppressed(f.getError()));
      return error;
   }

   private FailedConversionMapper() {
   }
}
