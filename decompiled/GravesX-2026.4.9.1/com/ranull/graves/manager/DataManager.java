package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.data.HologramData;
import com.ranull.graves.libraries.hikari.HikariConfig;
import com.ranull.graves.libraries.hikari.HikariCredentialsProvider;
import com.ranull.graves.libraries.hikari.HikariDataSource;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.InventoryUtil;
import com.ranull.graves.util.LocationUtil;
import com.ranull.graves.util.StringUtil;
import dev.cwhead.GravesX.api.provider.GraveProvider;
import dev.cwhead.GravesX.api.provider.RegisterGraveProviders;
import dev.cwhead.GravesX.manager.db.CredentialsProviderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public class DataManager {
   private final Graves plugin;
   private DataManager.Type type;
   private HikariDataSource dataSource;
   private volatile Set<String> customProviderSanitizedIds = Set.of();
   private volatile List<String> customProviderKeysSnapshot = List.of();

   public DataManager(Graves plugin) {
      this.plugin = plugin;
      String typeStr = plugin.getConfig().getString("settings.storage.type", "SQLITE");

      try {
         this.type = DataManager.Type.valueOf(typeStr.toUpperCase());
      } catch (IllegalArgumentException var4) {
         this.type = DataManager.Type.INVALID;
      }

      switch (this.type) {
         case SQLITE:
            plugin.getLogger().warning("Database Option SQLITE is no longer supported. Migrating data to H2.");
            this.loadType(DataManager.Type.H2);
            if (this.testDatabaseConnection()) {
               this.migrate();
               this.load();
               this.keepConnectionAlive();
            } else {
               plugin.getLogger().severe("Failed to connect to " + DataManager.Type.H2 + " database. Disabling plugin...");
               plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            }
            break;
         case MYSQL:
         case MARIADB:
         case POSTGRESQL:
         case H2:
            this.loadType(this.type);
            if (this.testDatabaseConnection()) {
               this.migrate();
               this.load();
               this.keepConnectionAlive();
            } else {
               plugin.getLogger().severe("Failed to connect to " + this.type + " database. Disabling plugin...");
               plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            }
            break;
         case MSSQL:
            this.loadType(DataManager.Type.MSSQL);
            if (this.testDatabaseConnection()) {
               this.migrate();
               this.load();
               this.keepConnectionAlive();
            } else {
               plugin.getLogger().severe("Failed to connect to " + this.type + " database. Disabling plugin...");
               plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            }
            break;
         default:
            plugin.getLogger().severe("Database Type is invalid. Only valid options: SQLITE, H2, POSTGRESQL, MARIADB, and MYSQL. Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(this.plugin);
      }
   }

   public String getType() {
      return switch (this.type) {
         case SQLITE, H2 -> "H2";
         case MYSQL -> "MySQL";
         case MARIADB -> "MariaDB";
         case POSTGRESQL -> "PostgreSQL";
         case MSSQL -> "Microsoft SQL Server";
         case INVALID -> null;
         default -> null;
      };
   }

   private void load() {
      try {
         try {
            RegisterGraveProviders.bootstrapFromServices();
         } catch (Throwable var2) {
         }

         this.snapshotCustomProviders();
      } catch (Throwable var3) {
         this.plugin.getLogger().severe("Failed while snapshotting GraveProviders (sync).");
         this.plugin.logStackTrace(var3);
         this.customProviderSanitizedIds = Set.of();
         this.customProviderKeysSnapshot = List.of();
      }

      List<String> customProviderKeys = this.customProviderKeysSnapshot;
      this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
         try {
            this.loadTables();
         } catch (SQLException var11) {
            throw new RuntimeException(var11);
         }

         this.loadGraveMap();
         this.loadBlockMap();
         this.loadEntityMap("armorstand", EntityData.Type.ARMOR_STAND);
         this.loadEntityMap("itemframe", EntityData.Type.ITEM_FRAME);
         this.loadHologramMap();
         Map<String, EntityData.Type> integrationMap = new HashMap<>();
         integrationMap.put("furniturelib", EntityData.Type.FURNITURELIB);
         integrationMap.put("furnitureengine", EntityData.Type.FURNITUREENGINE);
         integrationMap.put("itemsadder", EntityData.Type.ITEMSADDER);
         integrationMap.put("oraxen", EntityData.Type.ORAXEN);
         integrationMap.put("nexo", EntityData.Type.NEXO);
         integrationMap.put("playernpc", EntityData.Type.PLAYERNPC);
         integrationMap.put("mannequins", EntityData.Type.MANNEQUIN);

         for (Entry<String, EntityData.Type> entry : integrationMap.entrySet()) {
            String integration = entry.getKey();
            EntityData.Type type = entry.getValue();
            if (this.isIntegrationEnabled(integration)) {
               try {
                  this.createEntityDataMapTable(integration);
                  this.loadEntityDataMap(integration, type);
               } catch (Throwable var10) {
                  this.plugin.getLogger().severe("Failed initializing integration entity data map for: " + integration);
                  this.plugin.logStackTrace(var10);
               }

               if ("playernpc".equals(integration)) {
                  try {
                     this.plugin.getSchedulerManager().runTask(() -> {
                        try {
                           this.plugin.getIntegrationManager().getPlayerNPC().createCorpses();
                        } catch (Throwable var2x) {
                           this.plugin.getLogger().severe("PlayerNPC createCorpses failed.");
                           this.plugin.logStackTrace(var2x);
                        }
                     });
                  } catch (Throwable var9) {
                  }
               }
            }
         }

         for (String key : customProviderKeys) {
            try {
               this.createEntityDataMapTable(key);
               this.loadEntityDataMap(key, EntityData.Type.CUSTOM);
            } catch (Throwable var8) {
               this.plugin.getLogger().severe("Failed initializing custom provider for key: " + key);
               this.plugin.logStackTrace(var8);
            }
         }
      });
   }

   private boolean isIntegrationEnabled(String integration) {
      String key = sanitizeKey(integration);

      return switch (key) {
         case "furniturelib" -> this.plugin.getIntegrationManager().hasFurnitureLib();
         case "furnitureengine" -> this.plugin.getIntegrationManager().hasFurnitureEngine();
         case "itemsadder" -> this.plugin.getIntegrationManager().hasItemsAdder();
         case "oraxen" -> this.plugin.getIntegrationManager().hasOraxen();
         case "nexo" -> this.plugin.getIntegrationManager().hasNexo();
         case "playernpc" -> this.plugin.getIntegrationManager().hasPlayerNPC();
         case "mannequins" -> this.plugin.getIntegrationManager().hasMannequins();
         default -> {
            if (key.startsWith("custom_")) {
               String expected = key.substring("custom_".length());
               Set<String> snap = this.customProviderSanitizedIds;
               yield snap.contains(expected);
            } else {
               yield false;
            }
         }
      };
   }

   private void loadTables() throws SQLException {
      this.setupGraveTable();
      this.setupBlockTable();
      this.setupHologramTable();
      this.setupEntityTables();
   }

   private void setupEntityTables() throws SQLException {
      Map<String, Boolean> integrationMap = new HashMap<>();
      integrationMap.put("armorstand", true);
      integrationMap.put("itemframe", true);
      integrationMap.put("furniturelib", this.plugin.getIntegrationManager().hasFurnitureLib());
      integrationMap.put("furnitureengine", this.plugin.getIntegrationManager().hasFurnitureEngine());
      integrationMap.put("itemsadder", this.plugin.getIntegrationManager().hasItemsAdder());
      integrationMap.put("oraxen", this.plugin.getIntegrationManager().hasOraxen());
      integrationMap.put("nexo", this.plugin.getIntegrationManager().hasNexo());
      integrationMap.put("playernpc", this.plugin.getIntegrationManager().hasPlayerNPC());
      integrationMap.put("mannequins", this.plugin.getIntegrationManager().hasMannequins());

      for (Entry<String, Boolean> entry : integrationMap.entrySet()) {
         if (entry.getValue()) {
            this.setupEntityTable(entry.getKey());
         }
      }

      for (String key : this.customProviderKeysSnapshot) {
         this.setupEntityTable(key);
      }
   }

   private static String sanitizeKey(String s) {
      return s == null ? "unknown" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
   }

   public void reload() {
      this.reload(this.type);
   }

   public void reload(DataManager.Type type) {
      switch (type) {
         case SQLITE:
            this.plugin
               .getLogger()
               .warning("Database Option SQLITE is set for removal in a future release. Use H2 Database option instead for better reliance.");
            this.loadType(DataManager.Type.SQLITE);
            this.load();
            this.keepConnectionAlive();
            break;
         case MYSQL:
         case MARIADB:
         case POSTGRESQL:
         case H2:
            this.loadType(type);
            if (this.testDatabaseConnection()) {
               this.migrate();
               this.load();
               this.keepConnectionAlive();
            } else {
               this.plugin.getLogger().severe("Failed to connect to " + type + " database. Disabling plugin...");
               this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            }
            break;
         case MSSQL:
            this.loadType(DataManager.Type.MSSQL);
            if (this.testDatabaseConnection()) {
               this.migrate();
               this.load();
               this.keepConnectionAlive();
            } else {
               this.plugin.getLogger().severe("Failed to connect to " + type + " database. Disabling plugin...");
               this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            }
            break;
         default:
            this.plugin.getLogger().severe("Database Type is invalid. Only valid options: SQLITE, H2, POSTGRESQL, MARIADB, and MYSQL. Disabling plugin...");
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
      }
   }

   public void loadType(DataManager.Type type) {
      this.type = type;
      HikariConfig config = new HikariConfig();
      switch (type) {
         case SQLITE:
            this.migrateRootDataSubData();
            this.configureSQLite(config);
            break;
         case MYSQL:
         case MARIADB:
            this.configureMySQLOrMariaDB(config, type);
            break;
         case POSTGRESQL:
            this.configurePostgreSQL(config);
            break;
         case H2:
            this.configureH2(config);
            break;
         case MSSQL:
            this.configureMSSQL(config);
            break;
         default:
            throw new IllegalArgumentException("Unsupported database type: " + type);
      }

      HikariCredentialsProvider provider = CredentialsProviderFactory.forType(this.plugin, type);
      if (provider != null) {
         config.setCredentialsProvider(provider);
      }

      this.dataSource = new HikariDataSource(config);
      this.checkAndUnlockDatabase();
      if (type == DataManager.Type.MYSQL) {
         this.checkMariaDBasMySQL();
      }
   }

   private void checkMariaDBasMySQL() {
      try (
         Connection connection = this.dataSource.getConnection();
         Statement statement = connection.createStatement();
      ) {
         ResultSet resultSet = statement.executeQuery("SELECT VERSION()");
         if (resultSet.next()) {
            String version = resultSet.getString(1);
            if (version.contains("MariaDB")) {
               String versionNumber = version.split(" ")[0];
               if (this.isVersionGreaterThan(versionNumber)) {
                  this.plugin
                     .getLogger()
                     .warning(
                        "MySQL Warning: Your configuration is currently set to use MySQL, but the server is running MariaDB. As of MariaDB version 11, MySQL has been deprecated and will be removed in future versions. To avoid potential conflicts, we recommend updating your config.yml to use MARIADB."
                     );
               }
            }
         }
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Failed to check version of MySQL");
         this.plugin.logStackTrace((Exception)var10);
      }
   }

   private boolean isVersionGreaterThan(String version) {
      String[] versionParts = version.split("\\.");
      String[] compareToParts = "11".split("\\.");
      int maxLength = Math.max(versionParts.length, compareToParts.length);

      for (int i = 0; i < maxLength; i++) {
         int currentVersionPart = i < versionParts.length ? Integer.parseInt(versionParts[i]) : 0;
         int compareToVersionPart = i < compareToParts.length ? Integer.parseInt(compareToParts[i]) : 0;
         if (currentVersionPart > compareToVersionPart) {
            return true;
         }

         if (currentVersionPart < compareToVersionPart) {
            return false;
         }
      }

      return false;
   }

   private void configureSQLite(HikariConfig config) {
      String journalMode = this.plugin.getConfig().getString("settings.storage.sqlite.journal-mode", "WAL");
      String synchronous = this.plugin.getConfig().getString("settings.storage.sqlite.synchronous", "OFF");
      String jm = journalMode.toUpperCase(Locale.ROOT);
      if (!jm.equals("DELETE") && !jm.equals("TRUNCATE") && !jm.equals("PERSIST") && !jm.equals("MEMORY") && !jm.equals("WAL") && !jm.equals("OFF")) {
         jm = "WAL";
      }

      String syn = synchronous.toUpperCase(Locale.ROOT);
      if (!syn.equals("OFF")
         && !syn.equals("0")
         && !syn.equals("NORMAL")
         && !syn.equals("1")
         && !syn.equals("FULL")
         && !syn.equals("2")
         && !syn.equals("EXTRA")
         && !syn.equals("3")) {
         syn = "OFF";
      }

      File dataDir = new File(this.plugin.getDataFolder(), "data");
      if (!dataDir.exists()) {
         dataDir.mkdirs();
      }

      File dbFile = new File(dataDir, "data.db");
      String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath() + "?busy_timeout=30000";
      config.setJdbcUrl(jdbcUrl);
      config.setDriverClassName("org.sqlite.JDBC");
      config.setPoolName("GravesX SQLite");
      config.setMaximumPoolSize(10);
      config.setMinimumIdle(1);
      config.setConnectionTimeout(30000L);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(1800000L);
      config.setConnectionInitSql("PRAGMA journal_mode=" + jm + "; PRAGMA synchronous=" + syn + ";");
      config.setConnectionTestQuery("SELECT 1");
   }

   private void configurePostgreSQL(HikariConfig config) {
      String host = this.plugin.getConfig().getString("settings.storage.postgresql.host", "localhost");
      int port = this.plugin.getConfig().getInt("settings.storage.postgresql.port", 5432);
      String database = this.plugin.getConfig().getString("settings.storage.postgresql.database", "graves");
      long maxLifetime = this.plugin.getConfig().getLong("settings.storage.postgresql.maxLifetime", 1800000L);
      int maxConnections = this.plugin.getConfig().getInt("settings.storage.postgresql.maxConnections", 20);
      long connectionTimeout = this.plugin.getConfig().getLong("settings.storage.postgresql.connectionTimeout", 30000L);
      boolean ssl = this.plugin.getConfig().getBoolean("settings.storage.postgresql.ssl", true);
      String sslMode = this.plugin.getConfig().getString("settings.storage.postgresql.sslmode", ssl ? "require" : "disable");
      if (!"disable".equalsIgnoreCase(sslMode)
         && !"allow".equalsIgnoreCase(sslMode)
         && !"prefer".equalsIgnoreCase(sslMode)
         && !"require".equalsIgnoreCase(sslMode)
         && !"verify-ca".equalsIgnoreCase(sslMode)
         && !"verify-full".equalsIgnoreCase(sslMode)) {
         sslMode = ssl ? "require" : "disable";
      }

      String params = String.join("&", "sslmode=" + sslMode, "targetServerType=primary", "reWriteBatchedInserts=true", "ApplicationName=Graves");
      String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?%s", host, port, database, params);
      config.setJdbcUrl(jdbcUrl);
      config.setDriverClassName("com.ranull.graves.libraries.postgresql.Driver");
      config.setPoolName("GravesX PostgreSQL");
      config.setMaximumPoolSize(maxConnections);
      config.setMinimumIdle(Math.min(2, maxConnections));
      config.setConnectionTimeout(connectionTimeout);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(maxLifetime);

      try {
         config.setKeepaliveTime(300000L);
         config.setValidationTimeout(5000L);
      } catch (Throwable var18) {
      }

      config.addDataSourceProperty("tcpKeepAlive", "true");
      String sslFactory = this.plugin.getConfig().getString("settings.storage.postgresql.sslfactory", "");
      if (!sslFactory.trim().isEmpty()) {
         config.addDataSourceProperty("sslfactory", sslFactory.trim());
      }

      String sslRootCert = this.plugin.getConfig().getString("settings.storage.postgresql.sslrootcert", "");
      String sslCert = this.plugin.getConfig().getString("settings.storage.postgresql.sslcert", "");
      String sslKey = this.plugin.getConfig().getString("settings.storage.postgresql.sslkey", "");
      if (!sslRootCert.trim().isEmpty()) {
         config.addDataSourceProperty("sslrootcert", sslRootCert.trim());
      }

      if (!sslCert.trim().isEmpty()) {
         config.addDataSourceProperty("sslcert", sslCert.trim());
      }

      if (!sslKey.trim().isEmpty()) {
         config.addDataSourceProperty("sslkey", sslKey.trim());
      }
   }

   private void configureH2(HikariConfig config) {
      File dataDir = new File(this.plugin.getDataFolder(), "data");
      if (!dataDir.exists()) {
         dataDir.mkdirs();
      }

      File file = new File(dataDir, "graves.data");
      String filePath = file.getAbsolutePath();
      long maxLifetime = this.plugin.getConfig().getLong("settings.storage.h2.maxLifetime", 1800000L);
      int maxConnections = this.plugin.getConfig().getInt("settings.storage.h2.maxConnections", 50);
      long connectionTimeout = this.plugin.getConfig().getLong("settings.storage.h2.connectionTimeout", 30000L);
      String jdbcUrl = "jdbc:h2:file:" + filePath + ";AUTO_SERVER=FALSE;DB_CLOSE_DELAY=-1;";
      config.setJdbcUrl(jdbcUrl);
      config.setDriverClassName("com.ranull.graves.libraries.h2.Driver");
      config.setPoolName("GravesX h2");
      config.setMaximumPoolSize(maxConnections);
      config.setMinimumIdle(Math.min(2, maxConnections));
      config.setConnectionTimeout(connectionTimeout);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(maxLifetime);

      try {
         config.setKeepaliveTime(300000L);
         config.setValidationTimeout(5000L);
      } catch (Throwable var12) {
      }
   }

   private void configureMySQLOrMariaDB(HikariConfig config, DataManager.Type type) {
      String host = this.plugin.getConfig().getString("settings.storage.mysql.host", "localhost");
      int port = this.plugin.getConfig().getInt("settings.storage.mysql.port", 3306);
      String database = this.plugin.getConfig().getString("settings.storage.mysql.database", "graves");
      long maxLifetime = this.plugin.getConfig().getLong("settings.storage.mysql.maxLifetime", 1800000L);
      int maxConnections = this.plugin.getConfig().getInt("settings.storage.mysql.maxConnections", 20);
      long connectionTimeout = this.plugin.getConfig().getLong("settings.storage.mysql.connectionTimeout", 30000L);
      boolean useSSL = this.plugin.getConfig().getBoolean("settings.storage.mysql.useSSL", true);
      boolean allowPublicKeyRetrieval = this.plugin.getConfig().getBoolean("settings.storage.mysql.allowPublicKeyRetrieval", false);
      boolean verifyServerCertificate = this.plugin.getConfig().getBoolean("settings.storage.mysql.verifyServerCertificate", false);
      String jdbcUrl = type == DataManager.Type.MARIADB
         ? String.format("jdbc:mariadb://%s:%d/%s", host, port, database)
         : String.format("jdbc:mysql://%s:%d/%s", host, port, database);
      config.setJdbcUrl(jdbcUrl);
      config.setDriverClassName(
         type == DataManager.Type.MARIADB ? "com.ranull.graves.libraries.mariadb.jdbc.Driver" : "com.ranull.graves.libraries.mysql.cj.jdbc.Driver"
      );
      config.setPoolName(type == DataManager.Type.MARIADB ? "GravesX MariaDB" : "GravesX MySQL");
      config.setMaximumPoolSize(maxConnections);
      config.setMinimumIdle(Math.min(2, maxConnections));
      config.setConnectionTimeout(connectionTimeout);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(maxLifetime);

      try {
         config.setKeepaliveTime(300000L);
         config.setValidationTimeout(5000L);
      } catch (Throwable var16) {
      }

      config.addDataSourceProperty("autoReconnect", "true");
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      config.addDataSourceProperty("useServerPrepStmts", "true");
      config.addDataSourceProperty("useLocalSessionState", "true");
      config.addDataSourceProperty("cacheResultSetMetadata", "true");
      config.addDataSourceProperty("cacheServerConfiguration", "true");
      config.addDataSourceProperty("elideSetAutoCommits", "true");
      config.addDataSourceProperty("maintainTimeStats", "false");
      config.addDataSourceProperty("alwaysSendSetIsolation", "false");
      config.addDataSourceProperty("cacheCallableStmts", "true");
      config.addDataSourceProperty("useSSL", String.valueOf(useSSL));
      if (useSSL) {
         config.addDataSourceProperty("allowPublicKeyRetrieval", String.valueOf(allowPublicKeyRetrieval));
         config.addDataSourceProperty("verifyServerCertificate", String.valueOf(verifyServerCertificate));
      }
   }

   private void configureMSSQL(HikariConfig config) {
      String host = this.plugin.getConfig().getString("settings.storage.mssql.host", "localhost");
      int port = this.plugin.getConfig().getInt("settings.storage.mssql.port", 1433);
      String database = this.plugin.getConfig().getString("settings.storage.mssql.database", "graves");
      long maxLifetime = this.plugin.getConfig().getLong("settings.storage.mssql.maxLifetime", 1800000L);
      int maxConnections = this.plugin.getConfig().getInt("settings.storage.mssql.maxConnections", 20);
      long connectionTimeout = this.plugin.getConfig().getLong("settings.storage.mssql.connectionTimeout", 30000L);
      boolean encrypt = this.plugin.getConfig().getBoolean("settings.storage.mssql.encrypt", true);
      boolean trustServerCertificate = this.plugin.getConfig().getBoolean("settings.storage.mssql.trustServerCertificate", false);
      String jdbcUrl = String.format(
         "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=%s;trustServerCertificate=%s;applicationName=Graves;sendStringParametersAsUnicode=false",
         host,
         port,
         database,
         String.valueOf(encrypt),
         String.valueOf(trustServerCertificate)
      );
      config.setJdbcUrl(jdbcUrl);
      config.setDriverClassName("com.ranull.graves.libraries.microsoft.sqlserver.jdbc.SQLServerDriver");
      config.setPoolName("GravesX Microsoft SQL");
      config.setMaximumPoolSize(maxConnections);
      config.setMinimumIdle(Math.min(2, maxConnections));
      config.setConnectionTimeout(connectionTimeout);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(maxLifetime);

      try {
         config.setKeepaliveTime(300000L);
         config.setValidationTimeout(5000L);
      } catch (Throwable var14) {
      }

      config.addDataSourceProperty("encrypt", String.valueOf(encrypt));
      config.addDataSourceProperty("trustServerCertificate", String.valueOf(trustServerCertificate));
      config.addDataSourceProperty("applicationName", "Graves");
      config.addDataSourceProperty("sendStringParametersAsUnicode", "false");
   }

   private void migrateRootDataSubData() {
      Path root = this.plugin.getDataFolder().toPath();
      Path dataDir = root.resolve("data");

      try {
         Files.createDirectories(dataDir);

         try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, "data.db*")) {
            for (Path src : stream) {
               if (Files.isRegularFile(src)) {
                  Path dest = dataDir.resolve(src.getFileName());

                  try {
                     Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                  } catch (AtomicMoveNotSupportedException var9) {
                     Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
                  }
               }
            }
         }
      } catch (IOException var11) {
         this.plugin.getLogger().warning("Failed to migrate SQLite files to /data: " + var11.getMessage());
         this.plugin.logStackTrace((Exception)var11);
      }
   }

   public boolean hasChunkData(Location location) {
      if (location != null && location.getWorld() != null) {
         boolean isFolia = this.plugin.getVersionManager().isFolia();
         String key;
         if (isFolia) {
            int cx = location.getBlockX() >> 4;
            int cz = location.getBlockZ() >> 4;
            key = location.getWorld().getName() + ":" + cx + "," + cz;
         } else {
            key = LocationUtil.chunkToString(location);
         }

         return this.plugin.getCacheManager().getChunkMap().containsKey(key);
      } else {
         return false;
      }
   }

   public ChunkData getChunkData(Location location) {
      if (location != null && location.getWorld() != null) {
         boolean isFolia = this.plugin.getVersionManager().isFolia();
         int cx = location.getBlockX() >> 4;
         int cz = location.getBlockZ() >> 4;
         String chunkString;
         if (isFolia) {
            chunkString = location.getWorld().getName() + ":" + cx + "," + cz;
         } else {
            chunkString = LocationUtil.chunkToString(location);
         }

         return this.plugin
            .getCacheManager()
            .getChunkMap()
            .computeIfAbsent(chunkString, k -> isFolia ? new ChunkData(location.getWorld().getName(), cx, cz) : new ChunkData(location));
      } else {
         return null;
      }
   }

   public void removeChunkData(ChunkData chunkData) {
      if (chunkData != null) {
         World world = chunkData.getWorld();
         if (world != null) {
            boolean isFolia = this.plugin.getVersionManager().isFolia();
            if (isFolia) {
               String key = world.getName() + ":" + chunkData.getX() + "," + chunkData.getZ();
               this.plugin.getCacheManager().getChunkMap().remove(key);
            } else {
               int bx = chunkData.getX() << 4;
               int bz = chunkData.getZ() << 4;
               Location loc = new Location(world, bx, 0.0, bz);
               String key = LocationUtil.chunkToString(loc);
               this.plugin.getCacheManager().getChunkMap().remove(key);
            }
         }
      }
   }

   public List<String> getColumnList(String tableName) {
      List<String> columnList = new ArrayList<>();

      String query = switch (this.type) {
         case SQLITE -> "PRAGMA table_info(" + tableName + ");";
         case MYSQL, MARIADB -> "DESCRIBE " + tableName + ";";
         case POSTGRESQL -> "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "';";
         case H2 -> "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "';";
         case MSSQL -> "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "' AND TABLE_SCHEMA = 'dbo';";
         default -> {
            this.plugin.getLogger().severe("Unsupported database type: " + this.type);
            yield null;
         }
      };
      if (query == null) {
         return columnList;
      } else {
         try {
            label135: {
               Object var16;
               try (Connection connection = this.getConnection()) {
                  if (connection != null) {
                     try (
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                     ) {
                        while (resultSet.next()) {
                           String columnName = switch (this.type) {
                              case SQLITE -> resultSet.getString("name");
                              case MYSQL, MARIADB -> resultSet.getString("Field");
                              default -> resultSet.getString("COLUMN_NAME");
                           };
                           columnList.add(columnName);
                        }
                        break label135;
                     }
                  }

                  var16 = columnList;
               }

               return (List<String>)var16;
            }
         } catch (SQLException var15) {
            this.plugin.getLogger().severe("Error occurred while getting Column List");
            this.plugin.getLogger().severe("Query: " + query);
            this.plugin.logStackTrace((Exception)var15);
         }

         return columnList;
      }
   }

   public boolean tableExists(String tableName) {
      String query = switch (this.type) {
         case SQLITE -> "SELECT name FROM sqlite_master WHERE type='table' AND name='" + this.getStoragePrefix() + tableName + "';";
         case MYSQL, MARIADB -> "SHOW TABLES LIKE '" + tableName + "';";
         case POSTGRESQL -> "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = '" + this.getStoragePrefix() + tableName + "');";
         case H2 -> "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + this.getStoragePrefix() + tableName + "';";
         case MSSQL -> "IF OBJECT_ID('" + this.getStoragePrefix() + tableName + "', 'U') IS NOT NULL SELECT 1 AS TableExists ELSE SELECT 0 AS TableExists;";
         default -> {
            this.plugin.getLogger().severe("Unsupported database type: " + this.type);
            yield null;
         }
      };
      if (query == null) {
         return false;
      } else {
         try {
            boolean var6;
            try (Connection connection = this.getConnection()) {
               if (connection == null) {
                  return false;
               }

               try (
                  Statement statement = connection.createStatement();
                  ResultSet resultSet = statement.executeQuery(query);
               ) {
                  if (!resultSet.next()) {
                     return false;
                  }
                  var6 = switch (this.type) {
                     case SQLITE, MYSQL, MARIADB -> true;
                     case POSTGRESQL -> resultSet.getBoolean(1);
                     case H2 -> resultSet.getInt(1) > 0;
                     case MSSQL -> resultSet.getInt("TableExists") == 1;
                     default -> false;
                  };
               }
            }

            return var6;
         } catch (SQLException var14) {
            this.plugin.getLogger().severe("Error occurred while checking if table exists");
            this.plugin.getLogger().severe("Query: " + query);
            this.plugin.logStackTrace((Exception)var14);
            return false;
         }
      }
   }

   private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) throws SQLException {
      List<String> columnList = this.getColumnList(tableName);
      if (!columnList.contains(columnName)) {
         String query = switch (this.type) {
            case SQLITE, MYSQL, MARIADB -> "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition + ";";
            case POSTGRESQL -> "DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = '"
               + this.getStoragePrefix()
               + tableName
               + "' AND column_name = '"
               + columnName
               + "') THEN ALTER TABLE "
               + this.getStoragePrefix()
               + tableName
               + " ADD COLUMN "
               + columnName
               + " "
               + columnDefinition
               + "; END IF; END $$;";
            case H2 -> "ALTER TABLE " + this.getStoragePrefix() + tableName + " ADD COLUMN IF NOT EXISTS " + columnName + " " + columnDefinition + ";";
            case MSSQL -> "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
               + this.getStoragePrefix()
               + tableName
               + "' AND COLUMN_NAME = '"
               + columnName
               + "') BEGIN ALTER TABLE "
               + this.getStoragePrefix()
               + tableName
               + " ADD "
               + columnName
               + " "
               + columnDefinition
               + ";END;";
            default -> {
               this.plugin.getLogger().severe("Unsupported database type: " + this.type);
               yield null;
            }
         };
         if (query != null) {
            this.executeUpdate(query, new Object[0]);
         }
      }
   }

   private void alterColumnIfExists(String tableName, String columnName, String columnDefinition) throws SQLException {
      List<String> columnList = this.getColumnList(tableName);
      if (columnList.contains(columnName)) {
         String query = switch (this.type) {
            case SQLITE -> {
               this.plugin
                  .debugMessage(
                     "SQLite does not support altering columns. Skipping alteration of column '"
                        + columnName
                        + "' in table '"
                        + this.getStoragePrefix()
                        + tableName
                        + "'.",
                     2
                  );
               yield null;
            }
            case MYSQL, MARIADB -> "ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + columnDefinition + ";";
            case POSTGRESQL -> "ALTER TABLE " + this.getStoragePrefix() + tableName + " ALTER COLUMN " + columnName + " TYPE " + columnDefinition + ";";
            case H2, MSSQL -> "ALTER TABLE " + this.getStoragePrefix() + tableName + " ALTER COLUMN " + columnName + " " + columnDefinition + ";";
            default -> {
               this.plugin.getLogger().severe("Unsupported database type: " + this.type);
               yield null;
            }
         };
         if (query != null) {
            this.executeUpdate(query, new Object[0]);
         }
      }
   }

   public void setupGraveTable() throws SQLException {
      String name = this.getStoragePrefix() + "grave";
      if (!this.tableExists(name)) {
         String create = switch (this.type) {
            case POSTGRESQL, H2 -> "CREATE TABLE IF NOT EXISTS "
               + name
               + " (uuid VARCHAR(255) UNIQUE,owner_type VARCHAR(255),owner_name VARCHAR(255),owner_name_display VARCHAR(255),owner_uuid VARCHAR(255),owner_texture TEXT,owner_texture_signature TEXT,killer_type VARCHAR(255),killer_name VARCHAR(255),killer_name_display VARCHAR(255),killer_uuid VARCHAR(255),location_death VARCHAR(255),yaw REAL,pitch REAL,inventory TEXT,equipment TEXT,experience INT,protection INT,is_abandoned INT,time_alive BIGINT,time_protection BIGINT,time_creation BIGINT,permissions TEXT,provider_id VARCHAR(255),death_reason VARCHAR(255));";
            case MSSQL -> "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = '"
               + name
               + "') CREATE TABLE "
               + name
               + " (uuid NVARCHAR(255) UNIQUE,owner_type NVARCHAR(255),owner_name NVARCHAR(255),owner_name_display NVARCHAR(255),owner_uuid NVARCHAR(255),owner_texture NVARCHAR(MAX),owner_texture_signature NVARCHAR(MAX),killer_type NVARCHAR(255),killer_name NVARCHAR(255),killer_name_display NVARCHAR(255),killer_uuid NVARCHAR(255),location_death NVARCHAR(255),yaw FLOAT,pitch FLOAT,inventory NVARCHAR(MAX),equipment NVARCHAR(MAX),experience INT,protection BIT,is_abandoned BIT,time_alive BIGINT,time_protection BIGINT,time_creation BIGINT,permissions NVARCHAR(MAX),provider_id NVARCHAR(255),death_reason NVARCHAR(255));";
            default -> "CREATE TABLE IF NOT EXISTS "
               + name
               + " (uuid VARCHAR(255) UNIQUE,owner_type VARCHAR(255),owner_name VARCHAR(255),owner_name_display VARCHAR(255),owner_uuid VARCHAR(255),owner_texture LONGTEXT,owner_texture_signature LONGTEXT,killer_type VARCHAR(255),killer_name VARCHAR(255),killer_name_display VARCHAR(255),killer_uuid VARCHAR(255),location_death VARCHAR(255),yaw FLOAT(16),pitch FLOAT(16),inventory LONGTEXT,equipment LONGTEXT,experience INT(16),protection INT(1),is_abandoned INT(1),time_alive BIGINT,time_protection BIGINT,time_creation BIGINT,permissions LONGTEXT,provider_id VARCHAR(255),death_reason VARCHAR(255));";
         };
         this.executeUpdate(create, new Object[0]);
      }

      this.addColumnIfNotExists(name, "uuid", "VARCHAR(255) UNIQUE");
      this.alterColumnIfExists(name, "uuid", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "owner_type", "VARCHAR(255)");
      this.alterColumnIfExists(name, "owner_type", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "owner_name", "VARCHAR(255)");
      this.alterColumnIfExists(name, "owner_name", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "owner_name_display", "VARCHAR(255)");
      this.alterColumnIfExists(name, "owner_name_display", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "owner_uuid", "VARCHAR(255)");
      this.alterColumnIfExists(name, "owner_uuid", "VARCHAR(255)");
      switch (this.type) {
         case POSTGRESQL:
         case H2:
            this.addColumnIfNotExists(name, "owner_texture", "TEXT");
            this.alterColumnIfExists(name, "owner_texture", "TEXT");
            this.addColumnIfNotExists(name, "owner_texture_signature", "TEXT");
            this.alterColumnIfExists(name, "owner_texture_signature", "TEXT");
            break;
         case MSSQL:
            this.addColumnIfNotExists(name, "owner_texture", "NVARCHAR(MAX)");
            this.alterColumnIfExists(name, "owner_texture", "NVARCHAR(MAX)");
            this.addColumnIfNotExists(name, "owner_texture_signature", "NVARCHAR(MAX)");
            this.alterColumnIfExists(name, "owner_texture_signature", "NVARCHAR(MAX)");
            break;
         default:
            this.addColumnIfNotExists(name, "owner_texture", "LONGTEXT");
            this.alterColumnIfExists(name, "owner_texture", "LONGTEXT");
            this.addColumnIfNotExists(name, "owner_texture_signature", "LONGTEXT");
            this.alterColumnIfExists(name, "owner_texture_signature", "LONGTEXT");
      }

      this.addColumnIfNotExists(name, "killer_type", "VARCHAR(255)");
      this.alterColumnIfExists(name, "killer_type", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "killer_name", "VARCHAR(255)");
      this.alterColumnIfExists(name, "killer_name", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "killer_name_display", "VARCHAR(255)");
      this.alterColumnIfExists(name, "killer_name_display", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "killer_uuid", "VARCHAR(255)");
      this.alterColumnIfExists(name, "killer_uuid", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "location_death", "VARCHAR(255)");
      this.alterColumnIfExists(name, "location_death", "VARCHAR(255)");
      switch (this.type) {
         case POSTGRESQL:
         case H2:
            this.addColumnIfNotExists(name, "yaw", "REAL");
            this.alterColumnIfExists(name, "yaw", "REAL");
            this.addColumnIfNotExists(name, "pitch", "REAL");
            this.alterColumnIfExists(name, "pitch", "REAL");
            break;
         case MSSQL:
            this.addColumnIfNotExists(name, "yaw", "FLOAT");
            this.alterColumnIfExists(name, "yaw", "FLOAT");
            this.addColumnIfNotExists(name, "pitch", "FLOAT");
            this.alterColumnIfExists(name, "pitch", "FLOAT");
            break;
         default:
            this.addColumnIfNotExists(name, "yaw", "FLOAT(16)");
            this.alterColumnIfExists(name, "yaw", "FLOAT(16)");
            this.addColumnIfNotExists(name, "pitch", "FLOAT(16)");
            this.alterColumnIfExists(name, "pitch", "FLOAT(16)");
      }

      switch (this.type) {
         case POSTGRESQL:
         case H2:
            this.addColumnIfNotExists(name, "inventory", "TEXT");
            this.alterColumnIfExists(name, "inventory", "TEXT");
            this.addColumnIfNotExists(name, "equipment", "TEXT");
            this.alterColumnIfExists(name, "equipment", "TEXT");
            this.addColumnIfNotExists(name, "experience", "INT");
            this.alterColumnIfExists(name, "experience", "INT");
            this.addColumnIfNotExists(name, "protection", "INT");
            this.alterColumnIfExists(name, "protection", "INT");
            this.addColumnIfNotExists(name, "is_abandoned", "INT");
            this.alterColumnIfExists(name, "is_abandoned", "INT");
            break;
         case MSSQL:
            this.addColumnIfNotExists(name, "inventory", "NVARCHAR(MAX)");
            this.alterColumnIfExists(name, "inventory", "NVARCHAR(MAX)");
            this.addColumnIfNotExists(name, "equipment", "NVARCHAR(MAX)");
            this.alterColumnIfExists(name, "equipment", "NVARCHAR(MAX)");
            this.addColumnIfNotExists(name, "experience", "INT");
            this.alterColumnIfExists(name, "experience", "INT");
            this.addColumnIfNotExists(name, "protection", "BIT");
            this.alterColumnIfExists(name, "protection", "BIT");
            this.addColumnIfNotExists(name, "is_abandoned", "BIT");
            this.alterColumnIfExists(name, "is_abandoned", "BIT");
            break;
         default:
            this.addColumnIfNotExists(name, "inventory", "LONGTEXT");
            this.alterColumnIfExists(name, "inventory", "LONGTEXT");
            this.addColumnIfNotExists(name, "equipment", "LONGTEXT");
            this.alterColumnIfExists(name, "equipment", "LONGTEXT");
            this.addColumnIfNotExists(name, "experience", "INT(16)");
            this.alterColumnIfExists(name, "experience", "INT(16)");
            this.addColumnIfNotExists(name, "protection", "INT(1)");
            this.alterColumnIfExists(name, "protection", "INT(1)");
            this.addColumnIfNotExists(name, "is_abandoned", "INT(1)");
            this.alterColumnIfExists(name, "is_abandoned", "INT(1)");
      }

      this.addColumnIfNotExists(name, "time_alive", "BIGINT");
      this.alterColumnIfExists(name, "time_alive", "BIGINT");
      this.addColumnIfNotExists(name, "time_protection", "BIGINT");
      this.alterColumnIfExists(name, "time_protection", "BIGINT");
      this.addColumnIfNotExists(name, "time_creation", "BIGINT");
      this.alterColumnIfExists(name, "time_creation", "BIGINT");
      switch (this.type) {
         case POSTGRESQL:
         case H2:
            this.addColumnIfNotExists(name, "permissions", "TEXT");
            this.alterColumnIfExists(name, "permissions", "TEXT");
            this.addColumnIfNotExists(name, "death_reason", "TEXT");
            this.alterColumnIfExists(name, "death_reason", "TEXT");
            break;
         case MSSQL:
            this.addColumnIfNotExists(name, "permissions", "NVARCHAR(MAX)");
            this.alterColumnIfExists(name, "permissions", "NVARCHAR(MAX)");
            this.addColumnIfNotExists(name, "death_reason", "NVARCHAR(255)");
            this.alterColumnIfExists(name, "death_reason", "NVARCHAR(255)");
            break;
         default:
            this.addColumnIfNotExists(name, "permissions", "LONGTEXT");
            this.alterColumnIfExists(name, "permissions", "LONGTEXT");
            this.addColumnIfNotExists(name, "death_reason", "VARCHAR(255)");
            this.alterColumnIfExists(name, "death_reason", "VARCHAR(255)");
      }

      if (Objects.requireNonNull(this.type) == DataManager.Type.MSSQL) {
         this.addColumnIfNotExists(name, "provider_id", "NVARCHAR(255)");
         this.alterColumnIfExists(name, "provider_id", "NVARCHAR(255)");
      } else {
         this.addColumnIfNotExists(name, "provider_id", "VARCHAR(255)");
         this.alterColumnIfExists(name, "provider_id", "VARCHAR(255)");
      }
   }

   public void setupBlockTable() throws SQLException {
      String name = this.getStoragePrefix() + "block";
      if (!this.tableExists(name)) {
         String replaceDataType = switch (this.type) {
            case MSSQL -> "NVARCHAR(MAX)";
            default -> "TEXT";
         };
         String createTableQuery = "CREATE TABLE IF NOT EXISTS "
            + name
            + " (location VARCHAR(255),uuid_grave VARCHAR(255),replace_material VARCHAR(255),replace_data "
            + replaceDataType
            + ");";
         this.executeUpdate(createTableQuery, new Object[0]);
      }

      this.addColumnIfNotExists(name, "location", "VARCHAR(255)");
      this.alterColumnIfExists(name, "location", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "uuid_grave", "VARCHAR(255)");
      this.alterColumnIfExists(name, "uuid_grave", "VARCHAR(255)");
      this.addColumnIfNotExists(name, "replace_material", "VARCHAR(255)");
      this.alterColumnIfExists(name, "replace_material", "VARCHAR(255)");

      String replaceDataTypeForAlter = switch (this.type) {
         case MSSQL -> "NVARCHAR(MAX)";
         default -> "TEXT";
      };
      this.addColumnIfNotExists(name, "replace_data", replaceDataTypeForAlter);
      this.alterColumnIfExists(name, "replace_data", replaceDataTypeForAlter);
   }

   public void setupHologramTable() throws SQLException {
      String name = this.getStoragePrefix() + "hologram";
      if (!this.tableExists(name)) {
         String createTableQuery = switch (this.type) {
            case SQLITE, POSTGRESQL, H2 -> "CREATE TABLE IF NOT EXISTS "
               + name
               + " (uuid_entity VARCHAR(255),uuid_grave VARCHAR(255),line INTEGER,location VARCHAR(255),backend VARCHAR(255));";
            case MYSQL, MARIADB -> "CREATE TABLE IF NOT EXISTS "
               + name
               + " (uuid_entity VARCHAR(255),uuid_grave VARCHAR(255),line INT(16),location VARCHAR(255),backend VARCHAR(255));";
            case MSSQL -> "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = '"
               + name
               + "') CREATE TABLE "
               + name
               + " (uuid_entity NVARCHAR(255),uuid_grave NVARCHAR(255),line INT,location NVARCHAR(255),backend NVARCHAR(255));";
            default -> {
               this.plugin.getLogger().severe("Unsupported database type: " + this.type);
               yield null;
            }
         };
         if (createTableQuery == null) {
            return;
         }

         this.executeUpdate(createTableQuery, new Object[0]);
      }
      String varcharDef = switch (this.type) {
         case MSSQL -> "NVARCHAR(255)";
         default -> "VARCHAR(255)";
      };
      this.addColumnIfNotExists(name, "uuid_entity", varcharDef);
      this.alterColumnIfExists(name, "uuid_entity", varcharDef);
      this.addColumnIfNotExists(name, "uuid_grave", varcharDef);
      this.alterColumnIfExists(name, "uuid_grave", varcharDef);

      String lineType = switch (this.type) {
         case MYSQL, MARIADB -> "INT(16)";
         default -> "INTEGER";
         case MSSQL -> "INT";
      };
      this.addColumnIfNotExists(name, "line", lineType);
      this.alterColumnIfExists(name, "line", lineType);
      this.addColumnIfNotExists(name, "location", varcharDef);
      this.alterColumnIfExists(name, "location", varcharDef);
      this.addColumnIfNotExists(name, "backend", varcharDef);
      this.alterColumnIfExists(name, "backend", varcharDef);

      String backfillBackend;
      String var10 = backfillBackend = switch (this.type) {
         case SQLITE, MYSQL, MARIADB, POSTGRESQL, H2, MSSQL -> "UPDATE " + name + " SET backend='ARMOR_STAND' WHERE backend IS NULL OR TRIM(backend)='';";
         default -> null;
      };
      if (backfillBackend != null) {
         this.plugin.getSchedulerManager().runTaskLaterAsynchronously(() -> {
            List<String> columns = this.getColumnList(name);
            if (!columns.contains("backend")) {
               this.plugin.debugMessage("Skipping hologram backend backfill for table '" + name + "' because column 'backend' is still missing.", 2);
            } else {
               try {
                  this.executeUpdateMainThread(backfillBackend, new Object[0]);
               } catch (SQLException var5) {
                  this.plugin.getLogger().severe("Error executing hologram backend backfill");
                  this.plugin.getLogger().severe("Failed SQL statement: " + backfillBackend);
                  this.plugin.logStackTrace((Exception)var5);
               }
            }
         }, 40L);
      }
   }

   private void setupEntityTable(String name) throws SQLException {
      String table = this.getStoragePrefix() + name;
      if (!this.tableExists(table)) {
         String createTableQuery = switch (this.type) {
            case SQLITE -> "CREATE TABLE IF NOT EXISTS " + table + " (location TEXT, uuid_entity TEXT, uuid_grave TEXT);";
            case MYSQL, MARIADB, POSTGRESQL, H2 -> "CREATE TABLE IF NOT EXISTS "
               + table
               + " (location VARCHAR(255), uuid_entity VARCHAR(255), uuid_grave VARCHAR(255));";
            case MSSQL -> "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = '"
               + table
               + "') CREATE TABLE "
               + table
               + " (location NVARCHAR(255), uuid_entity NVARCHAR(255), uuid_grave NVARCHAR(255));";
            default -> {
               this.plugin.getLogger().severe("Unsupported database type: " + this.type);
               yield null;
            }
         };
         if (createTableQuery == null) {
            return;
         }

         this.executeUpdate(createTableQuery, new Object[0]);
      }
      String varcharDef = switch (this.type) {
         case SQLITE -> "TEXT";
         case MSSQL -> "NVARCHAR(255)";
         default -> "VARCHAR(255)";
      };
      this.addColumnIfNotExists(table, "location", varcharDef);
      this.alterColumnIfExists(table, "location", varcharDef);
      this.addColumnIfNotExists(table, "uuid_entity", varcharDef);
      this.alterColumnIfExists(table, "uuid_entity", varcharDef);
      this.addColumnIfNotExists(table, "uuid_grave", varcharDef);
      this.alterColumnIfExists(table, "uuid_grave", varcharDef);
   }

   public void loadGraveMap() {
      this.plugin.getCacheManager().getGraveMap().clear();
      this.plugin.getLogger().info("Loading grave maps...");
      String query = "SELECT * FROM " + this.getStoragePrefix() + "grave;";
      int graveCount = 0;

      try (
         Connection connection = this.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(query);
      ) {
         while (resultSet.next()) {
            Grave grave = this.resultSetToGrave(resultSet);
            if (grave != null) {
               this.plugin.getCacheManager().getGraveMap().put(grave.getUUID(), grave);
               graveCount++;
            }
         }

         if (graveCount == 0) {
            this.plugin.getLogger().info("Found 0 grave maps to load into cache.");
         } else {
            this.plugin.getLogger().info("Loaded " + graveCount + " grave maps into cache.");
         }
      } catch (SQLException var14) {
         String sqlState = var14.getSQLState();
         String message = var14.getMessage() != null ? var14.getMessage().toLowerCase(Locale.ROOT) : "";
         if (!"42701".equals(sqlState)
            && !"42P07".equals(sqlState)
            && !"42S01".equals(sqlState)
            && !"42S04".equals(sqlState)
            && !"X0Y32".equals(sqlState)
            && !"42000".equals(sqlState)
            && (!"SQLITE_ERROR".equals(sqlState) || !message.contains("duplicate column name"))) {
            this.plugin.getLogger().severe("Error occurred while loading Grave Map");
            this.plugin.logStackTrace((Exception)var14);
         } else {
            this.plugin.getLogger().info("Found 0 grave maps to load into cache.");
         }
      } catch (NullPointerException var15) {
         this.plugin.getLogger().severe("A null pointer exception occurred while loading Grave Map");
         this.plugin.logStackTrace((Exception)var15);
      }
   }

   public void loadBlockMap() {
      String query = "SELECT * FROM " + this.getStoragePrefix() + "block;";

      class BlockWork {
         Location loc;
         BlockData data;

         BlockWork(Location loc, BlockData data) {
            this.loc = loc;
            this.data = data;
         }
      }

      this.plugin
         .getSchedulerManager()
         .runTaskAsynchronously(
            () -> {
               this.plugin.getLogger().info("Loading Block Map cache...");
               Map<String, List<BlockWork>> byChunk = new HashMap<>();
               int scheduledCount = 0;

               try (
                  Connection connection = this.getConnection();
                  PreparedStatement statement = connection.prepareStatement(query);
                  ResultSet resultSet = statement.executeQuery();
               ) {
                  while (resultSet.next()) {
                     try {
                        Location location = LocationUtil.stringToLocation(resultSet.getString("location"));
                        UUID uuidGrave = UUID.fromString(resultSet.getString("uuid_grave"));
                        String replaceMaterial = resultSet.getString("replace_material");
                        String replaceData = resultSet.getString("replace_data");
                        if (location.getWorld() == null) {
                           this.plugin.getLogger().warning("Skipping block entry with invalid/missing location for Grave " + uuidGrave);
                        } else {
                           BlockData bd;
                           if (replaceMaterial != null && replaceData != null) {
                              bd = new BlockData(location, uuidGrave, replaceMaterial, replaceData);
                           } else {
                              bd = new BlockData(location, uuidGrave, "AIR", "minecraft:air");
                           }

                           int cx = location.getBlockX() >> 4;
                           int cz = location.getBlockZ() >> 4;
                           String key = location.getWorld().getUID() + ":" + cx + ":" + cz;
                           byChunk.computeIfAbsent(key, k -> new ArrayList<>()).add(new BlockWork(location, bd));
                           scheduledCount++;
                        }
                     } catch (Exception var18) {
                        String uuidGraveStr = resultSet.getString("uuid_grave");
                        this.plugin.getLogger().warning("Failed to process a block entry for Grave " + uuidGraveStr);
                        this.plugin.logStackTrace(var18);
                     }
                  }

                  for (List<BlockWork> group : byChunk.values()) {
                     if (!group.isEmpty()) {
                        Location anchor = group.get(0).loc.clone();
                        this.plugin.getSchedulerManager().execute(anchor, () -> {
                           for (BlockWork w : group) {
                              try {
                                 this.getChunkData(w.loc).addBlockData(w.data);
                              } catch (Throwable var5x) {
                                 this.plugin.getLogger().warning("Failed to cache block at " + w.loc + ": " + var5x.getMessage());
                              }
                           }
                        });
                     }
                  }

                  this.plugin.getLogger().info("Queued " + scheduledCount + " Blocks into the Block Map Cache (batched by chunk).");
               } catch (SQLException var22) {
                  String sqlState = var22.getSQLState();
                  String message = var22.getMessage() != null ? var22.getMessage().toLowerCase(Locale.ROOT) : "";
                  if (!"42701".equals(sqlState)
                     && !"42P07".equals(sqlState)
                     && !"42S01".equals(sqlState)
                     && !"42S04".equals(sqlState)
                     && !"X0Y32".equals(sqlState)
                     && !"42000".equals(sqlState)
                     && (!"SQLITE_ERROR".equals(sqlState) || !message.contains("duplicate column name"))) {
                     this.plugin.getLogger().severe("Error occurred while loading Block Map");
                     this.plugin.logStackTrace((Exception)var22);
                  } else {
                     this.plugin.getLogger().info("Loaded 0 Blocks into Block Map Cache.");
                  }
               }
            }
         );
   }

   private void loadEntityMap(String table, EntityData.Type type) {
      String query = "SELECT * FROM " + this.getStoragePrefix() + table + ";";
      this.plugin
         .getSchedulerManager()
         .runTaskAsynchronously(
            () -> {
               this.plugin.getLogger().info("Loading Entity Map Cache for " + table + "...");
               int entityCount = 0;

               try (
                  Connection connection = this.getConnection();
                  PreparedStatement statement = connection.prepareStatement(query);
                  ResultSet resultSet = statement.executeQuery();
               ) {
                  while (resultSet.next()) {
                     String locationString = resultSet.getString("location");
                     if (locationString == null) {
                        this.plugin.getLogger().warning("EntityMap row skipped: location is null (table=" + table + ").");
                     } else {
                        Location location = LocationUtil.stringToLocation(locationString);
                        if (location.getWorld() == null) {
                           this.plugin.getLogger().warning("EntityMap row skipped: invalid location '" + locationString + "' (table=" + table + ").");
                        } else {
                           String uuidEntityString = resultSet.getString("uuid_entity");
                           String uuidGraveString = resultSet.getString("uuid_grave");
                           if (uuidEntityString != null && uuidGraveString != null) {
                              try {
                                 UUID uuidEntity = UUID.fromString(uuidEntityString);
                                 UUID uuidGrave = UUID.fromString(uuidGraveString);
                                 EntityData entityData = new EntityData(location, uuidEntity, uuidGrave, type);
                                 this.getChunkData(location).addEntityData(entityData);
                                 this.plugin.getCacheManager().addEntityData(entityData);
                                 entityCount++;
                              } catch (IllegalArgumentException var18) {
                                 this.plugin
                                    .getLogger()
                                    .warning(
                                       "EntityMap row skipped: malformed UUIDs (entity="
                                          + uuidEntityString
                                          + ", grave="
                                          + uuidGraveString
                                          + ", table="
                                          + table
                                          + ")."
                                    );
                              }
                           } else {
                              this.plugin.getLogger().warning("EntityMap row skipped: missing UUIDs (location=" + location + ", table=" + table + ").");
                           }
                        }
                     }
                  }

                  if (entityCount == 0) {
                     this.plugin.getLogger().info("Loaded 0 entities into Entity Map Cache for " + this.getStoragePrefix() + table + ".");
                  } else {
                     this.plugin.getLogger().info("Loaded " + entityCount + " entities into Entity Map Cache for " + this.getStoragePrefix() + table + ".");
                  }
               } catch (SQLException var22) {
                  this.plugin.getLogger().severe("Error occurred while loading Entity Map for " + table);
                  this.plugin.logStackTrace((Exception)var22);
               }
            }
         );
   }

   public void loadHologramMap() {
      String query = "SELECT * FROM " + this.getStoragePrefix() + "hologram;";
      this.plugin
         .getSchedulerManager()
         .runTaskAsynchronously(
            () -> {
               this.plugin.getLogger().info("Loading Holograms into Hologram Map Cache...");
               int hologramCount = 0;

               try (
                  Connection connection = this.getConnection();
                  PreparedStatement statement = connection.prepareStatement(query);
                  ResultSet resultSet = statement.executeQuery();
               ) {
                  while (resultSet.next()) {
                     String locationString = resultSet.getString("location");
                     if (locationString == null) {
                        this.plugin.getLogger().warning("Hologram row skipped: location is null.");
                     } else {
                        Location location = LocationUtil.stringToLocation(locationString);
                        if (location.getWorld() == null) {
                           this.plugin.getLogger().warning("Hologram row skipped: invalid location '" + locationString + "'.");
                        } else {
                           String uuidEntityString = resultSet.getString("uuid_entity");
                           String uuidGraveString = resultSet.getString("uuid_grave");
                           if (uuidEntityString != null && uuidGraveString != null) {
                              String backendString = null;

                              try {
                                 backendString = resultSet.getString("backend");
                              } catch (SQLException var21) {
                              }

                              try {
                                 UUID uuidEntity = UUID.fromString(uuidEntityString);
                                 UUID uuidGrave = UUID.fromString(uuidGraveString);
                                 int line = resultSet.getInt("line");
                                 HologramData.Backend backend = HologramData.Backend.ARMOR_STAND;
                                 if (backendString != null && !backendString.isBlank()) {
                                    try {
                                       backend = HologramData.Backend.valueOf(backendString.trim().toUpperCase());
                                    } catch (IllegalArgumentException var19) {
                                    }
                                 }

                                 HologramData hologramData = new HologramData(location, uuidEntity, uuidGrave, line, backend);
                                 this.getChunkData(location).addEntityData(hologramData);
                                 this.plugin.getCacheManager().addEntityData(hologramData);
                                 hologramCount++;
                              } catch (IllegalArgumentException var20) {
                                 this.plugin
                                    .getLogger()
                                    .warning("Hologram row skipped: malformed UUIDs (entity=" + uuidEntityString + ", grave=" + uuidGraveString + ").");
                              }
                           } else {
                              this.plugin.getLogger().warning("Hologram row skipped: missing UUIDs (location=" + location + ").");
                           }
                        }
                     }
                  }

                  if (hologramCount == 0) {
                     this.plugin.getLogger().info("Loaded 0 Holograms into Hologram Map Cache.");
                  } else {
                     this.plugin.getLogger().info("Loaded " + hologramCount + " Holograms into Hologram Map Cache.");
                  }
               } catch (SQLException var25) {
                  this.plugin.getLogger().severe("Error occurred while loading Hologram Map");
                  this.plugin.logStackTrace((Exception)var25);
               }
            }
         );
   }

   private void createEntityDataMapTable(String name) {
      String physicalTable = this.getStoragePrefix() + name;

      String createTableQuery = switch (this.type) {
         case SQLITE -> "CREATE TABLE IF NOT EXISTS " + physicalTable + " (location TEXT, uuid_entity TEXT, uuid_grave TEXT);";
         case MYSQL, MARIADB, POSTGRESQL, H2 -> "CREATE TABLE IF NOT EXISTS "
            + physicalTable
            + " (location VARCHAR(255), uuid_entity VARCHAR(255), uuid_grave VARCHAR(255));";
         case MSSQL -> "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='"
            + physicalTable
            + "' AND xtype='U') CREATE TABLE "
            + physicalTable
            + " (location NVARCHAR(255), uuid_entity NVARCHAR(255), uuid_grave NVARCHAR(255));";
         default -> {
            this.plugin.getLogger().severe("Unsupported database type: " + this.type);
            yield null;
         }
      };
      if (createTableQuery != null) {
         try {
            this.executeUpdate(createTableQuery, new Object[0]);
         } catch (SQLException var7) {
            this.plugin.getLogger().severe("Failed to create entity data map table: " + physicalTable);
            this.plugin.logStackTrace((Exception)var7);
         }
         String varcharDef = switch (this.type) {
            case SQLITE -> "TEXT";
            case MSSQL -> "NVARCHAR(255)";
            default -> "VARCHAR(255)";
         };

         try {
            this.addColumnIfNotExists(physicalTable, "location", varcharDef);
            this.addColumnIfNotExists(physicalTable, "uuid_entity", varcharDef);
            this.addColumnIfNotExists(physicalTable, "uuid_grave", varcharDef);
         } catch (Exception var6) {
         }
      }
   }

   private void loadEntityDataMap(String table, EntityData.Type type) {
      String physicalTable = this.getStoragePrefix() + table;
      String query = "SELECT location, uuid_entity, uuid_grave FROM " + physicalTable + ";";
      this.plugin
         .getSchedulerManager()
         .runTaskAsynchronously(
            () -> {
               this.plugin.getLogger().info("Loading Entity Data Map Cache for " + physicalTable + "...");
               int entityCount = 0;

               try (
                  Connection connection = this.getConnection();
                  PreparedStatement statement = connection.prepareStatement(query);
                  ResultSet resultSet = statement.executeQuery();
               ) {
                  while (resultSet.next()) {
                     String locationString = resultSet.getString("location");
                     if (locationString == null) {
                        this.plugin.getLogger().warning("Entity Data row skipped: location is null (" + physicalTable + ").");
                     } else {
                        Location location = LocationUtil.stringToLocation(locationString);
                        if (location.getWorld() == null) {
                           this.plugin.getLogger().warning("Entity Data row skipped: invalid location '" + locationString + "' (" + physicalTable + ").");
                        } else {
                           String uuidEntityString = resultSet.getString("uuid_entity");
                           String uuidGraveString = resultSet.getString("uuid_grave");
                           if (uuidEntityString != null && uuidGraveString != null) {
                              try {
                                 UUID uuidEntity = UUID.fromString(uuidEntityString);
                                 UUID uuidGrave = UUID.fromString(uuidGraveString);
                                 EntityData entityData = new EntityData(location, uuidEntity, uuidGrave, type);
                                 this.getChunkData(location).addEntityData(entityData);
                                 this.plugin.getCacheManager().addEntityData(entityData);
                                 entityCount++;
                              } catch (IllegalArgumentException var18) {
                                 this.plugin
                                    .getLogger()
                                    .warning(
                                       "Entity Data row skipped: malformed UUIDs (entity="
                                          + uuidEntityString
                                          + ", grave="
                                          + uuidGraveString
                                          + ", table="
                                          + physicalTable
                                          + ")."
                                    );
                              }
                           } else {
                              this.plugin
                                 .getLogger()
                                 .warning("Entity Data row skipped: missing UUIDs (location=" + location + ", table=" + physicalTable + ").");
                           }
                        }
                     }
                  }

                  if (entityCount == 0) {
                     this.plugin.getLogger().info("Loaded 0 entities into Entity Data Map Cache for " + physicalTable + ".");
                  } else {
                     this.plugin.getLogger().info("Loaded " + entityCount + " entities into Entity Data Map Cache for " + physicalTable + ".");
                  }
               } catch (NullPointerException | SQLException var22) {
                  this.plugin.getLogger().severe("Error occurred while loading Entity Data Map for " + physicalTable);
                  this.plugin.logStackTrace((Exception)var22);
               }
            }
         );
   }

   public void addBlockData(BlockData blockData) {
      Location loc = blockData.getLocation();
      if (loc != null && loc.getWorld() != null && this.plugin.getVersionManager().isFolia()) {
         this.plugin.getSchedulerManager().execute(loc, () -> this.getChunkData(loc).addBlockData(blockData));
      } else {
         Objects.requireNonNull(this.getChunkData(loc)).addBlockData(blockData);
      }

      String query = "INSERT INTO " + this.getStoragePrefix() + "block (location, uuid_grave, replace_material, replace_data) VALUES (?, ?, ?, ?)";
      Object[] parameters = new Object[]{
         LocationUtil.locationToString(blockData.getLocation()),
         blockData.getGraveUUID() != null ? blockData.getGraveUUID().toString() : null,
         blockData.getReplaceMaterial(),
         blockData.getReplaceData()
      };
      this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
         try {
            this.executeUpdate(query, parameters);
         } catch (SQLException var4x) {
            this.plugin.getLogger().severe("Failed to add block data");
            this.plugin.logStackTrace((Exception)var4x);
         }
      });
   }

   public void removeBlockData(Location location) {
      if (location != null && location.getWorld() != null) {
         this.plugin.getSchedulerManager().execute(location, () -> {
            ChunkData chunkData = this.getChunkData(location);
            if (chunkData != null) {
               chunkData.removeBlockData(location);
            }
         });
         String query = "DELETE FROM " + this.getStoragePrefix() + "block WHERE location = ?";
         Object[] parameters = new Object[]{LocationUtil.locationToString(location)};
         this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
            try {
               this.executeUpdate(query, parameters);
            } catch (SQLException var4) {
               this.plugin.getLogger().severe("Failed to remove block data");
               this.plugin.logStackTrace((Exception)var4);
            }
         });
      }
   }

   public void addHologramData(HologramData hologramData) {
      Location loc = hologramData.getLocation();
      if (loc != null && loc.getWorld() != null) {
         this.plugin.getSchedulerManager().execute(loc, () -> {
            this.getChunkData(loc).addEntityData(hologramData);
            this.plugin.getCacheManager().addEntityData(hologramData);
         });
         String query = "INSERT INTO " + this.getStoragePrefix() + "hologram (uuid_entity, uuid_grave, line, location, backend) VALUES (?, ?, ?, ?, ?)";
         Object[] parameters = new Object[]{
            hologramData.getUUIDEntity().toString(),
            hologramData.getUUIDGrave().toString(),
            hologramData.getLine(),
            LocationUtil.locationToString(loc),
            hologramData.getBackend().name()
         };
         this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
            try {
               this.executeUpdate(query, parameters);
            } catch (SQLException var4x) {
               this.plugin.getLogger().severe("Failed to add hologram data");
               this.plugin.logStackTrace((Exception)var4x);
            }
         });
      } else {
         this.plugin.getLogger().warning("Skipped adding hologram data: location/world is null.");
      }
   }

   public void removeHologramData(Grave grave) {
      this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
         String selectSql = "SELECT uuid_entity, location, line, backend FROM " + this.getStoragePrefix() + "hologram WHERE uuid_grave = ?";
         String deleteSql = "DELETE FROM " + this.getStoragePrefix() + "hologram WHERE uuid_grave = ?";
         int scheduledRemovals = 0;

         try (
            Connection connection = this.plugin.getDataManager().getConnection();
            PreparedStatement selectStmt = connection.prepareStatement(selectSql);
         ) {
            selectStmt.setString(1, grave.getUUID().toString());

            try (ResultSet rs = selectStmt.executeQuery()) {
               while (rs.next()) {
                  String uuidEntityStr = rs.getString("uuid_entity");
                  String locString = rs.getString("location");
                  if (uuidEntityStr != null && locString != null) {
                     UUID uuidEntity;
                     try {
                        uuidEntity = UUID.fromString(uuidEntityStr);
                     } catch (IllegalArgumentException var24) {
                        continue;
                     }

                     Location location = LocationUtil.stringToLocation(locString);
                     if (location.getWorld() != null) {
                        int line = 0;

                        try {
                           line = rs.getInt("line");
                        } catch (SQLException var22) {
                        }

                        String backendString = null;

                        try {
                           backendString = rs.getString("backend");
                        } catch (SQLException var21) {
                        }

                        HologramData.Backend backend = HologramData.Backend.ARMOR_STAND;
                        if (backendString != null && !backendString.isBlank()) {
                           try {
                              backend = HologramData.Backend.valueOf(backendString.trim().toUpperCase());
                           } catch (IllegalArgumentException var20) {
                           }
                        }

                        HologramData hologramData = new HologramData(location, uuidEntity, grave.getUUID(), line, backend);
                        this.plugin.getSchedulerManager().execute(location, () -> {
                           this.getChunkData(location).removeEntityData(hologramData);
                           this.plugin.getCacheManager().removeEntityData(hologramData);
                        });
                        scheduledRemovals++;
                     }
                  }
               }
            }

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
               deleteStmt.setString(1, grave.getUUID().toString());
               deleteStmt.executeUpdate();
            }

            this.plugin.debugMessage("Deleted " + scheduledRemovals + " holograms from DB for grave UUID: " + grave.getUUID(), 2);
         } catch (SQLException var28) {
            this.plugin.getLogger().severe("Error deleting holograms for grave " + grave.getUUID());
            this.plugin.logStackTrace((Exception)var28);
         }
      });
   }

   public void addEntityData(EntityData entityData) {
      Location loc = entityData.getLocation();
      if (loc != null && loc.getWorld() != null) {
         this.plugin.getSchedulerManager().execute(loc, () -> {
            this.getChunkData(loc).addEntityData(entityData);
            this.plugin.getCacheManager().addEntityData(entityData);
         });
         String table = this.entityDataTypeTable(entityData.getType());
         String query = "INSERT INTO " + this.getStoragePrefix() + table + " (location, uuid_entity, uuid_grave) VALUES (?, ?, ?)";
         Object[] parameters = new Object[]{LocationUtil.locationToString(loc), entityData.getUUIDEntity().toString(), entityData.getUUIDGrave().toString()};
         this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
            try {
               this.executeUpdate(query, parameters);
            } catch (SQLException var4x) {
               this.plugin.getLogger().severe("Failed to add entity data");
               this.plugin.logStackTrace((Exception)var4x);
            }
         });
      } else {
         this.plugin.getLogger().warning("Skipped adding entity data: location/world is null (type=" + entityData.getType() + ").");
      }
   }

   public void removeEntityData(EntityData entityData) {
      this.removeEntityData(List.of(entityData));
   }

   public void removeEntityData(List<EntityData> entityDataList) {
      this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
         try {
            for (EntityData entityData : entityDataList) {
               Location loc = entityData.getLocation();
               if (loc != null && loc.getWorld() != null) {
                  this.plugin.getSchedulerManager().execute(loc, () -> {
                     if (entityData.getType() != EntityData.Type.HOLOGRAM) {
                        Grave grave = this.plugin.getCacheManager().getGrave(entityData.getUUIDGrave());
                        this.plugin.getHologramManager().removeHologram(grave);
                     }

                     this.getChunkData(loc).removeEntityData(entityData);
                     this.plugin.getCacheManager().removeEntityData(entityData);
                  });
               } else {
                  if (entityData.getType() != EntityData.Type.HOLOGRAM) {
                     Grave grave = this.plugin.getCacheManager().getGrave(entityData.getUUIDGrave());
                     this.plugin.getHologramManager().removeHologram(grave);
                  }

                  this.plugin.getCacheManager().removeEntityData(entityData);
               }

               String table = this.entityDataTypeTable(entityData.getType());
               String query = "DELETE FROM " + this.getStoragePrefix() + table + " WHERE uuid_entity = ?";
               this.executeUpdate(query, new Object[]{entityData.getUUIDEntity().toString()});
               this.plugin.debugMessage("Removing " + this.getStoragePrefix() + table + " for grave " + entityData.getUUIDGrave(), 1);
            }
         } catch (SQLException var7) {
            this.plugin.getLogger().severe("Failed to remove entity data");
            this.plugin.logStackTrace((Exception)var7);
         }
      });
   }

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2026.4.9.2"
   )
   public boolean hasGraveAtLocation(Location location) {
      String query = "SELECT COUNT(*) FROM " + this.getStoragePrefix() + "grave WHERE location_death = ?";

      try (
         Connection connection = this.getConnection();
         PreparedStatement statement = connection.prepareStatement(query);
      ) {
         statement.setString(1, LocationUtil.locationToString(location));

         try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) > 0 : false;
         }
      } catch (SQLException var14) {
         this.plugin.getLogger().severe("Failed to check if grave exists at location: " + var14.getMessage());
         return false;
      }
   }

   public String entityDataTypeTable(EntityData.Type type) {
      return switch (type) {
         case ARMOR_STAND -> "armorstand";
         case ITEM_FRAME -> "itemframe";
         case HOLOGRAM -> "hologram";
         case FURNITURELIB -> "furniturelib";
         case FURNITUREENGINE -> "furnitureengine";
         case ITEMSADDER -> "itemsadder";
         case ORAXEN -> "oraxen";
         case NEXO -> "nexo";
         case PLAYERNPC -> "playernpc";
         case MANNEQUIN -> "mannequins";
         default -> type.name().toLowerCase(Locale.ROOT).replace("_", "");
      };
   }

   public void addGrave(Grave grave) {
      Location deathLoc = null;

      try {
         deathLoc = grave.getLocationDeath();
      } catch (Throwable var7) {
      }

      if (deathLoc != null && deathLoc.getWorld() != null) {
         this.plugin.getSchedulerManager().execute(deathLoc, () -> this.plugin.getCacheManager().getGraveMap().put(grave.getUUID(), grave));
      } else {
         this.plugin.getCacheManager().getGraveMap().put(grave.getUUID(), grave);
      }

      String query = "INSERT INTO "
         + this.getStoragePrefix()
         + "grave (uuid, owner_type, owner_name, owner_name_display, owner_uuid, owner_texture, owner_texture_signature, killer_type, killer_name, killer_name_display, killer_uuid, location_death, yaw, pitch, inventory, equipment, experience, protection, is_abandoned, time_alive, time_protection, time_creation, permissions, provider_id, death_reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      String providerId = null;

      try {
         providerId = grave.getProviderId();
      } catch (Throwable var6) {
      }

      Object[] parameters = new Object[]{
         grave.getUUID(),
         grave.getOwnerType(),
         grave.getOwnerName() != null ? grave.getOwnerName().replace("'", "''") : null,
         grave.getOwnerNameDisplay() != null ? grave.getOwnerNameDisplay().replace("'", "''") : null,
         grave.getOwnerUUID(),
         grave.getOwnerTexture() != null ? grave.getOwnerTexture().replace("'", "''") : null,
         grave.getOwnerTextureSignature() != null ? grave.getOwnerTextureSignature().replace("'", "''") : null,
         grave.getKillerType(),
         grave.getKillerName() != null ? grave.getKillerName().replace("'", "''") : null,
         grave.getKillerNameDisplay() != null ? grave.getKillerNameDisplay().replace("'", "''") : null,
         grave.getKillerUUID(),
         deathLoc != null ? LocationUtil.locationToString(deathLoc) : null,
         grave.getYaw(),
         grave.getPitch(),
         InventoryUtil.inventoryToString(grave.getInventory(), this.plugin),
         InventoryUtil.equipmentMapToString(grave.getEquipmentMap(), this.plugin, grave),
         grave.getExperience(),
         grave.getProtection() ? 1 : 0,
         grave.isAbandoned() ? 1 : 0,
         grave.getTimeAlive(),
         grave.getTimeProtection(),
         grave.getTimeCreation(),
         grave.getPermissionList() != null && !grave.getPermissionList().isEmpty() ? StringUtils.join(grave.getPermissionList(), "|") : null,
         providerId,
         grave.getDeathCause() != null ? grave.getDeathCause() : null
      };
      this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
         try {
            this.executeUpdate(query, parameters);
         } catch (SQLException var4x) {
            this.plugin.getLogger().severe("Failed to add grave");
            this.plugin.logStackTrace((Exception)var4x);
         }
      });
   }

   public void removeGrave(Grave grave) {
      this.removeGrave(grave.getUUID());
   }

   public void removeGrave(UUID uuid) {
      Grave grave = this.plugin.getCacheManager().getGraveMap().remove(uuid);
      if (grave != null) {
         Location deathLoc = grave.getLocationDeath();
         if (deathLoc != null && deathLoc.getWorld() != null) {
            this.plugin.getSchedulerManager().execute(deathLoc, () -> this.plugin.getHologramManager().removeHologram(grave));
         } else {
            this.plugin.getHologramManager().removeHologram(grave);
         }

         this.removeHologramData(grave);
      }

      String deleteQuery = "DELETE FROM " + this.getStoragePrefix() + "grave WHERE uuid = ?";
      Object[] deleteParams = new Object[]{uuid};
      this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
         try {
            this.plugin.debugMessage("Attempting to remove grave for UUID: " + uuid, 1);
            this.executeUpdate(deleteQuery, deleteParams);
            this.plugin.debugMessage("Grave successfully removed for UUID: " + uuid, 1);
         } catch (SQLException var5x) {
            this.plugin.getLogger().severe("Failed to remove grave");
            this.plugin.logStackTrace((Exception)var5x);
         }
      });
   }

   public void updateGrave(Grave grave, String column, int integer) {
      String query = "UPDATE " + this.getStoragePrefix() + "grave SET " + column + " = ? WHERE uuid = ?";
      Object[] parameters = new Object[]{integer, grave.getUUID()};
      this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
         try {
            this.executeUpdate(query, parameters);
         } catch (SQLException var4x) {
            this.plugin.getLogger().severe("Failed to update grave");
            this.plugin.logStackTrace((Exception)var4x);
         }
      });
   }

   public void updateGrave(Grave grave, String column, String string) {
      String query = "UPDATE " + this.getStoragePrefix() + "grave SET " + column + " = ? WHERE uuid = ?";
      Object[] parameters = new Object[]{string, grave.getUUID()};
      this.plugin.getSchedulerManager().runTaskAsynchronously(() -> {
         try {
            this.executeUpdate(query, parameters);
         } catch (SQLException var4x) {
            this.plugin.getLogger().severe("Failed to update grave");
            this.plugin.logStackTrace((Exception)var4x);
         }
      });
   }

   @Experimental
   public void updateGraveMainThread(Grave grave, String column, String string) {
      String query = "UPDATE " + this.getStoragePrefix() + "grave SET " + column + " = ? WHERE uuid = ?";
      Object[] parameters = new Object[]{string, grave.getUUID()};

      try {
         this.executeUpdateMainThread(query, parameters);
      } catch (SQLException var7) {
         this.plugin.getLogger().severe("Failed to update grave over main thread");
         this.plugin.logStackTrace((Exception)var7);
      }
   }

   public Grave resultSetToGrave(ResultSet resultSet) {
      try {
         String uuidString = resultSet.getString("uuid");
         if (uuidString == null) {
            this.plugin.getLogger().warning("Skipping grave at row " + resultSet.getRow() + " due to null UUID.");
            return null;
         } else {
            Grave grave = new Grave(UUID.fromString(uuidString));
            List<String> invalidationReason = new ArrayList<>();
            Location graveLocation = null;
            String ownerType = resultSet.getString("owner_type");
            if (ownerType == null) {
               invalidationReason.add("owner_type");
            }

            grave.setOwnerType(ownerType != null ? EntityType.valueOf(ownerType) : null);
            String ownerName = resultSet.getString("owner_name");
            if (ownerType != null && ownerName == null) {
               this.plugin.getLogger().warning("Skipping grave at row " + resultSet.getRow() + " due to null Owner Name.");
               return null;
            } else {
               grave.setOwnerName(ownerName != null ? ownerName.replace(" ", "_") : null);
               String ownerNameDisplay = resultSet.getString("owner_name_display");
               if (ownerNameDisplay == null) {
                  invalidationReason.add("owner_name_display");
               }

               grave.setOwnerNameDisplay(ownerNameDisplay);
               String ownerUUID = resultSet.getString("owner_uuid");
               if (ownerUUID == null) {
                  invalidationReason.add("owner_uuid");
               }

               grave.setOwnerUUID(ownerUUID != null ? UUID.fromString(ownerUUID) : null);
               String ownerTexture = resultSet.getString("owner_texture");
               if (ownerTexture == null) {
                  invalidationReason.add("owner_texture");
               }

               grave.setOwnerTexture(ownerTexture);
               String ownerTextureSignature = resultSet.getString("owner_texture_signature");
               if (ownerTextureSignature == null) {
                  invalidationReason.add("owner_texture_signature");
               }

               grave.setOwnerTextureSignature(ownerTextureSignature);
               String killerType = resultSet.getString("killer_type");
               if (killerType == null) {
                  invalidationReason.add("killer_type");
               }

               grave.setKillerType(killerType != null ? EntityType.valueOf(killerType) : null);
               String killerName = resultSet.getString("killer_name");
               if (killerName == null) {
                  invalidationReason.add("killer_name");
               }

               grave.setKillerName(killerName != null ? killerName.replace(" ", "_") : null);
               String killerNameDisplay = resultSet.getString("killer_name_display");
               if (killerNameDisplay == null) {
                  invalidationReason.add("killer_name_display");
               }

               grave.setKillerNameDisplay(killerNameDisplay != null ? killerNameDisplay.replace(" ", "_") : null);
               String killerUUID = resultSet.getString("killer_uuid");
               if ("PLAYER".equalsIgnoreCase(killerType) && killerUUID == null) {
                  invalidationReason.add("killer_uuid for killer_type PLAYER");
               }

               grave.setKillerUUID(killerUUID != null ? UUID.fromString(killerUUID) : null);
               String locationDeath = resultSet.getString("location_death");
               graveLocation = locationDeath != null ? LocationUtil.stringToLocation(locationDeath) : null;
               if (graveLocation == null) {
                  this.plugin.getLogger().warning("Skipping grave " + uuidString + " at row " + resultSet.getRow() + " because location is null.");
                  return null;
               } else {
                  World world = graveLocation.getWorld();
                  if (world != null && Bukkit.getWorld(world.getName()) != null) {
                     grave.setLocationDeath(graveLocation);
                     if (resultSet.getString("equipment") == null) {
                        invalidationReason.add("equipment");
                     }

                     if (resultSet.getString("equipment") != null) {
                        grave.setEquipmentMap(InventoryUtil.stringToEquipmentMap(resultSet.getString("equipment"), this.plugin, grave));
                     }

                     grave.setYaw(resultSet.getFloat("yaw"));
                     grave.setPitch(resultSet.getFloat("pitch"));
                     grave.setExperience(resultSet.getInt("experience"));
                     grave.setProtection(resultSet.getInt("protection") == 1);
                     grave.setAbandoned(resultSet.getInt("is_abandoned") == 1);
                     grave.setTimeAlive(resultSet.getLong("time_alive"));
                     grave.setTimeProtection(resultSet.getLong("time_protection"));
                     grave.setTimeCreation(resultSet.getLong("time_creation"));
                     grave.setPermissionList(
                        resultSet.getString("permissions") != null
                           ? new ArrayList<>(Arrays.asList(resultSet.getString("permissions").split("\\|")))
                           : new ArrayList<>()
                     );
                     grave.setInventory(
                        InventoryUtil.stringToInventory(
                           grave,
                           resultSet.getString("inventory"),
                           StringUtil.parseString(
                              this.plugin
                                 .getConfigManager()
                                 .getConfigSection("gui.grave.title", grave.getOwnerType(), grave.getPermissionList())
                                 .getString("gui.grave.title"),
                              grave.getLocationDeath(),
                              grave,
                              this.plugin
                           ),
                           this.plugin
                        )
                     );
                     if (!invalidationReason.isEmpty()) {
                        this.plugin.logInvalidGraveSite(uuidString, graveLocation, invalidationReason);
                     }

                     grave.setProviderId(resultSet.getString("provider_id") != null ? resultSet.getString("provider_id") : null);
                     String deathReason = null;

                     try {
                        deathReason = resultSet.getString("death_reason");
                     } catch (SQLException var20) {
                     }

                     if (deathReason != null && !deathReason.isBlank()) {
                        grave.setDeathCause(deathReason);
                     }

                     return grave;
                  } else {
                     this.plugin
                        .getLogger()
                        .warning(
                           "Skipping grave "
                              + uuidString
                              + " at row "
                              + resultSet.getRow()
                              + " because the world '"
                              + (world != null ? world.getName() : "null")
                              + "' does not exist or is not loaded."
                        );
                     return null;
                  }
               }
            }
         }
      } catch (SQLException var21) {
         this.plugin.getLogger().severe("Error occurred while converting a ResultSet to a Grave object");
         this.plugin.logStackTrace((Exception)var21);
         return null;
      }
   }

   private boolean isConnected() {
      return this.dataSource != null && !this.dataSource.isClosed();
   }

   private Connection getConnection() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         try {
            return this.dataSource.getConnection();
         } catch (SQLException var2) {
            this.plugin.getLogger().severe("Error obtaining database connection");
            this.plugin.logStackTrace((Exception)var2);
            return null;
         }
      } else {
         this.plugin.getLogger().severe("DataSource is not initialized or has been closed");
         return null;
      }
   }

   public Map<String, Map<String, Integer>> getDatabaseVersions() throws SQLException {
      String version = this.getDatabaseVersion();

      return switch (this.type) {
         case MYSQL -> Map.of("MySQL", Map.of(version, 1));
         case MARIADB -> Map.of("MariaDB", Map.of(version, 1));
         case POSTGRESQL -> Map.of("PostgreSQL", Map.of(version, 1));
         default -> Map.of("Other", Map.of("Unknown", 1));
         case MSSQL -> Map.of("Microsoft SQL Server", Map.of(version, 1));
      };
   }

   public String getDatabaseVersion() {
      String query = switch (this.type) {
         case MYSQL, MARIADB, POSTGRESQL -> "SELECT version()";
         default -> null;
      };
      if (query == null) {
         return "Unknown";
      } else {
         try {
            String version;
            try (
               Connection connection = this.dataSource.getConnection();
               Statement statement = connection.createStatement();
               ResultSet resultSet = statement.executeQuery(query);
            ) {
               if (resultSet.next()) {
                  version = resultSet.getString(1);
                  return version != null && !version.isEmpty() ? version.split(",")[0] : "Unknown";
               }

               version = "Unknown";
            }

            return version;
         } catch (Exception var13) {
            return "Unknown";
         }
      }
   }

   public void closeConnection() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }

   private void executeUpdate(String sql, Object[] parameters) throws SQLException {
      this.plugin
         .getSchedulerManager()
         .runTaskAsynchronously(
            () -> {
               try {
                  try (Connection connection = this.getConnection()) {
                     if (connection != null) {
                        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                           if (parameters != null) {
                              for (int i = 0; i < parameters.length; i++) {
                                 Object parameter = parameters[i];
                                 int idx = i + 1;
                                 if (parameter == null) {
                                    statement.setNull(idx, 12);
                                 } else if (parameter instanceof String s) {
                                    statement.setString(idx, s);
                                 } else if (parameter instanceof Integer n) {
                                    statement.setInt(idx, n);
                                 } else if (parameter instanceof Long n) {
                                    statement.setLong(idx, n);
                                 } else if (parameter instanceof Double n) {
                                    statement.setDouble(idx, n);
                                 } else if (parameter instanceof Float n) {
                                    statement.setFloat(idx, n);
                                 } else if (parameter instanceof Boolean b) {
                                    statement.setBoolean(idx, b);
                                 } else if (parameter instanceof UUID u) {
                                    statement.setObject(idx, u.toString(), 12);
                                 } else if (parameter instanceof byte[] bytes) {
                                    statement.setBytes(idx, bytes);
                                 } else if (parameter instanceof Date d) {
                                    statement.setDate(idx, d);
                                 } else if (parameter instanceof Timestamp ts) {
                                    statement.setTimestamp(idx, ts);
                                 } else if (parameter instanceof LocalDate ld) {
                                    statement.setObject(idx, ld, 91);
                                 } else if (parameter instanceof LocalDateTime ldt) {
                                    statement.setObject(idx, ldt, 93);
                                 } else if (parameter instanceof Clob c) {
                                    statement.setClob(idx, c);
                                 } else if (parameter instanceof Blob b) {
                                    statement.setBlob(idx, b);
                                 } else if (parameter instanceof EntityType et) {
                                    statement.setString(idx, et.name());
                                 } else {
                                    statement.setObject(idx, parameter);
                                 }
                              }
                           }

                           statement.executeUpdate();
                           return;
                        }
                     }

                     this.plugin.getLogger().severe("Error executing SQL update: connection is null");
                     this.plugin.getLogger().severe("Failed SQL statement: " + sql);
                  }

                  return;
               } catch (SQLException var27) {
                  String sqlState = var27.getSQLState();
                  String message = var27.getMessage() != null ? var27.getMessage().toLowerCase(Locale.ROOT) : "";
                  if (!"42701".equals(sqlState)
                     && !"42P07".equals(sqlState)
                     && !"42S01".equals(sqlState)
                     && !"42S02".equals(sqlState)
                     && !"42S04".equals(sqlState)
                     && !"X0Y32".equals(sqlState)
                     && !"42000".equals(sqlState)
                     && (!"SQLITE_ERROR".equals(sqlState) || !message.contains("duplicate column name"))) {
                     this.plugin.getLogger().severe("Error executing SQL update");
                     this.plugin.getLogger().severe("Failed SQL statement: " + sql);
                     this.plugin.logStackTrace((Exception)var27);
                  }
               }
            }
         );
   }

   @Experimental
   private void executeUpdateMainThread(String sql, Object[] parameters) throws SQLException {
      try {
         try (Connection connection = this.getConnection()) {
            if (connection != null) {
               try (PreparedStatement statement = connection.prepareStatement(sql)) {
                  if (parameters != null) {
                     for (int i = 0; i < parameters.length; i++) {
                        Object parameter = parameters[i];
                        int idx = i + 1;
                        if (parameter == null) {
                           statement.setNull(idx, 12);
                        } else if (parameter instanceof String s) {
                           statement.setString(idx, s);
                        } else if (parameter instanceof Integer n) {
                           statement.setInt(idx, n);
                        } else if (parameter instanceof Long n) {
                           statement.setLong(idx, n);
                        } else if (parameter instanceof Double n) {
                           statement.setDouble(idx, n);
                        } else if (parameter instanceof Float n) {
                           statement.setFloat(idx, n);
                        } else if (parameter instanceof Boolean b) {
                           statement.setBoolean(idx, b);
                        } else if (parameter instanceof UUID u) {
                           statement.setObject(idx, u.toString(), 12);
                        } else if (parameter instanceof byte[] bytes) {
                           statement.setBytes(idx, bytes);
                        } else if (parameter instanceof Date d) {
                           statement.setDate(idx, d);
                        } else if (parameter instanceof Timestamp ts) {
                           statement.setTimestamp(idx, ts);
                        } else if (parameter instanceof LocalDate ld) {
                           statement.setObject(idx, ld, 91);
                        } else if (parameter instanceof LocalDateTime ldt) {
                           statement.setObject(idx, ldt, 93);
                        } else if (parameter instanceof Clob c) {
                           statement.setClob(idx, c);
                        } else if (parameter instanceof Blob b) {
                           statement.setBlob(idx, b);
                        } else if (parameter instanceof EntityType et) {
                           statement.setString(idx, et.name());
                        } else {
                           statement.setObject(idx, parameter);
                        }
                     }
                  }

                  statement.executeUpdate();
                  return;
               }
            }

            this.plugin.getLogger().severe("Error executing SQL update on main thread: connection is null");
            this.plugin.getLogger().severe("Failed SQL statement: " + sql);
         }

         return;
      } catch (SQLException var27) {
         String sqlState = var27.getSQLState();
         String message = var27.getMessage() != null ? var27.getMessage().toLowerCase(Locale.ROOT) : "";
         if (!"42701".equals(sqlState)
            && !"42P07".equals(sqlState)
            && !"42S01".equals(sqlState)
            && !"42S02".equals(sqlState)
            && !"42S04".equals(sqlState)
            && !"X0Y32".equals(sqlState)
            && !"42000".equals(sqlState)
            && (!"SQLITE_ERROR".equals(sqlState) || !message.contains("duplicate column name"))) {
            this.plugin.getLogger().severe("Error executing SQL update");
            this.plugin.getLogger().severe("Failed SQL statement: " + sql);
            this.plugin.logStackTrace((Exception)var27);
         }
      }
   }

   private boolean testDatabaseConnection() {
      try {
         boolean ignored;
         try (Connection testConnection = this.getConnection()) {
            if (testConnection == null) {
               this.plugin.getLogger().severe("Failed to obtain a database connection for type: " + this.type);
               return false;
            }

            try {
               ignored = testConnection.isValid(5);
            } catch (SQLException var5) {
               return !testConnection.isClosed();
            }
         }

         return ignored;
      } catch (SQLException var7) {
         this.plugin.getLogger().severe("Failed to connect to " + this.type + " database: " + var7.getMessage());
         return false;
      }
   }

   private void migrate() {
      DataManager.MigrationSource source = this.detectMigrationSource();
      if (source.path() != null && source.type() != DataManager.Type.INVALID) {
         switch (source.type()) {
            case SQLITE:
               this.migrateFromSQLite(source.path());
               break;
            case H2:
               this.migrateFromH2(source.path());
               break;
            default:
               this.plugin.getLogger().warning("Unsupported migration source type: " + source.type());
         }
      }
   }

   private DataManager.MigrationSource detectMigrationSource() {
      Path dataDir = this.plugin.getDataFolder().toPath().resolve("data");
      Path sqliteFile = dataDir.resolve("data.db");
      if (Files.isRegularFile(sqliteFile)) {
         return this.type == DataManager.Type.SQLITE
            ? new DataManager.MigrationSource(DataManager.Type.INVALID, null)
            : new DataManager.MigrationSource(DataManager.Type.SQLITE, sqliteFile);
      } else {
         Path h2Base = dataDir.resolve("graves.data");
         Path h2Mv = Paths.get(h2Base.toString() + ".mv.db");
         if (Files.isRegularFile(h2Mv)) {
            return this.type == DataManager.Type.H2
               ? new DataManager.MigrationSource(DataManager.Type.INVALID, null)
               : new DataManager.MigrationSource(DataManager.Type.H2, h2Base);
         } else {
            return new DataManager.MigrationSource(DataManager.Type.INVALID, null);
         }
      }
   }

   private void migrateFromSQLite(Path sqliteFile) {
      if (sqliteFile != null && Files.isRegularFile(sqliteFile)) {
         HikariConfig sourceConfig = new HikariConfig();
         String journalMode = this.plugin.getConfig().getString("settings.storage.sqlite.journal-mode", "WAL");
         String synchronous = this.plugin.getConfig().getString("settings.storage.sqlite.synchronous", "OFF");
         String jm = journalMode.toUpperCase(Locale.ROOT);
         if (!jm.equals("DELETE") && !jm.equals("TRUNCATE") && !jm.equals("PERSIST") && !jm.equals("MEMORY") && !jm.equals("WAL") && !jm.equals("OFF")) {
            jm = "WAL";
         }

         String syn = synchronous.toUpperCase(Locale.ROOT);
         if (!syn.equals("OFF")
            && !syn.equals("0")
            && !syn.equals("NORMAL")
            && !syn.equals("1")
            && !syn.equals("FULL")
            && !syn.equals("2")
            && !syn.equals("EXTRA")
            && !syn.equals("3")) {
            syn = "OFF";
         }

         String jdbcUrl = "jdbc:sqlite:" + sqliteFile.toAbsolutePath() + "?busy_timeout=30000";
         sourceConfig.setJdbcUrl(jdbcUrl);
         sourceConfig.setDriverClassName("org.sqlite.JDBC");
         sourceConfig.setPoolName("GravesX SQLite Migration to " + this.getType());
         sourceConfig.setMaximumPoolSize(10);
         sourceConfig.setMinimumIdle(1);
         sourceConfig.setConnectionTimeout(30000L);
         sourceConfig.setIdleTimeout(600000L);
         sourceConfig.setMaxLifetime(1800000L);
         sourceConfig.setConnectionInitSql("PRAGMA journal_mode=" + jm + "; PRAGMA synchronous=" + syn + ";");
         sourceConfig.setConnectionTestQuery("SELECT 1");

         try {
            HikariDataSource sourceDataSource = new HikariDataSource(sourceConfig);

            try (
               Connection sourceConn = sourceDataSource.getConnection();
               Connection targetConn = this.getConnection();
            ) {
               this.migrateFromConnection(sourceConn, targetConn, DataManager.Type.SQLITE);
               this.renameMigratedSQLiteFile(sqliteFile);
            } catch (Throwable var18) {
               try {
                  sourceDataSource.close();
               } catch (Throwable var13) {
                  var18.addSuppressed(var13);
               }

               throw var18;
            }

            sourceDataSource.close();
         } catch (Exception var19) {
            this.plugin.getLogger().severe("Failed to migrate legacy SQLite database.");
            this.plugin.logStackTrace(var19);
         }
      } else {
         this.plugin.getLogger().warning("Skipping SQLite migration because the source database file does not exist.");
      }
   }

   private void migrateFromH2(Path h2BasePath) {
      if (h2BasePath == null) {
         this.plugin.getLogger().warning("Skipping H2 migration because the source database path is null.");
      } else {
         Path mvFile = Paths.get(h2BasePath.toString() + ".mv.db");
         Path traceFile = Paths.get(h2BasePath.toString() + ".trace.db");
         if (!Files.isRegularFile(mvFile)) {
            this.plugin.getLogger().warning("Skipping H2 migration because the source database file does not exist: " + mvFile);
         } else {
            HikariConfig sourceConfig = new HikariConfig();
            long maxLifetime = this.plugin.getConfig().getLong("settings.storage.h2.maxLifetime", 1800000L);
            int maxConnections = this.plugin.getConfig().getInt("settings.storage.h2.maxConnections", 50);
            long connectionTimeout = this.plugin.getConfig().getLong("settings.storage.h2.connectionTimeout", 30000L);
            String username = this.plugin.getConfig().getString("settings.storage.h2.username", "sa");
            String password = this.plugin.getConfig().getString("settings.storage.h2.password", "");
            String jdbcUrl = "jdbc:h2:file:" + h2BasePath.toAbsolutePath() + ";AUTO_SERVER=FALSE;DB_CLOSE_DELAY=-1;";
            sourceConfig.setJdbcUrl(jdbcUrl);
            sourceConfig.setDriverClassName("com.ranull.graves.libraries.h2.Driver");
            sourceConfig.setUsername(username);
            sourceConfig.setPassword(password);
            sourceConfig.setPoolName("GravesX h2 Migration to " + this.getType());
            sourceConfig.setMaximumPoolSize(maxConnections);
            sourceConfig.setMinimumIdle(Math.min(2, maxConnections));
            sourceConfig.setConnectionTimeout(connectionTimeout);
            sourceConfig.setIdleTimeout(600000L);
            sourceConfig.setMaxLifetime(maxLifetime);

            try {
               sourceConfig.setKeepaliveTime(300000L);
               sourceConfig.setValidationTimeout(5000L);
            } catch (Throwable var21) {
            }

            sourceConfig.setConnectionTestQuery("SELECT 1");

            try {
               HikariDataSource sourceDs = new HikariDataSource(sourceConfig);

               try (
                  Connection sourceConn = sourceDs.getConnection();
                  Connection targetConn = this.getConnection();
               ) {
                  this.migrateFromConnection(sourceConn, targetConn, DataManager.Type.H2);
                  this.renameMigratedH2File(mvFile);
                  if (Files.exists(traceFile)) {
                     this.renameMigratedH2File(traceFile);
                  }
               } catch (Throwable var24) {
                  try {
                     sourceDs.close();
                  } catch (Throwable var18) {
                     var24.addSuppressed(var18);
                  }

                  throw var24;
               }

               sourceDs.close();
            } catch (Exception var25) {
               this.plugin.getLogger().severe("H2 migration failed");
               this.plugin.logStackTrace(var25);
            }
         }
      }
   }

   private void migrateFromConnection(Connection sourceConn, Connection targetConn, DataManager.Type sourceType) throws Exception {
      if (targetConn == null) {
         this.plugin.getLogger().severe("Migration aborted: target database connection is null.");
      } else {
         for (String tableName : this.getMigrationTableNames(sourceConn, sourceType)) {
            List<String> columns = new ArrayList<>();
            StringJoiner defs = new StringJoiner(", ");

            try (
               Statement stmt = sourceConn.createStatement();
               ResultSet rsMeta = stmt.executeQuery(this.getMetadataQuery(tableName, sourceType));
            ) {
               ResultSetMetaData md = rsMeta.getMetaData();

               for (int i = 1; i <= md.getColumnCount(); i++) {
                  String columnName = md.getColumnName(i);
                  String sourceColumnType = md.getColumnTypeName(i);
                  String targetColumnType = this.mapSourceTypeToTargetDB(sourceType, sourceColumnType, columnName);
                  if (targetColumnType != null) {
                     columns.add(columnName);
                     defs.add(columnName + " " + targetColumnType);
                  }
               }
            }

            if (columns.isEmpty()) {
               this.plugin.getLogger().warning("No columns mapped for " + tableName + "; skipping.");
            } else {
               String createSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + defs + ")";
               this.executeUpdate(createSql, new Object[0]);
               if (tableName.equals(this.getStoragePrefix() + "grave")) {
                  this.adjustGraveTableForTargetDB();
               }

               this.copyRows(sourceConn, targetConn, sourceType, tableName, columns);
            }
         }
      }
   }

   private List<String> getMigrationTableNames(Connection sourceConn, DataManager.Type sourceType) throws SQLException {
      List<String> tables = new ArrayList<>();
      String prefix = this.getStoragePrefix();
      this.addTableIfExists(tables, prefix + "grave");
      this.addTableIfExists(tables, prefix + "hologram");
      this.addTableIfExists(tables, prefix + "armorstand");
      this.addTableIfExists(tables, prefix + "itemframe");
      this.addTableIfExists(tables, prefix + "chunk");
      return tables;
   }

   private void addTableIfExists(List<String> tables, String tableName) {
      if (this.tableExists(tableName)) {
         tables.add(tableName);
      }
   }

   private String mapSourceTypeToTargetDB(DataManager.Type sourceType, String sourceColumnType, String columnName) {
      return switch (sourceType) {
         case SQLITE -> this.mapSQLiteTypeToTargetDB(sourceColumnType, columnName);
         case H2 -> this.mapH2TypeToTargetDB(sourceColumnType, columnName);
         default -> null;
      };
   }

   private String mapH2TypeToTargetDB(String h2Type, String columnName) {
      return switch (this.type) {
         case MYSQL, MARIADB -> this.mapH2TypeToMySQL(h2Type, columnName);
         case POSTGRESQL -> this.mapH2TypeToPostgreSQL(h2Type, columnName);
         default -> {
            this.plugin.getLogger().warning("Unhandled target database type: " + this.type);
            yield null;
         }
         case MSSQL -> this.mapH2TypeToMSSQL(h2Type, columnName);
      };
   }

   private String getMetadataQuery(String tableName, DataManager.Type sourceType) {
      switch (sourceType) {
         case SQLITE:
         case H2:
            return "SELECT * FROM " + tableName + " LIMIT 1";
         default:
            throw new IllegalArgumentException("Unsupported metadata source type: " + sourceType);
      }
   }

   private void copyRows(Connection sourceConn, Connection targetConn, DataManager.Type sourceType, String tableName, List<String> columns) throws SQLException {
      String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
      String insertSql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES (" + placeholders + ")";

      try (
         PreparedStatement insert = targetConn.prepareStatement(insertSql);
         Statement readStmt = sourceConn.createStatement();
         ResultSet rows = readStmt.executeQuery("SELECT * FROM " + tableName);
      ) {
         while (rows.next()) {
            for (int idx = 0; idx < columns.size(); idx++) {
               Object value = rows.getObject(columns.get(idx));
               insert.setObject(idx + 1, value);
            }

            insert.executeUpdate();
         }
      }
   }

   private void renameMigratedH2File(Path source) throws IOException {
      if (Files.exists(source)) {
         Path target = source.resolveSibling(source.getFileName().toString() + ".migrated.db");

         try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var4) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
         }
      }
   }

   private void renameMigratedSQLiteFile(Path sqliteFile) throws IOException {
      Path target = sqliteFile.resolveSibling("data.migrated.db");
      Files.move(sqliteFile, target, StandardCopyOption.REPLACE_EXISTING);
   }

   private String mapSQLiteTypeToTargetDB(String sqliteType, String columnName) {
      return switch (this.type) {
         case MYSQL, MARIADB -> this.mapSQLiteTypeToMySQL(sqliteType, columnName);
         case POSTGRESQL -> this.mapSQLiteTypeToPostgreSQL(sqliteType, columnName);
         case H2 -> this.mapSQLiteTypeToH2(sqliteType, columnName);
         case MSSQL -> this.mapSQLiteTypeToMSSQL(sqliteType, columnName);
         default -> {
            this.plugin.getLogger().warning("Unhandled database type: " + this.type);
            yield null;
         }
      };
   }

   private String mapSQLiteTypeToMySQL(String sqliteType, String columnName) {
      String upperType = sqliteType.toUpperCase(Locale.ROOT).trim();
      if (upperType.startsWith("INT")) {
         if ("protection".equals(columnName)) {
            return "INT(1)";
         } else {
            return !"time_protection".equals(columnName) && !"time_creation".equals(columnName) && !"time_alive".equals(columnName) ? "INT(16)" : "BIGINT";
         }
      } else if (upperType.startsWith("BIGINT")) {
         return "BIGINT";
      } else if (upperType.startsWith("VARCHAR")) {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String len = start >= 0 && end > start ? upperType.substring(start + 1, end) : "255";
         return "VARCHAR(" + len + ")";
      } else if (upperType.startsWith("CHAR")) {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String len = start >= 0 && end > start ? upperType.substring(start + 1, end) : "";
         return len.isEmpty() ? "CHAR" : "CHAR(" + len + ")";
      } else if (upperType.startsWith("TEXT")) {
         return "TEXT";
      } else if (upperType.startsWith("BLOB")) {
         return "BLOB";
      } else if (!upperType.startsWith("NUMERIC")) {
         if (upperType.startsWith("FLOAT")) {
            return "FLOAT(16)";
         } else if (!upperType.startsWith("REAL") && !upperType.startsWith("DOUBLE")) {
            this.plugin.getLogger().warning("Unhandled SQLite type: " + sqliteType + " for column: " + columnName);
            return null;
         } else {
            return "DOUBLE";
         }
      } else {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String spec = start >= 0 && end > start ? upperType.substring(start, end + 1) : "(10,5)";
         return "DECIMAL" + spec;
      }
   }

   private String mapSQLiteTypeToPostgreSQL(String sqliteType, String columnName) {
      String upperType = sqliteType.toUpperCase(Locale.ROOT).trim();
      if (upperType.startsWith("INT")) {
         if ("protection".equals(columnName)) {
            return "BOOLEAN";
         } else {
            return !"time_protection".equals(columnName) && !"time_creation".equals(columnName) && !"time_alive".equals(columnName) ? "INTEGER" : "BIGINT";
         }
      } else if (upperType.startsWith("BIGINT")) {
         return "BIGINT";
      } else if (upperType.startsWith("VARCHAR")) {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String len = start >= 0 && end > start ? upperType.substring(start + 1, end) : "255";
         return "VARCHAR(" + len + ")";
      } else if (upperType.startsWith("CHAR")) {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String len = start >= 0 && end > start ? upperType.substring(start + 1, end) : "";
         return len.isEmpty() ? "CHAR" : "CHAR(" + len + ")";
      } else if (upperType.startsWith("TEXT")) {
         return "TEXT";
      } else if (upperType.startsWith("BLOB")) {
         return "BYTEA";
      } else if (!upperType.startsWith("NUMERIC")) {
         if (upperType.startsWith("FLOAT")) {
            return "REAL";
         } else if (!upperType.startsWith("DOUBLE") && !upperType.startsWith("REAL")) {
            this.plugin.getLogger().warning("Unhandled SQLite type: " + sqliteType + " for column: " + columnName);
            return null;
         } else {
            return "DOUBLE PRECISION";
         }
      } else {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String spec = start >= 0 && end > start ? upperType.substring(start, end + 1) : "(10,5)";
         return "NUMERIC" + spec;
      }
   }

   private String mapSQLiteTypeToH2(String sqliteType, String columnName) {
      String upperType = sqliteType.toUpperCase(Locale.ROOT).trim();
      if (upperType.startsWith("INT")) {
         if ("protection".equals(columnName)) {
            return "BOOLEAN";
         } else {
            return !"time_protection".equals(columnName) && !"time_creation".equals(columnName) && !"time_alive".equals(columnName) ? "INTEGER" : "BIGINT";
         }
      } else if (upperType.startsWith("BIGINT")) {
         return "BIGINT";
      } else if (upperType.startsWith("VARCHAR")) {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String len = start >= 0 && end > start ? upperType.substring(start + 1, end) : "255";
         return "VARCHAR(" + len + ")";
      } else if (upperType.startsWith("CHAR")) {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String len = start >= 0 && end > start ? upperType.substring(start + 1, end) : "";
         return len.isEmpty() ? "CHAR" : "CHAR(" + len + ")";
      } else if (upperType.startsWith("TEXT")) {
         return "TEXT";
      } else if (upperType.startsWith("BLOB")) {
         return "BLOB";
      } else if (!upperType.startsWith("NUMERIC")) {
         if (!upperType.startsWith("REAL") && !upperType.startsWith("FLOAT") && !upperType.startsWith("DOUBLE")) {
            this.plugin.getLogger().warning("Unhandled SQLite type: " + sqliteType + " for column: " + columnName);
            return null;
         } else {
            return "DOUBLE";
         }
      } else {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String spec = start >= 0 && end > start ? upperType.substring(start, end + 1) : "(10,5)";
         return "NUMERIC" + spec;
      }
   }

   private String mapSQLiteTypeToMSSQL(String sqliteType, String columnName) {
      String upperType = sqliteType.toUpperCase(Locale.ROOT).trim();
      if (upperType.startsWith("INT")) {
         if ("protection".equals(columnName)) {
            return "BIT";
         } else {
            return !"time_protection".equals(columnName) && !"time_creation".equals(columnName) && !"time_alive".equals(columnName) ? "INT" : "BIGINT";
         }
      } else if (upperType.startsWith("BIGINT")) {
         return "BIGINT";
      } else if (upperType.startsWith("VARCHAR")) {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String len = start >= 0 && end > start ? upperType.substring(start + 1, end) : "255";
         return "NVARCHAR(" + len + ")";
      } else if (upperType.startsWith("CHAR")) {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String len = start >= 0 && end > start ? upperType.substring(start + 1, end) : "";
         return len.isEmpty() ? "NCHAR" : "NCHAR(" + len + ")";
      } else if (upperType.startsWith("TEXT")) {
         return "NVARCHAR(MAX)";
      } else if (upperType.startsWith("BLOB")) {
         return "VARBINARY(MAX)";
      } else if (!upperType.startsWith("NUMERIC")) {
         if (!upperType.startsWith("REAL") && !upperType.startsWith("FLOAT") && !upperType.startsWith("DOUBLE")) {
            this.plugin.getLogger().warning("Unhandled SQLite type: " + sqliteType + " for column: " + columnName);
            return null;
         } else {
            return "FLOAT";
         }
      } else {
         int start = upperType.indexOf(40);
         int end = upperType.indexOf(41);
         String spec = start >= 0 && end > start ? upperType.substring(start, end + 1) : "(10,5)";
         return "DECIMAL" + spec;
      }
   }

   private void adjustGraveTableForTargetDB() throws SQLException {
      String table = this.getStoragePrefix() + "grave";
      switch (this.type) {
         case MYSQL:
         case MARIADB:
            this.plugin.getLogger().info("Altering table " + table + " to ensure column sizes are correct for MySQL/MariaDB.");
            this.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN owner_texture LONGTEXT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN owner_texture_signature LONGTEXT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN inventory LONGTEXT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN equipment LONGTEXT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN time_creation BIGINT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN time_protection BIGINT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN time_alive BIGINT", new Object[0]);
            break;
         case POSTGRESQL:
         case H2:
            this.plugin.getLogger().info("Altering table " + table + " to ensure column sizes are correct for PostgreSQL/H2.");
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN owner_texture TYPE TEXT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN owner_texture_signature TYPE TEXT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN inventory TYPE TEXT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN equipment TYPE TEXT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN time_creation TYPE BIGINT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN time_protection TYPE BIGINT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN time_alive TYPE BIGINT", new Object[0]);
            break;
         case MSSQL:
            this.plugin.getLogger().info("Altering table " + table + " to ensure column sizes are correct for MSSQL.");
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN owner_texture NVARCHAR(MAX)", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN owner_texture_signature NVARCHAR(MAX)", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN inventory NVARCHAR(MAX)", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN equipment NVARCHAR(MAX)", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN time_creation BIGINT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN time_protection BIGINT", new Object[0]);
            this.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN time_alive BIGINT", new Object[0]);
      }
   }

   private void keepConnectionAlive() {
      this.plugin.getSchedulerManager().runTaskTimerAsynchronously(() -> {
         if (this.isConnected()) {
            this.checkAndUnlockDatabase();

            try (
               Connection connection = this.getConnection();
               PreparedStatement statement = connection != null ? connection.prepareStatement("SELECT 1") : null;
            ) {
               if (statement != null) {
                  statement.execute();
               } else {
                  this.plugin.getLogger().severe("Keep-alive skipped: connection is null");
               }
            } catch (SQLException var9) {
               this.plugin.getLogger().severe("Failed to keep connection alive.");
               this.plugin.logStackTrace((Exception)var9);
            }
         }
      }, 0L, 500L);
   }

   private void checkAndUnlockDatabase() {
      String checkQuery = "SELECT 1";

      try {
         try (
            Connection connection = this.getConnection();
            Statement statement = connection != null ? connection.createStatement() : null;
         ) {
            if (statement != null) {
               statement.execute(checkQuery);
               return;
            }

            this.plugin.getLogger().severe("Database check failed: connection is null.");
         }

         return;
      } catch (SQLException var10) {
         String msg = var10.getMessage();
         if (msg != null && msg.toLowerCase(Locale.ROOT).contains("database is locked")) {
            this.plugin.getLogger().severe("Database is locked. Attempting to unlock...");
            switch (this.type) {
               case SQLITE:
                  this.handleUnlockSQLite();
                  break;
               case MYSQL:
               case MARIADB:
                  this.handleUnlockMySQL();
                  break;
               case POSTGRESQL:
               case H2:
                  this.handleUnlockPostgreSQLandH2();
                  break;
               case MSSQL:
                  this.handleUnlockMSSQL();
                  break;
               case INVALID:
                  this.plugin.getLogger().warning("Server is using an invalid database type. No attempts to fix database locking will be attempted.");
                  break;
               default:
                  this.plugin.getLogger().warning("Unhandled database type. No attempts to fix database locking will be attempted.");
            }
         }
      }
   }

   private void handleUnlockSQLite() {
      try (Connection connection = this.dataSource.getConnection()) {
         if (connection == null) {
            throw new NullPointerException();
         }

         connection.setAutoCommit(false);
         connection.commit();
         this.plugin.getLogger().info("SQLite database unlocked successfully using COMMIT.");
      } catch (SQLException | NullPointerException var10) {
         this.plugin.getLogger().severe("Failed to unlock SQLite database using COMMIT");
         this.plugin.logStackTrace((Exception)var10);

         try (Connection connectionx = this.dataSource.getConnection()) {
            if (connectionx == null) {
               throw new NullPointerException();
            }

            connectionx.rollback();
            this.plugin.getLogger().info("SQLite database unlocked successfully using ROLLBACK.");
         } catch (SQLException | NullPointerException var9) {
            this.plugin.getLogger().severe("Failed to unlock SQLite database using ROLLBACK");
            this.plugin.logStackTrace((Exception)var9);
         }
      }
   }

   private void handleUnlockMySQL() {
      try {
         try (Connection connection = this.getConnection()) {
            if (connection != null) {
               this.plugin.getLogger().info("Attempting to unlock MySQL/MariaDB database.");
               connection.setAutoCommit(false);
               connection.commit();
               this.plugin.getLogger().info("MySQL/MariaDB database unlocked successfully using COMMIT.");
               return;
            }

            this.plugin.getLogger().severe("Failed to unlock MySQL/MariaDB database: connection is null");
         }
      } catch (SQLException var6) {
         this.plugin.getLogger().severe("Failed to unlock MySQL/MariaDB database using COMMIT");
         this.plugin.logStackTrace((Exception)var6);
      }
   }

   private void handleUnlockPostgreSQLandH2() {
      try {
         try (Connection connection = this.getConnection()) {
            if (connection == null) {
               this.plugin.getLogger().severe("Failed to unlock PostgreSQL/H2 database: connection is null");
               return;
            }

            connection.setAutoCommit(false);
            connection.commit();
            this.plugin.getLogger().info("PostgreSQL/H2 database unlocked successfully using COMMIT.");
         }
      } catch (SQLException var9) {
         this.plugin.getLogger().severe("Failed to unlock PostgreSQL/H2 database using COMMIT");
         this.plugin.logStackTrace((Exception)var9);

         try {
            try (Connection connection = this.getConnection()) {
               if (connection != null) {
                  connection.rollback();
                  this.plugin.getLogger().info("PostgreSQL/H2 database unlocked successfully using ROLLBACK.");
                  return;
               }

               this.plugin.getLogger().severe("Failed to unlock PostgreSQL/H2 database via ROLLBACK: connection is null");
            }
         } catch (SQLException var7) {
            this.plugin.getLogger().severe("Failed to unlock PostgreSQL/H2 database using ROLLBACK");
            this.plugin.logStackTrace((Exception)var7);
         }
      }
   }

   private void handleUnlockMSSQL() {
      try {
         try (Connection connection = this.getConnection()) {
            if (connection != null) {
               this.plugin.getLogger().info("Attempting to unlock MSSQL database.");
               connection.setAutoCommit(false);
               connection.commit();
               this.plugin.getLogger().info("MSSQL database unlocked successfully using COMMIT.");
               return;
            }

            this.plugin.getLogger().severe("Failed to unlock MSSQL database: connection is null");
         }
      } catch (SQLException var6) {
         this.plugin.getLogger().severe("Failed to unlock MSSQL database using COMMIT");
         this.plugin.logStackTrace((Exception)var6);
      }
   }

   @Experimental
   private String getStoragePrefix() {
      String prefix = this.plugin.getConfig().getString("settings.storage.prefix", "");
      return prefix.isBlank() ? "" : prefix.trim();
   }

   private void snapshotCustomProviders() {
      Set<String> idSet = new HashSet<>();
      List<String> keyList = new ArrayList<>();
      Set<String> seenKeys = new LinkedHashSet<>();

      for (GraveProvider p : RegisterGraveProviders.getAll()) {
         if (p != null) {
            String id = p.id();
            if (id != null && !id.isBlank()) {
               String sanitizedId = sanitizeKey(id);
               idSet.add(sanitizedId);
               String key = "custom_" + sanitizedId;
               if (seenKeys.add(key)) {
                  keyList.add(key);
               }
            }
         }
      }

      this.customProviderSanitizedIds = Set.copyOf(idSet);
      this.customProviderKeysSnapshot = List.copyOf(keyList);
   }

   private String mapH2TypeToMySQL(String h2Type, String columnName) {
      String normalized = h2Type.toUpperCase(Locale.ROOT);

      return switch (normalized) {
         case "CHARACTER VARYING", "VARCHAR", "VARCHAR_IGNORECASE", "TEXT", "CHARACTER LARGE OBJECT", "CLOB" -> !"uuid".equalsIgnoreCase(columnName)
               && !"owner_uuid".equalsIgnoreCase(columnName)
               && !"killer_uuid".equalsIgnoreCase(columnName)
               && !"entity_uuid".equalsIgnoreCase(columnName)
               && !"uuid_grave".equalsIgnoreCase(columnName)
               && !"uuid_entity".equalsIgnoreCase(columnName)
            ? "LONGTEXT"
            : "VARCHAR(36)";
         case "CHARACTER", "CHAR", "NCHAR" -> "CHAR(255)";
         case "BOOLEAN", "BIT" -> "BOOLEAN";
         case "TINYINT" -> "TINYINT";
         case "SMALLINT" -> "SMALLINT";
         case "INTEGER", "INT", "MEDIUMINT" -> "INT";
         case "BIGINT" -> "BIGINT";
         case "REAL" -> "FLOAT";
         case "DOUBLE", "DOUBLE PRECISION" -> "DOUBLE";
         case "NUMERIC", "DECIMAL", "DECFLOAT" -> "DECIMAL(65,30)";
         case "DATE" -> "DATE";
         case "TIME", "TIME WITHOUT TIME ZONE" -> "TIME";
         case "TIMESTAMP", "TIMESTAMP WITHOUT TIME ZONE", "TIMESTAMP WITH TIME ZONE" -> "DATETIME";
         case "BINARY", "VARBINARY", "BINARY VARYING", "BLOB", "BYTEA", "BINARY LARGE OBJECT" -> "LONGBLOB";
         case "JAVA_OBJECT", "JSON", "UUID", "ENUM", "GEOMETRY", "OTHER" -> "LONGTEXT";
         default -> {
            this.plugin.getLogger().warning("Unknown H2 type for MySQL/MariaDB migration: " + h2Type + " (column: " + columnName + ")");
            yield "LONGTEXT";
         }
      };
   }

   private String mapH2TypeToPostgreSQL(String h2Type, String columnName) {
      String normalized = h2Type.toUpperCase(Locale.ROOT);

      return switch (normalized) {
         case "CHARACTER VARYING", "VARCHAR", "VARCHAR_IGNORECASE", "TEXT", "CHARACTER LARGE OBJECT", "CLOB" -> !"uuid".equalsIgnoreCase(columnName)
               && !"owner_uuid".equalsIgnoreCase(columnName)
               && !"killer_uuid".equalsIgnoreCase(columnName)
               && !"entity_uuid".equalsIgnoreCase(columnName)
               && !"uuid_grave".equalsIgnoreCase(columnName)
               && !"uuid_entity".equalsIgnoreCase(columnName)
            ? "TEXT"
            : "VARCHAR(36)";
         case "CHARACTER", "CHAR", "NCHAR" -> "CHAR(255)";
         case "BOOLEAN", "BIT" -> "BOOLEAN";
         case "TINYINT", "SMALLINT" -> "SMALLINT";
         case "INTEGER", "INT", "MEDIUMINT" -> "INTEGER";
         case "BIGINT" -> "BIGINT";
         case "REAL" -> "REAL";
         case "DOUBLE", "DOUBLE PRECISION" -> "DOUBLE PRECISION";
         case "NUMERIC", "DECIMAL", "DECFLOAT" -> "NUMERIC";
         case "DATE" -> "DATE";
         case "TIME", "TIME WITHOUT TIME ZONE" -> "TIME";
         case "TIMESTAMP", "TIMESTAMP WITHOUT TIME ZONE" -> "TIMESTAMP";
         case "TIMESTAMP WITH TIME ZONE" -> "TIMESTAMPTZ";
         case "BINARY", "VARBINARY", "BINARY VARYING", "BLOB", "BINARY LARGE OBJECT", "BYTEA" -> "BYTEA";
         case "UUID" -> "UUID";
         case "JSON" -> "JSONB";
         case "JAVA_OBJECT", "ENUM", "GEOMETRY", "OTHER" -> "TEXT";
         default -> {
            this.plugin.getLogger().warning("Unknown H2 type for PostgreSQL migration: " + h2Type + " (column: " + columnName + ")");
            yield "TEXT";
         }
      };
   }

   private String mapH2TypeToMSSQL(String h2Type, String columnName) {
      String normalized = h2Type.toUpperCase(Locale.ROOT);

      return switch (normalized) {
         case "CHARACTER VARYING", "VARCHAR", "VARCHAR_IGNORECASE", "TEXT", "CHARACTER LARGE OBJECT", "CLOB" -> !"uuid".equalsIgnoreCase(columnName)
               && !"owner_uuid".equalsIgnoreCase(columnName)
               && !"killer_uuid".equalsIgnoreCase(columnName)
               && !"entity_uuid".equalsIgnoreCase(columnName)
               && !"uuid_grave".equalsIgnoreCase(columnName)
               && !"uuid_entity".equalsIgnoreCase(columnName)
            ? "NVARCHAR(MAX)"
            : "VARCHAR(36)";
         case "CHARACTER", "CHAR", "NCHAR" -> "NCHAR(255)";
         case "BOOLEAN", "BIT" -> "BIT";
         case "TINYINT" -> "TINYINT";
         case "SMALLINT" -> "SMALLINT";
         case "INTEGER", "INT", "MEDIUMINT" -> "INT";
         case "BIGINT" -> "BIGINT";
         case "REAL" -> "REAL";
         case "DOUBLE", "DOUBLE PRECISION" -> "FLOAT";
         case "NUMERIC", "DECIMAL", "DECFLOAT" -> "DECIMAL(38,18)";
         case "DATE" -> "DATE";
         case "TIME", "TIME WITHOUT TIME ZONE" -> "TIME";
         case "TIMESTAMP", "TIMESTAMP WITHOUT TIME ZONE", "TIMESTAMP WITH TIME ZONE" -> "DATETIME2";
         case "BINARY", "VARBINARY", "BINARY VARYING", "BLOB", "BINARY LARGE OBJECT", "BYTEA" -> "VARBINARY(MAX)";
         case "UUID" -> "UNIQUEIDENTIFIER";
         case "JSON", "JAVA_OBJECT", "ENUM", "GEOMETRY", "OTHER" -> "NVARCHAR(MAX)";
         default -> {
            this.plugin.getLogger().warning("Unknown H2 type for MSSQL migration: " + h2Type + " (column: " + columnName + ")");
            yield "NVARCHAR(MAX)";
         }
      };
   }

   private record MigrationSource(DataManager.Type type, Path path) {
   }

   public static enum Type {
      SQLITE,
      MYSQL,
      MARIADB,
      POSTGRESQL,
      H2,
      MSSQL,
      INVALID;
   }
}
