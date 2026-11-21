package org.jdbi.v3.core;

public class CloseException extends JdbiException {
   private static final long serialVersionUID = 1L;

   public CloseException(String string, Throwable throwable) {
      super(string, throwable);
   }
}
