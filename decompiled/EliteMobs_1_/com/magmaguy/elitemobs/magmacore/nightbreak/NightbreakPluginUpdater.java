package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class NightbreakPluginUpdater {
   public static final String AUTO_DOWNLOAD_CONFIG_PATH = "nightbreak.autoDownloadPluginUpdates";
   private static final long UPDATE_REFRESH_INTERVAL_TICKS = 72000L;
   private static final long INITIAL_UPDATE_REFRESH_DELAY_TICKS = 6000L;
   private static final long BOOT_STAGGER_MAX_TICKS = 2400L;
   private static final long AUTO_PLUGIN_UPDATE_EXTRA_DELAY_TICKS = 600L;
   private static final long AUTO_CONTENT_UPDATE_EXTRA_DELAY_TICKS = 1800L;
   private static final int SPIGOT_FALLBACK_TIMEOUT_MS = 3000;
   private static final String SPIGOT_UPDATE_BASE_URL = "https://api.spigotmc.org";
   private static final Object SPIGOT_TEST_BASE_URL_LOCK = new Object();
   private static volatile String scopedSpigotTestBaseUrl;
   private static final AtomicInteger PRODUCTION_SPIGOT_ORIGIN_SELECTIONS = new AtomicInteger();
   private static final List<String> AUTO_DOWNLOAD_CONFIG_COMMENTS = List.of(
      "When true, this plugin automatically downloads available plugin updates",
      "and content update files on startup. Downloaded plugin and content",
      "updates are used after the server restarts, unless",
      "nightbreak.applyPluginUpdatesWithoutRestart applies plugin updates sooner.",
      "Automatic plugin downloads require a valid account token and an active supporter",
      "Patreon membership. Leave this false if you prefer to use the in-game update button."
   );
   private static final List<String> RESTART_ONLY_AUTO_DOWNLOAD_CONFIG_COMMENTS = List.of(
      "When true, this plugin automatically downloads available plugin updates",
      "and content update files on startup. Downloaded plugin and content",
      "updates are used after the server restarts.",
      "Automatic plugin downloads require a valid account token and an active supporter",
      "Patreon membership. Leave this false if you prefer to use the in-game update button."
   );
   private static final Map<String, NightbreakPluginUpdater.PluginUpdateCheck> CACHED_UPDATE_CHECKS = new ConcurrentHashMap<>();
   private static final Map<String, Long> CACHED_UPDATE_CHECK_TIMES = new ConcurrentHashMap<>();
   private static final Set<String> RUNNING_UPDATE_CHECKS = ConcurrentHashMap.newKeySet();
   private static final Set<String> RUNNING_PLUGIN_DOWNLOADS = ConcurrentHashMap.newKeySet();
   private static final Map<String, Integer> UPDATE_CHECK_TASKS = new ConcurrentHashMap<>();
   private static final Map<String, Integer> AUTO_PLUGIN_UPDATE_TASKS = new ConcurrentHashMap<>();
   private static final Map<String, Integer> AUTO_CONTENT_UPDATE_TASKS = new ConcurrentHashMap<>();
   private static final Set<String> RUNNING_AUTO_CONTENT_UPDATES = ConcurrentHashMap.newKeySet();
   private static final Object PLUGIN_LIFECYCLE_LOCK = new Object();
   private static final Map<JavaPlugin, Long> PLUGIN_GENERATIONS = new IdentityHashMap<>();
   private static final Map<JavaPlugin, CopyOnWriteArrayList<NightbreakPluginUpdater.PluginUpdateListenerRegistration>> PLUGIN_UPDATE_LISTENERS = new IdentityHashMap<>();
   private static final NightbreakPluginAsyncWorkTracker ASYNC_WORK = new NightbreakPluginAsyncWorkTracker();

   private NightbreakPluginUpdater() {
   }

   public static NightbreakPluginUpdater.ListenerRegistration onPluginUpdateDownloaded(
      JavaPlugin plugin, Consumer<NightbreakPluginUpdater.PluginUpdateDownload> listener
   ) {
      Objects.requireNonNull(plugin, "plugin");
      Objects.requireNonNull(listener, "listener");
      NightbreakPluginUpdater.PluginUpdateListenerRegistration registration = new NightbreakPluginUpdater.PluginUpdateListenerRegistration(plugin, listener);
      synchronized (PLUGIN_LIFECYCLE_LOCK) {
         PLUGIN_UPDATE_LISTENERS.computeIfAbsent(plugin, ignored -> new CopyOnWriteArrayList<>()).add(registration);
         return registration;
      }
   }

   public static void shutdown(JavaPlugin plugin) {
      if (plugin != null) {
         NightbreakChangelogManager.shutdown(plugin);
         String pluginName = plugin.getName();
         List<NightbreakPluginAsyncWorkTracker.Work> activeWork;
         synchronized (PLUGIN_LIFECYCLE_LOCK) {
            PLUGIN_GENERATIONS.put(plugin, currentGenerationLocked(plugin) + 1L);
            CopyOnWriteArrayList<NightbreakPluginUpdater.PluginUpdateListenerRegistration> registrations = PLUGIN_UPDATE_LISTENERS.remove(plugin);
            if (registrations != null) {
               registrations.forEach(NightbreakPluginUpdater.PluginUpdateListenerRegistration::markClosedFromShutdown);
            }

            activeWork = ASYNC_WORK.snapshot(plugin);
         }

         String keyPrefix = pluginName.toLowerCase(Locale.ROOT) + ":";
         cancelTasksForPlugin(UPDATE_CHECK_TASKS, keyPrefix);
         cancelTasksForPlugin(AUTO_PLUGIN_UPDATE_TASKS, keyPrefix);
         cancelTasksForPlugin(AUTO_CONTENT_UPDATE_TASKS, keyPrefix);
         RUNNING_AUTO_CONTENT_UPDATES.removeIf(key -> key.startsWith(keyPrefix));
         CACHED_UPDATE_CHECKS.keySet().removeIf(key -> key.startsWith(keyPrefix));
         CACHED_UPDATE_CHECK_TIMES.keySet().removeIf(key -> key.startsWith(keyPrefix));
         activeWork.forEach(NightbreakPluginAsyncWorkTracker.Work::requestShutdown);
         ASYNC_WORK.awaitQuiescence(pluginName, activeWork);
      }
   }

   public static boolean setAutoDownloadConfigDefault(FileConfiguration fileConfiguration) {
      return setAutoDownloadConfigDefault(fileConfiguration, true);
   }

   public static boolean setAutoDownloadConfigDefault(FileConfiguration fileConfiguration, boolean offerRestartFreeUpdates) {
      boolean value = fileConfiguration.getBoolean("nightbreak.autoDownloadPluginUpdates", false);
      writeSharedKeys(fileConfiguration, offerRestartFreeUpdates);
      return value;
   }

   private static boolean writeSharedKeys(FileConfiguration configuration, boolean offerRestartFreeUpdates) {
      boolean changed = writeSharedKey(
         configuration,
         "nightbreak.autoDownloadPluginUpdates",
         false,
         offerRestartFreeUpdates ? AUTO_DOWNLOAD_CONFIG_COMMENTS : RESTART_ONLY_AUTO_DOWNLOAD_CONFIG_COMMENTS
      );
      if (offerRestartFreeUpdates) {
         changed |= writeSharedKey(configuration, "nightbreak.applyPluginUpdatesWithoutRestart", "never", NightbreakPluginHotSwap.APPLY_MODE_CONFIG_COMMENTS);
      }

      return changed;
   }

   private static boolean writeSharedKey(FileConfiguration configuration, String path, Object defaultValue, List<String> comments) {
      configuration.addDefault(path, defaultValue);
      boolean changed = false;
      if (!configuration.isSet(path)) {
         configuration.set(path, defaultValue);
         changed = true;
      }

      if (!sameComments(configuration.getComments(path), comments)) {
         configuration.setComments(path, comments);
         changed = true;
      }

      return changed;
   }

   private static boolean sameComments(List<String> current, List<String> expected) {
      if (current != null && current.size() == expected.size()) {
         for (int i = 0; i < expected.size(); i++) {
            String line = current.get(i);
            if (line == null || !line.trim().equals(expected.get(i).trim())) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static void ensureAutoDownloadConfigDefault(JavaPlugin plugin) {
      plugin.reloadConfig();
      FileConfiguration config = plugin.getConfig();
      if (writeSharedKeys(config, NightbreakPluginHotSwap.offersRestartFreeUpdates(plugin))) {
         plugin.saveConfig();
      }
   }

   public static void autoDownloadPluginUpdateIfEnabled(JavaPlugin plugin, NightbreakPluginSpec spec) {
      ensureAutoDownloadConfigDefault(plugin);
      NightbreakChangelogManager.initialize(plugin, spec);
      startPluginUpdateMonitor(plugin, spec);
      if (isAutoDownloadPluginUpdatesEnabled(plugin)) {
         String key = cacheKey(plugin, spec);
         long generation = currentGeneration(plugin);
         AtomicInteger scheduledTaskId = new AtomicInteger(-1);
         int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            AUTO_PLUGIN_UPDATE_TASKS.remove(key, scheduledTaskId.get());
            if (isCurrentGeneration(plugin, generation) && plugin.isEnabled()) {
               plugin.getLogger().info("Automatic update downloads are enabled.");
               downloadPluginUpdateAsync(plugin, spec, Bukkit.getConsoleSender(), result -> {
                  if (isCurrentGeneration(plugin, generation) && result.downloaded() && "never".equals(NightbreakPluginHotSwap.applyMode(plugin))) {
                     plugin.getLogger().info("Downloaded " + spec.displayName() + " " + result.remoteVersion() + ". Restart the server to use it.");
                  }
               });
            }
         }, initialRefreshDelayTicks(plugin, spec) + 600L);
         scheduledTaskId.set(taskId);
         if (taskId != -1) {
            Integer previousTaskId = AUTO_PLUGIN_UPDATE_TASKS.put(key, taskId);
            cancelReplacedTask(previousTaskId, taskId);
         }
      }
   }

   public static void startPluginUpdateMonitor(JavaPlugin plugin, NightbreakPluginSpec spec) {
      if (plugin != null && spec != null) {
         String key = cacheKey(plugin, spec);
         long generation = currentGeneration(plugin);
         Integer existingTaskId = UPDATE_CHECK_TASKS.get(key);
         if (existingTaskId == null || !Bukkit.getScheduler().isQueued(existingTaskId) && !Bukkit.getScheduler().isCurrentlyRunning(existingTaskId)) {
            int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
               if (isCurrentGeneration(plugin, generation)) {
                  refreshPluginUpdateCheckAsync(plugin, spec, null);
               }
            }, initialRefreshDelayTicks(plugin, spec), 72000L);
            if (taskId != -1) {
               UPDATE_CHECK_TASKS.put(key, taskId);
            }
         }
      }
   }

   public static void refreshPluginUpdateCheckAsync(JavaPlugin plugin, NightbreakPluginSpec spec, Consumer<NightbreakPluginUpdater.PluginUpdateCheck> callback) {
      if (plugin != null && spec != null) {
         String key = cacheKey(plugin, spec);
         long generation = currentGeneration(plugin);
         if (RUNNING_UPDATE_CHECKS.add(key)) {
            String spigotResourceId = NightbreakPluginCatalog.forSpec(spec).map(NightbreakPluginCatalog.Entry::spigotResourceId).orElse("");
            NightbreakPluginAsyncWorkTracker.Work work = registerAsyncWork(plugin, generation);
            if (work == null) {
               RUNNING_UPDATE_CHECKS.remove(key);
            } else {
               try {
                  BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                     if (!work.start()) {
                        RUNNING_UPDATE_CHECKS.remove(key);
                     } else {
                        try {
                           if (!isCurrentGeneration(plugin, generation)) {
                              return;
                           }

                           NightbreakPluginUpdater.PluginUpdateCheck check = checkForUpdate(plugin, spec, spigotResourceId);
                           if (isCurrentGeneration(plugin, generation)) {
                              cacheUpdateCheck(plugin, spec, check);
                              if (callback != null) {
                                 Bukkit.getScheduler().runTask(plugin, () -> {
                                    if (isCurrentGeneration(plugin, generation)) {
                                       callback.accept(check);
                                    }
                                 });
                              }

                              return;
                           }
                        } finally {
                           RUNNING_UPDATE_CHECKS.remove(key);
                           work.close();
                        }
                     }
                  });
                  work.attach(task);
               } catch (RuntimeException var9) {
                  RUNNING_UPDATE_CHECKS.remove(key);
                  work.dispatchFailed();
                  throw var9;
               }
            }
         }
      }
   }

   public static <T extends NightbreakManagedContent> void autoDownloadContentUpdatesIfEnabled(
      JavaPlugin plugin, NightbreakPluginSpec spec, Supplier<List<T>> packagesSupplier, AtomicBoolean guard, Consumer<CommandSender> reloadAction
   ) {
      if (plugin != null && spec != null && packagesSupplier != null && spec.hasContentPackages()) {
         NightbreakChangelogManager.registerContentSupplier(plugin, packagesSupplier);
         if (isAutoDownloadPluginUpdatesEnabled(plugin)) {
            String key = cacheKey(plugin, spec);
            long generation = currentGeneration(plugin);
            String runningKey = key + "#" + generation;
            if (RUNNING_AUTO_CONTENT_UPDATES.add(runningKey)) {
               AtomicInteger scheduledTaskId = new AtomicInteger(-1);
               int taskId = Bukkit.getScheduler()
                  .scheduleSyncDelayedTask(
                     plugin,
                     () -> {
                        AUTO_CONTENT_UPDATE_TASKS.remove(key, scheduledTaskId.get());

                        try {
                           if (isCurrentGeneration(plugin, generation) && plugin.isEnabled() && isAutoDownloadPluginUpdatesEnabled(plugin)) {
                              plugin.getLogger()
                                 .info("Automatic content update downloads are enabled. Downloaded content updates will be used after the server restarts.");
                              NightbreakBulkDownloader.execute(
                                 plugin, spec.displayName(), Bukkit.getConsoleSender(), packagesSupplier.get(), true, guard, reloadAction, false
                              );
                              return;
                           }
                        } finally {
                           RUNNING_AUTO_CONTENT_UPDATES.remove(runningKey);
                        }
                     },
                     initialRefreshDelayTicks(plugin, spec) + 1800L
                  );
               scheduledTaskId.set(taskId);
               if (taskId != -1) {
                  Integer previousTaskId = AUTO_CONTENT_UPDATE_TASKS.put(key, taskId);
                  cancelReplacedTask(previousTaskId, taskId);
               } else {
                  RUNNING_AUTO_CONTENT_UPDATES.remove(runningKey);
               }
            }
         }
      }
   }

   public static NightbreakPluginUpdater.CachedPluginUpdateCheck getCachedUpdateCheck(JavaPlugin plugin, NightbreakPluginSpec spec) {
      String key = cacheKey(plugin, spec);
      return new NightbreakPluginUpdater.CachedPluginUpdateCheck(
         CACHED_UPDATE_CHECKS.get(key), RUNNING_UPDATE_CHECKS.contains(key), CACHED_UPDATE_CHECK_TIMES.getOrDefault(key, 0L)
      );
   }

   public static void checkForUpdateAsync(
      JavaPlugin plugin, NightbreakPluginSpec spec, String spigotResourceId, Consumer<NightbreakPluginUpdater.PluginUpdateCheck> callback
   ) {
      long generation = currentGeneration(plugin);
      NightbreakPluginAsyncWorkTracker.Work work = registerAsyncWork(plugin, generation);
      if (work != null) {
         try {
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
               if (work.start()) {
                  try {
                     if (!isCurrentGeneration(plugin, generation)) {
                        return;
                     }

                     NightbreakPluginUpdater.PluginUpdateCheck check = checkForUpdate(plugin, spec, spigotResourceId);
                     if (isCurrentGeneration(plugin, generation)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                           if (isCurrentGeneration(plugin, generation)) {
                              callback.accept(check);
                           }
                        });
                        return;
                     }
                  } finally {
                     work.close();
                  }
               }
            });
            work.attach(task);
         } catch (RuntimeException var8) {
            work.dispatchFailed();
            throw var8;
         }
      }
   }

   public static NightbreakPluginUpdater.PluginUpdateCheck checkForUpdate(JavaPlugin plugin, NightbreakPluginSpec spec, String spigotResourceId) {
      String localVersion = cleanVersion(plugin.getDescription().getVersion());
      NightbreakAccount.VersionInfo versionInfo = NightbreakAccount.getPublicPluginVersion(spec.pluginSlug());
      boolean nightbreakReachable = versionInfo != null && versionInfo.version != null && !versionInfo.version.isBlank();
      boolean spigotFallbackUsed = false;
      String remoteVersion = nightbreakReachable ? versionInfo.version : null;
      if (!nightbreakReachable && spigotResourceId != null && !spigotResourceId.isBlank()) {
         remoteVersion = readSpigotVersion(spigotResourceId);
         spigotFallbackUsed = remoteVersion != null && !remoteVersion.isBlank();
      }

      boolean updateAvailable = remoteVersion != null && compareVersions(remoteVersion, localVersion) > 0;
      return new NightbreakPluginUpdater.PluginUpdateCheck(
         updateAvailable, nightbreakReachable, spigotFallbackUsed, localVersion, remoteVersion, spec.downloadPageUrl(), versionInfo
      );
   }

   public static void downloadPluginUpdateAsync(
      JavaPlugin plugin, NightbreakPluginSpec spec, CommandSender sender, Consumer<NightbreakPluginUpdater.PluginUpdateDownload> callback
   ) {
      long generation = currentGeneration(plugin);
      NightbreakPluginAsyncWorkTracker.Work work = registerAsyncWork(plugin, generation);
      if (work != null) {
         try {
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
               if (work.start()) {
                  try {
                     if (!isCurrentGeneration(plugin, generation)) {
                        return;
                     }

                     NightbreakPluginUpdater.PluginUpdateDownload result = downloadPluginUpdate(plugin, spec, sender, generation);
                     if (isCurrentGeneration(plugin, generation)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                           if (isCurrentGeneration(plugin, generation)) {
                              sendDownloadResult(sender, plugin, spec, result);
                              if (callback != null) {
                                 callback.accept(result);
                              }

                              if (result.downloaded() && !NightbreakPluginHotSwap.applyDownloadedUpdate(plugin, sender)) {
                                 NightbreakPluginUpdateMessages.broadcastRestartRequired(plugin, spec, result, sender);
                              }
                           }
                        });
                        return;
                     }
                  } finally {
                     work.close();
                  }
               }
            });
            work.attach(task);
         } catch (RuntimeException var8) {
            work.dispatchFailed();
            throw var8;
         }
      }
   }

   static NightbreakPluginUpdater.PluginUpdateDownload downloadPluginUpdate(JavaPlugin plugin, NightbreakPluginSpec spec, CommandSender sender) {
      return downloadPluginUpdate(plugin, spec, sender, currentGeneration(plugin));
   }

   private static NightbreakPluginUpdater.PluginUpdateDownload downloadPluginUpdate(
      JavaPlugin plugin, NightbreakPluginSpec spec, CommandSender sender, long generation
   ) {
      String runningKey = cacheKey(plugin, spec);
      if (!RUNNING_PLUGIN_DOWNLOADS.add(runningKey)) {
         return new NightbreakPluginUpdater.PluginUpdateDownload(
            NightbreakPluginUpdater.DownloadStatus.DOWNLOAD_FAILED,
            cleanVersion(plugin.getDescription().getVersion()),
            null,
            spec.downloadPageUrl(),
            null,
            "A plugin update download is already in progress."
         );
      } else {
         NightbreakPluginUpdater.PluginUpdateDownload var6;
         try {
            var6 = performPluginUpdateDownload(plugin, spec, sender, generation);
         } finally {
            RUNNING_PLUGIN_DOWNLOADS.remove(runningKey);
         }

         return var6;
      }
   }

   private static NightbreakPluginUpdater.PluginUpdateDownload performPluginUpdateDownload(
      JavaPlugin plugin, NightbreakPluginSpec spec, CommandSender sender, long generation
   ) {
      if (!isCurrentGeneration(plugin, generation)) {
         return new NightbreakPluginUpdater.PluginUpdateDownload(
            NightbreakPluginUpdater.DownloadStatus.DOWNLOAD_FAILED,
            cleanVersion(plugin.getDescription().getVersion()),
            null,
            spec.downloadPageUrl(),
            null,
            "Plugin lifecycle changed before the update download started."
         );
      } else {
         String localVersion = cleanVersion(plugin.getDescription().getVersion());
         String downloadPageUrl = spec.downloadPageUrl();
         NightbreakAccount.VersionInfo versionInfo = NightbreakAccount.getPublicPluginVersion(spec.pluginSlug());
         if (versionInfo != null && versionInfo.version != null && !versionInfo.version.isBlank()) {
            String remoteVersion = versionInfo.version;
            cacheUpdateCheck(
               plugin,
               spec,
               new NightbreakPluginUpdater.PluginUpdateCheck(
                  compareVersions(remoteVersion, localVersion) > 0, true, false, localVersion, remoteVersion, downloadPageUrl, versionInfo
               )
            );
            if (compareVersions(remoteVersion, localVersion) <= 0) {
               return new NightbreakPluginUpdater.PluginUpdateDownload(
                  NightbreakPluginUpdater.DownloadStatus.UP_TO_DATE, localVersion, remoteVersion, downloadPageUrl, null, null
               );
            } else if (!NightbreakAccount.hasToken()) {
               return new NightbreakPluginUpdater.PluginUpdateDownload(
                  NightbreakPluginUpdater.DownloadStatus.NO_TOKEN, localVersion, remoteVersion, downloadPageUrl, null, null
               );
            } else if (NightbreakAccount.hasAuthFailure()) {
               return new NightbreakPluginUpdater.PluginUpdateDownload(
                  NightbreakPluginUpdater.DownloadStatus.AUTH_FAILURE, localVersion, remoteVersion, downloadPageUrl, null, null
               );
            } else {
               NightbreakAccount account = NightbreakAccount.getInstance();
               if (account == null) {
                  return new NightbreakPluginUpdater.PluginUpdateDownload(
                     NightbreakPluginUpdater.DownloadStatus.NO_TOKEN, localVersion, remoteVersion, downloadPageUrl, null, null
                  );
               } else {
                  File updateFolder = resolveUpdateFolder(plugin);
                  if (!updateFolder.exists() && !updateFolder.mkdirs()) {
                     return new NightbreakPluginUpdater.PluginUpdateDownload(
                        NightbreakPluginUpdater.DownloadStatus.DOWNLOAD_FAILED,
                        localVersion,
                        remoteVersion,
                        downloadPageUrl,
                        null,
                        "Could not create update folder: " + updateFolder.getPath()
                     );
                  } else {
                     String targetName = currentJarName(plugin, versionInfo.fileName);
                     if (targetName == null) {
                        return new NightbreakPluginUpdater.PluginUpdateDownload(
                           NightbreakPluginUpdater.DownloadStatus.DOWNLOAD_FAILED,
                           localVersion,
                           remoteVersion,
                           downloadPageUrl,
                           null,
                           "The update service returned an unsafe plugin filename."
                        );
                     } else {
                        File tempFile = new File(updateFolder, targetName + "." + generation + "." + UUID.randomUUID() + ".download");
                        File downloadedFile = new File(updateFolder, targetName);
                        File legacyTempFile = new File(updateFolder, targetName + ".download");
                        if (legacyTempFile.exists() && !legacyTempFile.delete()) {
                           return new NightbreakPluginUpdater.PluginUpdateDownload(
                              NightbreakPluginUpdater.DownloadStatus.DOWNLOAD_FAILED,
                              localVersion,
                              remoteVersion,
                              downloadPageUrl,
                              null,
                              "Could not clear a legacy temporary download."
                           );
                        } else {
                           long[] lastProgressMessage = new long[]{0L};
                           NightbreakAccount.PluginDownloadResult downloadResult = account.downloadPluginUpdate(
                              spec.pluginSlug(),
                              tempFile,
                              (bytesDownloaded, totalBytes) -> {
                                 if (canMessage(sender)) {
                                    long now = System.currentTimeMillis();
                                    if (now - lastProgressMessage[0] >= 2000L) {
                                       lastProgressMessage[0] = now;
                                       String progress = totalBytes > 0L
                                          ? String.format(Locale.ROOT, "%.1f%%", bytesDownloaded * 100.0 / totalBytes)
                                          : formatBytes(bytesDownloaded);
                                       Bukkit.getScheduler().runTask(plugin, () -> {
                                          if (canMessage(sender)) {
                                             Logger.sendSimpleMessage(sender, "&7[" + spec.displayName() + "] Downloading plugin update... " + progress);
                                          }
                                       });
                                    }
                                 }
                              }
                           );
                           if (downloadResult.success && tempFile.exists()) {
                              if (versionInfo.checksum != null && !versionInfo.checksum.isBlank()) {
                                 String actualChecksum;
                                 try {
                                    actualChecksum = sha256(tempFile);
                                 } catch (IllegalStateException var23) {
                                    tempFile.delete();
                                    return new NightbreakPluginUpdater.PluginUpdateDownload(
                                       NightbreakPluginUpdater.DownloadStatus.CHECKSUM_FAILED,
                                       localVersion,
                                       remoteVersion,
                                       downloadPageUrl,
                                       null,
                                       var23.getMessage()
                                    );
                                 }

                                 if (!versionInfo.checksum.equalsIgnoreCase(actualChecksum)) {
                                    tempFile.delete();
                                    return new NightbreakPluginUpdater.PluginUpdateDownload(
                                       NightbreakPluginUpdater.DownloadStatus.CHECKSUM_FAILED,
                                       localVersion,
                                       remoteVersion,
                                       downloadPageUrl,
                                       null,
                                       "Expected " + versionInfo.checksum + ", got " + actualChecksum
                                    );
                                 }
                              }

                              DownloadedPluginJarValidator.ValidationResult jarValidation = DownloadedPluginJarValidator.validate(
                                 tempFile, List.of(plugin.getName()), remoteVersion
                              );
                              if (!jarValidation.valid()) {
                                 tempFile.delete();
                                 return new NightbreakPluginUpdater.PluginUpdateDownload(
                                    NightbreakPluginUpdater.DownloadStatus.DOWNLOAD_FAILED,
                                    localVersion,
                                    remoteVersion,
                                    downloadPageUrl,
                                    null,
                                    jarValidation.detail()
                                 );
                              } else {
                                 synchronized (PLUGIN_LIFECYCLE_LOCK) {
                                    if (currentGenerationLocked(plugin) != generation) {
                                       tempFile.delete();
                                       return new NightbreakPluginUpdater.PluginUpdateDownload(
                                          NightbreakPluginUpdater.DownloadStatus.DOWNLOAD_FAILED,
                                          localVersion,
                                          remoteVersion,
                                          downloadPageUrl,
                                          null,
                                          "Plugin lifecycle changed before the downloaded update could be published."
                                       );
                                    } else {
                                       try {
                                          moveReplacing(tempFile, downloadedFile);
                                       } catch (IOException var21) {
                                          tempFile.delete();
                                          return new NightbreakPluginUpdater.PluginUpdateDownload(
                                             NightbreakPluginUpdater.DownloadStatus.DOWNLOAD_FAILED,
                                             localVersion,
                                             remoteVersion,
                                             downloadPageUrl,
                                             null,
                                             var21.getMessage()
                                          );
                                       }

                                       NightbreakPluginUpdater.PluginUpdateDownload result = new NightbreakPluginUpdater.PluginUpdateDownload(
                                          NightbreakPluginUpdater.DownloadStatus.DOWNLOADED, localVersion, remoteVersion, downloadPageUrl, downloadedFile, null
                                       );
                                       notifyPluginUpdateDownloaded(plugin, generation, result);
                                       return result;
                                    }
                                 }
                              }
                           } else {
                              if (tempFile.exists()) {
                                 tempFile.delete();
                              }

                              return new NightbreakPluginUpdater.PluginUpdateDownload(
                                 downloadStatusFor(downloadResult), localVersion, remoteVersion, downloadPageUrl, null, downloadResult.displayDetail()
                              );
                           }
                        }
                     }
                  }
               }
            }
         } else {
            return new NightbreakPluginUpdater.PluginUpdateDownload(
               NightbreakPluginUpdater.DownloadStatus.NIGHTBREAK_UNREACHABLE, localVersion, null, downloadPageUrl, null, null
            );
         }
      }
   }

   public static void sendDownloadResult(
      CommandSender sender, JavaPlugin plugin, NightbreakPluginSpec spec, NightbreakPluginUpdater.PluginUpdateDownload result
   ) {
      if (canMessage(sender)) {
         NightbreakPluginUpdateMessages.sendResult(sender, plugin, spec, result);
      }
   }

   private static NightbreakPluginUpdater.DownloadStatus downloadStatusFor(NightbreakAccount.PluginDownloadResult downloadResult) {
      if (downloadResult.responseCode == 401 || NightbreakAccount.hasAuthFailure()) {
         return NightbreakPluginUpdater.DownloadStatus.AUTH_FAILURE;
      } else if ("NO_TOKEN".equalsIgnoreCase(downloadResult.error)) {
         return NightbreakPluginUpdater.DownloadStatus.NO_TOKEN;
      } else if (downloadResult.responseCode == 403) {
         return NightbreakPluginUpdater.DownloadStatus.NO_ACCESS;
      } else {
         return downloadResult.responseCode != 0 && !"NETWORK_ERROR".equalsIgnoreCase(downloadResult.error)
            ? NightbreakPluginUpdater.DownloadStatus.DOWNLOAD_FAILED
            : NightbreakPluginUpdater.DownloadStatus.NIGHTBREAK_UNREACHABLE;
      }
   }

   public static boolean isAutoDownloadPluginUpdatesEnabled(JavaPlugin plugin) {
      File configFile = new File(plugin.getDataFolder(), "config.yml");
      if (!configFile.exists()) {
         return false;
      } else {
         try {
            return YamlConfiguration.loadConfiguration(configFile).getBoolean("nightbreak.autoDownloadPluginUpdates", false);
         } catch (Exception var3) {
            plugin.getLogger().warning("Failed to read nightbreak.autoDownloadPluginUpdates from config.yml: " + var3.getMessage());
            return false;
         }
      }
   }

   public static boolean setAutoDownloadPluginUpdatesEnabled(JavaPlugin plugin, boolean enabled) {
      File configFile = new File(plugin.getDataFolder(), "config.yml");

      try {
         if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            configFile.createNewFile();
         }

         YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
         configuration.set("nightbreak.autoDownloadPluginUpdates", enabled);
         configuration.setComments("nightbreak.autoDownloadPluginUpdates", AUTO_DOWNLOAD_CONFIG_COMMENTS);
         configuration.save(configFile);
         return true;
      } catch (IOException var4) {
         plugin.getLogger().warning("Failed to write nightbreak.autoDownloadPluginUpdates to config.yml: " + var4.getMessage());
         return false;
      }
   }

   public static int compareVersions(String left, String right) {
      int[] leftParts = parseVersionParts(left);
      int[] rightParts = parseVersionParts(right);
      int length = Math.max(leftParts.length, rightParts.length);

      for (int i = 0; i < length; i++) {
         int leftPart = i < leftParts.length ? leftParts[i] : 0;
         int rightPart = i < rightParts.length ? rightParts[i] : 0;
         if (leftPart != rightPart) {
            return Integer.compare(leftPart, rightPart);
         }
      }

      return 0;
   }

   public static String cleanVersion(String version) {
      if (version == null) {
         return "0";
      } else {
         String cleaned = version.trim();
         if (cleaned.startsWith("v") || cleaned.startsWith("V")) {
            cleaned = cleaned.substring(1);
         }

         int suffix = cleaned.indexOf(45);
         if (suffix != -1) {
            cleaned = cleaned.substring(0, suffix);
         }

         suffix = cleaned.indexOf(43);
         if (suffix != -1) {
            cleaned = cleaned.substring(0, suffix);
         }

         return cleaned.isBlank() ? "0" : cleaned;
      }
   }

   private static int[] parseVersionParts(String version) {
      String[] rawParts = cleanVersion(version).split("\\.");
      int[] parts = new int[rawParts.length];

      for (int i = 0; i < rawParts.length; i++) {
         String part = rawParts[i].replaceAll("[^0-9].*$", "");
         if (part.isBlank()) {
            parts[i] = 0;
         } else {
            try {
               parts[i] = Integer.parseInt(part);
            } catch (NumberFormatException var6) {
               parts[i] = 0;
            }
         }
      }

      return parts;
   }

   private static String readSpigotVersion(String resourceId) {
      try {
         String encodedResourceId = URLEncoder.encode(resourceId, StandardCharsets.UTF_8);
         URLConnection connection = new URL(spigotUpdateBaseUrl() + "/legacy/update.php?resource=" + encodedResourceId).openConnection();
         if (scopedSpigotTestBaseUrl != null && connection instanceof HttpURLConnection httpConnection) {
            httpConnection.setInstanceFollowRedirects(false);
         }

         connection.setConnectTimeout(3000);
         connection.setReadTimeout(3000);

         String var4;
         try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            var4 = scanner.hasNext() ? scanner.next().trim() : null;
         }

         return var4;
      } catch (IOException var8) {
         return null;
      }
   }

   private static long initialRefreshDelayTicks(JavaPlugin plugin, NightbreakPluginSpec spec) {
      return 6000L + Math.floorMod(cacheKey(plugin, spec).hashCode(), 2400);
   }

   private static void cacheUpdateCheck(JavaPlugin plugin, NightbreakPluginSpec spec, NightbreakPluginUpdater.PluginUpdateCheck check) {
      String key = cacheKey(plugin, spec);
      CACHED_UPDATE_CHECKS.put(key, check);
      CACHED_UPDATE_CHECK_TIMES.put(key, System.currentTimeMillis());
   }

   private static String cacheKey(JavaPlugin plugin, NightbreakPluginSpec spec) {
      String pluginName = plugin == null ? "" : plugin.getName();
      String slug = spec == null ? "" : spec.pluginSlug();
      return pluginName.toLowerCase(Locale.ROOT) + ":" + slug.toLowerCase(Locale.ROOT);
   }

   static File updateFolder(JavaPlugin plugin) {
      return resolveUpdateFolder(plugin);
   }

   private static File resolveUpdateFolder(JavaPlugin plugin) {
      if (Bukkit.getServer() != null) {
         try {
            if (Bukkit.getServer().getClass().getMethod("getUpdateFolderFile").invoke(Bukkit.getServer()) instanceof File file) {
               return file;
            }
         } catch (ReflectiveOperationException var3) {
         }
      }

      File pluginsFolder = plugin.getDataFolder().getParentFile();
      return new File(pluginsFolder, "update");
   }

   private static String currentJarName(JavaPlugin plugin, String remoteFileName) {
      File currentJar = currentPluginFile(plugin);
      if (currentJar != null && currentJar.isFile()) {
         return currentJar.getName();
      } else {
         return remoteFileName != null && !remoteFileName.isBlank() ? safeJarFileName(remoteFileName) : plugin.getName() + ".jar";
      }
   }

   private static String safeJarFileName(String candidate) {
      String value = candidate == null ? "" : candidate.trim();
      return !value.isBlank()
            && value.toLowerCase(Locale.ROOT).endsWith(".jar")
            && value.indexOf(47) < 0
            && value.indexOf(92) < 0
            && value.indexOf(58) < 0
            && value.indexOf(0) < 0
            && !".".equals(value)
            && !"..".equals(value)
         ? value
         : null;
   }

   private static File currentPluginFile(JavaPlugin plugin) {
      try {
         URL location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
         File file = new File(location.toURI());
         return file.isFile() ? file : null;
      } catch (Exception var3) {
         return null;
      }
   }

   private static void moveReplacing(File source, File target) throws IOException {
      try {
         Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException var3) {
         Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
   }

   private static String sha256(File file) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");

         try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];

            int read;
            while ((read = inputStream.read(buffer)) != -1) {
               digest.update(buffer, 0, read);
            }
         }

         StringBuilder var10 = new StringBuilder();

         for (byte b : digest.digest()) {
            var10.append(String.format("%02x", b));
         }

         return var10.toString();
      } catch (NoSuchAlgorithmException | IOException var9) {
         throw new IllegalStateException("Could not calculate checksum", var9);
      }
   }

   static NightbreakPluginUpdater.ScopedSpigotTestBaseUrl useLoopbackSpigotBaseUrlForTests(URI baseUri) {
      String normalized = normalizeLoopbackTestOrigin(baseUri);
      synchronized (SPIGOT_TEST_BASE_URL_LOCK) {
         if (scopedSpigotTestBaseUrl != null) {
            throw new IllegalStateException("A Spigot test base URI is already active.");
         }

         scopedSpigotTestBaseUrl = normalized;
      }

      return new NightbreakPluginUpdater.ScopedSpigotTestBaseUrl(normalized);
   }

   private static String spigotUpdateBaseUrl() {
      String testBaseUrl = scopedSpigotTestBaseUrl;
      if (testBaseUrl != null) {
         return testBaseUrl;
      } else {
         PRODUCTION_SPIGOT_ORIGIN_SELECTIONS.incrementAndGet();
         return "https://api.spigotmc.org";
      }
   }

   static int productionOriginSelectionsForTests() {
      return PRODUCTION_SPIGOT_ORIGIN_SELECTIONS.get();
   }

   private static String normalizeLoopbackTestOrigin(URI baseUri) {
      if (baseUri == null) {
         throw new IllegalArgumentException("Test base URI cannot be null.");
      } else {
         String scheme = baseUri.getScheme();
         if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Test base URI must use HTTP or HTTPS.");
         } else {
            String host = baseUri.getHost();
            if (host == null || host.isBlank() || !isLoopbackTestHost(host)) {
               throw new IllegalArgumentException("Test base URI must have a loopback host.");
            } else if (baseUri.getRawUserInfo() == null && baseUri.getRawQuery() == null && baseUri.getRawFragment() == null) {
               String path = baseUri.getRawPath();
               if (path != null && !path.isBlank() && !"/".equals(path)) {
                  throw new IllegalArgumentException("Test base URI cannot contain an API path.");
               } else {
                  String normalized = baseUri.toString();

                  while (normalized.endsWith("/")) {
                     normalized = normalized.substring(0, normalized.length() - 1);
                  }

                  return normalized;
               }
            } else {
               throw new IllegalArgumentException("Test base URI cannot contain user info, a query, or a fragment.");
            }
         }
      }
   }

   private static boolean isLoopbackTestHost(String host) {
      String normalizedHost = host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
      if (!"localhost".equalsIgnoreCase(normalizedHost) && !"::1".equals(normalizedHost) && !"0:0:0:0:0:0:0:1".equals(normalizedHost)) {
         String[] octets = normalizedHost.split("\\.", -1);
         if (octets.length == 4 && "127".equals(octets[0])) {
            for (String octet : octets) {
               if (octet.isBlank()) {
                  return false;
               }

               try {
                  int value = Integer.parseInt(octet);
                  if (value < 0 || value > 255) {
                     return false;
                  }
               } catch (NumberFormatException var8) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   private static long currentGeneration(JavaPlugin plugin) {
      synchronized (PLUGIN_LIFECYCLE_LOCK) {
         return currentGenerationLocked(plugin);
      }
   }

   static long lifecycleGeneration(JavaPlugin plugin) {
      return currentGeneration(plugin);
   }

   private static long currentGenerationLocked(JavaPlugin plugin) {
      return PLUGIN_GENERATIONS.getOrDefault(plugin, 0L);
   }

   private static boolean isCurrentGeneration(JavaPlugin plugin, long generation) {
      synchronized (PLUGIN_LIFECYCLE_LOCK) {
         return currentGenerationLocked(plugin) == generation;
      }
   }

   static boolean isLifecycleCurrent(JavaPlugin plugin, long generation) {
      return isCurrentGeneration(plugin, generation);
   }

   static boolean publishIfLifecycleCurrent(JavaPlugin plugin, long generation, NightbreakPluginUpdater.LifecyclePublication publication) throws IOException {
      synchronized (PLUGIN_LIFECYCLE_LOCK) {
         if (currentGenerationLocked(plugin) != generation) {
            return false;
         } else {
            publication.publish();
            return true;
         }
      }
   }

   static NightbreakPluginAsyncWorkTracker.Work registerAsyncWork(JavaPlugin plugin, long generation) {
      synchronized (PLUGIN_LIFECYCLE_LOCK) {
         return currentGenerationLocked(plugin) != generation ? null : ASYNC_WORK.register(plugin);
      }
   }

   private static void notifyPluginUpdateDownloaded(JavaPlugin plugin, long generation, NightbreakPluginUpdater.PluginUpdateDownload result) {
      List<NightbreakPluginUpdater.PluginUpdateListenerRegistration> registrations;
      synchronized (PLUGIN_LIFECYCLE_LOCK) {
         if (currentGenerationLocked(plugin) != generation) {
            return;
         }

         CopyOnWriteArrayList<NightbreakPluginUpdater.PluginUpdateListenerRegistration> current = PLUGIN_UPDATE_LISTENERS.get(plugin);
         if (current == null || current.isEmpty()) {
            return;
         }

         registrations = new ArrayList<>(current);
      }

      for (NightbreakPluginUpdater.PluginUpdateListenerRegistration registration : registrations) {
         if (!registration.isClosed()) {
            try {
               registration.listener.accept(result);
            } catch (Exception var8) {
               plugin.getLogger().warning("Plugin update download listener failed: " + var8.getMessage());
            }
         }
      }
   }

   private static void removeListener(NightbreakPluginUpdater.PluginUpdateListenerRegistration registration) {
      synchronized (PLUGIN_LIFECYCLE_LOCK) {
         CopyOnWriteArrayList<NightbreakPluginUpdater.PluginUpdateListenerRegistration> registrations = PLUGIN_UPDATE_LISTENERS.get(registration.plugin);
         if (registrations != null) {
            registrations.remove(registration);
            if (registrations.isEmpty()) {
               PLUGIN_UPDATE_LISTENERS.remove(registration.plugin);
            }
         }
      }
   }

   private static void cancelTasksForPlugin(Map<String, Integer> tasks, String keyPrefix) {
      for (Entry<String, Integer> entry : new ArrayList<>(tasks.entrySet())) {
         if (entry.getKey().startsWith(keyPrefix) && tasks.remove(entry.getKey(), entry.getValue())) {
            cancelTask(entry.getValue());
         }
      }
   }

   private static void cancelReplacedTask(Integer previousTaskId, int replacementTaskId) {
      if (previousTaskId != null && previousTaskId != replacementTaskId) {
         cancelTask(previousTaskId);
      }
   }

   private static void cancelTask(int taskId) {
      if (Bukkit.getServer() != null) {
         try {
            Bukkit.getScheduler().cancelTask(taskId);
         } catch (RuntimeException var2) {
         }
      }
   }

   private static boolean canMessage(CommandSender sender) {
      return sender != null && !(sender instanceof Player player && !player.isOnline());
   }

   private static String formatBytes(long bytes) {
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         return bytes < 1048576L ? String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0) : String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
      }
   }

   public record CachedPluginUpdateCheck(NightbreakPluginUpdater.PluginUpdateCheck check, boolean checking, long checkedAtMillis) {
      public boolean hasResult() {
         return this.check != null;
      }
   }

   public static enum DownloadStatus {
      DOWNLOADED,
      UP_TO_DATE,
      NO_TOKEN,
      NO_ACCESS,
      AUTH_FAILURE,
      NIGHTBREAK_UNREACHABLE,
      DOWNLOAD_FAILED,
      CHECKSUM_FAILED;
   }

   @FunctionalInterface
   interface LifecyclePublication {
      void publish() throws IOException;
   }

   public interface ListenerRegistration extends AutoCloseable {
      @Override
      void close();
   }

   public record PluginUpdateCheck(
      boolean updateAvailable,
      boolean nightbreakReachable,
      boolean spigotFallbackUsed,
      String localVersion,
      String remoteVersion,
      String downloadPageUrl,
      NightbreakAccount.VersionInfo versionInfo
   ) {
   }

   public record PluginUpdateDownload(
      NightbreakPluginUpdater.DownloadStatus status, String localVersion, String remoteVersion, String downloadPageUrl, File downloadedFile, String detail
   ) {
      public boolean downloaded() {
         return this.status == NightbreakPluginUpdater.DownloadStatus.DOWNLOADED;
      }
   }

   private static final class PluginUpdateListenerRegistration implements NightbreakPluginUpdater.ListenerRegistration {
      private final JavaPlugin plugin;
      private final Consumer<NightbreakPluginUpdater.PluginUpdateDownload> listener;
      private final AtomicBoolean closed = new AtomicBoolean(false);

      private PluginUpdateListenerRegistration(JavaPlugin plugin, Consumer<NightbreakPluginUpdater.PluginUpdateDownload> listener) {
         this.plugin = plugin;
         this.listener = listener;
      }

      @Override
      public void close() {
         synchronized (NightbreakPluginUpdater.PLUGIN_LIFECYCLE_LOCK) {
            if (this.closed.compareAndSet(false, true)) {
               NightbreakPluginUpdater.removeListener(this);
            }
         }
      }

      private void markClosedFromShutdown() {
         this.closed.set(true);
      }

      private boolean isClosed() {
         return this.closed.get();
      }
   }

   static final class ScopedSpigotTestBaseUrl implements AutoCloseable {
      private final String expectedBaseUrl;
      private boolean closed;

      private ScopedSpigotTestBaseUrl(String expectedBaseUrl) {
         this.expectedBaseUrl = expectedBaseUrl;
      }

      @Override
      public void close() {
         synchronized (NightbreakPluginUpdater.SPIGOT_TEST_BASE_URL_LOCK) {
            if (!this.closed) {
               if (!this.expectedBaseUrl.equals(NightbreakPluginUpdater.scopedSpigotTestBaseUrl)) {
                  throw new IllegalStateException("Spigot test base URI scope changed unexpectedly.");
               } else {
                  NightbreakPluginUpdater.scopedSpigotTestBaseUrl = null;
                  this.closed = true;
               }
            }
         }
      }
   }
}
