package su.nightexpress.nightcore.database.sql.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.database.AbstractConnector;
import su.nightexpress.nightcore.database.DatabaseType;
import su.nightexpress.nightcore.database.connection.SQLiteConnector;
import su.nightexpress.nightcore.database.sql.SQLColumn;
import su.nightexpress.nightcore.database.sql.SQLExecutor;
import su.nightexpress.nightcore.database.sql.SQLQueries;
import su.nightexpress.nightcore.database.sql.SQLValue;
import su.nightexpress.nightcore.database.sql.column.ColumnType;

@Deprecated
public final class AlterTableExecutor extends SQLExecutor<Void> {
   private final DatabaseType databaseType;
   private final List<SQLValue> columns;
   private AlterTableExecutor.Type type;

   private AlterTableExecutor(@NonNull String table, @NonNull DatabaseType databaseType) {
      super(table);
      this.databaseType = databaseType;
      this.columns = new ArrayList<>();
   }

   @NonNull
   public static AlterTableExecutor builder(@NonNull String table, @NonNull DatabaseType databaseType) {
      return new AlterTableExecutor(table, databaseType);
   }

   @NonNull
   public AlterTableExecutor addColumn(SQLValue... columns) {
      return this.addColumn(Arrays.asList(columns));
   }

   @NonNull
   public AlterTableExecutor addColumn(@NonNull List<SQLValue> columns) {
      return this.columns(columns, AlterTableExecutor.Type.ADD_COLUMN);
   }

   @NonNull
   public AlterTableExecutor renameColumn(SQLValue... columns) {
      return this.addColumn(Arrays.asList(columns));
   }

   @NonNull
   public AlterTableExecutor renameColumn(@NonNull List<SQLValue> columns) {
      return this.columns(columns, AlterTableExecutor.Type.RENAME_COLUMN);
   }

   @NonNull
   public AlterTableExecutor dropColumn(SQLColumn... columns) {
      return this.dropColumn(Arrays.asList(columns));
   }

   @NonNull
   public AlterTableExecutor dropColumn(@NonNull List<SQLColumn> columns) {
      return this.columns(columns.stream().map(column -> column.toValue("dummy")).toList(), AlterTableExecutor.Type.DROP_COLUMN);
   }

   private AlterTableExecutor columns(@NonNull List<SQLValue> values, @NonNull AlterTableExecutor.Type type) {
      this.columns.clear();
      this.columns.addAll(values);
      this.type = type;
      return this;
   }

   @NonNull
   public Void execute(@NonNull AbstractConnector connector) {
      if (this.columns.isEmpty()) {
         return null;
      } else {
         if (this.type == AlterTableExecutor.Type.ADD_COLUMN) {
            this.columns.forEach(value -> {
               if (!SQLQueries.hasColumn(connector, this.getTable(), value.getColumn())) {
                  String sql = "ALTER TABLE " + this.getTable() + " ADD " + value.getColumn().getName() + " " + value.getColumn().formatType(this.databaseType);
                  if (connector instanceof SQLiteConnector || value.getColumn().getType() != ColumnType.STRING) {
                     sql = sql + " DEFAULT '" + value.getValue() + "'";
                  }

                  SQLQueries.executeStatement(connector, sql);
               }
            });
         } else if (this.type == AlterTableExecutor.Type.RENAME_COLUMN) {
            this.columns.forEach(value -> {
               if (SQLQueries.hasColumn(connector, this.getTable(), value.getColumn())) {
                  String sql = "ALTER TABLE " + this.getTable() + " RENAME COLUMN " + value.getColumn().getName() + " TO " + value.getValue();
                  SQLQueries.executeStatement(connector, sql);
               }
            });
         } else if (this.type == AlterTableExecutor.Type.DROP_COLUMN) {
            this.columns.forEach(value -> {
               if (SQLQueries.hasColumn(connector, this.getTable(), value.getColumn())) {
                  String sql = "ALTER TABLE " + this.getTable() + " DROP COLUMN " + value.getColumn().getName();
                  SQLQueries.executeStatement(connector, sql);
               }
            });
         }

         return null;
      }
   }

   private static enum Type {
      ADD_COLUMN,
      RENAME_COLUMN,
      DROP_COLUMN;
   }
}
