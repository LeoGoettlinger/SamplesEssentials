package org.jdbi.v3.core.statement;

import java.sql.SQLException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.core.result.ResultProducer;
import org.jdbi.v3.core.result.ResultProducers;
import org.jdbi.v3.core.result.UnableToProduceResultException;

public class Update extends SqlStatement<Update> {
   public Update(Handle handle, CharSequence sql) {
      super(handle, sql);
   }

   public Update(Handle handle, String sql) {
      super(handle, sql);
   }

   public void one() {
      int count = this.execute();
      if (count != 1) {
         throw new IllegalStateException("Expected 1 modified row, got " + count);
      }
   }

   public int execute() {
      return this.execute(ResultProducers.returningUpdateCount());
   }

   public long executeLarge() {
      return this.execute(ResultProducers.returningLargeUpdateCount());
   }

   public <R> R execute(ResultProducer<R> producer) {
      try {
         return producer.produce(this::internalExecute, this.getContext());
      } catch (SQLException var3) {
         this.cleanUpForException(var3);
         throw new UnableToProduceResultException("Could not produce statement result", var3, this.getContext());
      }
   }

   public ResultBearing executeAndReturnGeneratedKeys(String... generatedKeyColumnNames) {
      return this.execute(ResultProducers.returningGeneratedKeys(generatedKeyColumnNames));
   }
}
