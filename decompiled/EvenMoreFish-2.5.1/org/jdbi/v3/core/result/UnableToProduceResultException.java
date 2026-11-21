package org.jdbi.v3.core.result;

import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.StatementException;

public class UnableToProduceResultException extends StatementException {
   private static final long serialVersionUID = 1L;

   public UnableToProduceResultException(Throwable cause) {
      super(cause);
   }

   public UnableToProduceResultException(String message) {
      super(message);
   }

   public UnableToProduceResultException(Exception cause, StatementContext ctx) {
      super(cause, ctx);
   }

   public UnableToProduceResultException(String message, StatementContext ctx) {
      super(message, ctx);
   }

   public UnableToProduceResultException(String string, Throwable throwable, StatementContext ctx) {
      super(string, throwable, ctx);
   }
}
