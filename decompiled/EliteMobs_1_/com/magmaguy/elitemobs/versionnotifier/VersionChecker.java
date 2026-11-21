package com.magmaguy.elitemobs.versionnotifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakChatStyle;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakContentManager;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakPluginUpdater;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import lombok.Generated;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class VersionChecker {
   private static final List<EMPackage> outdatedPackages = Collections.synchronizedList(new ArrayList<>());
   private static final boolean SHA1Updated = false;
   private static final int MAX_RETRY_ATTEMPTS = 3;
   private static final int RETRY_DELAY_SECONDS = 60;
   private static final long REFRESH_COOLDOWN_MS = 300000L;
   private static boolean pluginIsUpToDate = true;
   private static boolean connectionFailed = false;
   private static int connectionRetryCount = 0;
   private static final long CHECK_INTERVAL_TICKS = 1728000L;
   private static NightbreakAccount.TokenChangeListenerRegistration tokenChangeListener;
   private static BukkitTask scheduledCheckTask;
   private static volatile long lifecycleGeneration;
   private static volatile long contentRefreshSequence;
   private static volatile VersionChecker.DlcCatalog dlcCatalog = new VersionChecker.DlcCatalog(Map.of(), Set.of(), Set.of());
   private static volatile long lastRefreshTimestamp = 0L;

   private VersionChecker() {
   }

   private static boolean isCurrent(long generation) {
      return generation == lifecycleGeneration && !MetadataHandler.shutdownRequested;
   }

   private static void publish(long generation, Runnable publication) {
      if (isCurrent(generation)) {
         if (Bukkit.isPrimaryThread()) {
            publication.run();
         } else {
            try {
               Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                  if (isCurrent(generation)) {
                     publication.run();
                  }
               });
            } catch (IllegalPluginAccessException var4) {
               if (isCurrent(generation)) {
                  throw var4;
               }
            }
         }
      }
   }

   private static boolean isCurrentContent(long generation, long refresh) {
      return isCurrent(generation) && refresh == contentRefreshSequence;
   }

   private static void publishContent(long generation, long refresh, Runnable publication) {
      publish(generation, () -> {
         if (isCurrentContent(generation, refresh)) {
            publication.run();
         }
      });
   }

   public static boolean serverVersionOlderThan(int majorVersion, int minorVersion) {
      return com.magmaguy.elitemobs.magmacore.util.VersionChecker.serverVersionOlderThan(majorVersion, minorVersion);
   }

   private static void checkPluginVersion() {
      final long generation = lifecycleGeneration;
      final String installedVersion = MetadataHandler.PLUGIN.getDescription().getVersion();
      (new BukkitRunnable() {
            public void run() {
               if (VersionChecker.isCurrent(generation)) {
                  String currentVersion = installedVersion;
                  boolean snapshot = false;
                  if (currentVersion.contains("SNAPSHOT")) {
                     snapshot = true;
                     currentVersion = currentVersion.split("-")[0];
                  }

                  NightbreakAccount.VersionInfo versionInfo = NightbreakAccount.getPublicPluginVersion("elitemobs");
                  String publicVersion;
                  if (versionInfo != null && versionInfo.version != null && !versionInfo.version.isBlank()) {
                     publicVersion = versionInfo.version;
                  } else {
                     try {
                        publicVersion = VersionChecker.readStringFromURL("https://api.spigotmc.org/legacy/update.php?resource=40090");
                     } catch (IOException var8) {
                        VersionChecker.publish(generation, () -> VersionChecker.handleConnectionError("plugin version check", var8));
                        return;
                     }
                  }

                  boolean outdated = NightbreakPluginUpdater.compareVersions(publicVersion, currentVersion) > 0;
                  boolean snapshotVersion = snapshot;
                  String latestVersion = publicVersion;
                  VersionChecker.publish(
                     generation,
                     () -> {
                        Logger.info("Latest public release is " + latestVersion);
                        Logger.info("Your version is " + installedVersion);
                        if (outdated) {
                           VersionChecker.outOfDateHandler();
                        } else {
                           Logger.info(
                              snapshotVersion
                                 ? "You are running a snapshot version! You can check for updates in the #releases channel on the EliteMobs Discord!"
                                 : "You are running the latest version!"
                           );
                           VersionChecker.pluginIsUpToDate = true;
                        }
                     }
                  );
               }
            }
         })
         .runTaskAsynchronously(MetadataHandler.PLUGIN);
   }

   public static void shutdown() {
      lifecycleGeneration++;
      if (tokenChangeListener != null) {
         tokenChangeListener.close();
         tokenChangeListener = null;
      }

      if (scheduledCheckTask != null) {
         scheduledCheckTask.cancel();
         scheduledCheckTask = null;
      }

      outdatedPackages.clear();
      connectionRetryCount = 0;
      connectionFailed = false;
      pluginIsUpToDate = true;
      lastRefreshTimestamp = 0L;
      dlcCatalog = new VersionChecker.DlcCatalog(Map.of(), Set.of(), Set.of());
   }

   private static String fetchFromNightbreak(String urlString) throws IOException {
      URL url = new URL(urlString);
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Accept", "application/json");
      connection.setConnectTimeout(10000);
      connection.setReadTimeout(10000);

      String var5;
      try {
         int responseCode = connection.getResponseCode();
         if (responseCode != 200) {
            throw new IOException("Nightbreak API returned status code: " + responseCode);
         }

         try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            var5 = scanner.hasNext() ? scanner.next() : "";
         }
      } finally {
         connection.disconnect();
      }

      return var5;
   }

   private static VersionChecker.DlcCatalog parseNightbreakDlcResponse(String json) throws IOException {
      try {
         JsonObject root = JsonParser.parseString(json).getAsJsonObject();
         Map<String, Integer> versions = new HashMap<>();
         Set<String> slugs = new HashSet<>();
         Set<String> unpublished = new HashSet<>();
         boolean foundCategory = false;

         for (String category : List.of("accessible", "patreonRequired", "purchaseAvailable")) {
            JsonElement entries = root.get(category);
            if (entries != null) {
               foundCategory = true;

               for (JsonElement element : entries.getAsJsonArray()) {
                  JsonObject entry = element.getAsJsonObject();
                  String slug = catalogString(entry, "slug");
                  if (slug == null || slug.isBlank()) {
                     throw new IllegalStateException("DLC entry has no slug");
                  }

                  slugs.add(slug);
                  String version = catalogString(entry, "currentVersion");
                  if (version != null && !version.isEmpty()) {
                     versions.put(slug, Integer.parseInt(version.startsWith("v") ? version.substring(1) : version));
                     unpublished.remove(slug);
                  } else {
                     versions.remove(slug);
                     unpublished.add(slug);
                  }
               }
            }
         }

         if (!foundCategory) {
            throw new IllegalStateException("DLC catalog has no recognized categories");
         } else {
            return new VersionChecker.DlcCatalog(Map.copyOf(versions), Set.copyOf(slugs), Set.copyOf(unpublished));
         }
      } catch (IllegalStateException | NumberFormatException | JsonParseException var14) {
         throw new IOException("Invalid Nightbreak DLC catalog", var14);
      }
   }

   private static String catalogString(JsonObject entry, String key) {
      JsonElement value = entry.get(key);
      if (value == null || value.isJsonNull()) {
         return null;
      } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
         return value.getAsString();
      } else {
         throw new IllegalStateException("DLC " + key + " must be a string");
      }
   }

   private static void checkContentVersion() {
      checkContentVersion(null);
   }

   private static void checkContentVersion(Runnable onComplete) {
      long generation = lifecycleGeneration;
      publish(
         generation,
         () -> {
            long refresh = ++contentRefreshSequence;
            List<EMPackage> packageSnapshot = new ArrayList<>(EMPackage.getEmPackages().values());
            Bukkit.getScheduler()
               .runTaskAsynchronously(
                  MetadataHandler.PLUGIN,
                  () -> {
                     try {
                        if (!isCurrentContent(generation, refresh)) {
                           return;
                        }

                        VersionChecker.DlcCatalog parsedCatalog = parseNightbreakDlcResponse(fetchFromNightbreak("https://nightbreak.io/api/dlc"));
                        publishContent(generation, refresh, () -> {
                           dlcCatalog = parsedCatalog;
                           connectionFailed = false;
                           connectionRetryCount = 0;
                           Logger.info("Parsed " + parsedCatalog.versions().size() + " content versions from Nightbreak API");
                           processContentVersionData(parsedCatalog.versions(), true);
                        });
                        if (isCurrentContent(generation, refresh)) {
                           prefetchAccessInfoInternal(packageSnapshot, parsedCatalog, generation, refresh);
                           return;
                        }
                     } catch (IOException var10) {
                        publishContent(
                           generation,
                           refresh,
                           () -> {
                              lastRefreshTimestamp = 0L;
                              handleConnectionError("content version check", var10);
                              if (connectionRetryCount >= 3
                                 || !(var10 instanceof UnknownHostException)
                                    && !(var10 instanceof ConnectException)
                                    && !(var10 instanceof SocketTimeoutException)) {
                                 Logger.info("Using local data for content version checks as remote server is unavailable.");
                              }
                           }
                        );
                        return;
                     } finally {
                        if (onComplete != null) {
                           publishContent(generation, refresh, onComplete);
                        }
                     }
                  }
               );
         }
      );
   }

   private static void processContentVersionData(Map<String, Integer> remoteVersions, boolean notifyAdmins) {
      List<EMPackage> newlyOutdated = new ArrayList<>();
      Set<EMPackage> checkedPackages = new HashSet<>();
      Set<EMPackage> currentlyOutdated = new HashSet<>();
      List<EMPackage> packageSnapshot = new ArrayList<>(EMPackage.getEmPackages().values());
      Set<String> metaChildren = EMPackage.getMetaPackageChildFilenames();

      for (EMPackage emPackage : packageSnapshot) {
         String slug = emPackage.getContentPackagesConfigFields().getNightbreakSlug();
         if ((emPackage.getContentPackagesConfigFields().isDefaultDungeon() || slug != null && !slug.isEmpty())
            && !metaChildren.contains(emPackage.getContentPackagesConfigFields().getFilename())
            && slug != null
            && !slug.isEmpty()) {
            checkedPackages.add(emPackage);
            if (!emPackage.isInstalled()) {
               emPackage.setOutOfDate(false);
               outdatedPackages.remove(emPackage);
            } else {
               Integer remoteVersion = remoteVersions.get(slug);
               if (remoteVersion == null) {
                  if (dlcCatalog.unpublishedSlugs().contains(slug)) {
                     emPackage.setOutOfDate(false);
                     outdatedPackages.remove(emPackage);
                  } else {
                     Logger.warn(
                        "No version info found on Nightbreak for content: " + emPackage.getContentPackagesConfigFields().getName() + " (slug: " + slug + ")"
                     );
                     emPackage.setOutOfDate(false);
                     outdatedPackages.remove(emPackage);
                  }
               } else {
                  int localVersion = emPackage.getContentPackagesConfigFields().getDungeonVersion();
                  if (remoteVersion > localVersion) {
                     emPackage.setOutOfDate(true);
                     currentlyOutdated.add(emPackage);
                     synchronized (outdatedPackages) {
                        if (!outdatedPackages.contains(emPackage)) {
                           outdatedPackages.add(emPackage);
                           newlyOutdated.add(emPackage);
                        }
                     }

                     Logger.warn(
                        "Content "
                           + emPackage.getContentPackagesConfigFields().getName()
                           + " is outdated! You should go download the updated version! Your version: "
                           + localVersion
                           + " / remote version: "
                           + remoteVersion
                           + " / Link: "
                           + emPackage.getContentPageUrl()
                     );
                  } else {
                     emPackage.setOutOfDate(false);
                     outdatedPackages.remove(emPackage);
                  }
               }
            }
         }
      }

      synchronized (outdatedPackages) {
         outdatedPackages.removeIf(emPackagex -> checkedPackages.contains(emPackagex) && !currentlyOutdated.contains(emPackagex));
      }

      if (notifyAdmins && !newlyOutdated.isEmpty()) {
         notifyOnlineAdmins(newlyOutdated);
      }
   }

   private static void handleConnectionError(String checkType, Exception e) {
      connectionFailed = true;
      if (!(e instanceof UnknownHostException) && !(e instanceof ConnectException) && !(e instanceof SocketTimeoutException)) {
         Logger.warn("Error during " + checkType + ": " + e.getMessage());
         if (e.getCause() != null) {
            Logger.warn("Caused by: " + e.getCause().getMessage());
         }
      } else {
         connectionRetryCount++;
         if (connectionRetryCount <= 3) {
            Logger.warn("Network error during " + checkType + ". Will retry in 60 seconds (Attempt " + connectionRetryCount + "/3)");
            long generation = lifecycleGeneration;
            long refresh = contentRefreshSequence;
            Bukkit.getScheduler()
               .runTaskLaterAsynchronously(MetadataHandler.PLUGIN, () -> publishContent(generation, refresh, VersionChecker::checkContentVersion), 1200L);
         } else {
            Logger.warn("Failed to connect for " + checkType + " after 3 attempts. Will continue without version checking. Error: " + e.getMessage());
         }
      }
   }

   private static String readStringFromURL(String url) throws IOException {
      URLConnection connection = new URL(url).openConnection();
      connection.setConnectTimeout(10000);
      connection.setReadTimeout(10000);

      String var3;
      try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
         scanner.useDelimiter("\\A");
         var3 = scanner.hasNext() ? scanner.next() : "";
      }

      return var3;
   }

   private static void outOfDateHandler() {
      Logger.warn("[EliteMobs] A newer version of this plugin is available for download!");
      pluginIsUpToDate = false;
   }

   private static void notifyOnlineAdmins(List<EMPackage> newlyOutdated) {
      publish(
         lifecycleGeneration,
         () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
               if (player.hasPermission("elitemobs.versionnotification")) {
                  Logger.sendSimpleMessage(player, NightbreakChatStyle.separator());
                  NightbreakPluginUpdater.PluginUpdateCheck pluginUpdateCheck = currentPluginUpdateCheck();
                  if (hasPluginUpdateNotice(pluginUpdateCheck)) {
                     sendPluginUpdateNotice(player, pluginUpdateCheck);
                     Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                  }

                  Logger.sendMessage(player, CommandMessagesConfig.getContentUpdatesAvailable().replace("$count", String.valueOf(newlyOutdated.size())));

                  for (EMPackage emPackage : newlyOutdated) {
                     String name = emPackage.getContentPackagesConfigFields().getName();
                     sendContentPageEntry(player, emPackage, CommandMessagesConfig.getContentUpdateEntry().replace("$name", name));
                  }

                  player.spigot()
                     .sendMessage(
                        new BaseComponent[]{
                           SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionUseSetupMessage()),
                           SpigotMessage.commandHoverMessage(InitializeConfig.getEmSetupDisplay(), InitializeConfig.getEmSetupHover(), "/em setup"),
                           SpigotMessage.simpleMessage(" &8| "),
                           SpigotMessage.hoverLinkMessage(
                              InitializeConfig.getContentLinkDisplay(),
                              InitializeConfig.getContentLinkHover(),
                              "https://nightbreak.io/plugin/elitemobs/#content"
                           )
                        }
                     );
                  if (NightbreakAccount.hasToken()) {
                     player.spigot()
                        .sendMessage(
                           SpigotMessage.commandHoverMessage(
                              CommandMessagesConfig.getVersionAutoUpdateMessage(), CommandMessagesConfig.getVersionAutoUpdateHover(), "/em updatecontent"
                           )
                        );
                  }

                  Logger.sendSimpleMessage(player, NightbreakChatStyle.separator());
               }
            }
         }
      );
   }

   public static void check() {
      long generation = lifecycleGeneration;
      checkPluginVersion();
      checkContentVersion();
      if (tokenChangeListener != null) {
         tokenChangeListener.close();
      }

      tokenChangeListener = NightbreakAccount.registerTokenChangeListener(() -> publish(generation, () -> {
         contentRefreshSequence++;
         invalidateRefreshCooldown();
         if (NightbreakAccount.hasToken()) {
            Logger.info("Account token changed; refreshing content access info.");
            refreshContentAndAccess();
         }
      }));
      if (scheduledCheckTask != null) {
         scheduledCheckTask.cancel();
      }

      scheduledCheckTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
         if (isCurrent(generation)) {
            Logger.info("Running scheduled 24-hour version and access check...");
            checkPluginVersion();
            checkContentVersion();
         }
      }, 1728000L, 1728000L);
   }

   public static boolean refreshContentAndAccess() {
      return refreshContentAndAccess(null);
   }

   public static boolean refreshContentAndAccess(Runnable onComplete) {
      long now = System.currentTimeMillis();
      if (now - lastRefreshTimestamp < 300000L) {
         return false;
      } else {
         lastRefreshTimestamp = now;
         checkContentVersion(onComplete);
         return true;
      }
   }

   public static void applyCachedContentAndAccess() {
      Map<String, Integer> cachedVersions = new HashMap<>();
      NightbreakContentManager.getVersionCache().forEach((slugx, versionInfo) -> {
         if (slugx != null && versionInfo != null && versionInfo.versionInt > 0) {
            cachedVersions.put(slugx, versionInfo.versionInt);
         }
      });
      if (!cachedVersions.isEmpty()) {
         processContentVersionData(cachedVersions, false);
      }

      Map<String, NightbreakAccount.AccessInfo> accessCache = NightbreakContentManager.getAccessCache();

      for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
         String slug = emPackage.getContentPackagesConfigFields().getNightbreakSlug();
         if (slug != null) {
            NightbreakAccount.AccessInfo accessInfo = accessCache.get(slug);
            if (accessInfo != null) {
               emPackage.setCachedAccessInfo(accessInfo);
            }
         }
      }
   }

   private static NightbreakPluginUpdater.PluginUpdateCheck currentPluginUpdateCheck() {
      NightbreakPluginUpdater.CachedPluginUpdateCheck snapshot = NightbreakPluginUpdater.getCachedUpdateCheck(
         MetadataHandler.PLUGIN, EliteMobs.NIGHTBREAK_PLUGIN_SPEC
      );
      return snapshot.check();
   }

   private static boolean hasPluginUpdateNotice(NightbreakPluginUpdater.PluginUpdateCheck check) {
      return check != null ? check.updateAvailable() : !pluginIsUpToDate;
   }

   private static void sendContentPageEntry(Player player, EMPackage emPackage, String displayText) {
      String link = emPackage.getContentPageUrl();
      if (link != null && !link.isBlank()) {
         player.spigot()
            .sendMessage(SpigotMessage.hoverLinkMessage(displayText, ChatColorConverter.convert(CommandMessagesConfig.getVersionClickDownloadHover()), link));
      } else {
         player.sendMessage(ChatColorConverter.convert(displayText));
      }
   }

   private static void sendPluginUpdateNotice(Player player, NightbreakPluginUpdater.PluginUpdateCheck check) {
      Logger.sendSimpleMessage(player, "<g:#8B0000:#FF4500:#DAA520>EliteMobs plugin update available</g>");
      if (check != null && check.remoteVersion() != null && !check.remoteVersion().isBlank()) {
         Logger.sendSimpleMessage(player, "&7Installed plugin: &f" + check.localVersion() + " &8| &7Available plugin: &a" + check.remoteVersion());
      }

      Logger.sendSimpleMessage(player, "&eThis is separate from content updates. Downloading the plugin update requires a server restart.");
      player.spigot()
         .sendMessage(
            SpigotMessage.commandHoverMessage(
               "&a[Download the EliteMobs plugin update]", "&eRuns /em downloadpluginupdate. Restart required after download.", "/em downloadpluginupdate"
            )
         );
      player.spigot()
         .sendMessage(
            new BaseComponent[]{
               SpigotMessage.simpleMessage("&7Manual download page: "),
               SpigotMessage.hoverLinkMessage(
                  "&9&nhttps://nightbreak.io/plugin/elitemobs/download/",
                  "&7Click to open the public EliteMobs download page.",
                  "https://nightbreak.io/plugin/elitemobs/download/"
               )
            }
         );
   }

   public static void invalidateRefreshCooldown() {
      lastRefreshTimestamp = 0L;
   }

   private static void prefetchAccessInfoInternal(List<EMPackage> packageSnapshot, VersionChecker.DlcCatalog catalog, long generation, long refresh) {
      if (isCurrentContent(generation, refresh) && NightbreakAccount.hasToken()) {
         NightbreakAccount account = NightbreakAccount.getInstance();
         if (account != null) {
            Map<String, NightbreakAccount.AccessInfo> slugCache = new HashMap<>();
            Map<EMPackage, NightbreakAccount.AccessInfo> resolvedAccess = new HashMap<>();
            List<String> failedSlugs = new ArrayList<>();
            boolean authFailed = NightbreakAccount.hasAuthFailure();
            if (authFailed) {
               publishContent(generation, refresh, VersionChecker::logNightbreakTokenRejected);
            } else {
               for (EMPackage pkg : packageSnapshot) {
                  if (!isCurrentContent(generation, refresh)) {
                     return;
                  }

                  String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
                  if (slug != null && !slug.isEmpty() && catalog.slugs().contains(slug) && !catalog.unpublishedSlugs().contains(slug)) {
                     NightbreakAccount.AccessInfo info;
                     if (slugCache.containsKey(slug)) {
                        info = slugCache.get(slug);
                     } else {
                        if (NightbreakAccount.getInstance() != account) {
                           return;
                        }

                        info = account.checkAccess(slug);
                        if (!isCurrentContent(generation, refresh) || NightbreakAccount.getInstance() != account) {
                           return;
                        }

                        if (NightbreakAccount.hasAuthFailure()) {
                           authFailed = true;
                           break;
                        }

                        slugCache.put(slug, info);
                        if (info == null && !failedSlugs.contains(slug)) {
                           failedSlugs.add(slug);
                        }
                     }

                     if (info != null) {
                        resolvedAccess.put(pkg, info);
                     }
                  }
               }

               boolean rejectedToken = authFailed;
               publishContent(generation, refresh, () -> {
                  if (NightbreakAccount.getInstance() == account) {
                     resolvedAccess.forEach(EMPackage::setCachedAccessInfo);
                     if (!resolvedAccess.isEmpty()) {
                        Logger.info("Prefetched Nightbreak access info for " + resolvedAccess.size() + " content packages");
                     }

                     if (rejectedToken) {
                        logNightbreakTokenRejected();
                     } else if (!failedSlugs.isEmpty()) {
                        Logger.warn("Failed to prefetch access info for " + failedSlugs.size() + " slugs: " + String.join(", ", failedSlugs));
                     }
                  }
               });
            }
         }
      }
   }

   private static void logNightbreakTokenRejected() {
      String status = NightbreakAccount.getLastAuthFailureStatus() > 0 ? " (HTTP " + NightbreakAccount.getLastAuthFailureStatus() + ")" : "";
      Logger.warn(
         "The saved account token needs to be updated"
            + status
            + ". Get a new token at https://nightbreak.io/account/ and run /nightbreaklogin <token>, then run /em setup again."
      );
   }

   @Generated
   public static boolean isSHA1Updated() {
      return false;
   }

   private record DlcCatalog(Map<String, Integer> versions, Set<String> slugs, Set<String> unpublishedSlugs) {
   }

   public static class VersionCheckerEvents implements Listener {
      @EventHandler
      public void onPlayerLogin(final PlayerJoinEvent event) {
         if (event.getPlayer().hasPermission("elitemobs.versionnotification")) {
            final long generation = VersionChecker.lifecycleGeneration;
            (new BukkitRunnable() {
                  public void run() {
                     if (VersionChecker.isCurrent(generation) && event.getPlayer().isOnline()) {
                        if (VersionChecker.connectionFailed && event.getPlayer().hasPermission("elitemobs.admin")) {
                           event.getPlayer().sendMessage(CommandMessagesConfig.getVersionCheckConnectionWarning());
                        }

                        List<EMPackage> outdatedSnapshot;
                        synchronized (VersionChecker.outdatedPackages) {
                           VersionChecker.outdatedPackages.removeIf(emPackagex -> !emPackagex.isOutOfDate());
                           outdatedSnapshot = new ArrayList<>(VersionChecker.outdatedPackages);
                        }

                        NightbreakPluginUpdater.PluginUpdateCheck pluginUpdateCheck = VersionChecker.currentPluginUpdateCheck();
                        boolean pluginUpdateAvailable = VersionChecker.hasPluginUpdateNotice(pluginUpdateCheck);
                        if (pluginUpdateAvailable && outdatedSnapshot.isEmpty()) {
                           Logger.sendSimpleMessage(event.getPlayer(), NightbreakChatStyle.separator());
                           VersionChecker.sendPluginUpdateNotice(event.getPlayer(), pluginUpdateCheck);
                           Logger.sendSimpleMessage(event.getPlayer(), NightbreakChatStyle.separator());
                        }

                        if (!outdatedSnapshot.isEmpty()) {
                           Logger.sendSimpleMessage(event.getPlayer(), NightbreakChatStyle.separator());
                           if (pluginUpdateAvailable) {
                              VersionChecker.sendPluginUpdateNotice(event.getPlayer(), pluginUpdateCheck);
                              Logger.sendSimpleMessage(event.getPlayer(), "&8&m-----------------------------------------------------");
                           }

                           Logger.sendMessage(event.getPlayer(), CommandMessagesConfig.getDungeonsOutdatedMessage());

                           for (EMPackage emPackage : outdatedSnapshot) {
                              String name = emPackage.getContentPackagesConfigFields().getName();
                              VersionChecker.sendContentPageEntry(event.getPlayer(), emPackage, CommandMessagesConfig.getVersionOutdatedEntryPrefix() + name);
                           }

                           event.getPlayer()
                              .spigot()
                              .sendMessage(
                                 new BaseComponent[]{
                                    SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionDownloadAtMessage()),
                                    SpigotMessage.hoverLinkMessage(
                                       InitializeConfig.getContentLinkDisplay(),
                                       InitializeConfig.getContentLinkHover(),
                                       "https://nightbreak.io/plugin/elitemobs/#content"
                                    ),
                                    SpigotMessage.simpleMessage(" !")
                                 }
                              );
                           event.getPlayer()
                              .spigot()
                              .sendMessage(
                                 new BaseComponent[]{
                                    SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionUpdateEasyMessage()),
                                    SpigotMessage.hoverLinkMessage(
                                       CommandMessagesConfig.getVersionClickHereLabel(),
                                       CommandMessagesConfig.getVersionWikiHover(),
                                       "https://nightbreak.io/plugin/elitemobs/#setup"
                                    ),
                                    SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionInstallInfoMessage()),
                                    SpigotMessage.hoverLinkMessage(
                                       CommandMessagesConfig.getVersionHereLabel(),
                                       CommandMessagesConfig.getVersionDiscordHover(),
                                       "https://discord.gg/eSxvPbWYy4"
                                    ),
                                    SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionSupportRoomMessage())
                                 }
                              );
                           if (NightbreakAccount.hasToken()) {
                              event.getPlayer()
                                 .spigot()
                                 .sendMessage(
                                    SpigotMessage.commandHoverMessage(
                                       CommandMessagesConfig.getVersionAutoUpdateMessage(),
                                       CommandMessagesConfig.getVersionAutoUpdateHover(),
                                       "/em updatecontent"
                                    )
                                 );
                           }

                           Logger.sendSimpleMessage(event.getPlayer(), NightbreakChatStyle.separator());
                        }
                     }
                  }
               })
               .runTaskLater(MetadataHandler.PLUGIN, 60L);
         }
      }
   }
}
