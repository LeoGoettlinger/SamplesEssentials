package org.evenmorefish.fish.libs.boostedyaml.updater;

import java.io.IOException;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.boostedyaml.settings.general.GeneralSettings;
import org.evenmorefish.fish.libs.boostedyaml.settings.updater.UpdaterSettings;
import org.evenmorefish.fish.libs.boostedyaml.updater.operators.Merger;
import org.jetbrains.annotations.NotNull;

public class Updater {
   public static void update(
      @NotNull Section document, @NotNull Section defaults, @NotNull UpdaterSettings updaterSettings, @NotNull GeneralSettings generalSettings
   ) throws IOException {
      if (!VersionedOperations.run(document, defaults, updaterSettings, generalSettings.getRouteSeparator())) {
         Merger.merge(document, defaults, updaterSettings);
         if (updaterSettings.getVersioning() != null) {
            updaterSettings.getVersioning().updateVersionID(document, defaults);
         }

         if (updaterSettings.isAutoSave()) {
            document.getRoot().save();
         }
      }
   }
}
