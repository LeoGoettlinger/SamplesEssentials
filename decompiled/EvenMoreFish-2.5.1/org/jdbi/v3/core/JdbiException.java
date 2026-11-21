package org.jdbi.v3.core;

public abstract class JdbiException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   protected JdbiException(String message, Throwable cause) {
      super(message, cause);
   }

   protected JdbiException(Throwable cause) {
      super(cause);
   }

   protected JdbiException(String message) {
      super(message);
   }
}
