package me.chancesd.pvpmanager.sdutils.updater;

import java.io.File;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class Updater {
   protected String versionName;
   protected String versionLink;
   protected String versionType;
   protected String versionGameVersion;
   private static final String[] TAGS = new String[]{"-DEV", "-PRE", "-SNAPSHOT", "-BETA"};
   private Updater.UpdateResult result = Updater.UpdateResult.SUCCESS;
   private final Thread thread;
   private final Plugin plugin;
   private final int id;
   private final Updater.UpdateType type;
   private final File file;
   private final String projectSlug;

   protected Updater(Plugin plugin, int id, Updater.UpdateType type, String projectSlug) {
      this.plugin = plugin;
      this.id = id;
      this.type = type;
      this.projectSlug = projectSlug;
      if (!Bukkit.getUpdateFolderFile().exists()) {
         Bukkit.getUpdateFolderFile().mkdirs();
      }

      this.file = new File(Bukkit.getUpdateFolderFile(), plugin.getName() + ".jar");
      this.thread = new Thread(this::runUpdater);
   }

   public final Updater.UpdateResult getResult() {
      this.waitForThread();
      return this.result;
   }

   protected final void runUpdater() {
      if (this.read()) {
         if (!this.versionName.matches("^\\d.*")) {
            this.setResult(Updater.UpdateResult.FAIL_NOVERSION);
         } else {
            if (this.versionCheck(this.versionName)) {
               if (this.versionLink != null && this.getType() == Updater.UpdateType.DOWNLOAD) {
                  try {
                     this.downloadFile();
                  } catch (Exception var2) {
                     Log.warning("The auto-updater tried to download a new update, but was unsuccessful.");
                     this.setResult(Updater.UpdateResult.FAIL_DOWNLOAD);
                  }
               } else {
                  this.setResult(Updater.UpdateResult.UPDATE_AVAILABLE);
               }
            } else {
               this.setResult(Updater.UpdateResult.NO_UPDATE);
            }
         }
      }
   }

   protected final void waitForThread() {
      if (this.thread.isAlive()) {
         try {
            this.thread.join();
         } catch (InterruptedException var2) {
            var2.printStackTrace();
            Thread.currentThread().interrupt();
         }
      }
   }

   protected final String[] getVersionArray(String version) {
      String[] versionArray;
      if (this.hasTag(version)) {
         versionArray = version.replaceAll("(-.+)", "").split("\\.");
      } else {
         versionArray = version.split("\\.");
      }

      return versionArray;
   }

   protected boolean hasTag(String version) {
      for (String string : TAGS) {
         if (version.contains(string)) {
            return true;
         }
      }

      return false;
   }

   protected final boolean versionCheck(String remoteVersion) {
      String version = this.plugin.getDescription().getVersion();
      if (remoteVersion.equalsIgnoreCase(version)) {
         return false;
      } else {
         String[] remote = this.getVersionArray(remoteVersion);
         String[] local = this.getVersionArray(version);
         int length = Math.max(local.length, remote.length);

         try {
            for (int i = 0; i < length; i++) {
               int localNumber = i < local.length ? Integer.parseInt(local[i]) : 0;
               int remoteNumber = i < remote.length ? Integer.parseInt(remote[i]) : 0;
               if (remoteNumber > localNumber) {
                  return true;
               }

               if (remoteNumber < localNumber) {
                  return false;
               }
            }
         } catch (NumberFormatException var9) {
            Log.warning("Error reading version number!");
         }

         return this.hasTag(version);
      }
   }

   protected final Thread getThread() {
      return this.thread;
   }

   protected final Plugin getPlugin() {
      return this.plugin;
   }

   protected final int getId() {
      return this.id;
   }

   public final Updater.UpdateType getType() {
      return this.type;
   }

   protected final File getFile() {
      return this.file;
   }

   public String getProjectSlug() {
      return this.projectSlug;
   }

   protected final void setResult(Updater.UpdateResult result) {
      this.result = result;
   }

   public final String getLatestName() {
      this.waitForThread();
      return this.versionName;
   }

   public final String getLatestFileLink() {
      this.waitForThread();
      return this.versionLink;
   }

   public final String getLatestType() {
      this.waitForThread();
      return this.versionType;
   }

   public final String getLatestGameVersion() {
      this.waitForThread();
      return this.versionGameVersion;
   }

   public final Updater check() {
      this.thread.start();
      return this;
   }

   public abstract String getUpdateLink();

   protected abstract boolean read();

   public abstract boolean downloadFile();

   public static enum UpdateResult {
      SUCCESS,
      NO_UPDATE,
      FAIL_DOWNLOAD,
      FAIL_DBO,
      FAIL_NOVERSION,
      FAIL_BADID,
      UPDATE_AVAILABLE;
   }

   public static enum UpdateType {
      VERSION_CHECK,
      DOWNLOAD;
   }
}
