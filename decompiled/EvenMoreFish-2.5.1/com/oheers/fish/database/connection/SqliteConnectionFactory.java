package com.oheers.fish.database.connection;

import java.util.Map;
import org.evenmorefish.fish.libs.hikaricp.hikari.HikariConfig;
import org.jspecify.annotations.NonNull;

public class SqliteConnectionFactory extends ConnectionFactory {
   @Override
   protected void configureDatabase(@NonNull HikariConfig config, String address, int port, String databaseName, String username, String password) {
      config.setJdbcUrl("jdbc:sqlite:plugins/EvenMoreFish/" + databaseName + ".db?journal_mode=WAL");
      config.setMaximumPoolSize(1);
      config.setMinimumIdle(1);
   }

   @Override
   protected void overrideProperties(@NonNull Map<String, String> properties) {
      properties.putIfAbsent("cachePrepStmts", "true");
      properties.putIfAbsent("prepStmtCacheSize", "250");
      properties.putIfAbsent("prepStmtCacheSqlLimit", "2048");
      properties.putIfAbsent("foreign_keys", "true");
      properties.putIfAbsent("journal_mode", "WAL");
      properties.putIfAbsent("synchronous", "NORMAL");
      super.overrideProperties(properties);
   }

   @Override
   public String getType() {
      return "SQLITE";
   }
}
