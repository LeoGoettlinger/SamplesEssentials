package github.nighter.smartspawner.spawner.config;

import github.nighter.smartspawner.SmartSpawner;
import java.io.File;

final class SupersededConfigNotice {
   private SupersededConfigNotice() {
   }

   static void warn(SmartSpawner plugin, String currentName, String legacyName) {
      File legacy = new File(plugin.getDataFolder(), legacyName);
      if (legacy.exists()) {
         plugin.getLogger().warning(legacyName + " was replaced by " + currentName + " in this version.");
         plugin.getLogger()
            .warning("Your old file has been left alone and is no longer read. Any changes you made in it need copying into " + currentName + " by hand.");
      }
   }
}
