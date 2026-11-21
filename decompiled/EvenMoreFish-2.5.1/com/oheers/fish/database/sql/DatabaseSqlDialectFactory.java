package com.oheers.fish.database.sql;

import java.util.Locale;
import org.jspecify.annotations.NonNull;

public final class DatabaseSqlDialectFactory {
   private DatabaseSqlDialectFactory() {
   }

   @NonNull
   public static DatabaseSqlDialect create(@NonNull String databaseType) {
      String var1 = databaseType.toUpperCase(Locale.ROOT);

      return (DatabaseSqlDialect)(switch (var1) {
         case "SQLITE" -> new SqliteDatabaseSqlDialect();
         case "POSTGRESQL", "POSTGRES" -> new PostgresDatabaseSqlDialect();
         case "MYSQL", "MARIADB", "H2" -> new MysqlLikeDatabaseSqlDialect();
         default -> throw new IllegalArgumentException("Unsupported database type: " + databaseType);
      });
   }
}
