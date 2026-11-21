package com.magmaguy.elitemobs.magmacore.util;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakPluginUpdater;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class VersionChecker {
   private static final boolean SHA1Updated = false;
   private static final long UPDATE_REFRESH_INTERVAL_TICKS = 72000L;
   private static final long INITIAL_UPDATE_REFRESH_DELAY_TICKS = 6000L;
   private static final long BOOT_STAGGER_MAX_TICKS = 2400L;
   private static final int SPIGOT_FALLBACK_TIMEOUT_MS = 3000;
   private static final Set<String> SCHEDULED_VERSION_CHECKS = ConcurrentHashMap.newKeySet();
   private static final ConcurrentHashMap<String, String> LAST_LOGGED_STATE = new ConcurrentHashMap<>();
   private static boolean pluginIsUpToDate = true;
   private static volatile VersionChecker.ServerVersion cachedServerVersion;

   private VersionChecker() {
   }

   public static boolean serverVersionOlderThan(int majorVersion, int minorVersion) {
      Server server = Bukkit.getServer();
      VersionChecker.ServerVersion version = cachedServerVersion;
      if (version == null || version.server() != server) {
         version = readServerVersion(server);
      }

      return version.major() < majorVersion || version.major() == majorVersion && version.minor() < minorVersion;
   }

   private static synchronized VersionChecker.ServerVersion readServerVersion(Server server) {
      VersionChecker.ServerVersion cached = cachedServerVersion;
      if (cached != null && cached.server() == server) {
         return cached;
      } else {
         String[] splitVersion = server.getBukkitVersion().split("[.]");
         int actualMinorVersion = 0;
         int actualMajorVersion;
         if (splitVersion[0].equals("1")) {
            actualMajorVersion = Integer.parseInt(splitVersion[1].split("-")[0]);
            if (splitVersion.length > 2) {
               actualMinorVersion = Integer.parseInt(splitVersion[2].split("-")[0]);
            }
         } else {
            actualMajorVersion = Integer.parseInt(splitVersion[0].split("-")[0]);
            if (splitVersion.length > 1) {
               actualMinorVersion = Integer.parseInt(splitVersion[1].split("-")[0]);
            }
         }

         VersionChecker.ServerVersion version = new VersionChecker.ServerVersion(server, actualMajorVersion, actualMinorVersion);
         cachedServerVersion = version;
         return version;
      }
   }

   public static void checkPluginVersion(String resourceID) {
      checkPluginVersion(resourceID, VersionChecker.VersionCheckerEvents.getDownloadURL());
   }

   public static void checkPluginVersion(String resourceID, String downloadURL) {
      String taskKey = MagmaCore.getInstance().getRequestingPlugin().getName() + ":" + resourceID + ":" + downloadURL;
      if (SCHEDULED_VERSION_CHECKS.add(taskKey)) {
         Bukkit.getScheduler().runTaskTimerAsynchronously(MagmaCore.getInstance().getRequestingPlugin(), () -> {
            String rawCurrentVersion = MagmaCore.getInstance().getRequestingPlugin().getDescription().getVersion();
            boolean snapshot = rawCurrentVersion.contains("SNAPSHOT");
            String currentVersion = NightbreakPluginUpdater.cleanVersion(rawCurrentVersion);
            String publicVersion = null;
            String versionSource = null;
            String pluginSlug = pluginSlugFromNightbreakUrl(downloadURL);
            if (pluginSlug != null) {
               NightbreakAccount.VersionInfo versionInfo = NightbreakAccount.getPublicPluginVersion(pluginSlug, false);
               if (versionInfo != null && versionInfo.version != null && !versionInfo.version.isBlank()) {
                  publicVersion = versionInfo.version;
                  versionSource = "nightbreak";
                  VersionChecker.VersionCheckerEvents.setDownloadURL(downloadPageUrl(pluginSlug));
               }
            }

            if ((publicVersion == null || publicVersion.isBlank()) && resourceID != null && !resourceID.isBlank()) {
               try {
                  publicVersion = readStringFromURL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceID).trim();
                  versionSource = "spigot";
               } catch (IOException var11) {
                  Logger.warn("Couldn't check latest plugin version from Nightbreak or Spigot.");
                  return;
               }
            }

            if (publicVersion != null && !publicVersion.isBlank()) {
               boolean upToDate = NightbreakPluginUpdater.compareVersions(publicVersion, currentVersion) <= 0;
               pluginIsUpToDate = upToDate;
               String signature = versionSource + "|" + publicVersion + "|" + rawCurrentVersion + "|" + upToDate;
               if (!signature.equals(LAST_LOGGED_STATE.put(taskKey, signature))) {
                  if ("nightbreak".equals(versionSource)) {
                     Logger.info("Latest Nightbreak release is " + publicVersion);
                  } else {
                     Logger.info("Nightbreak unavailable; latest Spigot release is " + publicVersion);
                  }

                  Logger.info("Your version is " + rawCurrentVersion);
                  if (!upToDate) {
                     outOfDateHandler();
                  } else if (!snapshot) {
                     Logger.info("You are running the latest version!");
                  } else {
                     Logger.info("You are running a snapshot version! You can check for updates in the #releases channel on the Nightbreak Discord!");
                  }
               }
            } else {
               Logger.warn("Couldn't check latest plugin version.");
            }
         }, initialRefreshDelayTicks(taskKey), 72000L);
      }
   }

   private static String readStringFromURL(String url) throws IOException {
      URLConnection connection = new URL(url).openConnection();
      connection.setConnectTimeout(3000);
      connection.setReadTimeout(3000);

      String var3;
      try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
         scanner.useDelimiter("\\A");
         var3 = scanner.hasNext() ? scanner.next() : "";
      }

      return var3;
   }

   private static long initialRefreshDelayTicks(String taskKey) {
      return 6000L + Math.floorMod(taskKey.hashCode(), 2400);
   }

   private static void outOfDateHandler() {
      Logger.warn("A newer version of this plugin is available for download!");
   }

   private static String pluginSlugFromNightbreakUrl(String downloadURL) {
      if (downloadURL != null && !downloadURL.isBlank()) {
         try {
            URI uri = URI.create(downloadURL);
            String[] parts = uri.getPath().split("/");

            for (int i = 0; i < parts.length - 1; i++) {
               if ("plugin".equals(parts[i]) && !parts[i + 1].isBlank()) {
                  return parts[i + 1];
               }
            }

            return null;
         } catch (IllegalArgumentException var4) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String downloadPageUrl(String pluginSlug) {
      return "https://nightbreak.io/plugin/" + pluginSlug + "/download/";
   }

   @Generated
   public static boolean isSHA1Updated() {
      return false;
   }

   private record ServerVersion(Server server, int major, int minor) {
   }

   public static class VersionCheckerEvents implements Listener {
      private static String downloadURL;

      @EventHandler
      public void onPlayerJoinEvent(final PlayerJoinEvent event) {
         (new BukkitRunnable() {
               public void run() {
                  if (event.getPlayer().isOp()) {
                     if (event.getPlayer().isOnline()) {
                        if (!VersionChecker.pluginIsUpToDate) {
                           Logger.sendMessage(
                              event.getPlayer(),
                              ChatColorConverter.convert(
                                 "&a["
                                    + MagmaCore.getInstance().getRequestingPlugin().getName()
                                    + "] &cYour version of "
                                    + MagmaCore.getInstance().getRequestingPlugin().getName()
                                    + " is outdated. &aYou can download the latest version from &3&n&o"
                                    + VersionChecker.VersionCheckerEvents.downloadURL
                              )
                           );
                        }
                     }
                  }
               }
            })
            .runTaskLater(MagmaCore.getInstance().getRequestingPlugin(), 60L);
      }

      @Generated
      public static String getDownloadURL() {
         return downloadURL;
      }

      @Generated
      public static void setDownloadURL(String downloadURL) {
         VersionChecker.VersionCheckerEvents.downloadURL = downloadURL;
      }
   }
}
