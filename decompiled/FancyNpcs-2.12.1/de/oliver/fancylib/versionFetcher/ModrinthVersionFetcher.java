package de.oliver.fancylib.versionFetcher;

import com.google.gson.Gson;
import java.util.Map;
import org.apache.maven.artifact.versioning.ComparableVersion;

public class ModrinthVersionFetcher implements VersionFetcher {
   private final String pluginName;
   private ComparableVersion newestVersion;

   public ModrinthVersionFetcher(String pluginName) {
      this.pluginName = pluginName;
      this.newestVersion = null;
   }

   @Override
   public ComparableVersion fetchNewestVersion() {
      if (this.newestVersion != null) {
         return this.newestVersion;
      } else {
         String jsonString = VersionFetcher.getDataFromUrl("https://api.modrinth.com/v2/project/" + this.pluginName.toLowerCase() + "/version");
         if (jsonString != null && !jsonString.isEmpty()) {
            Gson gson = new Gson();
            Map<String, Object>[] versions = gson.fromJson(jsonString, Map[].class);

            for (Map<String, Object> version : versions) {
               if (version.get("version_type").equals("release")) {
                  String versionNumber = (String)version.get("version_number");
                  this.newestVersion = new ComparableVersion(versionNumber);
                  break;
               }
            }

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
