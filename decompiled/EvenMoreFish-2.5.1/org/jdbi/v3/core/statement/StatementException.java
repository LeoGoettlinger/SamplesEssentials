package org.jdbi.v3.core.statement;

import java.util.Optional;
import org.jdbi.v3.core.JdbiException;

public abstract class StatementException extends JdbiException {
   private static final long serialVersionUID = 1L;
   private final transient StatementContext statementContext;

   public StatementException(Throwable cause) {
      super(cause);
      this.statementContext = null;
   }

   public StatementException(String string, Throwable throwable, StatementContext ctx) {
      super(string, throwable);
      this.statementContext = ctx;
   }

   public StatementException(String string, Throwable throwable) {
      super(string, throwable);
      this.statementContext = null;
   }

   public StatementException(Throwable cause, StatementContext ctx) {
      super(cause);
      this.statementContext = ctx;
   }

   public StatementException(String message, StatementContext ctx) {
      super(message);
      this.statementContext = ctx;
   }

   public StatementException(String string) {
      super(string);
      this.statementContext = null;
   }

   public StatementContext getStatementContext() {
      return this.statementContext;
   }

   public String getShortMessage() {
      return super.getMessage();
   }

   @Override
   public String getMessage() {
      return Optional.ofNullable(this.getStatementContext())
         .map(c -> c.getConfig(StatementExceptions.class).getMessageRendering())
         .orElse(StatementExceptions.MessageRendering.NONE)
         .apply(this);
   }
}
