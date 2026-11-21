package ac.grim.grimac.internal.storage.backend.postgres;

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
public final class PostgresSchema {
   public static final int CURRENT_VERSION = 8;

   private PostgresSchema() {
   }

   public static void ensureInitialized(Connection c, String grimCoreVersion, TableNames t) throws SQLException {
      try (Statement s = c.createStatement()) {
         s.executeUpdate(
            "CREATE TABLE IF NOT EXISTS "
               + quoteId(t.meta())
               + " (id SMALLINT PRIMARY KEY CHECK (id = 0), schema_version INTEGER NOT NULL, grim_core_version TEXT, initialized_at BIGINT NOT NULL, last_migration_at BIGINT NOT NULL)"
         );
      }

      int existing = readSchemaVersion(c, t);
      if (existing < 0) {
         applyBaseline(c, t);
         long now = System.currentTimeMillis();

         try (PreparedStatement ps = c.prepareStatement(
               "INSERT INTO " + quoteId(t.meta()) + "(id, schema_version, grim_core_version, initialized_at, last_migration_at) VALUES (0, ?, ?, ?, ?)"
            )) {
            ps.setInt(1, 8);
            ps.setString(2, grimCoreVersion);
            ps.setLong(3, now);
            ps.setLong(4, now);
            ps.executeUpdate();
         }
      } else if (existing > 8) {
         throw new SQLException(
            "[grim-datastore] Postgres schema is version " + existing + " but this core supports up to 8; refusing to downgrade. Roll Grim forward."
         );
      } else {
         if (existing < 6) {
            migrateV5ToV6(c, t);
         }

         if (existing < 7) {
            migrateV6ToV7(c, t);
         }

         if (existing < 8) {
            migrateV7ToV8(c, t);
         }

         if (existing < 8) {
            try (PreparedStatement ps = c.prepareStatement("UPDATE " + quoteId(t.meta()) + " SET schema_version = ?, last_migration_at = ? WHERE id = 0")) {
               ps.setInt(1, 8);
               ps.setLong(2, System.currentTimeMillis());
               ps.executeUpdate();
            }
         }
      }
   }

   private static void migrateV5ToV6(Connection c, TableNames t) throws SQLException {
      try (Statement s = c.createStatement()) {
         s.executeUpdate("ALTER TABLE " + quoteId(t.sessions()) + " ADD COLUMN closed_at BIGINT");
      }
   }

   private static void migrateV6ToV7(Connection c, TableNames t) throws SQLException {
      try (PreparedStatement ps = c.prepareStatement("UPDATE " + quoteId(t.checks()) + " SET stable_key = ? WHERE stable_key = ?")) {
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
         byte var11;
         try (
            PreparedStatement ps = c.prepareStatement("SELECT schema_version FROM " + quoteId(t.meta()) + " WHERE id = 0");
            ResultSet rs = ps.executeQuery();
         ) {
            if (rs.next()) {
               return rs.getInt(1);
            }

            var11 = -1;
         }

         return var11;
      } catch (SQLException var10) {
         if ("42P01".equals(var10.getSQLState())) {
            return -1;
         } else {
            throw var10;
         }
      }
   }

   private static void applyBaseline(Connection c, TableNames t) throws SQLException {
      try (Statement s = c.createStatement()) {
         s.executeUpdate(
            "CREATE TABLE "
               + quoteId(t.checks())
               + " (check_id SERIAL PRIMARY KEY, stable_key TEXT NOT NULL UNIQUE, display TEXT, description TEXT, introduced_version TEXT, removed_version TEXT, introduced_at BIGINT)"
         );
         s.executeUpdate(
            "CREATE TABLE " + quoteId(t.players()) + " (uuid BYTEA PRIMARY KEY, current_name TEXT, first_seen BIGINT NOT NULL, last_seen BIGINT NOT NULL)"
         );
         s.executeUpdate("CREATE INDEX " + quoteId("idx_" + t.players() + "_name_lower") + " ON " + quoteId(t.players()) + " (lower(current_name))");
         s.executeUpdate(
            "CREATE TABLE "
               + quoteId(t.sessions())
               + " (session_id BYTEA PRIMARY KEY, player_uuid BYTEA NOT NULL, server_name TEXT, started_at BIGINT NOT NULL, last_activity BIGINT NOT NULL, closed_at BIGINT, grim_version TEXT, client_brand TEXT, client_version_pvn INTEGER NOT NULL DEFAULT -1, server_version TEXT, replay_clips_json TEXT)"
         );
         s.executeUpdate(
            "CREATE INDEX " + quoteId("idx_" + t.sessions() + "_player_started") + " ON " + quoteId(t.sessions()) + " (player_uuid, started_at DESC)"
         );
         s.executeUpdate(
            "CREATE TABLE "
               + quoteId(t.violations())
               + " (id BYTEA PRIMARY KEY, session_id BYTEA NOT NULL, player_uuid BYTEA NOT NULL, check_id INTEGER NOT NULL, vl DOUBLE PRECISION NOT NULL, occurred_at BIGINT NOT NULL, \"verbose\" TEXT, verbose_format INTEGER NOT NULL DEFAULT 0, metadata TEXT)"
         );
         s.executeUpdate(
            "CREATE INDEX " + quoteId("idx_" + t.violations() + "_session_time") + " ON " + quoteId(t.violations()) + " (session_id, occurred_at, id)"
         );
         s.executeUpdate("CREATE INDEX " + quoteId("idx_" + t.violations() + "_player") + " ON " + quoteId(t.violations()) + " (player_uuid)");
         s.executeUpdate(
            "CREATE TABLE "
               + quoteId(t.settings())
               + " (scope TEXT NOT NULL, scope_key TEXT NOT NULL, key TEXT NOT NULL, value BYTEA NOT NULL, updated_at BIGINT NOT NULL, PRIMARY KEY (scope, scope_key, key))"
         );
      }
   }

   private static void migrateV7ToV8(Connection c, TableNames t) throws SQLException {
      String oldTable = t.violations();
      String newTable = t.violations() + "_uuid_v7_tmp";
      boolean priorAutoCommit = c.getAutoCommit();
      c.setAutoCommit(false);

      try {
         try (Statement s = c.createStatement()) {
            s.executeUpdate("DROP TABLE IF EXISTS " + quoteId(newTable));
            s.executeUpdate(
               "CREATE TABLE "
                  + quoteId(newTable)
                  + " (id BYTEA PRIMARY KEY, session_id BYTEA NOT NULL, player_uuid BYTEA NOT NULL, check_id INTEGER NOT NULL, vl DOUBLE PRECISION NOT NULL, occurred_at BIGINT NOT NULL, \"verbose\" TEXT, verbose_format INTEGER NOT NULL DEFAULT 0, metadata TEXT)"
            );
         }

         try (
            PreparedStatement sel = c.prepareStatement(
               "SELECT id, session_id, player_uuid, check_id, vl, occurred_at, \"verbose\", verbose_format, metadata FROM " + quoteId(oldTable)
            );
            PreparedStatement ins = c.prepareStatement(
               "INSERT INTO "
                  + quoteId(newTable)
                  + "(id, session_id, player_uuid, check_id, vl, occurred_at, \"verbose\", verbose_format, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
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
            s.executeUpdate("DROP TABLE " + quoteId(oldTable) + " CASCADE");
            s.executeUpdate("ALTER TABLE " + quoteId(newTable) + " RENAME TO " + quoteId(oldTable));
            s.executeUpdate("CREATE INDEX " + quoteId("idx_" + oldTable + "_session_time") + " ON " + quoteId(oldTable) + " (session_id, occurred_at, id)");
            s.executeUpdate("CREATE INDEX " + quoteId("idx_" + oldTable + "_player") + " ON " + quoteId(oldTable) + " (player_uuid)");
         }

         c.commit();
      } catch (SQLException var37) {
         try {
            c.rollback();
         } catch (SQLException var26) {
         }

         throw var37;
      } finally {
         c.setAutoCommit(priorAutoCommit);
      }
   }

   static String quoteId(String id) {
      return "\"" + id.replace("\"", "\"\"") + "\"";
   }
}
