package com.oheers.fish.database.connection;

import java.util.Map;
import org.evenmorefish.fish.libs.hikaricp.hikari.HikariConfig;
import org.jspecify.annotations.NonNull;

public class H2ConnectionFactory extends ConnectionFactory {
   @Override
   protected void configureDatabase(@NonNull HikariConfig config, String address, int port, String databaseName, String username, String password) {
      config.setJdbcUrl("jdbc:h2:file:./plugins/EvenMoreFish/" + databaseName);
      config.setUsername(username);
      config.setPassword(password);
   }

   @Override
   protected void overrideProperties(@NonNull Map<String, String> properties) {
      properties.putIfAbsent("MODE", "MySQL");
      properties.putIfAbsent("DATABASE_TO_UPPER", "false");
      properties.putIfAbsent("MV_STORE", "true");
      properties.putIfAbsent("TRACE_LEVEL_FILE", "0");
      super.overrideProperties(properties);
   }

   @Override
   public String getType() {
      return "H2";
   }

   @Override
   public String getDriverClass() {
      return "org.h2.Driver";
   }
}
