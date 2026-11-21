package su.nightexpress.nightcore.db.sql.query;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.db.config.DatabaseType;
import su.nightexpress.nightcore.db.connection.AbstractConnector;
import su.nightexpress.nightcore.db.connection.impl.SQLiteConnector;
import su.nightexpress.nightcore.db.sql.column.Column;
import su.nightexpress.nightcore.db.sql.column.ColumnType;
import su.nightexpress.nightcore.db.sql.query.impl.SelectQuery;
import su.nightexpress.nightcore.db.sql.query.type.AbstractQuery;
import su.nightexpress.nightcore.db.sql.util.SQLUtils;
import su.nightexpress.nightcore.util.Lists;

@Deprecated
public class SQLQueries {
   public static boolean hasTable(@NonNull AbstractConnector connector, @NonNull String table) {
      try {
         boolean var6;
         try (Connection connection = connector.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, table, null);
            boolean has = tables.next();
            tables.close();
            var6 = has;
         }

         return var6;
      } catch (SQLException var9) {
         var9.printStackTrace();
         return false;
      }
   }

   public static boolean hasColumn(@NonNull AbstractConnector connector, @NonNull String table, @NonNull Column column) {
      return hasColumn(connector, table, column.getName());
   }

   public static boolean hasColumn(@NonNull AbstractConnector connector, @NonNull String table, @NonNull String columnName) {
      String sql = "SELECT * FROM " + table;

      try {
         boolean var16;
         try (
            Connection connection = connector.getConnection();
            Statement statement = connection.createStatement();
         ) {
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columns = metaData.getColumnCount();

            for (int index = 1; index <= columns; index++) {
               if (columnName.equals(metaData.getColumnName(index))) {
                  return true;
               }
            }

            var16 = false;
         }

         return var16;
      } catch (SQLException var15) {
         var15.printStackTrace();
         return false;
      }
   }

   public static void addColumn(
      @NonNull AbstractConnector connector, @NonNull DatabaseType type, @NonNull String table, @NonNull Column column, @NonNull String defVal
   ) {
      if (!hasColumn(connector, table, column)) {
         StringBuilder builder = new StringBuilder()
            .append("ALTER TABLE ")
            .append(table)
            .append(" ADD ")
            .append(column.getName())
            .append(" ")
            .append(column.formatType(type));
         if (connector instanceof SQLiteConnector || column.getType() != ColumnType.STRING) {
            builder.append(" DEFAULT ").append("'").append(defVal).append("'");
         }

         executeSimpleQuery(connector, builder.toString());
      }
   }

   public static void renameColumn(@NonNull AbstractConnector connector, @NonNull String table, @NonNull Column column, @NonNull String toName) {
      renameColumn(connector, table, column.getName(), toName);
   }

   public static void renameColumn(@NonNull AbstractConnector connector, @NonNull String table, @NonNull String columnName, @NonNull String toName) {
      if (hasColumn(connector, table, columnName)) {
         String sql = "ALTER TABLE " + table + " RENAME COLUMN " + columnName + " TO " + toName;
         executeSimpleQuery(connector, sql);
      }
   }

   public static void dropColumn(@NonNull AbstractConnector connector, @NonNull String table, @NonNull Column column) {
      dropColumn(connector, table, column.getName());
   }

   public static void dropColumn(@NonNull AbstractConnector connector, @NonNull String table, @NonNull String columnName) {
      if (hasColumn(connector, table, columnName)) {
         String sql = "ALTER TABLE " + table + " DROP COLUMN " + columnName;
         executeSimpleQuery(connector, sql);
      }
   }

   public static void createTable(@NonNull AbstractConnector connector, @NonNull DatabaseType type, @NonNull String table, @NonNull List<Column> columns) {
      if (!columns.isEmpty()) {
         StringBuilder idBuilder = new StringBuilder(SQLUtils.escape("id")).append(" ").append(ColumnType.INTEGER.build(type, 11));
         if (type == DatabaseType.SQLITE) {
            idBuilder.append(" PRIMARY KEY AUTOINCREMENT");
         } else {
            idBuilder.append(" PRIMARY KEY AUTO_INCREMENT");
         }

         String columnNames = columns.stream().map(column -> column.getNameEscaped() + " " + column.formatType(type)).collect(Collectors.joining(", "));
         String allColumns = idBuilder + ", " + columnNames;
         String sql = "CREATE TABLE IF NOT EXISTS " + table + "(" + allColumns + ");";
         executeSimpleQuery(connector, sql);
      }
   }

   public static void renameTable(@NonNull AbstractConnector connector, @NonNull DatabaseType type, @NonNull String table, @NonNull String toName) {
      if (hasTable(connector, table)) {
         StringBuilder sql = new StringBuilder();
         if (type == DatabaseType.MYSQL) {
            sql.append("RENAME TABLE ").append(table).append(" TO ").append(toName).append(";");
         } else {
            sql.append("ALTER TABLE ").append(table).append(" RENAME TO ").append(toName);
         }

         executeSimpleQuery(connector, sql.toString());
      }
   }

   public static void executeSimpleQuery(@NonNull AbstractConnector connector, @NonNull String sql) {
      try (
         Connection connection = connector.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.executeUpdate();
      } catch (SQLException var10) {
         var10.printStackTrace();
      }
   }

   public static <T> void executeQuery(@NonNull AbstractConnector connector, @NonNull String table, @NonNull AbstractQuery<T> query, @NonNull T entity) {
      executeQuery(connector, table, query, Lists.newList(entity));
   }

   public static <T> void executeQuery(
      @NonNull AbstractConnector connector, @NonNull String table, @NonNull AbstractQuery<T> query, @NonNull Collection<T> entities
   ) {
      if (!query.isEmpty()) {
         String sql = query.createSQL(table);

         try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
         ) {
            int entityCount = 0;

            for (T entity : entities) {
               query.onExecute(statement, entity);
               statement.addBatch();
               entityCount++;
               if (entityCount % 500 == 0 || entityCount == entities.size()) {
                  statement.executeBatch();
               }
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }
      }
   }

   @NonNull
   public static <T> List<T> executeSelect(@NonNull AbstractConnector connector, @NonNull String table, @NonNull SelectQuery<T> query) {
      ArrayList<T> list = new ArrayList<>();
      if (query.isEmpty()) {
         return list;
      } else {
         String sql = query.createSQL(table);

         try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
         ) {
            query.onExecute(statement, list);
         } catch (SQLException var13) {
            var13.printStackTrace();
         }

         return list;
      }
   }
}
