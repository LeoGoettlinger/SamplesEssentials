package github.nighter.smartspawner.spawner.data.database;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.libs.hikari.HikariConfig;
import github.nighter.smartspawner.libs.hikari.HikariDataSource;
import github.nighter.smartspawner.spawner.data.legacy.LegacyInventoryCodec;
import github.nighter.smartspawner.spawner.data.storage.SpawnerInventoryCodec;
import github.nighter.smartspawner.spawner.data.storage.StorageMode;
import github.nighter.smartspawner.spawner.properties.ItemSignature;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

public class DatabaseManager {
   public static final String DEFAULT_TABLE_PREFIX = "sspawner_";
   private static final long CONNECTION_TIMEOUT_MS = 10000L;
   private static final long MAX_LIFETIME_MS = 1800000L;
   private static final long IDLE_TIMEOUT_MS = 600000L;
   private static final long KEEPALIVE_TIME_MS = 30000L;
   private static final String SUFFIX_SPAWNERS = "data";
   private static final String SUFFIX_META = "schema_meta";
   private static final String SUFFIX_LEGACY_SHARED = "legacy_shared_data";
   private static final String LEGACY_TABLE_SPAWNERS = "smart_spawners";
   private static final String LEGACY_TABLE_META = "smartspawner_meta";
   private static final String[] LEGACY_INDEXES = new String[]{"idx_server", "idx_world", "idx_chunk"};
   private final SmartSpawner plugin;
   private final Logger logger;
   private final StorageMode storageMode;
   private HikariDataSource dataSource;
   private final String tablePrefix;
   private final String tableSpawners;
   private final String tableMeta;
   private final boolean crossServer;
   private final String tableSingleServer;
   private final String tableThisServer;
   private final String tableLegacyShared;
   private final String host;
   private final int port;
   private final String database;
   private final String username;
   private final String password;
   private final String serverName;
   private final String sqliteFile;
   private final int poolSize;
   private static final String CREATE_TABLE_MYSQL = "CREATE TABLE IF NOT EXISTS %1$s (\n    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n    spawner_id VARCHAR(64) NOT NULL,\n\n    -- Location (separate columns for indexing).\n    -- chunk_x/chunk_z are derived from loc_x/loc_z, indexed for per-chunk lookups\n    world VARCHAR(128) NOT NULL,\n    loc_x INT NOT NULL,\n    loc_y INT NOT NULL,\n    loc_z INT NOT NULL,\n    chunk_x INT NOT NULL DEFAULT 0,\n    chunk_z INT NOT NULL DEFAULT 0,\n\n    -- What the spawner spawns. itemspawner_type is only set when entity_type is ITEM\n    entity_type VARCHAR(64) NOT NULL,\n    itemspawner_type VARCHAR(64) DEFAULT NULL,\n    config_name VARCHAR(128) DEFAULT NULL,\n\n    -- Stacking\n    stack_size INT NOT NULL DEFAULT 1,\n    max_stack_size INT NOT NULL DEFAULT 1000,\n\n    -- Spawning behaviour\n    active BOOLEAN NOT NULL DEFAULT TRUE,\n    stop BOOLEAN NOT NULL DEFAULT TRUE,\n    activation_range INT NOT NULL DEFAULT 16,\n    delay BIGINT NOT NULL DEFAULT 500,\n    last_spawn_time BIGINT NOT NULL DEFAULT 0,\n    min_mobs INT NOT NULL DEFAULT 1,\n    max_mobs INT NOT NULL DEFAULT 4,\n\n    -- Stored loot. total_items is denormalized from storage_items below\n    max_loot_slots INT NOT NULL DEFAULT 45,\n    is_at_capacity BOOLEAN NOT NULL DEFAULT FALSE,\n    total_items BIGINT NOT NULL DEFAULT 0,\n\n    -- Stored experience\n    exp BIGINT NOT NULL DEFAULT 0,\n    max_stored_exp BIGINT NOT NULL DEFAULT 1000,\n\n    -- Player interaction\n    last_interacted_player VARCHAR(64) DEFAULT NULL,\n    preferred_sort_item VARCHAR(64) DEFAULT NULL,\n    filtered_items TEXT DEFAULT NULL,\n\n    -- Virtual inventory, see SpawnerInventoryCodec\n    storage_items MEDIUMBLOB DEFAULT NULL,\n\n    -- Indexes. The table is one server's data, so none of these carry a server name.\n    UNIQUE KEY uk_spawner (spawner_id),\n    UNIQUE KEY uk_location (world, loc_x, loc_y, loc_z),\n    INDEX %2$sidx_world (world),\n    INDEX %2$sidx_chunk (world, chunk_x, chunk_z)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4\n";
   private static final String CREATE_TABLE_SQLITE = "CREATE TABLE IF NOT EXISTS %1$s (\n    id INTEGER PRIMARY KEY AUTOINCREMENT,\n    spawner_id VARCHAR(64) NOT NULL,\n\n    -- Location (separate columns for indexing).\n    -- chunk_x/chunk_z are derived from loc_x/loc_z, indexed for per-chunk lookups\n    world VARCHAR(128) NOT NULL,\n    loc_x INT NOT NULL,\n    loc_y INT NOT NULL,\n    loc_z INT NOT NULL,\n    chunk_x INT NOT NULL DEFAULT 0,\n    chunk_z INT NOT NULL DEFAULT 0,\n\n    -- What the spawner spawns. itemspawner_type is only set when entity_type is ITEM\n    entity_type VARCHAR(64) NOT NULL,\n    itemspawner_type VARCHAR(64) DEFAULT NULL,\n    config_name VARCHAR(128) DEFAULT NULL,\n\n    -- Stacking\n    stack_size INT NOT NULL DEFAULT 1,\n    max_stack_size INT NOT NULL DEFAULT 1000,\n\n    -- Spawning behaviour\n    active BOOLEAN NOT NULL DEFAULT 1,\n    stop BOOLEAN NOT NULL DEFAULT 1,\n    activation_range INT NOT NULL DEFAULT 16,\n    delay BIGINT NOT NULL DEFAULT 500,\n    last_spawn_time BIGINT NOT NULL DEFAULT 0,\n    min_mobs INT NOT NULL DEFAULT 1,\n    max_mobs INT NOT NULL DEFAULT 4,\n\n    -- Stored loot. total_items is denormalized from storage_items below\n    max_loot_slots INT NOT NULL DEFAULT 45,\n    is_at_capacity BOOLEAN NOT NULL DEFAULT 0,\n    total_items BIGINT NOT NULL DEFAULT 0,\n\n    -- Stored experience\n    exp BIGINT NOT NULL DEFAULT 0,\n    max_stored_exp BIGINT NOT NULL DEFAULT 1000,\n\n    -- Player interaction\n    last_interacted_player VARCHAR(64) DEFAULT NULL,\n    preferred_sort_item VARCHAR(64) DEFAULT NULL,\n    filtered_items TEXT DEFAULT NULL,\n\n    -- Virtual inventory, see SpawnerInventoryCodec\n    storage_items BLOB DEFAULT NULL,\n\n    -- Unique constraints\n    UNIQUE (spawner_id),\n    UNIQUE (world, loc_x, loc_y, loc_z)\n)\n";
   private static final String CREATE_INDEX_WORLD_SQLITE = "CREATE INDEX IF NOT EXISTS %2$sidx_world ON %1$s (world)";
   private static final String CREATE_INDEX_CHUNK_SQLITE = "CREATE INDEX IF NOT EXISTS %2$sidx_chunk ON %1$s (world, chunk_x, chunk_z)";
   private static final String SCHEMA_VERSION_KEY = "schema_version";
   private static final int LEGACY_SCHEMA_VERSION = 1;
   private static final int CURRENT_SCHEMA_VERSION = 4;
   private static final int MIGRATION_BATCH_SIZE = 250;
   private static final String CREATE_META_TABLE_MYSQL = "CREATE TABLE IF NOT EXISTS %1$s (\n    meta_key VARCHAR(64) PRIMARY KEY,\n    meta_value VARCHAR(64) NOT NULL,\n    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4\n";
   private static final String CREATE_META_TABLE_SQLITE = "CREATE TABLE IF NOT EXISTS %1$s (\n    meta_key VARCHAR(64) PRIMARY KEY,\n    meta_value VARCHAR(64) NOT NULL,\n    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n)\n";
   private static final String REBUILD_COLUMNS = "id, spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\nentity_type, itemspawner_type, stack_size, max_stack_size,\nactive, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\nmax_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items, storage_items\n";
   static final String[][] COLUMN_RENAMES = new String[][]{
      {"world_name", "world"},
      {"item_spawner_material", "itemspawner_type"},
      {"spawner_exp", "exp"},
      {"spawner_active", "active"},
      {"spawner_range", "activation_range"},
      {"spawner_stop", "stop"},
      {"spawn_delay", "delay"},
      {"max_spawner_loot_slots", "max_loot_slots"}
   };

   public DatabaseManager(SmartSpawner plugin, StorageMode storageMode) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
      this.storageMode = storageMode;
      this.tablePrefix = sanitizeTablePrefix(plugin.getConfig().getString("database.table-prefix", "sspawner_"));
      this.tableMeta = this.tablePrefix + "schema_meta";
      this.host = plugin.getConfig().getString("database.host", "localhost");
      this.port = plugin.getConfig().getInt("database.port", 3306);
      this.database = plugin.getConfig().getString("database.database", "smartspawner");
      this.username = plugin.getConfig().getString("database.username", "root");
      this.password = plugin.getConfig().getString("database.password", "");
      this.serverName = plugin.getConfig().getString("database.server-name", "server1");
      this.sqliteFile = plugin.getConfig().getString("database.sqlite-file", "spawners.db");
      this.poolSize = Math.max(1, plugin.getConfig().getInt("database.pool-size", 10));
      this.crossServer = storageMode == StorageMode.MYSQL && plugin.getConfig().getBoolean("database.sync-across-servers", false);
      this.tableSingleServer = this.tablePrefix + "data";
      this.tableThisServer = this.tablePrefix + sanitizeIdentifier(this.serverName) + "_data";
      this.tableSpawners = this.crossServer ? this.tableThisServer : this.tableSingleServer;
      this.tableLegacyShared = this.tablePrefix + "legacy_shared_data";
   }

   static String sanitizeTablePrefix(String value) {
      String cleaned = sanitizeIdentifier(value);
      return cleaned.isEmpty() ? "sspawner_" : cleaned;
   }

   static String sanitizeIdentifier(String value) {
      return value == null ? "" : value.replaceAll("[^A-Za-z0-9_]", "");
   }

   private String sql(String template) {
      return String.format(template, this.tableSpawners, this.tablePrefix);
   }

   public boolean initialize() {
      try {
         this.setupDataSource();
         this.renameLegacyTables();
         this.adoptTableForCurrentMode();
         this.createTables();
         this.createSchemaMetaTable();
         this.runSchemaMigrations();
         this.logger.info("Database connection pool initialized successfully.");
         return true;
      } catch (Exception var2) {
         this.logger.log(Level.SEVERE, "Failed to initialize database connection pool", (Throwable)var2);
         return false;
      }
   }

   private void adoptTableForCurrentMode() throws SQLException {
      String other = this.crossServer ? this.tableSingleServer : this.tableThisServer;
      if (!other.equals(this.tableSpawners) && this.tableExists(other)) {
         if (this.tableExists(this.tableSpawners)) {
            this.logger
               .warning(
                  "Both "
                     + other
                     + " and "
                     + this.tableSpawners
                     + " exist. Using "
                     + this.tableSpawners
                     + " and leaving "
                     + other
                     + " alone. Delete whichever one is stale."
               );
         } else {
            try (
               Connection conn = this.getConnection();
               Statement stmt = conn.createStatement();
            ) {
               stmt.execute("ALTER TABLE " + other + " RENAME TO " + this.tableSpawners);
            }

            this.logger.info("Renamed database table " + other + " to " + this.tableSpawners + " to match database.sync-across-servers.");
         }
      }
   }

   private void setupDataSource() {
      HikariConfig config = new HikariConfig();
      if (this.storageMode == StorageMode.SQLITE) {
         this.setupSQLiteDataSource(config);
      } else {
         this.setupMySQLDataSource(config);
      }

      this.dataSource = new HikariDataSource(config);
   }

   private void setupMySQLDataSource(HikariConfig config) {
      String jdbcUrl = String.format(
         "jdbc:mariadb://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", this.host, this.port, this.database
      );
      config.setJdbcUrl(jdbcUrl);
      config.setDriverClassName("github.nighter.smartspawner.libs.mariadb.Driver");
      config.setUsername(this.username);
      config.setPassword(this.password);
      config.setMaximumPoolSize(this.poolSize);
      config.setMinimumIdle(Math.min(2, this.poolSize));
      config.setConnectionTimeout(10000L);
      config.setMaxLifetime(1800000L);
      config.setIdleTimeout(600000L);
      config.setKeepaliveTime(30000L);
      config.setPoolName("SmartSpawner-HikariCP");
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      config.addDataSourceProperty("useServerPrepStmts", "true");
      config.addDataSourceProperty("useLocalSessionState", "true");
      config.addDataSourceProperty("rewriteBatchedStatements", "true");
      config.addDataSourceProperty("cacheResultSetMetadata", "true");
      config.addDataSourceProperty("cacheServerConfiguration", "true");
      config.addDataSourceProperty("elideSetAutoCommits", "true");
      config.addDataSourceProperty("maintainTimeStats", "false");
   }

   private void setupSQLiteDataSource(HikariConfig config) {
      File dataFolder = this.plugin.getDataFolder();
      if (!dataFolder.exists()) {
         dataFolder.mkdirs();
      }

      File dbFile = new File(dataFolder, this.sqliteFile);
      String jdbcUrl = "jdbc:sqlite:"
         + dbFile.getAbsolutePath()
         + "?journal_mode=WAL&synchronous=NORMAL&busy_timeout=5000&foreign_keys=true&cache_size=-16000&temp_store=MEMORY";
      config.setJdbcUrl(jdbcUrl);
      config.setDriverClassName("org.sqlite.JDBC");
      config.setMaximumPoolSize(this.poolSize);
      config.setMinimumIdle(1);
      config.setConnectionTimeout(10000L);
      config.setMaxLifetime(0L);
      config.setIdleTimeout(0L);
      config.setPoolName("SmartSpawner-SQLite-HikariCP");
   }

   private void renameLegacyTables() throws SQLException {
      this.renameTableIfNeeded("smartspawner_meta", this.tableMeta);
      if (this.renameTableIfNeeded("smart_spawners", this.tableSpawners)) {
         this.dropLegacyIndexes();
      }
   }

   private boolean renameTableIfNeeded(String from, String to) throws SQLException {
      if (this.tableExists(from) && !this.tableExists(to)) {
         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            stmt.execute("ALTER TABLE " + from + " RENAME TO " + to);
         }

         this.logger.info("Renamed database table " + from + " to " + to + ".");
         return true;
      } else {
         return false;
      }
   }

   private void dropLegacyIndexes() {
      if (this.storageMode == StorageMode.SQLITE) {
         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            for (String index : LEGACY_INDEXES) {
               stmt.execute("DROP INDEX IF EXISTS " + index);
            }
         } catch (SQLException var11) {
            this.logger.warning("Could not drop the pre-prefix indexes, leaving them in place: " + var11.getMessage());
         }
      }
   }

   private boolean tableExists(String tableName) throws SQLException {
      try (Connection conn = this.getConnection()) {
         DatabaseMetaData meta = conn.getMetaData();

         try (ResultSet rs = meta.getTables(conn.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            while (rs.next()) {
               if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean columnExists(String tableName, String columnName) throws SQLException {
      try (Connection conn = this.getConnection()) {
         DatabaseMetaData meta = conn.getMetaData();

         try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, tableName, null)) {
            while (rs.next()) {
               if (columnName.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void createTables() throws SQLException {
      try (
         Connection conn = this.getConnection();
         Statement stmt = conn.createStatement();
      ) {
         if (this.storageMode == StorageMode.SQLITE) {
            stmt.execute(
               this.sql(
                  "CREATE TABLE IF NOT EXISTS %1$s (\n    id INTEGER PRIMARY KEY AUTOINCREMENT,\n    spawner_id VARCHAR(64) NOT NULL,\n\n    -- Location (separate columns for indexing).\n    -- chunk_x/chunk_z are derived from loc_x/loc_z, indexed for per-chunk lookups\n    world VARCHAR(128) NOT NULL,\n    loc_x INT NOT NULL,\n    loc_y INT NOT NULL,\n    loc_z INT NOT NULL,\n    chunk_x INT NOT NULL DEFAULT 0,\n    chunk_z INT NOT NULL DEFAULT 0,\n\n    -- What the spawner spawns. itemspawner_type is only set when entity_type is ITEM\n    entity_type VARCHAR(64) NOT NULL,\n    itemspawner_type VARCHAR(64) DEFAULT NULL,\n    config_name VARCHAR(128) DEFAULT NULL,\n\n    -- Stacking\n    stack_size INT NOT NULL DEFAULT 1,\n    max_stack_size INT NOT NULL DEFAULT 1000,\n\n    -- Spawning behaviour\n    active BOOLEAN NOT NULL DEFAULT 1,\n    stop BOOLEAN NOT NULL DEFAULT 1,\n    activation_range INT NOT NULL DEFAULT 16,\n    delay BIGINT NOT NULL DEFAULT 500,\n    last_spawn_time BIGINT NOT NULL DEFAULT 0,\n    min_mobs INT NOT NULL DEFAULT 1,\n    max_mobs INT NOT NULL DEFAULT 4,\n\n    -- Stored loot. total_items is denormalized from storage_items below\n    max_loot_slots INT NOT NULL DEFAULT 45,\n    is_at_capacity BOOLEAN NOT NULL DEFAULT 0,\n    total_items BIGINT NOT NULL DEFAULT 0,\n\n    -- Stored experience\n    exp BIGINT NOT NULL DEFAULT 0,\n    max_stored_exp BIGINT NOT NULL DEFAULT 1000,\n\n    -- Player interaction\n    last_interacted_player VARCHAR(64) DEFAULT NULL,\n    preferred_sort_item VARCHAR(64) DEFAULT NULL,\n    filtered_items TEXT DEFAULT NULL,\n\n    -- Virtual inventory, see SpawnerInventoryCodec\n    storage_items BLOB DEFAULT NULL,\n\n    -- Unique constraints\n    UNIQUE (spawner_id),\n    UNIQUE (world, loc_x, loc_y, loc_z)\n)\n"
               )
            );
         } else {
            stmt.execute(
               this.sql(
                  "CREATE TABLE IF NOT EXISTS %1$s (\n    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n    spawner_id VARCHAR(64) NOT NULL,\n\n    -- Location (separate columns for indexing).\n    -- chunk_x/chunk_z are derived from loc_x/loc_z, indexed for per-chunk lookups\n    world VARCHAR(128) NOT NULL,\n    loc_x INT NOT NULL,\n    loc_y INT NOT NULL,\n    loc_z INT NOT NULL,\n    chunk_x INT NOT NULL DEFAULT 0,\n    chunk_z INT NOT NULL DEFAULT 0,\n\n    -- What the spawner spawns. itemspawner_type is only set when entity_type is ITEM\n    entity_type VARCHAR(64) NOT NULL,\n    itemspawner_type VARCHAR(64) DEFAULT NULL,\n    config_name VARCHAR(128) DEFAULT NULL,\n\n    -- Stacking\n    stack_size INT NOT NULL DEFAULT 1,\n    max_stack_size INT NOT NULL DEFAULT 1000,\n\n    -- Spawning behaviour\n    active BOOLEAN NOT NULL DEFAULT TRUE,\n    stop BOOLEAN NOT NULL DEFAULT TRUE,\n    activation_range INT NOT NULL DEFAULT 16,\n    delay BIGINT NOT NULL DEFAULT 500,\n    last_spawn_time BIGINT NOT NULL DEFAULT 0,\n    min_mobs INT NOT NULL DEFAULT 1,\n    max_mobs INT NOT NULL DEFAULT 4,\n\n    -- Stored loot. total_items is denormalized from storage_items below\n    max_loot_slots INT NOT NULL DEFAULT 45,\n    is_at_capacity BOOLEAN NOT NULL DEFAULT FALSE,\n    total_items BIGINT NOT NULL DEFAULT 0,\n\n    -- Stored experience\n    exp BIGINT NOT NULL DEFAULT 0,\n    max_stored_exp BIGINT NOT NULL DEFAULT 1000,\n\n    -- Player interaction\n    last_interacted_player VARCHAR(64) DEFAULT NULL,\n    preferred_sort_item VARCHAR(64) DEFAULT NULL,\n    filtered_items TEXT DEFAULT NULL,\n\n    -- Virtual inventory, see SpawnerInventoryCodec\n    storage_items MEDIUMBLOB DEFAULT NULL,\n\n    -- Indexes. The table is one server's data, so none of these carry a server name.\n    UNIQUE KEY uk_spawner (spawner_id),\n    UNIQUE KEY uk_location (world, loc_x, loc_y, loc_z),\n    INDEX %2$sidx_world (world),\n    INDEX %2$sidx_chunk (world, chunk_x, chunk_z)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4\n"
               )
            );
         }
      }
   }

   private void createSqliteIndexes() throws SQLException {
      if (this.storageMode == StorageMode.SQLITE) {
         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            stmt.execute(this.sql("CREATE INDEX IF NOT EXISTS %2$sidx_world ON %1$s (world)"));
            stmt.execute(this.sql("CREATE INDEX IF NOT EXISTS %2$sidx_chunk ON %1$s (world, chunk_x, chunk_z)"));
         }
      }
   }

   private void createSchemaMetaTable() throws SQLException {
      String template = this.storageMode == StorageMode.SQLITE
         ? "CREATE TABLE IF NOT EXISTS %1$s (\n    meta_key VARCHAR(64) PRIMARY KEY,\n    meta_value VARCHAR(64) NOT NULL,\n    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n)\n"
         : "CREATE TABLE IF NOT EXISTS %1$s (\n    meta_key VARCHAR(64) PRIMARY KEY,\n    meta_value VARCHAR(64) NOT NULL,\n    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4\n";

      try (
         Connection conn = this.getConnection();
         Statement stmt = conn.createStatement();
      ) {
         stmt.execute(String.format(template, this.tableMeta));
      }
   }

   private void runSchemaMigrations() throws SQLException {
      Integer currentVersion = this.getSchemaVersionFromMeta();
      if (currentVersion == null) {
         currentVersion = this.detectInitialSchemaVersion();
         this.setSchemaVersion(currentVersion);
         this.logger.info("Initialized database schema version at v" + currentVersion + ".");
      }

      if (currentVersion > 4) {
         this.logger.warning("Database schema version v" + currentVersion + " is newer than plugin-supported v4.");
      } else {
         while (currentVersion < 4) {
            int targetVersion = currentVersion + 1;
            this.logger.info("Applying database schema migration v" + currentVersion + " -> v" + targetVersion + "...");
            this.applyMigrationStep(targetVersion);
            this.setSchemaVersion(targetVersion);
            currentVersion = targetVersion;
            this.logger.info("Database schema migration completed to v" + currentVersion + ".");
         }

         this.dropServerNameColumn();
         this.importFromLegacySharedTable();
         this.createSqliteIndexes();
      }
   }

   private void dropServerNameColumn() throws SQLException {
      if (this.columnExists(this.tableSpawners, "server_name")) {
         String rebuilt = this.tableSpawners + "_rebuild";
         String columns = "id, spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\nentity_type, itemspawner_type, stack_size, max_stack_size,\nactive, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\nmax_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items, storage_items\n"
            .replace("\n", " ")
            .trim();

         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            stmt.execute("DROP TABLE IF EXISTS " + rebuilt);
         }

         String template = this.storageMode == StorageMode.SQLITE
            ? "CREATE TABLE IF NOT EXISTS %1$s (\n    id INTEGER PRIMARY KEY AUTOINCREMENT,\n    spawner_id VARCHAR(64) NOT NULL,\n\n    -- Location (separate columns for indexing).\n    -- chunk_x/chunk_z are derived from loc_x/loc_z, indexed for per-chunk lookups\n    world VARCHAR(128) NOT NULL,\n    loc_x INT NOT NULL,\n    loc_y INT NOT NULL,\n    loc_z INT NOT NULL,\n    chunk_x INT NOT NULL DEFAULT 0,\n    chunk_z INT NOT NULL DEFAULT 0,\n\n    -- What the spawner spawns. itemspawner_type is only set when entity_type is ITEM\n    entity_type VARCHAR(64) NOT NULL,\n    itemspawner_type VARCHAR(64) DEFAULT NULL,\n    config_name VARCHAR(128) DEFAULT NULL,\n\n    -- Stacking\n    stack_size INT NOT NULL DEFAULT 1,\n    max_stack_size INT NOT NULL DEFAULT 1000,\n\n    -- Spawning behaviour\n    active BOOLEAN NOT NULL DEFAULT 1,\n    stop BOOLEAN NOT NULL DEFAULT 1,\n    activation_range INT NOT NULL DEFAULT 16,\n    delay BIGINT NOT NULL DEFAULT 500,\n    last_spawn_time BIGINT NOT NULL DEFAULT 0,\n    min_mobs INT NOT NULL DEFAULT 1,\n    max_mobs INT NOT NULL DEFAULT 4,\n\n    -- Stored loot. total_items is denormalized from storage_items below\n    max_loot_slots INT NOT NULL DEFAULT 45,\n    is_at_capacity BOOLEAN NOT NULL DEFAULT 0,\n    total_items BIGINT NOT NULL DEFAULT 0,\n\n    -- Stored experience\n    exp BIGINT NOT NULL DEFAULT 0,\n    max_stored_exp BIGINT NOT NULL DEFAULT 1000,\n\n    -- Player interaction\n    last_interacted_player VARCHAR(64) DEFAULT NULL,\n    preferred_sort_item VARCHAR(64) DEFAULT NULL,\n    filtered_items TEXT DEFAULT NULL,\n\n    -- Virtual inventory, see SpawnerInventoryCodec\n    storage_items BLOB DEFAULT NULL,\n\n    -- Unique constraints\n    UNIQUE (spawner_id),\n    UNIQUE (world, loc_x, loc_y, loc_z)\n)\n"
            : "CREATE TABLE IF NOT EXISTS %1$s (\n    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n    spawner_id VARCHAR(64) NOT NULL,\n\n    -- Location (separate columns for indexing).\n    -- chunk_x/chunk_z are derived from loc_x/loc_z, indexed for per-chunk lookups\n    world VARCHAR(128) NOT NULL,\n    loc_x INT NOT NULL,\n    loc_y INT NOT NULL,\n    loc_z INT NOT NULL,\n    chunk_x INT NOT NULL DEFAULT 0,\n    chunk_z INT NOT NULL DEFAULT 0,\n\n    -- What the spawner spawns. itemspawner_type is only set when entity_type is ITEM\n    entity_type VARCHAR(64) NOT NULL,\n    itemspawner_type VARCHAR(64) DEFAULT NULL,\n    config_name VARCHAR(128) DEFAULT NULL,\n\n    -- Stacking\n    stack_size INT NOT NULL DEFAULT 1,\n    max_stack_size INT NOT NULL DEFAULT 1000,\n\n    -- Spawning behaviour\n    active BOOLEAN NOT NULL DEFAULT TRUE,\n    stop BOOLEAN NOT NULL DEFAULT TRUE,\n    activation_range INT NOT NULL DEFAULT 16,\n    delay BIGINT NOT NULL DEFAULT 500,\n    last_spawn_time BIGINT NOT NULL DEFAULT 0,\n    min_mobs INT NOT NULL DEFAULT 1,\n    max_mobs INT NOT NULL DEFAULT 4,\n\n    -- Stored loot. total_items is denormalized from storage_items below\n    max_loot_slots INT NOT NULL DEFAULT 45,\n    is_at_capacity BOOLEAN NOT NULL DEFAULT FALSE,\n    total_items BIGINT NOT NULL DEFAULT 0,\n\n    -- Stored experience\n    exp BIGINT NOT NULL DEFAULT 0,\n    max_stored_exp BIGINT NOT NULL DEFAULT 1000,\n\n    -- Player interaction\n    last_interacted_player VARCHAR(64) DEFAULT NULL,\n    preferred_sort_item VARCHAR(64) DEFAULT NULL,\n    filtered_items TEXT DEFAULT NULL,\n\n    -- Virtual inventory, see SpawnerInventoryCodec\n    storage_items MEDIUMBLOB DEFAULT NULL,\n\n    -- Indexes. The table is one server's data, so none of these carry a server name.\n    UNIQUE KEY uk_spawner (spawner_id),\n    UNIQUE KEY uk_location (world, loc_x, loc_y, loc_z),\n    INDEX %2$sidx_world (world),\n    INDEX %2$sidx_chunk (world, chunk_x, chunk_z)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4\n";

         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            stmt.execute(String.format(template, rebuilt, this.tablePrefix + "rebuild_"));
         }

         long foreign;
         long copied;
         try (Connection conn = this.getConnection()) {
            try (PreparedStatement insert = conn.prepareStatement(
                  "INSERT INTO " + rebuilt + " (" + columns + ") SELECT " + columns + " FROM " + this.tableSpawners + " WHERE server_name = ?"
               )) {
               insert.setString(1, this.serverName);
               copied = insert.executeUpdate();
            }

            try (PreparedStatement count = conn.prepareStatement("SELECT COUNT(*) FROM " + this.tableSpawners + " WHERE server_name <> ?")) {
               count.setString(1, this.serverName);

               try (ResultSet rs = count.executeQuery()) {
                  foreign = rs.next() ? rs.getLong(1) : 0L;
               }
            }
         }

         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            if (foreign > 0L) {
               stmt.execute("DROP TABLE IF EXISTS " + this.tableLegacyShared);
               stmt.execute("ALTER TABLE " + this.tableSpawners + " RENAME TO " + this.tableLegacyShared);
               this.logger
                  .warning(
                     "Kept "
                        + foreign
                        + " spawners belonging to other servers in "
                        + this.tableLegacyShared
                        + ". Each of those servers claims its own when it next starts on this version, and the table is removed once it is empty."
                  );
            } else {
               stmt.execute("DROP TABLE " + this.tableSpawners);
            }

            stmt.execute("ALTER TABLE " + rebuilt + " RENAME TO " + this.tableSpawners);
         }

         this.logger.info("Removed the redundant server_name column, keeping " + copied + " spawners.");
      }
   }

   private void importFromLegacySharedTable() throws SQLException {
      if (!this.tableLegacyShared.equals(this.tableSpawners) && this.tableExists(this.tableLegacyShared)) {
         String columns = "id, spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\nentity_type, itemspawner_type, stack_size, max_stack_size,\nactive, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\nmax_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items, storage_items\n"
            .replace("\n", " ")
            .trim();

         long claimed;
         try (Connection conn = this.getConnection()) {
            try (PreparedStatement insert = conn.prepareStatement(
                  "INSERT INTO " + this.tableSpawners + " (" + columns + ") SELECT " + columns + " FROM " + this.tableLegacyShared + " WHERE server_name = ?"
               )) {
               insert.setString(1, this.serverName);
               claimed = insert.executeUpdate();
            }

            if (claimed > 0L) {
               try (PreparedStatement delete = conn.prepareStatement("DELETE FROM " + this.tableLegacyShared + " WHERE server_name = ?")) {
                  delete.setString(1, this.serverName);
                  delete.executeUpdate();
               }
            }
         }

         long remaining;
         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + this.tableLegacyShared);
         ) {
            remaining = rs.next() ? rs.getLong(1) : 0L;
         }

         if (remaining == 0L) {
            try (
               Connection conn = this.getConnection();
               Statement stmt = conn.createStatement();
            ) {
               stmt.execute("DROP TABLE " + this.tableLegacyShared);
            }
         }

         if (claimed > 0L) {
            this.logger
               .info(
                  "Claimed "
                     + claimed
                     + " spawners for "
                     + this.serverName
                     + " from "
                     + this.tableLegacyShared
                     + (remaining == 0L ? ", which is now removed." : ".")
               );
         }
      }
   }

   private Integer getSchemaVersionFromMeta() throws SQLException {
      String sql = "SELECT meta_value FROM " + this.tableMeta + " WHERE meta_key = ?";

      Integer ex;
      try (
         Connection conn = this.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
      ) {
         stmt.setString(1, "schema_version");

         try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
               return null;
            }

            String rawVersion = rs.getString("meta_value");

            try {
               ex = Integer.parseInt(rawVersion);
            } catch (NumberFormatException var10) {
               throw new SQLException("Invalid database schema version value: " + rawVersion, var10);
            }
         }
      }

      return ex;
   }

   private int detectInitialSchemaVersion() throws SQLException {
      if (this.columnExists(this.tableSpawners, "config_name")) {
         return 4;
      } else if (this.columnExists(this.tableSpawners, "storage_items")) {
         return 3;
      } else {
         return this.xpColumnsRequireMigration() ? 1 : 2;
      }
   }

   private void setSchemaVersion(int version) throws SQLException {
      String sql = this.storageMode == StorageMode.SQLITE
         ? "INSERT INTO " + this.tableMeta + " (meta_key, meta_value) VALUES (?, ?) ON CONFLICT(meta_key) DO UPDATE SET meta_value = excluded.meta_value"
         : "INSERT INTO " + this.tableMeta + " (meta_key, meta_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE meta_value = VALUES(meta_value)";

      try (
         Connection conn = this.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
      ) {
         stmt.setString(1, "schema_version");
         stmt.setString(2, String.valueOf(version));
         stmt.executeUpdate();
      }
   }

   private void applyMigrationStep(int targetVersion) throws SQLException {
      switch (targetVersion) {
         case 2:
            this.migrateXpColumnsToBigIntIfNeeded();
            break;
         case 3:
            this.migrateToChunkAndItemBlobColumns();
            break;
         case 4:
            this.addColumnIfMissing("config_name", "VARCHAR(128) DEFAULT NULL");
            break;
         default:
            throw new SQLException("No database migration handler found for schema version: " + targetVersion);
      }
   }

   private void migrateXpColumnsToBigIntIfNeeded() throws SQLException {
      if (this.xpColumnsRequireMigration()) {
         String backupName = this.createPreMigrationBackup("bigint");
         this.logger.info("Created database backup before XP BIGINT migration: " + backupName);
         if (this.storageMode == StorageMode.SQLITE) {
            this.migrateSQLiteXpColumnsToBigInt();
         } else {
            this.migrateMySqlXpColumnsToBigInt();
         }

         this.logger.info("Successfully migrated XP columns to BIGINT.");
      }
   }

   private boolean xpColumnsRequireMigration() throws SQLException {
      return this.storageMode == StorageMode.SQLITE ? this.sqliteXpColumnsRequireMigration() : this.mySqlXpColumnsRequireMigration();
   }

   private boolean mySqlXpColumnsRequireMigration() throws SQLException {
      String sql = "SELECT column_name, data_type\nFROM information_schema.columns\nWHERE table_schema = ?\n  AND table_name = ?\n  AND column_name IN ('spawner_exp', 'max_stored_exp')\n";
      boolean needsMigration = false;

      try (
         Connection conn = this.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
      ) {
         stmt.setString(1, this.database);
         stmt.setString(2, this.tableSpawners);

         try (ResultSet rs = stmt.executeQuery()) {
            int seen = 0;

            while (rs.next()) {
               seen++;
               String type = rs.getString("data_type");
               if (type == null || !"bigint".equalsIgnoreCase(type)) {
                  needsMigration = true;
               }
            }

            if (seen < 2) {
               needsMigration = true;
            }
         }
      }

      return needsMigration;
   }

   private boolean sqliteXpColumnsRequireMigration() throws SQLException {
      String sql = "PRAGMA table_info(" + this.tableSpawners + ")";
      boolean spawnerExpBigInt = false;
      boolean maxStoredExpBigInt = false;

      try (
         Connection conn = this.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql);
      ) {
         while (rs.next()) {
            String name = rs.getString("name");
            String type = rs.getString("type");
            boolean isBigInt = type != null && type.equalsIgnoreCase("BIGINT");
            if ("spawner_exp".equalsIgnoreCase(name)) {
               spawnerExpBigInt = isBigInt;
            } else if ("max_stored_exp".equalsIgnoreCase(name)) {
               maxStoredExpBigInt = isBigInt;
            }
         }
      }

      return !spawnerExpBigInt || !maxStoredExpBigInt;
   }

   private String createPreMigrationBackup(String label) throws SQLException {
      String backupTableName = this.tableSpawners + "_backup_" + label + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
      if (this.storageMode == StorageMode.SQLITE) {
         String backupSql = "CREATE TABLE " + backupTableName + " AS SELECT * FROM " + this.tableSpawners;

         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            stmt.execute(backupSql);
         }
      } else {
         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            stmt.execute("CREATE TABLE " + backupTableName + " LIKE " + this.tableSpawners);
            stmt.execute("INSERT INTO " + backupTableName + " SELECT * FROM " + this.tableSpawners);
         }
      }

      return backupTableName;
   }

   private void migrateMySqlXpColumnsToBigInt() throws SQLException {
      String alterSql = "ALTER TABLE %1$s\n    MODIFY COLUMN spawner_exp BIGINT NOT NULL DEFAULT 0,\n    MODIFY COLUMN max_stored_exp BIGINT NOT NULL DEFAULT 1000\n"
         .formatted(this.tableSpawners);

      try (
         Connection conn = this.getConnection();
         Statement stmt = conn.createStatement();
      ) {
         stmt.execute(alterSql);
      }
   }

   private void migrateSQLiteXpColumnsToBigInt() throws SQLException {
      String scratchTable = this.tableSpawners + "_pre_bigint";
      String[] migrationSql = new String[]{
         "BEGIN IMMEDIATE TRANSACTION",
         "ALTER TABLE " + this.tableSpawners + " RENAME TO " + scratchTable,
         "CREATE TABLE %1$s (\n    id INTEGER PRIMARY KEY AUTOINCREMENT,\n    spawner_id VARCHAR(64) NOT NULL,\n    server_name VARCHAR(64) NOT NULL,\n    world_name VARCHAR(128) NOT NULL,\n    loc_x INT NOT NULL,\n    loc_y INT NOT NULL,\n    loc_z INT NOT NULL,\n    entity_type VARCHAR(64) NOT NULL,\n    item_spawner_material VARCHAR(64) DEFAULT NULL,\n    spawner_exp BIGINT NOT NULL DEFAULT 0,\n    spawner_active BOOLEAN NOT NULL DEFAULT 1,\n    spawner_range INT NOT NULL DEFAULT 16,\n    spawner_stop BOOLEAN NOT NULL DEFAULT 1,\n    spawn_delay BIGINT NOT NULL DEFAULT 500,\n    max_spawner_loot_slots INT NOT NULL DEFAULT 45,\n    max_stored_exp BIGINT NOT NULL DEFAULT 1000,\n    min_mobs INT NOT NULL DEFAULT 1,\n    max_mobs INT NOT NULL DEFAULT 4,\n    stack_size INT NOT NULL DEFAULT 1,\n    max_stack_size INT NOT NULL DEFAULT 1000,\n    last_spawn_time BIGINT NOT NULL DEFAULT 0,\n    is_at_capacity BOOLEAN NOT NULL DEFAULT 0,\n    last_interacted_player VARCHAR(64) DEFAULT NULL,\n    preferred_sort_item VARCHAR(64) DEFAULT NULL,\n    filtered_items TEXT DEFAULT NULL,\n    inventory_data TEXT DEFAULT NULL,\n    UNIQUE (server_name, spawner_id),\n    UNIQUE (server_name, world_name, loc_x, loc_y, loc_z)\n)\n"
            .formatted(this.tableSpawners),
         "INSERT INTO %1$s (\n    id, spawner_id, server_name, world_name, loc_x, loc_y, loc_z,\n    entity_type, item_spawner_material, spawner_exp, spawner_active,\n    spawner_range, spawner_stop, spawn_delay, max_spawner_loot_slots,\n    max_stored_exp, min_mobs, max_mobs, stack_size, max_stack_size,\n    last_spawn_time, is_at_capacity, last_interacted_player,\n    preferred_sort_item, filtered_items, inventory_data\n)\nSELECT\n    id, spawner_id, server_name, world_name, loc_x, loc_y, loc_z,\n    entity_type, item_spawner_material, spawner_exp, spawner_active,\n    spawner_range, spawner_stop, spawn_delay, max_spawner_loot_slots,\n    max_stored_exp, min_mobs, max_mobs, stack_size, max_stack_size,\n    last_spawn_time, is_at_capacity, last_interacted_player,\n    preferred_sort_item, filtered_items, inventory_data\nFROM %2$s\n"
            .formatted(this.tableSpawners, scratchTable),
         "DROP TABLE " + scratchTable,
         this.sql("CREATE INDEX IF NOT EXISTS %2$sidx_world ON %1$s (world)"),
         "COMMIT"
      };

      try {
         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            for (String sql : migrationSql) {
               stmt.execute(sql);
            }
         }
      } catch (SQLException var18) {
         try (
            Connection rollbackConn = this.getConnection();
            Statement rollbackStmt = rollbackConn.createStatement();
         ) {
            rollbackStmt.execute("ROLLBACK");
         } catch (SQLException var15) {
            this.logger.log(Level.SEVERE, "Failed to rollback SQLite BIGINT migration", (Throwable)var15);
         }

         throw var18;
      }
   }

   private void migrateToChunkAndItemBlobColumns() throws SQLException {
      boolean hasLegacyInventory = this.columnExists(this.tableSpawners, "inventory_data");
      if (hasLegacyInventory && this.countRows() > 0L) {
         String backupName = this.createPreMigrationBackup("items");
         this.logger.info("Created database backup before inventory format migration: " + backupName);
      }

      this.renameLegacyColumns();
      this.addColumnIfMissing("chunk_x", "INT NOT NULL DEFAULT 0");
      this.addColumnIfMissing("chunk_z", "INT NOT NULL DEFAULT 0");
      this.addColumnIfMissing("storage_items", this.storageMode == StorageMode.SQLITE ? "BLOB DEFAULT NULL" : "MEDIUMBLOB DEFAULT NULL");
      this.addColumnIfMissing("total_items", "BIGINT NOT NULL DEFAULT 0");
      this.backfillChunkColumns();
      if (hasLegacyInventory) {
         int converted = this.convertLegacyInventories();
         this.logger.info("Converted " + converted + " spawner inventories to the binary item format.");
         this.dropLegacyInventoryColumn();
      }

      if (this.storageMode != StorageMode.SQLITE) {
         this.createMySqlChunkIndexIfMissing();
      }
   }

   private void renameLegacyColumns() throws SQLException {
      int renamed = 0;

      for (String[] rename : COLUMN_RENAMES) {
         if (this.columnExists(this.tableSpawners, rename[0]) && !this.columnExists(this.tableSpawners, rename[1])) {
            try (
               Connection conn = this.getConnection();
               Statement stmt = conn.createStatement();
            ) {
               stmt.execute("ALTER TABLE " + this.tableSpawners + " RENAME COLUMN " + rename[0] + " TO " + rename[1]);
            }

            renamed++;
         }
      }

      if (renamed > 0) {
         this.logger.info("Renamed " + renamed + " database columns to the 1.8.0 names.");
      }
   }

   private long countRows() throws SQLException {
      long var4;
      try (
         Connection conn = this.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + this.tableSpawners);
      ) {
         var4 = rs.next() ? rs.getLong(1) : 0L;
      }

      return var4;
   }

   private void addColumnIfMissing(String columnName, String definition) throws SQLException {
      if (!this.columnExists(this.tableSpawners, columnName)) {
         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            stmt.execute("ALTER TABLE " + this.tableSpawners + " ADD COLUMN " + columnName + " " + definition);
         }
      }
   }

   private void backfillChunkColumns() throws SQLException {
      try (Connection conn = this.getConnection()) {
         conn.setAutoCommit(false);

         try (
            PreparedStatement select = conn.prepareStatement("SELECT id, loc_x, loc_z FROM " + this.tableSpawners);
            PreparedStatement update = conn.prepareStatement("UPDATE " + this.tableSpawners + " SET chunk_x = ?, chunk_z = ? WHERE id = ?");
         ) {
            int pending = 0;

            try (ResultSet rs = select.executeQuery()) {
               while (rs.next()) {
                  update.setInt(1, rs.getInt("loc_x") >> 4);
                  update.setInt(2, rs.getInt("loc_z") >> 4);
                  update.setLong(3, rs.getLong("id"));
                  update.addBatch();
                  if (++pending >= 250) {
                     update.executeBatch();
                     pending = 0;
                  }
               }
            }

            if (pending > 0) {
               update.executeBatch();
            }
         }

         conn.commit();
      }
   }

   private int convertLegacyInventories() throws SQLException {
      int converted = 0;

      try (Connection conn = this.getConnection()) {
         conn.setAutoCommit(false);

         try (
            PreparedStatement select = conn.prepareStatement(
               "SELECT id, spawner_id, inventory_data FROM " + this.tableSpawners + " WHERE inventory_data IS NOT NULL AND inventory_data <> ''"
            );
            PreparedStatement update = conn.prepareStatement("UPDATE " + this.tableSpawners + " SET storage_items = ?, total_items = ? WHERE id = ?");
         ) {
            int pending = 0;

            try (ResultSet rs = select.executeQuery()) {
               while (rs.next()) {
                  long rowId = rs.getLong("id");
                  String spawnerId = rs.getString("spawner_id");
                  String legacy = rs.getString("inventory_data");

                  byte[] blob;
                  long total;
                  try {
                     Map<ItemStack, Long> items = LegacyInventoryCodec.deserialize(LegacyInventoryCodec.parseJsonArray(legacy));
                     blob = this.encodeLegacyItems(items);
                     total = 0L;

                     for (Long amount : items.values()) {
                        total += amount;
                     }
                  } catch (Exception var21) {
                     this.logger
                        .warning("Could not convert stored inventory for spawner " + spawnerId + ", it will be empty after migration: " + var21.getMessage());
                     blob = null;
                     total = 0L;
                  }

                  update.setBytes(1, blob);
                  update.setLong(2, total);
                  update.setLong(3, rowId);
                  update.addBatch();
                  converted++;
                  if (++pending >= 250) {
                     update.executeBatch();
                     pending = 0;
                  }
               }
            }

            if (pending > 0) {
               update.executeBatch();
            }
         }

         conn.commit();
      }

      return converted;
   }

   private byte[] encodeLegacyItems(Map<ItemStack, Long> items) throws Exception {
      if (items.isEmpty()) {
         return null;
      } else {
         Map<ItemSignature, Long> consolidated = new LinkedHashMap<>(Math.max(16, items.size() * 2));

         for (Entry<ItemStack, Long> entry : items.entrySet()) {
            consolidated.merge(new ItemSignature(entry.getKey()), entry.getValue(), Long::sum);
         }

         return SpawnerInventoryCodec.encode(consolidated);
      }
   }

   private void dropLegacyInventoryColumn() {
      try (
         Connection conn = this.getConnection();
         Statement stmt = conn.createStatement();
      ) {
         stmt.execute("ALTER TABLE " + this.tableSpawners + " DROP COLUMN inventory_data");
      } catch (SQLException var9) {
         this.logger.warning("Could not drop the legacy inventory_data column, leaving it in place: " + var9.getMessage());
      }
   }

   private void createMySqlChunkIndexIfMissing() throws SQLException {
      List<String> existingIndexes = new ArrayList<>();

      try (Connection conn = this.getConnection()) {
         DatabaseMetaData meta = conn.getMetaData();

         try (ResultSet rs = meta.getIndexInfo(conn.getCatalog(), null, this.tableSpawners, false, false)) {
            while (rs.next()) {
               String name = rs.getString("INDEX_NAME");
               if (name != null) {
                  existingIndexes.add(name.toLowerCase(Locale.ROOT));
               }
            }
         }
      }

      String indexName = this.tablePrefix + "idx_chunk";
      if (!existingIndexes.contains(indexName.toLowerCase(Locale.ROOT))) {
         try (
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
         ) {
            stmt.execute("CREATE INDEX " + indexName + " ON " + this.tableSpawners + " (world, chunk_x, chunk_z)");
         }
      }
   }

   public Connection getConnection() throws SQLException {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         return this.dataSource.getConnection();
      } else {
         throw new SQLException("Database connection pool is not initialized or has been closed");
      }
   }

   public String getServerName() {
      return this.serverName;
   }

   public String getTableSpawners() {
      return this.tableSpawners;
   }

   public String getTableMeta() {
      return this.tableMeta;
   }

   public String getTableSingleServer() {
      return this.tableSingleServer;
   }

   public boolean isCrossServer() {
      return this.crossServer;
   }

   public Map<String, String> listServerTables() throws SQLException {
      Map<String, String> tables = new LinkedHashMap<>();
      if (!this.crossServer) {
         tables.put(this.serverName, this.tableSpawners);
         return tables;
      } else {
         String suffix = "_data";

         try (Connection conn = this.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, this.tablePrefix + "%", new String[]{"TABLE"})) {
               while (rs.next()) {
                  String name = rs.getString("TABLE_NAME");
                  if (name != null && name.startsWith(this.tablePrefix) && name.endsWith(suffix)) {
                     String server = name.substring(this.tablePrefix.length(), name.length() - suffix.length());
                     if (!server.isEmpty()) {
                        tables.put(server, name);
                     }
                  }
               }
            }
         }

         tables.putIfAbsent(this.serverName, this.tableSpawners);
         return tables;
      }
   }

   public StorageMode getStorageMode() {
      return this.storageMode;
   }

   public boolean isActive() {
      return this.dataSource != null && !this.dataSource.isClosed();
   }

   public void shutdown() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
         this.logger.info("Database connection pool closed.");
      }
   }
}
