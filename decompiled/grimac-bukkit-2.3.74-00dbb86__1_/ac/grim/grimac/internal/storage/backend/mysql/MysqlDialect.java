package ac.grim.grimac.internal.storage.backend.mysql;

import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.SQLException;
import java.sql.Statement;

@ApiStatus.Internal
public interface MysqlDialect {
   default void applyBaseline(Statement s, TableNames t) throws SQLException {
      s.executeUpdate(
         "CREATE TABLE "
            + t.checks()
            + " (check_id INT AUTO_INCREMENT PRIMARY KEY, stable_key VARCHAR(255) NOT NULL UNIQUE, display VARCHAR(255), description VARCHAR(1024), introduced_version VARCHAR(64), removed_version VARCHAR(64), introduced_at BIGINT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
      );
      s.executeUpdate(
         "CREATE TABLE "
            + t.players()
            + " (uuid BINARY(16) PRIMARY KEY, current_name VARCHAR(32), current_name_lower VARCHAR(32) AS (LOWER(current_name)) STORED, first_seen BIGINT NOT NULL, last_seen BIGINT NOT NULL, INDEX idx_"
            + t.players()
            + "_name_lower (current_name_lower)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
      );
      s.executeUpdate(
         "CREATE TABLE "
            + t.sessions()
            + " (session_id BINARY(16) PRIMARY KEY, player_uuid BINARY(16) NOT NULL, server_name VARCHAR(64), started_at BIGINT NOT NULL, last_activity BIGINT NOT NULL, closed_at BIGINT NULL, grim_version VARCHAR(64), client_brand VARCHAR(64), client_version_pvn INT NOT NULL DEFAULT -1, server_version VARCHAR(64), replay_clips_json MEDIUMTEXT, INDEX idx_"
            + t.sessions()
            + "_player_started (player_uuid, started_at DESC)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
      );
      s.executeUpdate(
         "CREATE TABLE "
            + t.violations()
            + " (id BINARY(16) PRIMARY KEY, session_id BINARY(16) NOT NULL, player_uuid BINARY(16) NOT NULL, check_id INT NOT NULL, vl DOUBLE NOT NULL, occurred_at BIGINT NOT NULL, verbose MEDIUMTEXT, verbose_format INT NOT NULL DEFAULT 0, metadata MEDIUMTEXT, INDEX idx_"
            + t.violations()
            + "_session_time (session_id, occurred_at, id), INDEX idx_"
            + t.violations()
            + "_player (player_uuid)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
      );
      s.executeUpdate(
         "CREATE TABLE "
            + t.settings()
            + " (scope VARCHAR(16) NOT NULL, scope_key VARCHAR(128) NOT NULL, `key` VARCHAR(128) NOT NULL, value LONGBLOB NOT NULL, updated_at BIGINT NOT NULL, PRIMARY KEY (scope, scope_key, `key`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
      );
   }

   String upsertSessions(TableNames var1);

   String upsertIdentities(TableNames var1);

   String upsertSettings(TableNames var1);

   default String selectIdentityByName(TableNames t) {
      return "SELECT uuid, current_name, first_seen, last_seen FROM " + t.players() + " WHERE current_name_lower = ? ORDER BY last_seen DESC LIMIT 1";
   }

   default String selectIdentitiesByNamePrefix(TableNames t) {
      return "SELECT uuid, current_name, first_seen, last_seen FROM "
         + t.players()
         + " WHERE current_name_lower LIKE ? ESCAPE '\\\\' ORDER BY last_seen DESC LIMIT ?";
   }

   default void migrateV7ToV8(Statement s, TableNames t) throws SQLException {
   }
}
