package me.chancesd.pvpmanager.sdutils.updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import org.bukkit.plugin.Plugin;

public class SpigotUpdater extends Updater {
   public SpigotUpdater(Plugin plugin, int id, Updater.UpdateType type) {
      super(plugin, id, type, plugin.getName());
   }

   @Override
   protected boolean read() {
      try {
         try (
            InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.getId()).openStream();
            Scanner scanner = new Scanner(inputStream);
         ) {
            if (scanner.hasNext()) {
               this.versionName = scanner.next();
            }
         }

         return true;
      } catch (IOException var9) {
         Log.warning("Couldn't check for spigot updates, check your internet connection");
         this.setResult(Updater.UpdateResult.FAIL_DBO);
         return false;
      }
   }

   @Override
   public final boolean downloadFile() {
      return false;
   }

   @Override
   public String getUpdateLink() {
      return "https://www.spigotmc.org/resources/" + this.getPlugin().getName().toLowerCase() + "." + this.getId();
   }
}
