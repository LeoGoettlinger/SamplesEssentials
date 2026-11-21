package com.artillexstudios.axafkzone.libs.axapi.database;

import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class RunnableQuery<T> {
   private final Function<Class<?>, Function<Object, List<Object>>> transformer;
   private final Supplier<Connection> connectionSupplier;
   private final ResultHandler<T> resultHandler;
   private final String sql;

   public RunnableQuery(
      Function<Class<?>, Function<Object, List<Object>>> transformer, Supplier<Connection> connectionSupplier, ResultHandler<T> resultHandler, String sql
   ) {
      this.transformer = transformer;
      this.connectionSupplier = connectionSupplier;
      this.resultHandler = resultHandler;
      this.sql = sql;
   }

   public int update(Object... parameters) {
      try {
         int var4;
         try (
            Connection connection = this.connectionSupplier.get();
            PreparedStatement statement = connection.prepareStatement(this.sql);
         ) {
            this.handleStatement(parameters, statement);
            var4 = statement.executeUpdate();
         }

         return var4;
      } catch (SQLException var10) {
         LogUtils.error("An exception occurred while executing sql query {} with parameters: {}!", this.sql, Arrays.toString(parameters));
         throw new RuntimeException(var10);
      }
   }

   public T execute(Object... parameters) {
      try {
         Object var5;
         try (
            Connection connection = this.connectionSupplier.get();
            PreparedStatement statement = connection.prepareStatement(this.sql, 1);
         ) {
            this.handleStatement(parameters, statement);
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
               var5 = this.resultHandler.handle(resultSet);
            }
         }

         return (T)var5;
      } catch (SQLException var13) {
         LogUtils.error("An exception occurred while executing sql query {} with parameters: {}!", this.sql, Arrays.toString(parameters));
         throw new RuntimeException(var13);
      }
   }

   public T query(Object... parameters) {
      try {
         Object var5;
         try (
            Connection connection = this.connectionSupplier.get();
            PreparedStatement statement = connection.prepareStatement(this.sql);
         ) {
            this.handleStatement(parameters, statement);

            try (ResultSet resultSet = statement.executeQuery()) {
               var5 = this.resultHandler.handle(resultSet);
            }
         }

         return (T)var5;
      } catch (SQLException var13) {
         LogUtils.error("An exception occurred while executing sql query {} with parameters: {}!", this.sql, Arrays.toString(parameters));
         throw new RuntimeException(var13);
      }
   }

   public int[] batch(List<Object[]> batch) {
      try {
         int[] var11;
         try (
            Connection connection = this.connectionSupplier.get();
            PreparedStatement statement = connection.prepareStatement(this.sql);
         ) {
            for (Object[] parameters : batch) {
               this.handleStatement(parameters, statement);
               statement.addBatch();
            }

            var11 = statement.executeBatch();
         }

         return var11;
      } catch (SQLException var10) {
         LogUtils.error("An exception occurred while executing sql query {} with parameters: {}!", this.sql, batch);
         throw new RuntimeException(var10);
      }
   }

   private void handleStatement(Object[] parameters, PreparedStatement statement) throws SQLException {
      for (int i = 0; i < parameters.length; i++) {
         Object parameter = parameters[i];
         if (parameter == null) {
            statement.setObject(i + 1, null);
         } else {
            Function<Object, List<Object>> transformer = this.transformer.apply(parameter.getClass());
            if (transformer == null) {
               statement.setObject(i + 1, parameter);
            } else {
               List<Object> out = transformer.apply(parameter);
               int k = 0;

               int j;
               for (j = i; j < i + out.size(); j++) {
                  statement.setObject(j + 1, out.get(k));
                  k++;
               }

               i = j;
            }
         }
      }
   }
}
