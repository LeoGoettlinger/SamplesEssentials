package ac.grim.grimac.internal.storage.backend.sqlite.writers;

import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.internal.storage.util.UuidCodec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@ApiStatus.Internal
final class ModernSessionUpserter implements SessionUpserter {
   private final PreparedStatement upsert;

   ModernSessionUpserter(Connection c, TableNames t) throws SQLException {
      this.upsert = c.prepareStatement(
         "INSERT INTO "
            + t.sessions()
            + "(session_id, player_uuid, server_name, started_at, last_activity, closed_at, grim_version, client_brand, client_version_pvn, server_version, replay_clips_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(session_id) DO UPDATE SET server_name=excluded.server_name, last_activity=excluded.last_activity, closed_at=COALESCE("
            + t.sessions()
            + ".closed_at, excluded.closed_at), grim_version=excluded.grim_version, client_brand=excluded.client_brand, client_version_pvn=excluded.client_version_pvn, server_version=excluded.server_version, replay_clips_json=excluded.replay_clips_json"
      );
   }

   @Override
   public void addBatch(
      UUID sessionId,
      UUID playerUuid,
      String serverName,
      long startedEpochMs,
      long lastActivityEpochMs,
      long closedAtEpochMs,
      String grimVersion,
      String clientBrand,
      int clientVersionPvn,
      String serverVersionString,
      String sessionBlobsJson
   ) throws SQLException {
      this.upsert.setBytes(1, UuidCodec.toBytes(sessionId));
      this.upsert.setBytes(2, UuidCodec.toBytes(playerUuid));
      this.upsert.setString(3, serverName);
      this.upsert.setLong(4, startedEpochMs);
      this.upsert.setLong(5, lastActivityEpochMs);
      if (closedAtEpochMs == 0L) {
         this.upsert.setNull(6, -5);
      } else {
         this.upsert.setLong(6, closedAtEpochMs);
      }

      this.upsert.setString(7, grimVersion);
      this.upsert.setString(8, clientBrand);
      this.upsert.setInt(9, clientVersionPvn);
      this.upsert.setString(10, serverVersionString);
      this.upsert.setString(11, sessionBlobsJson);
      this.upsert.addBatch();
   }

   @Override
   public void executeBatch() throws SQLException {
      this.upsert.executeBatch();
   }

   @Override
   public void close() throws SQLException {
      this.upsert.close();
   }
}
