package vrx.economy.vRXClans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class DatabaseMigrationService {
   private final VRXClans plugin;
   private final DatabaseManager databaseManager;

   public DatabaseMigrationService(VRXClans plugin, DatabaseManager databaseManager) {
      this.plugin = plugin;
      this.databaseManager = databaseManager;
   }

   public void migrate() throws SQLException {
      try (Connection connection = this.databaseManager.getConnection()) {
         this.createMigrationTable(connection);

         for (Entry<Integer, DatabaseMigrationService.Migration> entry : this.migrations().entrySet()) {
            int version = entry.getKey();
            if (!this.isApplied(connection, version)) {
               entry.getValue().apply(connection);
               this.markApplied(connection, version);
               this.plugin.getLogger().info("Database migration V" + version + " successfully applied: " + entry.getValue().description());
            }
         }
      }
   }

   private Map<Integer, DatabaseMigrationService.Migration> migrations() {
      Map<Integer, DatabaseMigrationService.Migration> migrations = new LinkedHashMap<>();
      migrations.put(
         1,
         new DatabaseMigrationService.Migration(
            "Clan-Rollennamen und Clan-Logs",
            connection -> {
               this.addColumnIfMissing(connection, "vrx_clans", "rankNames", "TEXT");
               String idColumn = this.plugin.getConfig().getBoolean("database.use-mysql", false)
                  ? "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                  : "id INTEGER PRIMARY KEY AUTOINCREMENT, ";

               try (Statement statement = connection.createStatement()) {
                  statement.execute(
                     "CREATE TABLE IF NOT EXISTS vrx_clan_logs ("
                        + idColumn
                        + "timestamp BIGINT NOT NULL, clan_name VARCHAR(32) NOT NULL, type VARCHAR(32) NOT NULL, actor VARCHAR(64), target VARCHAR(64), amount DOUBLE, message TEXT)"
                  );
               }
            }
         )
      );
      migrations.put(2, new DatabaseMigrationService.Migration("Clan-Level-Rewards Status", connection -> {
         this.addColumnIfMissing(connection, "vrx_clans", "rewardLevel", "INT DEFAULT 1");

         try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE vrx_clans SET rewardLevel = level WHERE rewardLevel IS NULL OR rewardLevel < 1");
         }
      }));
      migrations.put(
         3,
         new DatabaseMigrationService.Migration(
            "Clan-Tags, Homes, Achievements und War-History",
            connection -> {
               this.addColumnIfMissing(connection, "vrx_clans", "tag", "VARCHAR(12)");
               this.addColumnIfMissing(connection, "vrx_clans", "tagColor", "VARCHAR(8)");
               this.addColumnIfMissing(connection, "vrx_clans", "homes", "TEXT");
               this.addColumnIfMissing(connection, "vrx_clans", "achievements", "TEXT");
               String idColumn = this.plugin.getConfig().getBoolean("database.use-mysql", false)
                  ? "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                  : "id INTEGER PRIMARY KEY AUTOINCREMENT, ";

               try (Statement statement = connection.createStatement()) {
                  statement.execute(
                     "CREATE TABLE IF NOT EXISTS vrx_war_history ("
                        + idColumn
                        + "timestamp BIGINT NOT NULL, winner VARCHAR(32) NOT NULL, loser VARCHAR(32) NOT NULL, clan_a VARCHAR(32) NOT NULL, clan_b VARCHAR(32) NOT NULL, winner_kills INT, loser_kills INT, pot DOUBLE, duration_millis BIGINT)"
                  );
               }
            }
         )
      );
      migrations.put(4, new DatabaseMigrationService.Migration("Clan-Profil, Mail/MOTD und Shop-Upgrades", connection -> {
         this.addColumnIfMissing(connection, "vrx_clans", "description", "TEXT");
         this.addColumnIfMissing(connection, "vrx_clans", "iconMaterial", "VARCHAR(64)");
         this.addColumnIfMissing(connection, "vrx_clans", "motd", "TEXT");
         this.addColumnIfMissing(connection, "vrx_clans", "mail", "TEXT");
         this.addColumnIfMissing(connection, "vrx_clans", "extraMembers", "INT DEFAULT 0");
         this.addColumnIfMissing(connection, "vrx_clans", "extraHomes", "INT DEFAULT 0");
         this.addColumnIfMissing(connection, "vrx_clans", "extraVaultRows", "INT DEFAULT 0");
         this.addColumnIfMissing(connection, "vrx_clans", "extraAllies", "INT DEFAULT 0");
         this.addColumnIfMissing(connection, "vrx_clans", "extraRivals", "INT DEFAULT 0");
         this.addColumnIfMissing(connection, "vrx_clans", "tpDelayReduction", "INT DEFAULT 0");
         this.addColumnIfMissing(connection, "vrx_clans", "tpCostReduction", "DOUBLE DEFAULT 0");
      }));
      return migrations;
   }

   private void createMigrationTable(Connection connection) throws SQLException {
      try (Statement statement = connection.createStatement()) {
         statement.execute(
            "CREATE TABLE IF NOT EXISTS vrx_schema_migrations (version INT PRIMARY KEY, description VARCHAR(128) NOT NULL, applied_at BIGINT NOT NULL)"
         );
      }
   }

   private boolean isApplied(Connection connection, int version) throws SQLException {
      boolean var5;
      try (PreparedStatement ps = connection.prepareStatement("SELECT version FROM vrx_schema_migrations WHERE version = ?")) {
         ps.setInt(1, version);

         try (ResultSet rs = ps.executeQuery()) {
            var5 = rs.next();
         }
      }

      return var5;
   }

   private void markApplied(Connection connection, int version) throws SQLException {
      DatabaseMigrationService.Migration migration = this.migrations().get(version);

      try (PreparedStatement ps = connection.prepareStatement("INSERT INTO vrx_schema_migrations (version, description, applied_at) VALUES (?,?,?)")) {
         ps.setInt(1, version);
         ps.setString(2, migration.description());
         ps.setLong(3, System.currentTimeMillis());
         ps.executeUpdate();
      }
   }

   private void addColumnIfMissing(Connection connection, String table, String column, String type) throws SQLException {
      try (Statement statement = connection.createStatement()) {
         statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
      } catch (SQLException var10) {
      }
   }

   private record Migration(String description, DatabaseMigrationService.SqlMigration migration) {
      void apply(Connection connection) throws SQLException {
         this.migration.apply(connection);
      }
   }

   @FunctionalInterface
   private interface SqlMigration {
      void apply(Connection var1) throws SQLException;
   }
}
