package ac.grim.grimac.api.storage.event;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public final class CheckCatalogEvent {
   @Nullable
   private String stableKey;
   private int checkId;
   @Nullable
   private String display;
   @Nullable
   private String description;
   @Nullable
   private String introducedVersion;
   private long introducedAt;

   @Nullable
   public String stableKey() {
      return this.stableKey;
   }

   @NotNull
   public CheckCatalogEvent stableKey(@NotNull String v) {
      this.stableKey = v;
      return this;
   }

   public int checkId() {
      return this.checkId;
   }

   @NotNull
   public CheckCatalogEvent checkId(int v) {
      this.checkId = v;
      return this;
   }

   @Nullable
   public String display() {
      return this.display;
   }

   @NotNull
   public CheckCatalogEvent display(@Nullable String v) {
      this.display = v;
      return this;
   }

   @Nullable
   public String description() {
      return this.description;
   }

   @NotNull
   public CheckCatalogEvent description(@Nullable String v) {
      this.description = v;
      return this;
   }

   @Nullable
   public String introducedVersion() {
      return this.introducedVersion;
   }

   @NotNull
   public CheckCatalogEvent introducedVersion(@Nullable String v) {
      this.introducedVersion = v;
      return this;
   }

   public long introducedAt() {
      return this.introducedAt;
   }

   @NotNull
   public CheckCatalogEvent introducedAt(long v) {
      this.introducedAt = v;
      return this;
   }

   public void reset() {
      this.stableKey = null;
      this.checkId = 0;
      this.display = null;
      this.description = null;
      this.introducedVersion = null;
      this.introducedAt = 0L;
   }
}
