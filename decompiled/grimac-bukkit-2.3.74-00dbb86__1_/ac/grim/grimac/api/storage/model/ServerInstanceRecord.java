package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.api.storage.codec.Id;
import ac.grim.grimac.api.storage.codec.Indexed;
import ac.grim.grimac.api.storage.codec.Name;
import ac.grim.grimac.api.storage.codec.Nullable;
import ac.grim.grimac.api.storage.codec.Persistent;
import ac.grim.grimac.api.storage.codec.Value;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.UUID;

@Persistent
@ApiStatus.Experimental
public record ServerInstanceRecord(
   @Id @NotNull UUID instanceId,
   @Indexed @Name("server_name") @NotNull String serverName,
   @Value @Name("started_at") long startedEpochMs,
   @Value @Name("last_heartbeat") long lastHeartbeatEpochMs,
   @Value @Nullable String hostname,
   @Value @Name("grim_version") @Nullable String grimVersion,
   @Value @Name("drain_mode") boolean drainMode
) {
   public ServerInstanceRecord(
      @NotNull UUID instanceId,
      @NotNull String serverName,
      long startedEpochMs,
      long lastHeartbeatEpochMs,
      String hostname,
      String grimVersion,
      boolean drainMode
   ) {
      if (instanceId == null) {
         throw new IllegalArgumentException("instanceId");
      } else if (serverName == null) {
         throw new IllegalArgumentException("serverName");
      } else {
         this.instanceId = instanceId;
         this.serverName = serverName;
         this.startedEpochMs = startedEpochMs;
         this.lastHeartbeatEpochMs = lastHeartbeatEpochMs;
         this.hostname = hostname;
         this.grimVersion = grimVersion;
         this.drainMode = drainMode;
      }
   }
}
