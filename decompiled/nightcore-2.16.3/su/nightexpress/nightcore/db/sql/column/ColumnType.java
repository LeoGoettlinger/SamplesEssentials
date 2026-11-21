package su.nightexpress.nightcore.db.sql.column;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.db.config.DatabaseType;

@Deprecated
public interface ColumnType {
   ColumnType STRING = (type, length) -> {
      if (length >= 1 && type != DatabaseType.SQLITE) {
         return "varchar(" + length + ") CHARACTER SET utf8 NOT NULL";
      } else {
         return type == DatabaseType.SQLITE ? "TEXT NOT NULL" : "MEDIUMTEXT NOT NULL";
      }
   };
   ColumnType INTEGER = (type, length) -> length >= 1 && type != DatabaseType.SQLITE ? "int(" + length + ") NOT NULL" : "INTEGER NOT NULL";
   ColumnType DOUBLE = (type, length) -> type == DatabaseType.SQLITE ? "REAL NOT NULL" : "double NOT NULL";
   ColumnType LONG = (type, length) -> length >= 1 && type != DatabaseType.SQLITE ? "bigint(" + length + ") NOT NULL" : "BIGINT NOT NULL";
   ColumnType TIMESTAMP = (type, length) -> "TIMESTAMP NOT NULL";
   ColumnType BOOLEAN = (type, length) -> type == DatabaseType.SQLITE ? "INTEGER NOT NULL" : "tinyint(1) NOT NULL";

   @NonNull
   String build(@NonNull DatabaseType var1, int var2);
}
