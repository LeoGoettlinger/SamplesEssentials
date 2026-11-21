package org.sethomegui.SetHomeGUI.libs.dvs.versioning;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.SetHomeGUI.libs.dvs.Pattern;
import org.sethomegui.SetHomeGUI.libs.dvs.Version;

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
