package su.nightexpress.nightcore.database.sql.column;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.database.DatabaseType;

@Deprecated
public interface ColumnFormer {
   ColumnFormer STRING = (storageType, length) -> {
      if (length >= 1 && storageType != DatabaseType.SQLITE) {
         return "varchar(" + length + ") CHARACTER SET utf8 NOT NULL";
      } else {
         return storageType == DatabaseType.SQLITE ? "TEXT NOT NULL" : "MEDIUMTEXT NOT NULL";
      }
   };
   ColumnFormer INTEGER = (storageType, length) -> length >= 1 && storageType != DatabaseType.SQLITE ? "int(" + length + ") NOT NULL" : "INTEGER NOT NULL";
   ColumnFormer DOUBLE = (storageType, length) -> storageType == DatabaseType.SQLITE ? "REAL NOT NULL" : "double NOT NULL";
   ColumnFormer LONG = (storageType, length) -> length >= 1 && storageType != DatabaseType.SQLITE ? "bigint(" + length + ") NOT NULL" : "BIGINT NOT NULL";
   ColumnFormer BOOLEAN = (storageType, length) -> storageType == DatabaseType.SQLITE ? "INTEGER NOT NULL" : "tinyint(1) NOT NULL";

   @NonNull
   String build(@NonNull DatabaseType var1, int var2);
}
