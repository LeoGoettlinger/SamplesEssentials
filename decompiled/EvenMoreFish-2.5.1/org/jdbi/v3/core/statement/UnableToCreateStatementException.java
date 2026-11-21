package org.jdbi.v3.core.statement;

public class UnableToCreateStatementException extends StatementException {
   private static final long serialVersionUID = 1L;

   public UnableToCreateStatementException(String string, Throwable cause, StatementContext ctx) {
      super(string, cause, ctx);
   }

   public UnableToCreateStatementException(String string, Throwable cause) {
      super(string, cause);
   }

   public UnableToCreateStatementException(Throwable cause) {
      super(cause);
   }

   public UnableToCreateStatementException(String string) {
      super(string);
   }

   public UnableToCreateStatementException(String string, StatementContext ctx) {
      super(string, ctx);
   }

   public UnableToCreateStatementException(Exception cause, StatementContext ctx) {
      super(cause, ctx);
   }
}
