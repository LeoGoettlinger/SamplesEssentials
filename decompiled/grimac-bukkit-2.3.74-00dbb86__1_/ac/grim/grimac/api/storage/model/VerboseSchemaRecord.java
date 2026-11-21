package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.api.storage.codec.Id;
import ac.grim.grimac.api.storage.codec.Indexed;
import ac.grim.grimac.api.storage.codec.MergeMin;
import ac.grim.grimac.api.storage.codec.Name;
import ac.grim.grimac.api.storage.codec.Persistent;
import ac.grim.grimac.api.storage.codec.Value;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@Persistent
@ApiStatus.Experimental
public record VerboseSchemaRecord(
   @Id @NotNull String schemaKey,
   @Indexed @Name("flavor") int flavor,
   @Indexed @Name("check_id") int checkId,
   @Value @Name("verbose_version") int version,
   @Value @Name("layout") @NotNull byte[] layout,
   @Value @Name("introduced_at") @MergeMin long introducedAt
) {
   public VerboseSchemaRecord(@NotNull String schemaKey, int flavor, int checkId, int version, byte[] layout, long introducedAt) {
      if (schemaKey == null || schemaKey.isEmpty()) {
         throw new IllegalArgumentException("schemaKey");
      } else if (version < 1) {
         throw new IllegalArgumentException("version");
      } else if (layout == null) {
         throw new IllegalArgumentException("layout");
      } else {
         this.schemaKey = schemaKey;
         this.flavor = flavor;
         this.checkId = checkId;
         this.version = version;
         this.layout = layout;
         this.introducedAt = introducedAt;
      }
   }

   @NotNull
   public static String keyOf(int flavor, int checkId, int version) {
      return flavor + ":" + checkId + ":" + version;
   }
}
