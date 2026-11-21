package ac.grim.grimac.api.storage.event;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public final class VerboseSchemaEvent {
   @Nullable
   private String schemaKey;
   private int flavor;
   private int checkId;
   private int version;
   @Nullable
   private byte[] layout;
   private long introducedAt;

   @Nullable
   public String schemaKey() {
      return this.schemaKey;
   }

   @NotNull
   public VerboseSchemaEvent schemaKey(@NotNull String v) {
      this.schemaKey = v;
      return this;
   }

   public int flavor() {
      return this.flavor;
   }

   @NotNull
   public VerboseSchemaEvent flavor(int v) {
      this.flavor = v;
      return this;
   }

   public int checkId() {
      return this.checkId;
   }

   @NotNull
   public VerboseSchemaEvent checkId(int v) {
      this.checkId = v;
      return this;
   }

   public int version() {
      return this.version;
   }

   @NotNull
   public VerboseSchemaEvent version(int v) {
      if (v < 1) {
         throw new IllegalArgumentException("version");
      } else {
         this.version = v;
         return this;
      }
   }

   @Nullable
   public byte[] layout() {
      return this.layout;
   }

   @NotNull
   public VerboseSchemaEvent layout(@NotNull byte[] v) {
      this.layout = v;
      return this;
   }

   public long introducedAt() {
      return this.introducedAt;
   }

   @NotNull
   public VerboseSchemaEvent introducedAt(long v) {
      this.introducedAt = v;
      return this;
   }

   public void reset() {
      this.schemaKey = null;
      this.flavor = 0;
      this.checkId = 0;
      this.version = 0;
      this.layout = null;
      this.introducedAt = 0L;
   }
}
