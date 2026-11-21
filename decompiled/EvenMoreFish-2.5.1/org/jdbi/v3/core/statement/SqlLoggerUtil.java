package org.jdbi.v3.core.statement;

import java.sql.SQLException;
import java.time.Instant;
import org.jdbi.v3.core.internal.UtilityClassException;
import org.jdbi.v3.core.statement.internal.SqlLoggable;

class SqlLoggerUtil {
   private SqlLoggerUtil() {
      throw new UtilityClassException();
   }

   static <T> T wrap(SqlLoggable<T> r, StatementContext ctx, SqlLogger logger) throws SQLException {
      try {
         ctx.setExecutionMoment(Instant.now());
         logger.logBeforeExecution(ctx);
         T result = r.invoke();
         ctx.setCompletionMoment(Instant.now());
         logger.logAfterExecution(ctx);
         return result;
      } catch (SQLException var4) {
         ctx.setExceptionMoment(Instant.now());
         logger.logException(ctx, var4);
         throw var4;
      }
   }
}
