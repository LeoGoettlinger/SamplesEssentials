package me.chancesd.pvpmanager.sdutils.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import org.bukkit.plugin.Plugin;

public class BukkitUpdater extends Updater {
   private URL url;
   private static final String TITLE_VALUE = "name";
   private static final String LINK_VALUE = "downloadUrl";
   private static final String TYPE_VALUE = "releaseType";
   private static final String VERSION_VALUE = "gameVersion";
   private static final String QUERY = "/servermods/files?projectIds=";
   private static final String HOST = "https://api.curseforge.com";

   public BukkitUpdater(Plugin plugin, int id, Updater.UpdateType type, String projectSlug) {
      super(plugin, id, type, projectSlug);

      try {
         this.url = new URL("https://api.curseforge.com/servermods/files?projectIds=" + id);
      } catch (MalformedURLException var6) {
         Log.severe("The project ID provided for updating, " + id + " is invalid.");
         this.setResult(Updater.UpdateResult.FAIL_BADID);
         var6.printStackTrace();
      }
   }

   public BukkitUpdater(Plugin plugin, int id, Updater.UpdateType type) {
      this(plugin, id, type, plugin.getName());
   }

   @Override
   public final boolean downloadFile() {
      try {
         HttpURLConnection httpConn = (HttpURLConnection)new URL(this.versionLink).openConnection();
         int responseCode = httpConn.getResponseCode();
         if (responseCode == 301) {
            String location = httpConn.getHeaderField("Location");
            httpConn = (HttpURLConnection)new URL(location).openConnection();
         }

         Files.copy(httpConn.getInputStream(), this.getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
         return true;
      } catch (IOException var4) {
         var4.printStackTrace();
         return false;
      }
   }

   @Override
   protected boolean read() {
      try {
         URLConnection conn = this.url.openConnection();
         conn.setConnectTimeout(5000);
         conn.addRequestProperty("User-Agent", "Updater");
         conn.setDoOutput(true);

         boolean var6;
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = reader.readLine();
            JsonArray array = new JsonParser().parse(response).getAsJsonArray();
            if (array.size() == 0) {
               Log.warning("The updater could not find any files for the project id " + this.getId());
               this.setResult(Updater.UpdateResult.FAIL_BADID);
               return false;
            }

            JsonObject jsonObject = array.get(array.size() - 1).getAsJsonObject();
            this.versionName = jsonObject.get("name").getAsString();
            this.versionLink = jsonObject.get("downloadUrl").getAsString();
            this.versionType = jsonObject.get("releaseType").getAsString();
            this.versionGameVersion = jsonObject.get("gameVersion").getAsString();
            if (this.versionName.split(" v").length == 2) {
               this.versionName = this.versionName.split(" v")[1].split(" ")[0];
            }

            var6 = true;
         }

         return var6;
      } catch (IOException var9) {
         Log.warning("The updater could not contact dev.bukkit.org for updating.");
         Log.warning(
            "If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime."
         );
         this.setResult(Updater.UpdateResult.FAIL_DBO);
         var9.printStackTrace();
         return false;
      }
   }

   @Override
   public String getUpdateLink() {
      return "https://dev.bukkit.org/projects/" + this.getProjectSlug();
   }
}
