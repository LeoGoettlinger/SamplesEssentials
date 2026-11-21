package de.oliver.fancylib.versionFetcher;

import java.util.LinkedList;
import org.apache.maven.artifact.versioning.ComparableVersion;

public class MasterVersionFetcher implements VersionFetcher {
   private final String pluginName;
   private ComparableVersion newestVersion;
   private LinkedList<VersionFetcher> fetchers;

   public MasterVersionFetcher(String pluginName) {
      this.pluginName = pluginName;
      this.fetchers = new LinkedList<>();
      this.fetchers.push(new ReposiliteVersionFetcher(pluginName));
      this.fetchers.push(new HangarVersionFetcher(pluginName));
      this.fetchers.push(new FancySpacesVersionFetcher(pluginName));
      this.fetchers.push(new ModrinthVersionFetcher(pluginName));
   }

   @Override
   public ComparableVersion fetchNewestVersion() {
      for (VersionFetcher fetcher : this.fetchers) {
         ComparableVersion version = fetcher.fetchNewestVersion();
         if (version != null) {
            this.newestVersion = version;
            return this.newestVersion;
         }
      }

      return null;
   }

   @Override
   public String getDownloadUrl() {
      return "https://modrinth.com/plugin/" + this.pluginName;
   }
}
