package de.oliver.fancylib.versionFetcher;

import com.google.gson.Gson;
import java.util.Map;
import org.apache.maven.artifact.versioning.ComparableVersion;

public class FancySpacesVersionFetcher implements VersionFetcher {
   private final String pluginName;
   private ComparableVersion newestVersion;

   public FancySpacesVersionFetcher(String pluginName) {
      this.pluginName = pluginName;
      this.newestVersion = null;
   }

   @Override
   public ComparableVersion fetchNewestVersion() {
      if (this.newestVersion != null) {
         return this.newestVersion;
      } else {
         String jsonString = VersionFetcher.getDataFromUrl(
            "https://fancyspaces.net/api/v1/spaces/" + this.pluginName.toLowerCase() + "/versions?channel=release&platform=paper"
         );
         if (jsonString != null && !jsonString.isEmpty()) {
            Gson gson = new Gson();
            Map<String, Object> version = gson.fromJson(jsonString, Map.class);
            String versionNumber = (String)version.get("name");
            this.newestVersion = new ComparableVersion(versionNumber);
            return this.newestVersion;
         } else {
            return null;
         }
      }
   }

   @Override
   public String getDownloadUrl() {
      return "https://fancyspaces.net/spaces/" + this.pluginName + "/versions";
   }
}
