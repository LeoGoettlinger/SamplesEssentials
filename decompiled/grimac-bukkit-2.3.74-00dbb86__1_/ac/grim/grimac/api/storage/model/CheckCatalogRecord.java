package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.api.storage.codec.Id;
import ac.grim.grimac.api.storage.codec.Indexed;
import ac.grim.grimac.api.storage.codec.InsertOnly;
import ac.grim.grimac.api.storage.codec.MergeMin;
import ac.grim.grimac.api.storage.codec.Name;
import ac.grim.grimac.api.storage.codec.Nullable;
import ac.grim.grimac.api.storage.codec.Persistent;
import ac.grim.grimac.api.storage.codec.Value;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@Persistent
@ApiStatus.Experimental
public record CheckCatalogRecord(
   @Id @NotNull String stableKey,
   @Indexed @Name("check_id") @InsertOnly int checkId,
   @Value @Nullable String display,
   @Value @Nullable String description,
   @Value @Name("introduced_version") @Nullable String introducedVersion,
   @Value @Name("introduced_at") @MergeMin long introducedAt
) {
   public CheckCatalogRecord(@NotNull String stableKey, int checkId, String display, String description, String introducedVersion, long introducedAt) {
      if (stableKey == null) {
         throw new IllegalArgumentException("stableKey");
      } else if (stableKey.isEmpty()) {
         throw new IllegalArgumentException("stableKey must not be empty");
      } else {
         this.stableKey = stableKey;
         this.checkId = checkId;
         this.display = display;
         this.description = description;
         this.introducedVersion = introducedVersion;
         this.introducedAt = introducedAt;
      }
   }
}
