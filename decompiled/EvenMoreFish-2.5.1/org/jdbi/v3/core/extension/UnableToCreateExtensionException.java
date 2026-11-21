package org.jdbi.v3.core.extension;

import com.google.errorprone.annotations.FormatMethod;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.meta.Beta;

@Beta
public final class UnableToCreateExtensionException extends JdbiException {
   private static final long serialVersionUID = 1L;

   @FormatMethod
   public UnableToCreateExtensionException(String format, Object... args) {
      super(String.format(format, args));
   }

   @FormatMethod
   public UnableToCreateExtensionException(Throwable throwable, String format, Object... args) {
      super(String.format(format, args), throwable);
   }

   public UnableToCreateExtensionException(Throwable cause) {
      super(cause);
   }
}
