package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.updater;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.updater.operators.Merger;
import java.io.IOException;
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
