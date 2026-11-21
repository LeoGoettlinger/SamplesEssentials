package ac.grim.grimac.internal.storage.backend.mysql;

import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.SQLException;
import java.sql.Statement;

@ApiStatus.Internal
final class MysqlEightDialect implements MysqlDialect {
   @Override
   public String upsertSessions(TableNames t) {
      return "INSERT INTO "
         + t.sessions()
         + "(session_id, player_uuid, server_name, started_at, last_activity, closed_at, grim_version, client_brand, client_version_pvn, server_version, replay_clips_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) AS new ON DUPLICATE KEY UPDATE server_name=new.server_name, last_activity=new.last_activity, closed_at=COALESCE("
         + t.sessions()
         + ".closed_at, new.closed_at), grim_version=new.grim_version, client_brand=new.client_brand, client_version_pvn=new.client_version_pvn, server_version=new.server_version, replay_clips_json=new.replay_clips_json";
   }

   @Override
   public String upsertIdentities(TableNames t) {
      return "INSERT INTO "
         + t.players()
         + "(uuid, current_name, first_seen, last_seen) VALUES (?, ?, ?, ?) AS new ON DUPLICATE KEY UPDATE current_name = new.current_name, first_seen = LEAST("
         + t.players()
         + ".first_seen, new.first_seen), last_seen = GREATEST("
         + t.players()
         + ".last_seen, new.last_seen)";
   }

   @Override
   public String upsertSettings(TableNames t) {
      return "INSERT INTO "
         + t.settings()
         + "(scope, scope_key, `key`, value, updated_at) VALUES (?, ?, ?, ?, ?) AS new ON DUPLICATE KEY UPDATE value = new.value, updated_at = new.updated_at";
   }

   @Override
   public void migrateV7ToV8(Statement s, TableNames t) throws SQLException {
      s.executeUpdate("ALTER TABLE " + t.players() + " ADD COLUMN current_name_lower VARCHAR(32) AS (LOWER(current_name)) STORED");
      s.executeUpdate(
         "ALTER TABLE " + t.players() + " DROP INDEX idx_" + t.players() + "_name_lower, ADD INDEX idx_" + t.players() + "_name_lower (current_name_lower)"
      );
      s.execute("ANALYZE TABLE " + t.players());
   }
}
