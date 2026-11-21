package su.nightexpress.nightcore.database.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.database.AbstractConnector;
import su.nightexpress.nightcore.database.sql.query.UpdateEntity;
import su.nightexpress.nightcore.database.sql.query.UpdateQuery;

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

   public static boolean hasColumn(@NonNull AbstractConnector connector, @NonNull String table, @NonNull SQLColumn column) {
      String sql = "SELECT * FROM " + table;
      String columnName = column.getName();

      try {
         boolean var17;
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

            var17 = false;
         }

         return var17;
      } catch (SQLException var16) {
         var16.printStackTrace();
         return false;
      }
   }

   public static void executeStatement(@NonNull AbstractConnector connector, @NonNull String sql) {
      executeStatement(connector, sql, Collections.emptySet());
   }

   public static void executeStatement(@NonNull AbstractConnector connector, @NonNull String sql, @NonNull Collection<String> values1) {
      executeStatement(connector, sql, values1, Collections.emptySet());
   }

   public static void executeStatement(
      @NonNull AbstractConnector connector, @NonNull String sql, @NonNull Collection<String> values1, @NonNull Collection<String> values2
   ) {
      try (
         Connection connection = connector.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         int count = 1;

         for (String columnName : values1) {
            statement.setString(count++, columnName);
         }

         for (String columnValue : values2) {
            statement.setString(count++, columnValue);
         }

         statement.executeUpdate();
      } catch (SQLException var13) {
         var13.printStackTrace();
      }
   }

   public static void executeUpdate(@NonNull AbstractConnector connector, @NonNull UpdateQuery query) {
      executeUpdates(connector, query);
   }

   public static void executeUpdates(@NonNull AbstractConnector connector, @NonNull UpdateQuery query) {
      if (!query.isEmpty()) {
         List<UpdateEntity> entities = query.getEntities();
         String sql = query.getSQL();

         try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
         ) {
            int entityCount = 0;

            for (UpdateEntity entity : entities) {
               int paramCount = 1;

               for (String columnValue : entity.getValues()) {
                  statement.setString(paramCount++, columnValue);
               }

               for (String conditionValue : entity.getWheres()) {
                  statement.setString(paramCount++, conditionValue);
               }

               statement.addBatch();
               entityCount++;
               if (entityCount % 500 == 0 || entityCount == entities.size()) {
                  statement.executeBatch();
               }
            }
         } catch (SQLException var16) {
            var16.printStackTrace();
         }
      }
   }

   @NonNull
   public static <T> List<T> executeQuery(
      @NonNull AbstractConnector connector, @NonNull String sql, @NonNull Collection<String> values, @NonNull Function<ResultSet, T> dataFunction, int amount
   ) {
      List<T> list = new ArrayList<>();

      try (
         Connection connection = connector.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         int count = 1;

         for (String wValue : values) {
            statement.setString(count++, wValue);
         }

         ResultSet resultSet = statement.executeQuery();

         while (resultSet.next() && (amount < 0 || list.size() < amount)) {
            list.add(dataFunction.apply(resultSet));
         }

         resultSet.close();
      } catch (SQLException var15) {
         var15.printStackTrace();
      }

      list.removeIf(Objects::isNull);
      return list;
   }
}
