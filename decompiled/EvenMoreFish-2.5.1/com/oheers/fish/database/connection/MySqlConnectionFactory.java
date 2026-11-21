package com.oheers.fish.database.connection;

import java.util.Map;
import org.evenmorefish.fish.libs.hikaricp.hikari.HikariConfig;
import org.jspecify.annotations.NonNull;

public class MySqlConnectionFactory extends ConnectionFactory {
   @Override
   protected void configureDatabase(@NonNull HikariConfig config, String address, int port, String databaseName, String username, String password) {
      config.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + databaseName);
      config.setUsername(username);
      config.setPassword(password);
   }

   @Override
   protected void overrideProperties(@NonNull Map<String, String> properties) {
      properties.putIfAbsent("cachePrepStmts", "true");
      properties.putIfAbsent("prepStmtCacheSize", "250");
      properties.putIfAbsent("prepStmtCacheSqlLimit", "2048");
      properties.putIfAbsent("useServerPrepStmts", "true");
      properties.putIfAbsent("useLocalSessionState", "true");
      properties.putIfAbsent("rewriteBatchedStatements", "true");
      properties.putIfAbsent("cacheResultSetMetadata", "true");
      properties.putIfAbsent("cacheServerConfiguration", "true");
      properties.putIfAbsent("elideSetAutoCommits", "true");
      properties.putIfAbsent("maintainTimeStats", "false");
      properties.putIfAbsent("alwaysSendSetIsolation", "false");
      properties.putIfAbsent("cacheCallableStmts", "true");
      properties.putIfAbsent("serverTimezone", "UTC");
      super.overrideProperties(properties);
   }

   @Override
   public String getType() {
      return "MYSQL";
   }
}
