package org.jdbi.v3.core.result.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.jdbi.v3.core.statement.Cleanable;
import org.jdbi.v3.core.statement.StatementContext;

public abstract class ResultSetSupplier implements Supplier<ResultSet>, Cleanable {
   private final Supplier<ResultSet> supplier;
   protected Cleanable cleanable = NO_OP;

   public static ResultSetSupplier closingContext(Supplier<ResultSet> supplier, StatementContext context) {
      return new ResultSetSupplier(supplier) {
         @Override
         public void close() throws SQLException {
            try (StatementContext var1 = context) {
               this.cleanable.close();
            }
         }
      };
   }

   public static ResultSetSupplier notClosingContext(Supplier<ResultSet> supplier) {
      return new ResultSetSupplier(supplier) {
         @Override
         public void close() throws SQLException {
            this.cleanable.close();
         }
      };
   }

   private ResultSetSupplier(Supplier<ResultSet> supplier) {
      this.supplier = supplier;
   }

   public ResultSet get() {
      ResultSet resultSet = this.supplier.get();
      if (resultSet != null) {
         this.cleanable = resultSet::close;
      }

      return resultSet;
   }
}
