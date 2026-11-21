package ac.grim.grimac.api.storage.event;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@ApiStatus.Experimental
public final class PlayerIdentityEvent {
   private UUID uuid;
   @Nullable
   private String currentName;
   private long firstSeenEpochMs;
   private long lastSeenEpochMs;

   @NotNull
   public UUID uuid() {
      return this.uuid;
   }

   @NotNull
   public PlayerIdentityEvent uuid(@NotNull UUID v) {
      this.uuid = v;
      return this;
   }

   @Nullable
   public String currentName() {
      return this.currentName;
   }

   @NotNull
   public PlayerIdentityEvent currentName(@Nullable String v) {
      this.currentName = v;
      return this;
   }

   public long firstSeenEpochMs() {
      return this.firstSeenEpochMs;
   }

   @NotNull
   public PlayerIdentityEvent firstSeenEpochMs(long v) {
      this.firstSeenEpochMs = v;
      return this;
   }

   public long lastSeenEpochMs() {
      return this.lastSeenEpochMs;
   }

   @NotNull
   public PlayerIdentityEvent lastSeenEpochMs(long v) {
      this.lastSeenEpochMs = v;
      return this;
   }

   public void reset() {
      this.uuid = null;
      this.currentName = null;
      this.firstSeenEpochMs = 0L;
      this.lastSeenEpochMs = 0L;
   }
}
