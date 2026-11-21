package org.postgresql.jdbc;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Field;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.Tuple;

class CallableBatchResultHandler extends BatchResultHandler {
   CallableBatchResultHandler(PgStatement statement, Query[] queries, ParameterList[] parameterLists) {
      super(statement, queries, parameterLists, false);
   }

   @Override
   public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
   }
}
