package org.jdbi.v3.core;

public class ConnectionException extends JdbiException {
   private static final long serialVersionUID = 1L;

   public ConnectionException(Throwable cause) {
      super(cause);
   }
}
