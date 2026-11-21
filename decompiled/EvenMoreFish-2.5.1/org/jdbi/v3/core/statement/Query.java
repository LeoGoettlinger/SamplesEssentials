package org.jdbi.v3.core.statement;

import java.sql.SQLException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.core.result.ResultProducer;
import org.jdbi.v3.core.result.ResultProducers;
import org.jdbi.v3.core.result.ResultSetScanner;
import org.jdbi.v3.core.result.UnableToProduceResultException;

public class Query extends SqlStatement<Query> implements ResultBearing {
   public Query(Handle handle, CharSequence sql) {
      super(handle, sql);
   }

   public Query(Handle handle, String sql) {
      super(handle, sql);
   }

   public <R> R execute(ResultProducer<R> producer) {
      try {
         return producer.produce(this::internalExecute, this.getContext());
      } catch (SQLException var3) {
         this.cleanUpForException(var3);
         throw new UnableToProduceResultException(var3, this.getContext());
      }
   }

   @Override
   public <R> R scanResultSet(ResultSetScanner<R> resultSetScanner) {
      return this.execute(ResultProducers.returningResults()).scanResultSet(resultSetScanner);
   }

   public Query setFetchSize(final int fetchSize) {
      return this.addCustomizer(StatementCustomizers.fetchSize(fetchSize));
   }

   public Query setMaxRows(final int maxRows) {
      return this.addCustomizer(StatementCustomizers.maxRows(maxRows));
   }

   public Query setMaxFieldSize(final int maxFields) {
      return this.addCustomizer(StatementCustomizers.maxFieldSize(maxFields));
   }

   public Query concurrentUpdatable() {
      this.getContext().setConcurrentUpdatable(true);
      return this;
   }
}
