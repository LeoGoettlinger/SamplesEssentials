package ac.grim.grimac.api.storage.event;

import ac.grim.grimac.api.storage.model.VerboseFormat;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ApiStatus.Experimental
public final class ViolationEvent {
   @Nullable
   private UUID id;
   private UUID sessionId;
   private UUID playerUuid;
   private int checkId;
   private double vl;
   private long occurredEpochMs;
   @Nullable
   private byte[] verboseData;
   private VerboseFormat verboseFormat = VerboseFormat.TEXT;

   @Nullable
   public UUID id() {
      return this.id;
   }

   @NotNull
   public ViolationEvent id(@Nullable UUID v) {
      this.id = v;
      return this;
   }

   @NotNull
   public UUID sessionId() {
      return this.sessionId;
   }

   @NotNull
   public ViolationEvent sessionId(@NotNull UUID v) {
      this.sessionId = v;
      return this;
   }

   @NotNull
   public UUID playerUuid() {
      return this.playerUuid;
   }

   @NotNull
   public ViolationEvent playerUuid(@NotNull UUID v) {
      this.playerUuid = v;
      return this;
   }

   public int checkId() {
      return this.checkId;
   }

   @NotNull
   public ViolationEvent checkId(int v) {
      this.checkId = v;
      return this;
   }

   public double vl() {
      return this.vl;
   }

   @NotNull
   public ViolationEvent vl(double v) {
      this.vl = v;
      return this;
   }

   public long occurredEpochMs() {
      return this.occurredEpochMs;
   }

   @NotNull
   public ViolationEvent occurredEpochMs(long v) {
      this.occurredEpochMs = v;
      return this;
   }

   @Nullable
   public String verbose() {
      return this.verboseData == null ? null : new String(this.verboseData, StandardCharsets.UTF_8);
   }

   @NotNull
   public ViolationEvent verbose(@Nullable String v) {
      this.verboseData = v == null ? null : v.getBytes(StandardCharsets.UTF_8);
      return this;
   }

   @Nullable
   public byte[] verboseData() {
      return this.verboseData;
   }

   @NotNull
   public ViolationEvent verboseData(@Nullable byte[] v) {
      this.verboseData = v;
      return this;
   }

   @NotNull
   public VerboseFormat verboseFormat() {
      return this.verboseFormat;
   }

   @NotNull
   public ViolationEvent verboseFormat(@NotNull VerboseFormat v) {
      this.verboseFormat = v;
      return this;
   }

   public void reset() {
      this.id = null;
      this.sessionId = null;
      this.playerUuid = null;
      this.checkId = 0;
      this.vl = 0.0;
      this.occurredEpochMs = 0L;
      this.verboseData = null;
      this.verboseFormat = VerboseFormat.TEXT;
   }
}
