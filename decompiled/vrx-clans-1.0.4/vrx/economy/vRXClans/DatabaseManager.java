package vrx.economy.vRXClans;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import vrx.economy.vRXClans.libs.hikari.HikariConfig;
import vrx.economy.vRXClans.libs.hikari.HikariDataSource;

public class DatabaseManager {
   private final VRXClans plugin;
   private HikariDataSource dataSource;

   public DatabaseManager(VRXClans plugin) {
      this.plugin = plugin;
   }

   public void connect() throws SQLException {
      HikariConfig config = new HikariConfig();
      config.setPoolName("VRXClansPool");
      config.setMaximumPoolSize(this.plugin.getConfig().getInt("database.pool.maximum-pool-size", 10));
      config.setMinimumIdle(this.plugin.getConfig().getInt("database.pool.minimum-idle", 2));
      config.setConnectionTimeout(this.plugin.getConfig().getLong("database.pool.connection-timeout", 10000L));
      config.setValidationTimeout(this.plugin.getConfig().getLong("database.pool.validation-timeout", 5000L));
      config.setLeakDetectionThreshold(this.plugin.getConfig().getLong("database.pool.leak-detection-threshold", 0L));
      boolean useMysql = this.plugin.getConfig().getBoolean("database.use-mysql", false);
      if (useMysql) {
         String host = this.plugin.getConfig().getString("database.host", "localhost");
         int port = this.plugin.getConfig().getInt("database.port", 3306);
         String database = this.plugin.getConfig().getString("database.database", "minecraft");
         String username = this.plugin.getConfig().getString("database.username", "root");
         String password = this.plugin.getConfig().getString("database.password", "");
         config.setJdbcUrl(
            "jdbc:mysql://"
               + host
               + ":"
               + port
               + "/"
               + database
               + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048&useUnicode=true&characterEncoding=utf8"
         );
         config.setUsername(username);
         config.setPassword(password);
         config.addDataSourceProperty("cachePrepStmts", "true");
         config.addDataSourceProperty("prepStmtCacheSize", "250");
         config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
         this.plugin.getLogger().info("Using MySQL with HikariCP connection pool.");
      } else {
         File folder = new File(this.plugin.getDataFolder(), "Clans");
         if (!folder.exists() && !folder.mkdirs()) {
            this.plugin.getLogger().warning("Could not create database folder: " + folder.getAbsolutePath());
         }

         File dbFile = new File(folder, "database.db");
         config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
         config.setMaximumPoolSize(1);
         config.setConnectionTestQuery("SELECT 1");
         this.plugin.getLogger().info("Using SQLite with HikariCP (Clans/database.db).");
      }

      this.dataSource = new HikariDataSource(config);
      this.createTables();
   }

   public void disconnect() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }

   public Connection getConnection() throws SQLException {
      if (this.dataSource == null || this.dataSource.isClosed()) {
         this.connect();
      }

      return this.dataSource.getConnection();
   }

   public DataSource getDataSource() {
      return this.dataSource;
   }

   private void createTables() throws SQLException {
      try (
         Connection connection = this.getConnection();
         Statement statement = connection.createStatement();
      ) {
         statement.execute(
            "CREATE TABLE IF NOT EXISTS vrx_clans (name VARCHAR(32) PRIMARY KEY, bank DOUBLE, level INT, xp DOUBLE, lastUpkeep BIGINT, radBonus INT, xpBoost BIGINT, hasteBoost BIGINT, speedBoost BIGINT, strBoost BIGINT, qZombies INT, qDiamonds INT, qMoney DOUBLE, qZDone BOOLEAN, qDDone BOOLEAN, qMDone BOOLEAN, qReset BIGINT, kills INT, deaths INT, warActive VARCHAR(32), warKills INT, warWager DOUBLE, bWorld VARCHAR(64), bX DOUBLE, bY DOUBLE, bZ DOUBLE, bYaw FLOAT, bPitch FLOAT, vault TEXT, allies TEXT, rivals TEXT, bRegen BOOLEAN, bSat BOOLEAN, perms TEXT, rankNames TEXT, rewardLevel INT DEFAULT 1, tag VARCHAR(12), tagColor VARCHAR(8), homes TEXT, achievements TEXT, description TEXT, iconMaterial VARCHAR(64), motd TEXT, mail TEXT, extraMembers INT DEFAULT 0, extraHomes INT DEFAULT 0, extraVaultRows INT DEFAULT 0, extraAllies INT DEFAULT 0, extraRivals INT DEFAULT 0, tpDelayReduction INT DEFAULT 0, tpCostReduction DOUBLE DEFAULT 0)"
         );
         statement.execute("CREATE TABLE IF NOT EXISTS vrx_members (uuid VARCHAR(36) PRIMARY KEY, clan_name VARCHAR(32), rank VARCHAR(16))");
         String idColumn = this.plugin.getConfig().getBoolean("database.use-mysql", false)
            ? "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
            : "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
         statement.execute(
            "CREATE TABLE IF NOT EXISTS vrx_clan_logs ("
               + idColumn
               + "timestamp BIGINT NOT NULL, clan_name VARCHAR(32) NOT NULL, type VARCHAR(32) NOT NULL, actor VARCHAR(64), target VARCHAR(64), amount DOUBLE, message TEXT)"
         );
         statement.execute(
            "CREATE TABLE IF NOT EXISTS vrx_war_history ("
               + idColumn
               + "timestamp BIGINT NOT NULL, winner VARCHAR(32) NOT NULL, loser VARCHAR(32) NOT NULL, clan_a VARCHAR(32) NOT NULL, clan_b VARCHAR(32) NOT NULL, winner_kills INT, loser_kills INT, pot DOUBLE, duration_millis BIGINT)"
         );
         this.addColumnIfMissing(statement, "vrx_clans", "rankNames", "TEXT");
         this.addColumnIfMissing(statement, "vrx_clans", "rewardLevel", "INT DEFAULT 1");
         this.addColumnIfMissing(statement, "vrx_clans", "achievements", "TEXT");
         this.addColumnIfMissing(statement, "vrx_clans", "homes", "TEXT");
         this.addColumnIfMissing(statement, "vrx_clans", "tagColor", "VARCHAR(8)");
         this.addColumnIfMissing(statement, "vrx_clans", "tag", "VARCHAR(12)");
         this.addColumnIfMissing(statement, "vrx_clans", "description", "TEXT");
         this.addColumnIfMissing(statement, "vrx_clans", "iconMaterial", "VARCHAR(64)");
         this.addColumnIfMissing(statement, "vrx_clans", "motd", "TEXT");
         this.addColumnIfMissing(statement, "vrx_clans", "mail", "TEXT");
         this.addColumnIfMissing(statement, "vrx_clans", "extraMembers", "INT DEFAULT 0");
         this.addColumnIfMissing(statement, "vrx_clans", "extraHomes", "INT DEFAULT 0");
         this.addColumnIfMissing(statement, "vrx_clans", "extraVaultRows", "INT DEFAULT 0");
         this.addColumnIfMissing(statement, "vrx_clans", "extraAllies", "INT DEFAULT 0");
         this.addColumnIfMissing(statement, "vrx_clans", "extraRivals", "INT DEFAULT 0");
         this.addColumnIfMissing(statement, "vrx_clans", "tpDelayReduction", "INT DEFAULT 0");
         this.addColumnIfMissing(statement, "vrx_clans", "tpCostReduction", "DOUBLE DEFAULT 0");
      }

      new DatabaseMigrationService(this.plugin, this).migrate();
   }

   private void addColumnIfMissing(Statement statement, String table, String column, String type) {
      try {
         statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
         this.plugin.getLogger().info("Database migration: Column " + table + "." + column + " was added.");
      } catch (SQLException var6) {
      }
   }
}
