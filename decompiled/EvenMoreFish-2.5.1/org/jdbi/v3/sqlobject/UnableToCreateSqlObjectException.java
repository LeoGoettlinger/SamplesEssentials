package org.jdbi.v3.sqlobject;

import org.jdbi.v3.core.JdbiException;

public class UnableToCreateSqlObjectException extends JdbiException {
   private static final long serialVersionUID = 1L;

   public UnableToCreateSqlObjectException(String message) {
      super(message);
   }

   public UnableToCreateSqlObjectException(Throwable cause) {
      super(cause);
   }

   public UnableToCreateSqlObjectException(String message, Throwable cause) {
      super(message, cause);
   }
}
