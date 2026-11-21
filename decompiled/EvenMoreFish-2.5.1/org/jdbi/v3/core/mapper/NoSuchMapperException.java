package org.jdbi.v3.core.mapper;

import org.jdbi.v3.core.JdbiException;

public class NoSuchMapperException extends JdbiException {
   public NoSuchMapperException(String string, Throwable throwable) {
      super(string, throwable);
   }

   public NoSuchMapperException(Throwable cause) {
      super(cause);
   }

   public NoSuchMapperException(String message) {
      super(message);
   }
}
