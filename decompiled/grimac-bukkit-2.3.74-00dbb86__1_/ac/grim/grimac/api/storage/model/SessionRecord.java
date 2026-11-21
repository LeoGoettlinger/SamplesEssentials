package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.api.storage.codec.Id;
import ac.grim.grimac.api.storage.codec.Indexed;
import ac.grim.grimac.api.storage.codec.Name;
import ac.grim.grimac.api.storage.codec.Nullable;
import ac.grim.grimac.api.storage.codec.Persistent;
import ac.grim.grimac.api.storage.codec.PreserveOnNonNull;
import ac.grim.grimac.api.storage.codec.Sentinel;
import ac.grim.grimac.api.storage.codec.Transient;
import ac.grim.grimac.api.storage.codec.Value;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.List;
import java.util.UUID;

@Persistent
@ApiStatus.Experimental
public record SessionRecord(
   @Id UUID sessionId,
   @Indexed UUID playerUuid,
   @Value @Nullable String serverName,
   @Value @Name("started_at") long startedEpochMs,
   @Value @Name("last_activity") long lastActivityEpochMs,
   @Value @Name("closed_at") @Sentinel(0L) long closedAtEpochMs,
   @Value @Nullable String grimVersion,
   @Value @Nullable String clientBrand,
   @Value @Name("client_version_pvn") int clientVersion,
   @Value @Name("server_version") @Nullable String serverVersionString,
   @Value @Name("instance_id") @Nullable @PreserveOnNonNull UUID instanceId,
   @Indexed @Name("startup_id") @Nullable @PreserveOnNonNull UUID startupId,
   @Transient List<SessionBlobRecord> sessionBlobs
) {
   public static final long OPEN = 0L;

   public SessionRecord(
      UUID sessionId,
      UUID playerUuid,
      String serverName,
      long startedEpochMs,
      long lastActivityEpochMs,
      long closedAtEpochMs,
      String grimVersion,
      String clientBrand,
      int clientVersion,
      String serverVersionString,
      UUID instanceId,
      UUID startupId,
      List<SessionBlobRecord> sessionBlobs
   ) {
      if (sessionId == null) {
         throw new IllegalArgumentException("sessionId");
      } else if (playerUuid == null) {
         throw new IllegalArgumentException("playerUuid");
      } else {
         sessionBlobs = sessionBlobs == null ? List.of() : List.copyOf(sessionBlobs);
         this.sessionId = sessionId;
         this.playerUuid = playerUuid;
         this.serverName = serverName;
         this.startedEpochMs = startedEpochMs;
         this.lastActivityEpochMs = lastActivityEpochMs;
         this.closedAtEpochMs = closedAtEpochMs;
         this.grimVersion = grimVersion;
         this.clientBrand = clientBrand;
         this.clientVersion = clientVersion;
         this.serverVersionString = serverVersionString;
         this.instanceId = instanceId;
         this.startupId = startupId;
         this.sessionBlobs = sessionBlobs;
      }
   }

   public SessionRecord(
      UUID sessionId,
      UUID playerUuid,
      String serverName,
      long startedEpochMs,
      long lastActivityEpochMs,
      @ac.grim.grimac.shaded.jetbrains.annotations.Nullable Long closedAtEpochMs,
      String grimVersion,
      String clientBrand,
      int clientVersion,
      String serverVersionString,
      List<SessionBlobRecord> sessionBlobs
   ) {
      this(
         sessionId,
         playerUuid,
         serverName,
         startedEpochMs,
         lastActivityEpochMs,
         closedAtEpochMs == null ? 0L : closedAtEpochMs,
         grimVersion,
         clientBrand,
         clientVersion,
         serverVersionString,
         null,
         null,
         sessionBlobs
      );
   }

   public SessionRecord(
      UUID sessionId,
      UUID playerUuid,
      String serverName,
      long startedEpochMs,
      long lastActivityEpochMs,
      long closedAtEpochMs,
      String grimVersion,
      String clientBrand,
      int clientVersion,
      String serverVersionString,
      UUID instanceId,
      List<SessionBlobRecord> sessionBlobs
   ) {
      this(
         sessionId,
         playerUuid,
         serverName,
         startedEpochMs,
         lastActivityEpochMs,
         closedAtEpochMs,
         grimVersion,
         clientBrand,
         clientVersion,
         serverVersionString,
         instanceId,
         null,
         sessionBlobs
      );
   }

   public boolean isClosed() {
      return this.closedAtEpochMs != 0L;
   }
}
