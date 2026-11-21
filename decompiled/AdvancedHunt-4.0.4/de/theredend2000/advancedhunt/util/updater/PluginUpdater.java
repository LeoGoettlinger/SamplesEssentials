package de.theredend2000.advancedhunt.util.updater;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.util.VersionComparator;
import de.theredend2000.advancedhunt.util.updater.source.BukkitSource;
import de.theredend2000.advancedhunt.util.updater.source.ModrinthSource;
import de.theredend2000.advancedhunt.util.updater.source.SpigotSource;
import java.io.File;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginUpdater {
   private static final Duration MINIMUM_RELEASE_AGE = Duration.ofDays(3L);
   private final Main plugin;
   private final Map<String, UpdateSource> sources;
   private final Map<String, PluginUpdater.TrackedPlugin> trackedPlugins;
   private final VersionComparator versionComparator;
   private final boolean paperOrPurpur;
   private final boolean above1_19;
   private final File pluginsDir;
   private final File updateDir;
   private final File oldPluginsDir;
   private final Set<String> activeDownloads = ConcurrentHashMap.newKeySet();
   private static final boolean IS_PAPER_OR_PURPUR = detectPaperOrPurpur();

   public PluginUpdater(Main plugin) {
      this.plugin = plugin;
      this.sources = new HashMap<>();
      this.trackedPlugins = new HashMap<>();
      this.versionComparator = new VersionComparator();
      this.paperOrPurpur = IS_PAPER_OR_PURPUR;
      this.above1_19 = this.detectAbove1_19();
      this.pluginsDir = Bukkit.getUpdateFolderFile().getParentFile();
      this.updateDir = Bukkit.getUpdateFolderFile();
      this.oldPluginsDir = new File(this.pluginsDir, "OLD_PLUGINS");
      this.initializeDirectories();
      this.registerSource(new ModrinthSource());
      this.registerSource(new SpigotSource());
      this.registerSource(new BukkitSource());
   }

   private void initializeDirectories() {
      if (!this.updateDir.exists()) {
         this.updateDir.mkdirs();
      }

      if (!this.oldPluginsDir.exists()) {
         this.oldPluginsDir.mkdirs();
      }
   }

   public void registerSource(UpdateSource source) {
      this.sources.put(source.getName(), source);
   }

   public void trackPlugin(String name, String currentVersion, Map<String, String> ids) {
      this.trackedPlugins.put(name, new PluginUpdater.TrackedPlugin(name, currentVersion, ids));
   }

   public void checkForUpdates() {
      for (PluginUpdater.TrackedPlugin tracked : this.trackedPlugins.values()) {
         this.checkUpdatesFor(tracked);
      }
   }

   private void checkUpdatesFor(PluginUpdater.TrackedPlugin tracked) {
      for (Entry<String, String> entry : tracked.ids.entrySet()) {
         String sourceName = entry.getKey();
         String id = entry.getValue();
         UpdateSource source = this.sources.get(sourceName);
         if (source != null && id != null && !id.isEmpty()) {
            source.getLatestUpdate(id)
               .thenAccept(
                  update -> {
                     if (update != null) {
                        if (this.versionComparator.isGreaterThan(update.version(), tracked.currentVersion)) {
                           Instant publishedAt = update.publishedAt();
                           if (publishedAt == null) {
                              this.plugin
                                 .getLogger()
                                 .info(
                                    "["
                                       + tracked.name
                                       + "] Skipping automatic download from "
                                       + sourceName
                                       + " for version "
                                       + update.version()
                                       + " because the release date is unavailable."
                                 );
                           } else if (!this.isOldEnoughToAutoDownload(publishedAt)) {
                              this.plugin
                                 .getLogger()
                                 .info(
                                    "["
                                       + tracked.name
                                       + "] Skipping automatic download from "
                                       + sourceName
                                       + " for version "
                                       + update.version()
                                       + " because it is newer than "
                                       + MINIMUM_RELEASE_AGE.toDays()
                                       + " days (published at "
                                       + publishedAt
                                       + ")."
                                 );
                           } else if (!this.activeDownloads.add(tracked.name)) {
                              this.plugin
                                 .getLogger()
                                 .info("[" + tracked.name + "] Download already in progress from another source. Skipping " + sourceName + ".");
                           } else {
                              this.plugin.getLogger().info("[" + tracked.name + "] Found a new version on " + sourceName + ": " + update.version());
                              this.downloadUpdate(tracked, sourceName, update);
                           }
                        }
                     }
                  }
               );
         }
      }
   }

   private boolean isOldEnoughToAutoDownload(Instant publishedAt) {
      Instant threshold = Instant.now().minus(MINIMUM_RELEASE_AGE);
      return !publishedAt.isAfter(threshold);
   }

   public void downloadUpdate(PluginUpdater.TrackedPlugin tracked, String sourceName, UpdateInfo update) {
      UpdateSource source = this.sources.get(sourceName);
      String id = tracked.ids.get(sourceName);
      if (source != null && id != null) {
         this.moveOldVersion(tracked.name);
         String filename = tracked.name + "-" + update.version() + ".jar";
         File currentFile = this.resolvePluginFile(tracked.name);
         File targetDir = this.determineTargetDirectory(tracked.name, filename, currentFile);
         if (targetDir != null) {
            if (!targetDir.exists()) {
               targetDir.mkdirs();
            }

            File destination = new File(targetDir, filename);
            if (destination.exists()) {
               this.plugin.getLogger().info("[" + tracked.name + "] Update " + update.version() + " is already downloaded.");
            } else {
               source.downloadPlugin(id, update, destination).thenAccept(success -> {
                  try {
                     if (!success) {
                        this.plugin.getLogger().warning("[" + tracked.name + "] Failed to download update from " + sourceName);
                        if (destination.exists()) {
                           destination.delete();
                        }

                        return;
                     }

                     this.plugin.getLogger().info("[" + tracked.name + "] Downloaded " + filename + " to " + targetDir.getName());
                     if (currentFile != null) {
                        if (this.paperOrPurpur && this.above1_19) {
                           this.plugin.getLogger().info("[" + tracked.name + "] Update will be applied on next restart.");
                           return;
                        }

                        this.handleFileOrder(tracked.name, filename, destination, currentFile);
                        return;
                     }

                     this.loadPlugin(tracked.name, destination);
                  } finally {
                     this.activeDownloads.remove(tracked.name);
                  }
               });
            }
         }
      }
   }

   private File determineTargetDirectory(String pluginName, String filename, File currentFile) {
      if (this.paperOrPurpur && this.above1_19 && currentFile != null) {
         return this.updateDir;
      } else if (currentFile != null && !this.isVersionedFileNameFirst(filename, currentFile.getName())) {
         this.plugin.getLogger().warning("Cannot continue with automatic download of " + pluginName + ": new filename would not load before the current one.");
         return null;
      } else {
         return this.pluginsDir;
      }
   }

   private boolean isVersionedFileNameFirst(String filename1, String filename2) {
      String v1 = this.extractVersionFromFilename(filename1);
      String v2 = this.extractVersionFromFilename(filename2);
      return this.compareVersionSegments(v1, v2) > 0;
   }

   private String extractVersionFromFilename(String filename) {
      int dashIndex = filename.lastIndexOf(45);
      int dotJarIndex = filename.lastIndexOf(".jar");
      return dashIndex != -1 && dotJarIndex != -1 && dashIndex < dotJarIndex ? filename.substring(dashIndex + 1, dotJarIndex) : "";
   }

   private int compareVersionSegments(String version1, String version2) {
      String[] parts1 = version1.split("\\.");
      String[] parts2 = version2.split("\\.");
      int length = Math.max(parts1.length, parts2.length);

      for (int i = 0; i < length; i++) {
         int v1 = i < parts1.length ? this.parseIntSafe(parts1[i]) : 0;
         int v2 = i < parts2.length ? this.parseIntSafe(parts2[i]) : 0;
         if (v1 != v2) {
            return Integer.compare(v1, v2);
         }
      }

      return 0;
   }

   private int parseIntSafe(String s) {
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException var3) {
         return 0;
      }
   }

   private void handleFileOrder(String pluginName, String filename, File newFile, File currentFile) {
      File[] files = this.pluginsDir.listFiles();
      if (files != null) {
         boolean isFirst = true;

         for (File f : files) {
            if (f.getName().endsWith(".jar") && f.getName().compareToIgnoreCase(filename) < 0 && !f.getName().equalsIgnoreCase(currentFile.getName())) {
               isFirst = false;
               break;
            }
         }

         if (!isFirst) {
            this.plugin.getLogger().warning("[" + pluginName + "] Downloaded plugin may not load first due to file ordering.");
         }
      }
   }

   private void moveOldVersion(String pluginName) {
      File currentFile = this.resolvePluginFile(pluginName);
      if (currentFile != null && currentFile.exists()) {
         File[] files = this.pluginsDir.listFiles();
         if (files != null) {
            String prefix = pluginName + "-";

            for (File f : files) {
               if (f.getName().startsWith(prefix) && f.getName().endsWith(".jar") && !f.getName().equals(currentFile.getName())) {
                  File dest = new File(this.oldPluginsDir, f.getName());
                  if (f.renameTo(dest)) {
                     this.plugin.getLogger().info("Moved old version " + f.getName() + " to OLD_PLUGINS");
                  } else {
                     this.plugin.getLogger().severe("Failed to move old version " + f.getName() + " to OLD_PLUGINS");
                  }
               }
            }
         }
      }
   }

   private void loadPlugin(String pluginName, File pluginFile) {
      Runnable loadTask = () -> {
         if (!pluginFile.exists()) {
            this.plugin.getLogger().severe("[" + pluginName + "] Cannot find plugin file: " + pluginFile.getAbsolutePath());
         } else {
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin existing = pluginManager.getPlugin(pluginName);
            if (existing != null) {
               pluginManager.disablePlugin(existing);
               this.plugin.getLogger().info("[" + pluginName + "] Disabled existing instance before loading new version.");
            }

            try {
               Plugin loadedPlugin = pluginManager.loadPlugin(pluginFile);
               if (loadedPlugin != null) {
                  pluginManager.enablePlugin(loadedPlugin);
                  this.plugin.getLogger().info("[" + pluginName + "] Successfully loaded and enabled plugin.");
               } else {
                  this.plugin.getLogger().severe("[" + pluginName + "] Failed to load plugin from " + pluginFile.getName());
               }
            } catch (InvalidDescriptionException | InvalidPluginException var6) {
               this.plugin.getLogger().log(Level.SEVERE, "[" + pluginName + "] Failed to load plugin", (Throwable)var6);
            }
         }
      };
      if (Bukkit.isPrimaryThread()) {
         loadTask.run();
      } else if (this.plugin.isEnabled()) {
         Bukkit.getScheduler().runTask(this.plugin, loadTask);
      } else {
         this.plugin.getLogger().info("[" + pluginName + "] Download complete. Will be loaded on next restart.");
      }
   }

   private static boolean detectPaperOrPurpur() {
      try {
         Class.forName("com.destroystokyo.paper.PaperConfig");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      } catch (Throwable var2) {
         return false;
      }
   }

   private boolean detectAbove1_19() {
      return this.versionComparator.isGreaterThanOrEqual(Bukkit.getBukkitVersion(), "1.19");
   }

   private File resolvePluginFile(String pluginName) {
      Plugin target = Bukkit.getPluginManager().getPlugin(pluginName);
      if (target instanceof JavaPlugin) {
         try {
            Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
            getFileMethod.setAccessible(true);
            return (File)getFileMethod.invoke(target);
         } catch (Exception var4) {
            this.plugin.getLogger().warning("Could not resolve jar file for " + pluginName + ": " + var4.getMessage());
         }
      }

      return null;
   }

   private static class TrackedPlugin {
      String name;
      String currentVersion;
      Map<String, String> ids;

      public TrackedPlugin(String name, String currentVersion, Map<String, String> ids) {
         this.name = name;
         this.currentVersion = currentVersion;
         this.ids = ids;
      }
   }
}
