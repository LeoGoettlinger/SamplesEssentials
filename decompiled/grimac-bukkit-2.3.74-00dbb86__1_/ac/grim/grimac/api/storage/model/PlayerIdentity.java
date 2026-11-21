package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.api.storage.codec.Id;
import ac.grim.grimac.api.storage.codec.Indexed;
import ac.grim.grimac.api.storage.codec.MergeMax;
import ac.grim.grimac.api.storage.codec.MergeMin;
import ac.grim.grimac.api.storage.codec.Name;
import ac.grim.grimac.api.storage.codec.Nullable;
import ac.grim.grimac.api.storage.codec.Persistent;
import ac.grim.grimac.api.storage.codec.Value;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.UUID;

@Persistent
@ApiStatus.Experimental
public record PlayerIdentity(
   @Id UUID uuid,
   @Indexed @Nullable String currentName,
   @Value @MergeMin @Name("first_seen") long firstSeenEpochMs,
   @Value @MergeMax @Name("last_seen") long lastSeenEpochMs
) {
   public PlayerIdentity(UUID uuid, String currentName, long firstSeenEpochMs, long lastSeenEpochMs) {
      if (uuid == null) {
         throw new IllegalArgumentException("uuid");
      } else {
         this.uuid = uuid;
         this.currentName = currentName;
         this.firstSeenEpochMs = firstSeenEpochMs;
         this.lastSeenEpochMs = lastSeenEpochMs;
      }
   }
}
