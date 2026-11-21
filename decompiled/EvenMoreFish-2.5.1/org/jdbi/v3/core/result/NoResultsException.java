package org.jdbi.v3.core.result;

import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.StatementException;

public class NoResultsException extends StatementException {
   private static final long serialVersionUID = 1L;

   public NoResultsException(String msg, Throwable e, StatementContext ctx) {
      super(msg, e, ctx);
   }

   public NoResultsException(Throwable e, StatementContext ctx) {
      super(e, ctx);
   }

   public NoResultsException(String msg, StatementContext ctx) {
      super(msg, ctx);
   }
}
