package org.postgresql.jdbc;

import java.util.TimeZone;
import org.postgresql.core.Provider;
import org.postgresql.core.QueryExecutor;
import org.postgresql.util.GT;

class QueryExecutorTimeZoneProvider implements Provider<TimeZone> {
   private final QueryExecutor queryExecutor;

   QueryExecutorTimeZoneProvider(QueryExecutor queryExecutor) {
      this.queryExecutor = queryExecutor;
   }

   public TimeZone get() {
      TimeZone timeZone = this.queryExecutor.getTimeZone();
      if (timeZone == null) {
         throw new IllegalStateException(GT.tr("Backend timezone is not known. Backend should have returned TimeZone when establishing a connection"));
      } else {
         return timeZone;
      }
   }
}
