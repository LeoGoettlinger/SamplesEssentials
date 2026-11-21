package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.api.storage.codec.Id;
import ac.grim.grimac.api.storage.codec.Indexed;
import ac.grim.grimac.api.storage.codec.Name;
import ac.grim.grimac.api.storage.codec.Nullable;
import ac.grim.grimac.api.storage.codec.Persistent;
import ac.grim.grimac.api.storage.codec.PreserveOnNonNull;
import ac.grim.grimac.api.storage.codec.Sentinel;
import ac.grim.grimac.api.storage.codec.Value;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.UUID;

@Persistent
@ApiStatus.Experimental
public record ServerStartupRecord(
   @Id @NotNull UUID startupId,
   @Indexed @Name("instance_id") @NotNull UUID instanceId,
   @Indexed @Name("server_name") @NotNull String serverName,
   @Value @Name("grim_version") @Nullable String grimVersion,
   @Value @Name("server_version") @Nullable String serverVersionString,
   @Value @Nullable String hostname,
   @Value @Name("started_at") long startedEpochMs,
   @Value @Name("last_heartbeat") long lastHeartbeatEpochMs,
   @Value @Name("closed_at") @Sentinel(0L) long closedAtEpochMs,
   @Value @Name("close_reason") @Nullable @PreserveOnNonNull String closeReason,
   @Value @Name("verbose_manifest") @Nullable byte[] verboseManifest
) {
   public static final long OPEN = 0L;

   public ServerStartupRecord(
      @NotNull UUID startupId,
      @NotNull UUID instanceId,
      @NotNull String serverName,
      String grimVersion,
      String serverVersionString,
      String hostname,
      long startedEpochMs,
      long lastHeartbeatEpochMs,
      long closedAtEpochMs,
      String closeReason,
      byte[] verboseManifest
   ) {
      if (startupId == null) {
         throw new IllegalArgumentException("startupId");
      } else if (instanceId == null) {
         throw new IllegalArgumentException("instanceId");
      } else if (serverName == null) {
         throw new IllegalArgumentException("serverName");
      } else {
         this.startupId = startupId;
         this.instanceId = instanceId;
         this.serverName = serverName;
         this.grimVersion = grimVersion;
         this.serverVersionString = serverVersionString;
         this.hostname = hostname;
         this.startedEpochMs = startedEpochMs;
         this.lastHeartbeatEpochMs = lastHeartbeatEpochMs;
         this.closedAtEpochMs = closedAtEpochMs;
         this.closeReason = closeReason;
         this.verboseManifest = verboseManifest;
      }
   }

   public boolean isClosed() {
      return this.closedAtEpochMs != 0L;
   }
}
