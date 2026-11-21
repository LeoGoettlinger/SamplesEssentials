package org.jdbi.v3.core.internal.exceptions;

import com.google.errorprone.annotations.CheckReturnValue;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import org.jdbi.v3.core.internal.UtilityClassException;

public class Sneaky {
   private Sneaky() {
      throw new UtilityClassException();
   }

   @CheckReturnValue
   @Nonnull
   public static DummyException throwAnyway(Throwable t) {
      if (t instanceof Error e) {
         throw e;
      } else if (t instanceof RuntimeException re) {
         throw re;
      } else if (t instanceof IOException ioe) {
         throw new UncheckedIOException(ioe);
      } else {
         if (t instanceof InterruptedException) {
            Thread.currentThread().interrupt();
         } else if (t instanceof InvocationTargetException) {
            throw throwAnyway(t.getCause());
         }

         throw (RuntimeException)throwEvadingChecks(t);
      }
   }

   private static <E extends Throwable> E throwEvadingChecks(Throwable throwable) throws E {
      throw throwable;
   }
}
