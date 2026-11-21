package de.oliver.fancylib;

import de.oliver.fancylib.versionFetcher.VersionFetcher;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class VersionConfig {
   private final Plugin plugin;
   private final VersionFetcher fetcher;
   private String version;
   private String commitHash;
   private String channel;
   private String platform;

   public VersionConfig(Plugin plugin, VersionFetcher fetcher) {
      this.plugin = plugin;
      this.fetcher = fetcher;
   }

   public void load() {
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.loadFromString(new FileUtils().readResource("version.yml"));
      } catch (InvalidConfigurationException var3) {
         var3.printStackTrace();
      }

      this.version = config.getString("version", "undefined");
      this.commitHash = config.getString("commit_hash", "undefined");
      this.channel = config.getString("channel", "undefined");
      this.platform = config.getString("platform", "undefined");
   }

   public boolean isDevelopmentBuild() {
      return !this.channel.equalsIgnoreCase("release");
   }

   public void checkVersionAndDisplay(CommandSender sender, boolean displayOnlyIfOutdated) {
      String newestVersion = this.usingLatestVersion();
      if (newestVersion == null) {
         MessageHelper.error(sender, "Could not fetch latest version");
      } else if (!newestVersion.isEmpty()) {
         MessageHelper.warning(sender, this.outdatedVersion(newestVersion, this.fetcher.getDownloadUrl()));
      } else {
         if (!displayOnlyIfOutdated) {
            MessageHelper.success(sender, this.latestVersion());
         }
      }
   }

   private String usingLatestVersion() {
      ComparableVersion newestVersion = this.fetcher.fetchNewestVersion();
      ComparableVersion currentVersion = new ComparableVersion(this.version);
      if (newestVersion == null) {
         return null;
      } else {
         return newestVersion.compareTo(currentVersion) <= 0 ? "" : newestVersion.toString();
      }
   }

   private String latestVersion() {
      String result = "This server is using the latest version of {plugin}!\nVersion: {version} (Git: {hash})";
      return result.replace("{plugin}", this.plugin.getName()).replace("{version}", this.version).replace("{hash}", this.commitHash.substring(0, 7));
   }

   private String outdatedVersion(String latestVersion, String downloadUrl) {
      String result = "This server is using an outdated version of {plugin}\nCurrent version: {current_ver}\nLatest version: {latest_ver}\nDownload latest version: <click:open_url:'{download_url}'><u>click here</u></click>";
      return result.replace("{plugin}", this.plugin.getName())
         .replace("{current_ver}", this.version)
         .replace("{latest_ver}", latestVersion)
         .replace("{download_url}", downloadUrl);
   }

   public String getVersion() {
      return this.version;
   }

   public String getCommitHash() {
      return this.commitHash;
   }

   public String getChannel() {
      return this.channel;
   }

   public String getPlatform() {
      return this.platform;
   }
}
