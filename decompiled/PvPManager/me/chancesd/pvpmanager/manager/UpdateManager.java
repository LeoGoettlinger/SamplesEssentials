package me.chancesd.pvpmanager.manager;

import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.updater.BukkitUpdater;
import me.chancesd.pvpmanager.sdutils.updater.SpigotUpdater;
import me.chancesd.pvpmanager.sdutils.updater.Updater;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import org.bukkit.Bukkit;

public class UpdateManager {
   private final PvPManager plugin;
   private Updater updater;
   private String newVersion;
   private final String currentVersion;
   private boolean updateAvailable;

   public UpdateManager(PvPManager plugin) {
      this.plugin = plugin;
      this.currentVersion = plugin.getDescription().getVersion();
      this.setup();
   }

   private void setup() {
      if (Conf.CHECK_UPDATES.asBool()) {
         ScheduleUtils.runAsyncTimer(this::checkForUpdates, 3L, 18000L, TimeUnit.SECONDS);
      }
   }

   public void checkForUpdates() {
      Log.info("Checking for updates...");
      this.updater = new BukkitUpdater(this.plugin, 63773, Updater.UpdateType.VERSION_CHECK).check();
      if (this.updater.getResult() != Updater.UpdateResult.UPDATE_AVAILABLE) {
         this.updater = new SpigotUpdater(this.plugin, 845, Updater.UpdateType.VERSION_CHECK).check();
      }

      if (this.updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
         this.setNewVersion(this.updater.getLatestName());
         String updateMsg = Lang.PREFIX + " §aUpdate available: §c" + this.getNewVersion() + " §aCurrent version: §c" + this.getCurrentversion();
         Lang.queueAdminMsg(updateMsg);
         Bukkit.broadcast(updateMsg, Permissions.ADMIN.getPermission());
         if (Conf.AUTO_UPDATE.asBool()) {
            if (this.updater.downloadFile()) {
               Lang.queueAdminMsg(Lang.PREFIX + " §aUpdate downloaded, it will be applied automatically on the next server restart");
               Bukkit.broadcast(
                  Lang.PREFIX + " §aUpdate downloaded to update folder, it will be applied automatically on the next restart",
                  Permissions.ADMIN.getPermission()
               );
               return;
            }

            Log.info("Could not download latest update. Please update manually from one of the links below.");
         }

         this.updateAvailable = true;
         String linkMsg = Lang.PREFIX
            + " §aFollow one of the links to download: \n§7- https://modrinth.com/plugin/pvpmanager\n§7- https://dev.bukkit.org/projects/pvpmanager";
         Lang.queueAdminMsg(linkMsg);
         Bukkit.broadcast(linkMsg, Permissions.ADMIN.getPermission());
      } else {
         Log.info("No update found");
      }
   }

   public String getCurrentversion() {
      return this.currentVersion;
   }

   public String getNewVersion() {
      return this.newVersion;
   }

   public void setNewVersion(String newVersion) {
      this.newVersion = newVersion;
   }

   public boolean hasUpdateAvailable() {
      return this.updateAvailable;
   }

   public void setUpdateAvailable(boolean updateAvailable) {
      this.updateAvailable = updateAvailable;
   }

   public Updater getUpdater() {
      return this.updater;
   }
}
