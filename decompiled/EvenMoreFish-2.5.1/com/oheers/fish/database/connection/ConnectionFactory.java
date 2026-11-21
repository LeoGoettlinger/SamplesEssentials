package com.oheers.fish.database.connection;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.evenmorefish.fish.libs.hikaricp.hikari.HikariConfig;
import org.evenmorefish.fish.libs.hikaricp.hikari.HikariDataSource;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConnectionFactory {
   private static final int DEFAULT_PORT = 3306;
   protected HikariDataSource dataSource;
   private final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);
   private Boolean supportsTransaction;

   protected abstract void configureDatabase(HikariConfig config, String address, int port, String databaseName, String username, String password);

   private String getDatabaseAddress() {
      return MainConfig.getInstance().getAddress().split(":")[0];
   }

   private int getDatabasePort() {
      if (!MainConfig.getInstance().getAddress().contains(":")) {
         return 3306;
      } else {
         try {
            return Integer.parseInt(MainConfig.getInstance().getAddress().split(":")[1]);
         } catch (NumberFormatException var2) {
            return 3306;
         }
      }
   }

   public void init() {
      HikariConfig config = new HikariConfig();
      config.setPoolName("evenmorefish-hikari");
      this.configureDatabase(
         config,
         this.getDatabaseAddress(),
         this.getDatabasePort(),
         MainConfig.getInstance().getDatabase(),
         MainConfig.getInstance().getUsername(),
         MainConfig.getInstance().getPassword()
      );
      config.setInitializationFailTimeout(-1L);
      config.setValidationTimeout(5000L);
      config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(5L));
      config.setKeepaliveTime(TimeUnit.MINUTES.toMillis(1L));
      config.setLeakDetectionThreshold(30000L);
      Map<String, String> properties = new HashMap<>();
      this.overrideProperties(properties);
      this.setProperties(config, properties);
      this.initDriver();
      this.dataSource = new HikariDataSource(config);
      this.logger.info("Connected to database!");
   }

   protected void overrideProperties(@NonNull Map<String, String> properties) {
      properties.putIfAbsent("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30L)));
   }

   protected void setProperties(HikariConfig config, @NonNull Map<String, String> properties) {
      for (Entry<String, String> property : properties.entrySet()) {
         config.addDataSourceProperty(property.getKey(), property.getValue());
      }
   }

   public void shutdown() {
      if (this.dataSource != null) {
         this.dataSource.close();
      }
   }

   public abstract String getType();

   public Connection getConnection() throws SQLException {
      if (this.dataSource == null) {
         throw new SQLException("Null data source");
      } else {
         Connection connection = this.dataSource.getConnection();
         if (connection == null) {
            throw new SQLException("Null connection");
         } else {
            return connection;
         }
      }
   }

   public String getDriverClass() {
      return "";
   }

   protected void initDriver() {
      if (!this.getDriverClass().isEmpty()) {
         try {
            Class.forName(this.getDriverClass());
         } catch (ClassNotFoundException var2) {
            EvenMoreFish.getInstance().getLogger().severe("Tried to init driver: %s, but could not find it.".formatted(this.getDriverClass()));
         }
      }
   }

   public boolean supportsTransactions() {
      if (this.supportsTransaction != null) {
         return this.supportsTransaction;
      } else {
         try (Connection connection = this.getConnection()) {
            this.supportsTransaction = connection.getMetaData().supportsTransactions();
         } catch (SQLException var6) {
            EvenMoreFish.getInstance().getLogger().log(Level.WARNING, "Failed to check transaction support, assuming false", (Throwable)var6);
            this.supportsTransaction = false;
         }

         return this.supportsTransaction;
      }
   }
}
