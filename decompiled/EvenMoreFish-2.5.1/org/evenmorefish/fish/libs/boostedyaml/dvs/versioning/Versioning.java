package org.evenmorefish.fish.libs.boostedyaml.dvs.versioning;

import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.boostedyaml.dvs.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Versioning {
   @Nullable
   Version getDocumentVersion(@NotNull Section var1, boolean var2);

   @NotNull
   Version getFirstVersion();

   default void updateVersionID(@NotNull Section updated, @NotNull Section def) {
   }
}
