package org.jdbi.v3.core.mapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jdbi.v3.core.statement.StatementContext;

public class MapMapper implements RowMapper<Map<String, Object>> {
   private final Function<StatementContext, UnaryOperator<String>> caseStrategy;

   public MapMapper() {
      this.caseStrategy = ctx -> ctx.getConfig(MapMappers.class).getCaseChange();
   }

   @Deprecated(
      since = "3.38.0",
      forRemoval = true
   )
   public MapMapper(boolean toLowerCase) {
      this.caseStrategy = toLowerCase ? ctx -> CaseStrategy.LOCALE_LOWER : ctx -> CaseStrategy.NOP;
   }

   public Map<String, Object> map(ResultSet rs, StatementContext ctx) throws SQLException {
      return this.specialize(rs, ctx).map(rs, ctx);
   }

   @Override
   public RowMapper<Map<String, Object>> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
      List<String> columnNames = getColumnNames(rs, this.caseStrategy.apply(ctx));
      return (r, c) -> {
         Map<String, Object> row = new LinkedHashMap<>(columnNames.size());

         for (int i = 0; i < columnNames.size(); i++) {
            row.put(columnNames.get(i), rs.getObject(i + 1));
         }

         return row;
      };
   }

   private static List<String> getColumnNames(ResultSet rs, UnaryOperator<String> caseChange) throws SQLException {
      Set<String> columnNames = new LinkedHashSet<>();
      ResultSetMetaData meta = rs.getMetaData();
      int columnCount = meta.getColumnCount();

      for (int i = 0; i < columnCount; i++) {
         String columnName = meta.getColumnName(i + 1);
         String alias = meta.getColumnLabel(i + 1);
         String name = caseChange.apply(alias == null ? columnName : alias);
         boolean added = columnNames.add(name);
         if (!added) {
            throw new RuntimeException("column " + name + " appeared twice in this resultset!");
         }
      }

      return new ArrayList<>(columnNames);
   }
}
