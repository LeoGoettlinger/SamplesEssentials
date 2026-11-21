package org.jdbi.v3.core.result;

import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.StatementException;

public class ResultSetException extends StatementException {
   private static final long serialVersionUID = 1L;

   public ResultSetException(String msg, Exception e, StatementContext ctx) {
      super(msg, e, ctx);
   }
}
