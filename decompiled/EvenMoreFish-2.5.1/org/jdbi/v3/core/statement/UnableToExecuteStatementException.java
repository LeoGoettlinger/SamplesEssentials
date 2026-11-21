package org.jdbi.v3.core.statement;

public class UnableToExecuteStatementException extends StatementException {
   private static final long serialVersionUID = 1L;

   public UnableToExecuteStatementException(Exception e, StatementContext ctx) {
      super(e, ctx);
   }

   public UnableToExecuteStatementException(String message) {
      super(message);
   }

   public UnableToExecuteStatementException(String message, StatementContext ctx) {
      super(message, ctx);
   }

   public UnableToExecuteStatementException(String message, Throwable throwable, StatementContext ctx) {
      super(message, throwable, ctx);
   }
}
