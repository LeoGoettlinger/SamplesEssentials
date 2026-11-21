package org.jdbi.v3.core.array;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

class CollectorColumnMapper<T, A, R> implements ColumnMapper<R> {
   private final ColumnMapper<T> elementMapper;
   private final Collector<T, A, R> collector;

   CollectorColumnMapper(ColumnMapper<T> elementMapper, Collector<T, A, R> collector) {
      this.elementMapper = elementMapper;
      this.collector = collector;
   }

   @Override
   public R map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
      Array array = r.getArray(columnNumber);
      if (array == null) {
         return null;
      } else {
         Object var5;
         try {
            var5 = this.buildFromResultSet(array, ctx);
         } finally {
            array.free();
         }

         return (R)var5;
      }
   }

   private R buildFromResultSet(Array array, StatementContext ctx) throws SQLException {
      A result = this.collector.supplier().get();
      BiConsumer<A, T> accumulator = this.collector.accumulator();

      try (ResultSet rs = array.getResultSet()) {
         while (rs.next()) {
            accumulator.accept(result, this.elementMapper.map(rs, 2, ctx));
         }
      }

      return this.collector.finisher().apply(result);
   }
}
