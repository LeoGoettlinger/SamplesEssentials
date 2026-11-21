package ac.grim.grimac.internal.storage.backend.mysql;

import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
final class MariaDbDialect implements MysqlDialect {
   @Override
   public String upsertSessions(TableNames t) {
      return "INSERT INTO "
         + t.sessions()
         + "(session_id, player_uuid, server_name, started_at, last_activity, closed_at, grim_version, client_brand, client_version_pvn, server_version, replay_clips_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE server_name=VALUES(server_name), last_activity=VALUES(last_activity), closed_at=COALESCE("
         + t.sessions()
         + ".closed_at, VALUES(closed_at)), grim_version=VALUES(grim_version), client_brand=VALUES(client_brand), client_version_pvn=VALUES(client_version_pvn), server_version=VALUES(server_version), replay_clips_json=VALUES(replay_clips_json)";
   }

   @Override
   public String upsertIdentities(TableNames t) {
      return "INSERT INTO "
         + t.players()
         + "(uuid, current_name, first_seen, last_seen) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE current_name = VALUES(current_name), first_seen = LEAST("
         + t.players()
         + ".first_seen, VALUES(first_seen)), last_seen = GREATEST("
         + t.players()
         + ".last_seen, VALUES(last_seen))";
   }

   @Override
   public String upsertSettings(TableNames t) {
      return "INSERT INTO "
         + t.settings()
         + "(scope, scope_key, `key`, value, updated_at) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value), updated_at = VALUES(updated_at)";
   }
}
