package ac.grim.grimac.internal.storage.backend.sqlite.writers;

import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.internal.storage.util.UuidCodec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@ApiStatus.Internal
final class LegacySessionUpserter implements SessionUpserter {
   private final PreparedStatement insertIgnore;
   private final PreparedStatement update;

   LegacySessionUpserter(Connection c, TableNames t) throws SQLException {
      this.insertIgnore = c.prepareStatement(
         "INSERT OR IGNORE INTO "
            + t.sessions()
            + "(session_id, player_uuid, server_name, started_at, last_activity, closed_at, grim_version, client_brand, client_version_pvn, server_version, replay_clips_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      );
      this.update = c.prepareStatement(
         "UPDATE "
            + t.sessions()
            + " SET server_name = ?, last_activity = ?, closed_at = COALESCE(closed_at, ?), grim_version = ?, client_brand = ?, client_version_pvn = ?, server_version = ?, replay_clips_json = ? WHERE session_id = ?"
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
      byte[] sessionIdBytes = UuidCodec.toBytes(sessionId);
      this.insertIgnore.setBytes(1, sessionIdBytes);
      this.insertIgnore.setBytes(2, UuidCodec.toBytes(playerUuid));
      this.insertIgnore.setString(3, serverName);
      this.insertIgnore.setLong(4, startedEpochMs);
      this.insertIgnore.setLong(5, lastActivityEpochMs);
      if (closedAtEpochMs == 0L) {
         this.insertIgnore.setNull(6, -5);
      } else {
         this.insertIgnore.setLong(6, closedAtEpochMs);
      }

      this.insertIgnore.setString(7, grimVersion);
      this.insertIgnore.setString(8, clientBrand);
      this.insertIgnore.setInt(9, clientVersionPvn);
      this.insertIgnore.setString(10, serverVersionString);
      this.insertIgnore.setString(11, sessionBlobsJson);
      this.insertIgnore.addBatch();
      this.update.setString(1, serverName);
      this.update.setLong(2, lastActivityEpochMs);
      if (closedAtEpochMs == 0L) {
         this.update.setNull(3, -5);
      } else {
         this.update.setLong(3, closedAtEpochMs);
      }

      this.update.setString(4, grimVersion);
      this.update.setString(5, clientBrand);
      this.update.setInt(6, clientVersionPvn);
      this.update.setString(7, serverVersionString);
      this.update.setString(8, sessionBlobsJson);
      this.update.setBytes(9, sessionIdBytes);
      this.update.addBatch();
   }

   @Override
   public void executeBatch() throws SQLException {
      this.insertIgnore.executeBatch();
      this.update.executeBatch();
   }

   @Override
   public void close() throws SQLException {
      SQLException first = null;

      try {
         this.insertIgnore.close();
      } catch (SQLException var3) {
         first = var3;
      }

      try {
         this.update.close();
      } catch (SQLException var4) {
         if (first == null) {
            first = var4;
         } else {
            first.addSuppressed(var4);
         }
      }

      if (first != null) {
         throw first;
      }
   }
}
