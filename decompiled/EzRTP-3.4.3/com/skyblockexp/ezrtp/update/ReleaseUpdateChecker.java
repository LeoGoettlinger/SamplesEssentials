package com.skyblockexp.ezrtp.update;

import com.skyblockexp.ezrtp.libs.updater.ChainedUpdateChecker;
import com.skyblockexp.ezrtp.libs.updater.ModrinthUpdateChecker;
import com.skyblockexp.ezrtp.libs.updater.UpdateChecker;
import com.skyblockexp.ezrtp.libs.updater.UpdateResult;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReleaseUpdateChecker {
   private static final String MODRINTH_PROJECT_SLUG = "ezplugins-ezrtp";
   private static final String GITHUB_OWNER = "ez-plugins";
   private static final String GITHUB_REPO = "EzRTP";
   private final JavaPlugin plugin;
   private final String userAgent;

   public ReleaseUpdateChecker(JavaPlugin plugin) {
      this.plugin = plugin;
      this.userAgent = "ez-plugins/ezrtp-update-checker (ez-plugins/EzRTP)";
   }

   public void checkForUpdates() {
      String currentVersion = this.plugin.getDescription().getVersion();
      if (currentVersion != null && currentVersion.contains("-nightly.")) {
         this.plugin.getLogger().info("Running a nightly build (" + currentVersion + "); skipping update check. Check Modrinth for the latest stable release.");
      } else {
         ModrinthUpdateChecker modrinthChecker = ModrinthUpdateChecker.builder("ezplugins-ezrtp", currentVersion)
            .loaders(List.of("paper", "spigot", "purpur", "bukkit"))
            .includeChangelog(false)
            .userAgent(this.userAgent)
            .build();
         UpdateChecker githubChecker = UpdateChecker.builder("ez-plugins", "EzRTP", currentVersion).build();
         ChainedUpdateChecker.builder()
            .primary(modrinthChecker)
            .backup(githubChecker)
            .build()
            .checkNowAsync()
            .thenAccept(chainedResult -> this.logResult(currentVersion, chainedResult.getResult()))
            .exceptionally(ex -> {
               this.plugin.getLogger().warning("Failed to check for EzRTP updates: " + ex.getMessage());
               return null;
            });
      }
   }

   private void logResult(String currentVersion, UpdateResult result) {
      if (result == null) {
         this.plugin.getLogger().warning("Update check did not return a result.");
      } else if (result.hasError()) {
         this.plugin.getLogger().warning("Update check failed: " + result.getError().map(Throwable::getMessage).orElse("unknown error"));
      } else if (result.isUpdateAvailable()) {
         String latest = result.getLatestVersion().orElse("unknown");
         String releaseUrl = result.getReleaseUrl().orElse("https://modrinth.com/plugin/ezplugins-ezrtp");
         this.plugin.getLogger().info("A new EzRTP version is available: " + latest + " (current: " + currentVersion + "). Download: " + releaseUrl);
      } else {
         this.plugin.getLogger().info("EzRTP is up to date.");
      }
   }
}
