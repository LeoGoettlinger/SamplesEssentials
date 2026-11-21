package org.sethomegui.SetHomeGUI.libs.updater;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.SetHomeGUI.libs.settings.general.GeneralSettings;
import org.sethomegui.SetHomeGUI.libs.settings.updater.UpdaterSettings;
import org.sethomegui.SetHomeGUI.libs.updater.operators.Merger;

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
