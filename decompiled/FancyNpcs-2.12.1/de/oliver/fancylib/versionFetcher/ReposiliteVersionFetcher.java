package de.oliver.fancylib.versionFetcher;

import com.google.gson.Gson;
import java.util.Map;
import org.apache.maven.artifact.versioning.ComparableVersion;

public class ReposiliteVersionFetcher implements VersionFetcher {
   private final String pluginName;
   private ComparableVersion newestVersion;

   public ReposiliteVersionFetcher(String pluginName) {
      this.pluginName = pluginName;
      this.newestVersion = null;
   }

   @Override
   public ComparableVersion fetchNewestVersion() {
      if (this.newestVersion != null) {
         return this.newestVersion;
      } else {
         String jsonString = VersionFetcher.getDataFromUrl("https://repo.fancyinnovations.com/api/maven/latest/version/releases/de/oliver/" + this.pluginName);
         if (jsonString != null && !jsonString.isEmpty()) {
            Gson gson = new Gson();
            Map<String, Object> data = gson.fromJson(jsonString, Map.class);
            String versionStr = (String)data.get("version");
            this.newestVersion = new ComparableVersion(versionStr);
            return this.newestVersion;
         } else {
            return null;
         }
      }
   }

   @Override
   public String getDownloadUrl() {
      return "https://modrinth.com/plugin/" + this.pluginName;
   }
}
