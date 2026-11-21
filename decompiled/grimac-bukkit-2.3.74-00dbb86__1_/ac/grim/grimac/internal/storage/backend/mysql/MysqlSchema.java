package ac.grim.grimac.internal.storage.backend.mysql;

import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.internal.storage.checks.LegacyKeyRenames;
import ac.grim.grimac.internal.storage.util.UuidCodec;
import ac.grim.grimac.internal.storage.util.UuidV7;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.Map.Entry;

@ApiStatus.Internal
public final class MysqlSchema {
   public static final int CURRENT_VERSION = 9;

   private MysqlSchema() {
   }

   public static void ensureInitialized(Connection c, String grimCoreVersion, TableNames t, MysqlDialect dialect) throws SQLException {
      try (Statement s = c.createStatement()) {
         s.executeUpdate(
            "CREATE TABLE IF NOT EXISTS "
               + t.meta()
               + " (id TINYINT PRIMARY KEY, schema_version INT NOT NULL, grim_core_version VARCHAR(64), initialized_at BIGINT NOT NULL, last_migration_at BIGINT NOT NULL, CONSTRAINT "
               + t.meta()
               + "_singleton CHECK (id = 0)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
         );
      }

      int existing = readSchemaVersion(c, t);
      if (existing < 0) {
         applyBaseline(c, t, dialect);
         long now = System.currentTimeMillis();

         try (PreparedStatement ps = c.prepareStatement(
               "INSERT INTO " + t.meta() + "(id, schema_version, grim_core_version, initialized_at, last_migration_at) VALUES (0, ?, ?, ?, ?)"
            )) {
            ps.setInt(1, 9);
            ps.setString(2, grimCoreVersion);
            ps.setLong(3, now);
            ps.setLong(4, now);
            ps.executeUpdate();
         }
      } else if (existing > 9) {
         throw new SQLException(
            "[grim-datastore] MySQL schema is version " + existing + " but this core supports up to 9; refusing to downgrade. Roll Grim forward."
         );
      } else {
         if (existing < 6) {
            migrateV5ToV6(c, t);
         }

         if (existing < 7) {
            migrateV6ToV7(c, t);
         }

         if (existing < 8) {
            migrateV7ToV8(c, t, dialect);
         }

         if (existing < 9) {
            migrateV8ToV9(c, t);
         }

         if (existing < 9) {
            try (PreparedStatement ps = c.prepareStatement("UPDATE " + t.meta() + " SET schema_version = ?, last_migration_at = ? WHERE id = 0")) {
               ps.setInt(1, 9);
               ps.setLong(2, System.currentTimeMillis());
               ps.executeUpdate();
            }
         }
      }
   }

   private static void migrateV5ToV6(Connection c, TableNames t) throws SQLException {
      try (Statement s = c.createStatement()) {
         s.executeUpdate("ALTER TABLE " + t.sessions() + " ADD COLUMN closed_at BIGINT NULL");
      }
   }

   private static void migrateV6ToV7(Connection c, TableNames t) throws SQLException {
      try (PreparedStatement ps = c.prepareStatement("UPDATE " + t.checks() + " SET stable_key = ? WHERE stable_key = ?")) {
         for (Entry<String, String> e : LegacyKeyRenames.OLD_TO_NEW.entrySet()) {
            ps.setString(1, e.getValue());
            ps.setString(2, e.getKey());
            ps.addBatch();
         }

         ps.executeBatch();
      }
   }

   public static int readSchemaVersion(Connection c, TableNames t) throws SQLException {
      try {
         byte var12;
         try (
            PreparedStatement ps = c.prepareStatement("SELECT schema_version FROM " + t.meta() + " WHERE id = 0");
            ResultSet rs = ps.executeQuery();
         ) {
            if (rs.next()) {
               return rs.getInt(1);
            }

            var12 = -1;
         }

         return var12;
      } catch (SQLException var10) {
         String msg = var10.getMessage() == null ? "" : var10.getMessage();
         if (!msg.contains("doesn't exist") && var10.getErrorCode() != 1146) {
            throw var10;
         } else {
            return -1;
         }
      }
   }

   private static void migrateV7ToV8(Connection c, TableNames t, MysqlDialect dialect) throws SQLException {
      try (Statement s = c.createStatement()) {
         dialect.migrateV7ToV8(s, t);
      }
   }

   private static void applyBaseline(Connection c, TableNames t, MysqlDialect dialect) throws SQLException {
      try (Statement s = c.createStatement()) {
         dialect.applyBaseline(s, t);
      }
   }

   private static void migrateV8ToV9(Connection c, TableNames t) throws SQLException {
      String oldTable = t.violations();
      String newTable = t.violations() + "_uuid_v7_tmp";
      String backupTable = t.violations() + "_pre_v9_backup";
      if (violationsIdIsBinary16(c, oldTable)) {
         try (Statement s = c.createStatement()) {
            s.executeUpdate("DROP TABLE IF EXISTS " + backupTable);
         }
      } else {
         try (Statement s = c.createStatement()) {
            s.executeUpdate("DROP TABLE IF EXISTS " + newTable);
            s.executeUpdate("DROP TABLE IF EXISTS " + backupTable);
            s.executeUpdate(
               "CREATE TABLE "
                  + newTable
                  + " (id BINARY(16) PRIMARY KEY, session_id BINARY(16) NOT NULL, player_uuid BINARY(16) NOT NULL, check_id INT NOT NULL, vl DOUBLE NOT NULL, occurred_at BIGINT NOT NULL, verbose MEDIUMTEXT, verbose_format INT NOT NULL DEFAULT 0, metadata MEDIUMTEXT, INDEX idx_"
                  + oldTable
                  + "_session_time (session_id, occurred_at, id), INDEX idx_"
                  + oldTable
                  + "_player (player_uuid)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
         }

         try (
            PreparedStatement sel = c.prepareStatement(
               "SELECT id, session_id, player_uuid, check_id, vl, occurred_at, verbose, verbose_format, metadata FROM " + oldTable
            );
            PreparedStatement ins = c.prepareStatement(
               "INSERT INTO "
                  + newTable
                  + "(id, session_id, player_uuid, check_id, vl, occurred_at, verbose, verbose_format, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ResultSet rs = sel.executeQuery();
         ) {
            int batched = 0;

            while (rs.next()) {
               long occurred = rs.getLong("occurred_at");
               UUID newId = UuidV7.fromTimestampMs(occurred, rs.getLong("id"));
               ins.setBytes(1, UuidCodec.toBytes(newId));
               ins.setBytes(2, rs.getBytes("session_id"));
               ins.setBytes(3, rs.getBytes("player_uuid"));
               ins.setInt(4, rs.getInt("check_id"));
               ins.setDouble(5, rs.getDouble("vl"));
               ins.setLong(6, occurred);
               ins.setString(7, rs.getString("verbose"));
               ins.setInt(8, rs.getInt("verbose_format"));
               ins.setString(9, rs.getString("metadata"));
               ins.addBatch();
               if (++batched % 1024 == 0) {
                  ins.executeBatch();
               }
            }

            ins.executeBatch();
         }

         try (Statement s = c.createStatement()) {
            s.executeUpdate("RENAME TABLE " + oldTable + " TO " + backupTable + ", " + newTable + " TO " + oldTable);
            s.executeUpdate("DROP TABLE IF EXISTS " + backupTable);
         }
      }
   }

   private static boolean violationsIdIsBinary16(Connection c, String table) throws SQLException {
      boolean var10;
      try (PreparedStatement ps = c.prepareStatement(
            "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'id'"
         )) {
         ps.setString(1, table);

         try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
               return false;
            }

            var10 = "binary".equalsIgnoreCase(rs.getString(1));
         }
      }

      return var10;
   }
}
