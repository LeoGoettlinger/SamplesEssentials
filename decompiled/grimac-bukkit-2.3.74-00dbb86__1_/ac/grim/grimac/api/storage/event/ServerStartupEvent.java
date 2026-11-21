package ac.grim.grimac.api.storage.event;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@ApiStatus.Experimental
public final class ServerStartupEvent {
   @Nullable
   private UUID startupId;
   @Nullable
   private UUID instanceId;
   @Nullable
   private String serverName;
   @Nullable
   private String grimVersion;
   @Nullable
   private String serverVersionString;
   @Nullable
   private String hostname;
   private long startedEpochMs;
   private long lastHeartbeatEpochMs;
   private long closedAtEpochMs;
   @Nullable
   private String closeReason;
   @Nullable
   private byte[] verboseManifest;

   @Nullable
   public UUID startupId() {
      return this.startupId;
   }

   @NotNull
   public ServerStartupEvent startupId(@NotNull UUID v) {
      this.startupId = v;
      return this;
   }

   @Nullable
   public UUID instanceId() {
      return this.instanceId;
   }

   @NotNull
   public ServerStartupEvent instanceId(@NotNull UUID v) {
      this.instanceId = v;
      return this;
   }

   @Nullable
   public String serverName() {
      return this.serverName;
   }

   @NotNull
   public ServerStartupEvent serverName(@NotNull String v) {
      this.serverName = v;
      return this;
   }

   @Nullable
   public String grimVersion() {
      return this.grimVersion;
   }

   @NotNull
   public ServerStartupEvent grimVersion(@Nullable String v) {
      this.grimVersion = v;
      return this;
   }

   @Nullable
   public String serverVersionString() {
      return this.serverVersionString;
   }

   @NotNull
   public ServerStartupEvent serverVersionString(@Nullable String v) {
      this.serverVersionString = v;
      return this;
   }

   @Nullable
   public String hostname() {
      return this.hostname;
   }

   @NotNull
   public ServerStartupEvent hostname(@Nullable String v) {
      this.hostname = v;
      return this;
   }

   public long startedEpochMs() {
      return this.startedEpochMs;
   }

   @NotNull
   public ServerStartupEvent startedEpochMs(long v) {
      this.startedEpochMs = v;
      return this;
   }

   public long lastHeartbeatEpochMs() {
      return this.lastHeartbeatEpochMs;
   }

   @NotNull
   public ServerStartupEvent lastHeartbeatEpochMs(long v) {
      this.lastHeartbeatEpochMs = v;
      return this;
   }

   public long closedAtEpochMs() {
      return this.closedAtEpochMs;
   }

   @NotNull
   public ServerStartupEvent closedAtEpochMs(long v) {
      this.closedAtEpochMs = v;
      return this;
   }

   @Nullable
   public String closeReason() {
      return this.closeReason;
   }

   @NotNull
   public ServerStartupEvent closeReason(@Nullable String v) {
      this.closeReason = v;
      return this;
   }

   @Nullable
   public byte[] verboseManifest() {
      return this.verboseManifest;
   }

   @NotNull
   public ServerStartupEvent verboseManifest(@Nullable byte[] v) {
      this.verboseManifest = v;
      return this;
   }

   public void reset() {
      this.startupId = null;
      this.instanceId = null;
      this.serverName = null;
      this.grimVersion = null;
      this.serverVersionString = null;
      this.hostname = null;
      this.startedEpochMs = 0L;
      this.lastHeartbeatEpochMs = 0L;
      this.closedAtEpochMs = 0L;
      this.closeReason = null;
      this.verboseManifest = null;
   }
}
