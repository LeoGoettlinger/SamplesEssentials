package ac.grim.grimac.api.storage.event;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@ApiStatus.Experimental
public final class ServerInstanceEvent {
   @Nullable
   private UUID instanceId;
   @Nullable
   private String serverName;
   private long startedEpochMs;
   private long lastHeartbeatEpochMs;
   @Nullable
   private String hostname;
   @Nullable
   private String grimVersion;
   private boolean drainMode;

   @Nullable
   public UUID instanceId() {
      return this.instanceId;
   }

   @NotNull
   public ServerInstanceEvent instanceId(@NotNull UUID v) {
      this.instanceId = v;
      return this;
   }

   @Nullable
   public String serverName() {
      return this.serverName;
   }

   @NotNull
   public ServerInstanceEvent serverName(@NotNull String v) {
      this.serverName = v;
      return this;
   }

   public long startedEpochMs() {
      return this.startedEpochMs;
   }

   @NotNull
   public ServerInstanceEvent startedEpochMs(long v) {
      this.startedEpochMs = v;
      return this;
   }

   public long lastHeartbeatEpochMs() {
      return this.lastHeartbeatEpochMs;
   }

   @NotNull
   public ServerInstanceEvent lastHeartbeatEpochMs(long v) {
      this.lastHeartbeatEpochMs = v;
      return this;
   }

   @Nullable
   public String hostname() {
      return this.hostname;
   }

   @NotNull
   public ServerInstanceEvent hostname(@Nullable String v) {
      this.hostname = v;
      return this;
   }

   @Nullable
   public String grimVersion() {
      return this.grimVersion;
   }

   @NotNull
   public ServerInstanceEvent grimVersion(@Nullable String v) {
      this.grimVersion = v;
      return this;
   }

   public boolean drainMode() {
      return this.drainMode;
   }

   @NotNull
   public ServerInstanceEvent drainMode(boolean v) {
      this.drainMode = v;
      return this;
   }

   public void reset() {
      this.instanceId = null;
      this.serverName = null;
      this.startedEpochMs = 0L;
      this.lastHeartbeatEpochMs = 0L;
      this.hostname = null;
      this.grimVersion = null;
      this.drainMode = false;
   }
}
