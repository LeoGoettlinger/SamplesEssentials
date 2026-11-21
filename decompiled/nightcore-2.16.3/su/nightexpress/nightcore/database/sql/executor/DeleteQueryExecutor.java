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
public final class DeleteQueryExecutor extends SQLExecutor<Void> {
   private final List<SQLCondition> wheres = new ArrayList<>();

   private DeleteQueryExecutor(@NonNull String table) {
      super(table);
   }

   @NonNull
   public static DeleteQueryExecutor builder(@NonNull String table) {
      return new DeleteQueryExecutor(table);
   }

   @NonNull
   public DeleteQueryExecutor where(SQLCondition... wheres) {
      return this.where(Arrays.asList(wheres));
   }

   @NonNull
   public DeleteQueryExecutor where(@NonNull List<SQLCondition> wheres) {
      this.wheres.clear();
      this.wheres.addAll(wheres);
      return this;
   }

   @NonNull
   public Void execute(@NonNull AbstractConnector connector) {
      String whereCols = this.wheres
         .stream()
         .map(where -> where.getValue().getColumn().getNameEscaped() + " " + where.getType().getOperator() + " ?")
         .collect(Collectors.joining(" AND "));
      String sql = "DELETE FROM " + this.getTable() + (whereCols.isEmpty() ? "" : " WHERE " + whereCols);
      List<String> whereVals = this.wheres.stream().map(SQLCondition::getValue).map(SQLValue::getValue).toList();
      SQLQueries.executeStatement(connector, sql, whereVals);
      return null;
   }
}
