package org.sethomegui.SetHomeGUI.libs.dvs.versioning;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.SetHomeGUI.libs.dvs.Version;

public interface Versioning {
   @Nullable
   Version getDocumentVersion(@NotNull Section var1, boolean var2);

   @NotNull
   Version getFirstVersion();

   default void updateVersionID(@NotNull Section updated, @NotNull Section def) {
   }
}
