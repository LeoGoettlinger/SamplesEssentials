package com.artillexstudios.axafkzone.libs.axapi.database.handler;

import com.artillexstudios.axafkzone.libs.axapi.database.ResultHandler;
import com.artillexstudios.axafkzone.libs.axapi.utils.UncheckedUtils;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleHandler<T> implements ResultHandler<T> {
   private final Integer columnId;
   private final String columnName;

   public SimpleHandler() {
      this(1, null);
   }

   public SimpleHandler(Integer columnId) {
      this(columnId, null);
   }

   public SimpleHandler(String columnName) {
      this(null, columnName);
   }

   public SimpleHandler(Integer columnId, String columnName) {
      this.columnId = columnId;
      this.columnName = columnName;
   }

   @Override
   public T handle(ResultSet resultSet, boolean checkNext) throws SQLException {
      if (checkNext && !resultSet.next()) {
         return null;
      } else {
         return this.columnName != null
            ? UncheckedUtils.unsafeCast(resultSet.getObject(this.columnName))
            : UncheckedUtils.unsafeCast(resultSet.getObject(this.columnId));
      }
   }
}
