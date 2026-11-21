package ac.grim.grimac.api.storage.event;

import ac.grim.grimac.api.storage.model.SessionBlobRecord;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiStatus.Experimental
public final class SessionEvent {
   private UUID sessionId;
   private UUID playerUuid;
   @Nullable
   private String serverName;
   private long startedEpochMs;
   private long lastActivityEpochMs;
   private long closedAtEpochMs;
   @Nullable
   private String grimVersion;
   @Nullable
   private String clientBrand;
   private int clientVersion = -1;
   @Nullable
   private String serverVersionString;
   @Nullable
   private UUID instanceId;
   @Nullable
   private UUID startupId;
   private final List<SessionBlobRecord> sessionBlobs = new ArrayList<>();

   @NotNull
   public UUID sessionId() {
      return this.sessionId;
   }

   @NotNull
   public SessionEvent sessionId(@NotNull UUID v) {
      this.sessionId = v;
      return this;
   }

   @NotNull
   public UUID playerUuid() {
      return this.playerUuid;
   }

   @NotNull
   public SessionEvent playerUuid(@NotNull UUID v) {
      this.playerUuid = v;
      return this;
   }

   @Nullable
   public String serverName() {
      return this.serverName;
   }

   @NotNull
   public SessionEvent serverName(@Nullable String v) {
      this.serverName = v;
      return this;
   }

   public long startedEpochMs() {
      return this.startedEpochMs;
   }

   @NotNull
   public SessionEvent startedEpochMs(long v) {
      this.startedEpochMs = v;
      return this;
   }

   public long lastActivityEpochMs() {
      return this.lastActivityEpochMs;
   }

   @NotNull
   public SessionEvent lastActivityEpochMs(long v) {
      this.lastActivityEpochMs = v;
      return this;
   }

   public long closedAtEpochMs() {
      return this.closedAtEpochMs;
   }

   @NotNull
   public SessionEvent closedAtEpochMs(long v) {
      this.closedAtEpochMs = v;
      return this;
   }

   public boolean isClosed() {
      return this.closedAtEpochMs != 0L;
   }

   @Nullable
   public String grimVersion() {
      return this.grimVersion;
   }

   @NotNull
   public SessionEvent grimVersion(@Nullable String v) {
      this.grimVersion = v;
      return this;
   }

   @Nullable
   public String clientBrand() {
      return this.clientBrand;
   }

   @NotNull
   public SessionEvent clientBrand(@Nullable String v) {
      this.clientBrand = v;
      return this;
   }

   public int clientVersion() {
      return this.clientVersion;
   }

   @NotNull
   public SessionEvent clientVersion(int v) {
      this.clientVersion = v;
      return this;
   }

   @Nullable
   public String serverVersionString() {
      return this.serverVersionString;
   }

   @NotNull
   public SessionEvent serverVersionString(@Nullable String v) {
      this.serverVersionString = v;
      return this;
   }

   @Nullable
   public UUID instanceId() {
      return this.instanceId;
   }

   @NotNull
   public SessionEvent instanceId(@Nullable UUID v) {
      this.instanceId = v;
      return this;
   }

   @Nullable
   public UUID startupId() {
      return this.startupId;
   }

   @NotNull
   public SessionEvent startupId(@Nullable UUID v) {
      this.startupId = v;
      return this;
   }

   @NotNull
   public List<SessionBlobRecord> sessionBlobs() {
      return this.sessionBlobs;
   }

   @NotNull
   public SessionEvent addSessionBlob(@NotNull SessionBlobRecord blob) {
      this.sessionBlobs.add(blob);
      return this;
   }

   @NotNull
   public SessionEvent replaceSessionBlobs(@Nullable List<SessionBlobRecord> blobs) {
      this.sessionBlobs.clear();
      if (blobs != null) {
         this.sessionBlobs.addAll(blobs);
      }

      return this;
   }

   public void reset() {
      this.sessionId = null;
      this.playerUuid = null;
      this.serverName = null;
      this.startedEpochMs = 0L;
      this.lastActivityEpochMs = 0L;
      this.closedAtEpochMs = 0L;
      this.grimVersion = null;
      this.clientBrand = null;
      this.clientVersion = -1;
      this.serverVersionString = null;
      this.instanceId = null;
      this.startupId = null;
      this.sessionBlobs.clear();
   }
}
