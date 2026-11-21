package su.nightexpress.nightcore.database.sql.executor;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.database.AbstractConnector;
import su.nightexpress.nightcore.database.sql.SQLColumn;
import su.nightexpress.nightcore.database.sql.SQLCondition;
import su.nightexpress.nightcore.database.sql.SQLExecutor;
import su.nightexpress.nightcore.database.sql.SQLQueries;
import su.nightexpress.nightcore.database.sql.SQLValue;

@Deprecated
public final class SelectQueryExecutor<T> extends SQLExecutor<List<T>> {
   private final List<SQLColumn> columns = new ArrayList<>();
   private final List<SQLCondition> wheres = new ArrayList<>();
   private final Function<ResultSet, T> dataFunction;
   private int amount;

   private SelectQueryExecutor(@NonNull String table, @NonNull Function<ResultSet, T> dataFunction) {
      super(table);
      this.dataFunction = dataFunction;
      this.amount = -1;
   }

   @NonNull
   public static <T> SelectQueryExecutor<T> builder(@NonNull String table, @NonNull Function<ResultSet, T> dataFunction) {
      return new SelectQueryExecutor<>(table, dataFunction);
   }

   @NonNull
   public SelectQueryExecutor<T> columns(SQLColumn... columns) {
      return this.columns(Arrays.asList(columns));
   }

   @NonNull
   public SelectQueryExecutor<T> columns(@NonNull List<SQLColumn> columns) {
      this.columns.clear();
      this.columns.addAll(columns);
      return this;
   }

   @NonNull
   public SelectQueryExecutor<T> where(SQLCondition... wheres) {
      return this.where(Arrays.asList(wheres));
   }

   @NonNull
   public SelectQueryExecutor<T> where(@NonNull List<SQLCondition> wheres) {
      this.wheres.clear();
      this.wheres.addAll(wheres);
      return this;
   }

   @NonNull
   public SelectQueryExecutor<T> amount(int amount) {
      this.amount = amount;
      return this;
   }

   @NonNull
   public List<T> execute(@NonNull AbstractConnector connector) {
      String columns = this.columns.stream().map(SQLColumn::getNameEscaped).collect(Collectors.joining(","));
      if (columns.isEmpty()) {
         columns = "*";
      }

      String wheres = this.wheres
         .stream()
         .map(where -> where.getColumn().getNameEscaped() + " " + where.getType().getOperator() + " ?")
         .collect(Collectors.joining(" AND "));
      String sql = "SELECT " + columns + " FROM " + this.getTable() + (wheres.isEmpty() ? "" : " WHERE " + wheres);
      List<String> values = this.wheres.stream().map(SQLCondition::getValue).map(SQLValue::getValue).toList();
      return SQLQueries.executeQuery(connector, sql, values, this.dataFunction, this.amount);
   }
}
