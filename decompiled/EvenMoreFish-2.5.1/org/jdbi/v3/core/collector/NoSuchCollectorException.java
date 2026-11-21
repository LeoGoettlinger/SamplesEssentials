package org.jdbi.v3.core.collector;

import org.jdbi.v3.core.JdbiException;

public class NoSuchCollectorException extends JdbiException {
   public NoSuchCollectorException(String string, Throwable throwable) {
      super(string, throwable);
   }

   public NoSuchCollectorException(Throwable cause) {
      super(cause);
   }

   public NoSuchCollectorException(String message) {
      super(message);
   }
}
