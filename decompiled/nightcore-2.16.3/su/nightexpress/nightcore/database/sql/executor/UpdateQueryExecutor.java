package su.nightexpress.nightcore.database.sql.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.database.AbstractConnector;
import su.nightexpress.nightcore.database.sql.SQLCondition;
import su.nightexpress.nightcore.database.sql.SQLExecutor;
import su.nightexpress.nightcore.database.sql.SQLQueries;
import su.nightexpress.nightcore.database.sql.SQLValue;

@Deprecated
public final class UpdateQueryExecutor extends SQLExecutor<Void> {
   private final List<SQLValue> values = new ArrayList<>();
   private final List<SQLCondition> wheres = new ArrayList<>();

   private UpdateQueryExecutor(@NonNull String table) {
      super(table);
   }

   @NonNull
   public static UpdateQueryExecutor builder(@NonNull String table) {
      return new UpdateQueryExecutor(table);
   }

   @NonNull
   public UpdateQueryExecutor values(SQLValue... values) {
      return this.values(Arrays.asList(values));
   }

   @NonNull
   public UpdateQueryExecutor values(@NonNull List<SQLValue> values) {
      this.values.clear();
      this.values.addAll(values);
      return this;
   }

   @NonNull
   public UpdateQueryExecutor where(SQLCondition... wheres) {
      return this.where(Arrays.asList(wheres));
   }

   @NonNull
   public UpdateQueryExecutor where(@NonNull List<SQLCondition> wheres) {
      this.wheres.clear();
      this.wheres.addAll(wheres);
      return this;
   }

   @NonNull
   public Void execute(@NonNull AbstractConnector connector) {
      if (this.values.isEmpty()) {
         return null;
      } else {
         String values = this.values.stream().map(value -> value.getColumn().getNameEscaped() + " = ?").collect(Collectors.joining(","));
         String wheres = this.wheres
            .stream()
            .map(where -> where.getColumn().getNameEscaped() + " " + where.getType().getOperator() + " ?")
            .collect(Collectors.joining(" AND "));
         String sql = "UPDATE " + this.getTable() + " SET " + values + (wheres.isEmpty() ? "" : " WHERE " + wheres);
         List<String> values2 = this.values.stream().map(SQLValue::getValue).toList();
         List<String> whers2 = this.wheres.stream().map(SQLCondition::getValue).map(SQLValue::getValue).toList();
         SQLQueries.executeStatement(connector, sql, values2, whers2);
         return null;
      }
   }
}
