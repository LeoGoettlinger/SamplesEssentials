package org.jdbi.v3.core.mapper;

import org.jdbi.v3.core.JdbiException;

public class MappingException extends JdbiException {
   public MappingException(String string, Throwable throwable) {
      super(string, throwable);
   }

   public MappingException(Throwable cause) {
      super(cause);
   }

   public MappingException(String message) {
      super(message);
   }
}
