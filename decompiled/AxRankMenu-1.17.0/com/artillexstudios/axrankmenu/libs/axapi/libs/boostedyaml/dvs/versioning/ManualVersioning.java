package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.dvs.versioning;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.dvs.Pattern;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.dvs.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManualVersioning implements Versioning {
   private final Version documentVersion;
   private final Version defaultsVersion;

   public ManualVersioning(@NotNull Pattern pattern, @Nullable String documentVersionId, @NotNull String defaultsVersionId) {
      this.documentVersion = documentVersionId == null ? null : pattern.getVersion(documentVersionId);
      this.defaultsVersion = pattern.getVersion(defaultsVersionId);
   }

   @Nullable
   @Override
   public Version getDocumentVersion(@NotNull Section document, boolean defaults) {
      return defaults ? this.defaultsVersion : this.documentVersion;
   }

   @NotNull
   @Override
   public Version getFirstVersion() {
      return this.defaultsVersion.getPattern().getFirstVersion();
   }

   @Override
   public String toString() {
      return "ManualVersioning{documentVersion=" + this.documentVersion + ", defaultsVersion=" + this.defaultsVersion + '}';
   }
}
