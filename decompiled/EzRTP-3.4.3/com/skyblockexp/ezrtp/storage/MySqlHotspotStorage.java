package com.skyblockexp.ezrtp.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public final class MySqlHotspotStorage implements HotspotStorage {
   private static final String TABLE = "ezrtp_hotspots";
   private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ezrtp_hotspots (world     VARCHAR(64) NOT NULL, biome     VARCHAR(64) NOT NULL, x         DOUBLE      NOT NULL, y         DOUBLE      NOT NULL, z         DOUBLE      NOT NULL, timestamp BIGINT      NOT NULL, PRIMARY KEY (world, biome, x, z))";
   private static final String UPSERT = "INSERT INTO ezrtp_hotspots (world, biome, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE y = VALUES(y), timestamp = VALUES(timestamp)";
   private static final String SELECT_ALL = "SELECT world, biome, x, y, z, timestamp FROM ezrtp_hotspots";
   private final HikariDataSource dataSource;
   private final Logger logger;

   public MySqlHotspotStorage(String jdbcUrl, String user, String password, Logger logger) {
      this.logger = logger;
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(jdbcUrl);
      config.setUsername(user);
      config.setPassword(password);
      config.setMaximumPoolSize(4);
      config.setMinimumIdle(1);
      config.setConnectionTimeout(3000L);
      config.setIdleTimeout(300000L);
      config.setMaxLifetime(600000L);
      config.setPoolName("ezrtp-hotspot");
      this.dataSource = new HikariDataSource(config);
      this.setupTable();
   }

   private void setupTable() {
      try (
         Connection conn = this.dataSource.getConnection();
         Statement st = conn.createStatement();
      ) {
         st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS ezrtp_hotspots (world     VARCHAR(64) NOT NULL, biome     VARCHAR(64) NOT NULL, x         DOUBLE      NOT NULL, y         DOUBLE      NOT NULL, z         DOUBLE      NOT NULL, timestamp BIGINT      NOT NULL, PRIMARY KEY (world, biome, x, z))"
         );
      } catch (SQLException var9) {
         this.logger.warning("Failed to create hotspot table: " + var9.getMessage());
      }
   }

   @Override
   public void saveHotspot(String world, String biome, double x, double y, double z) {
      try (
         Connection conn = this.dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO ezrtp_hotspots (world, biome, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE y = VALUES(y), timestamp = VALUES(timestamp)"
         );
      ) {
         ps.setString(1, world);
         ps.setString(2, biome);
         ps.setDouble(3, x);
         ps.setDouble(4, y);
         ps.setDouble(5, z);
         ps.setLong(6, System.currentTimeMillis());
         ps.executeUpdate();
      } catch (SQLException var17) {
         this.logger.warning("Failed to save hotspot: " + var17.getMessage());
      }
   }

   @Override
   public List<HotspotStorage.HotspotRecord> loadAll() {
      List<HotspotStorage.HotspotRecord> records = new ArrayList<>();

      try {
         try (
            Connection conn = this.dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT world, biome, x, y, z, timestamp FROM ezrtp_hotspots");
            ResultSet rs = ps.executeQuery();
         ) {
            while (rs.next()) {
               records.add(
                  new HotspotStorage.HotspotRecord(
                     rs.getString("world"), rs.getString("biome"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getLong("timestamp")
                  )
               );
            }
         }

         return records;
      } catch (SQLException var13) {
         this.logger.warning("Failed to load hotspots: " + var13.getMessage());
         return Collections.emptyList();
      }
   }

   @Override
   public void close() {
      this.dataSource.close();
   }
}
