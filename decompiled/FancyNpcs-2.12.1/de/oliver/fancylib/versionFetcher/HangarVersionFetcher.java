package de.oliver.fancylib.versionFetcher;

import org.apache.maven.artifact.versioning.ComparableVersion;

public class HangarVersionFetcher implements VersionFetcher {
   private final String pluginName;
   private ComparableVersion newestVersion;

   public HangarVersionFetcher(String pluginName) {
      this.pluginName = pluginName;
      this.newestVersion = null;
   }

   @Override
   public ComparableVersion fetchNewestVersion() {
      if (this.newestVersion != null) {
         return this.newestVersion;
      } else {
         String versionStr = VersionFetcher.getDataFromUrl("https://hangar.papermc.io/api/v1/projects/" + this.pluginName + "/latestrelease");
         if (versionStr != null && !versionStr.isEmpty()) {
            this.newestVersion = new ComparableVersion(versionStr);
            return this.newestVersion;
         } else {
            return null;
         }
      }
   }

   @Override
   public String getDownloadUrl() {
      return "https://hangar.papermc.io/Oliver/" + this.pluginName;
   }
}
