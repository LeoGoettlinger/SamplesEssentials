package de.theredend2000.advancedhunt.util.updater.source;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.theredend2000.advancedhunt.util.updater.UpdateInfo;
import de.theredend2000.advancedhunt.util.updater.UpdateSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class SpigotSource implements UpdateSource {
   private static final String API_URL = "https://api.spiget.org/v2/resources/%s/versions?size=10&sort=-releaseDate";
   private static final String DOWNLOAD_URL = "https://api.spiget.org/v2/resources/%s/versions/%s/download";

   @Override
   public CompletableFuture<UpdateInfo> getLatestUpdate(String id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            URL url = new URL(String.format("https://api.spiget.org/v2/resources/%s/versions?size=10&sort=-releaseDate", id));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "AdvancedHunt/Updater");
            if (connection.getResponseCode() == 200) {
               InputStreamReader reader = new InputStreamReader(connection.getInputStream());

               for (JsonElement element : JsonParser.parseReader(reader).getAsJsonArray()) {
                  JsonObject versionObj = element.getAsJsonObject();
                  String versionName = versionObj.get("name").getAsString();
                  if (!this.isUnstable(versionName)) {
                     String cleaned = this.cleanVersion(versionName);
                     Instant publishedAt = null;
                     if (versionObj.has("releaseDate") && !versionObj.get("releaseDate").isJsonNull()) {
                        long raw = versionObj.get("releaseDate").getAsLong();
                        publishedAt = Instant.ofEpochMilli(this.normalizeEpochMillis(raw));
                     }

                     return new UpdateInfo(cleaned, versionName, publishedAt);
                  }
               }
            }
         } catch (Exception var14) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to check Spigot updates", (Throwable)var14);
         }

         return null;
      });
   }

   private long normalizeEpochMillis(long value) {
      return value > 0L && value < 1000000000000L ? value * 1000L : value;
   }

   private String cleanVersion(String version) {
      if (version.toLowerCase().startsWith("v") && version.length() > 1 && Character.isDigit(version.charAt(1))) {
         version = version.substring(1);
      }

      if (version.contains(" ")) {
         String[] parts = version.split(" ");
         String last = parts[parts.length - 1];
         if (Character.isDigit(last.charAt(0)) || last.toLowerCase().startsWith("v") && last.length() > 1 && Character.isDigit(last.charAt(1))) {
            return this.cleanVersion(last);
         }
      }

      return version;
   }

   private boolean isUnstable(String version) {
      String v = version.toLowerCase();
      return v.contains("alpha") || v.contains("beta") || v.contains("snapshot") || v.contains("rc");
   }

   @Override
   public CompletableFuture<Boolean> downloadPlugin(String id, UpdateInfo update, File destination) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            String versionId = this.getVersionId(id, update.sourceVersion());
            if (versionId == null) {
               return false;
            } else {
               String downloadUrl = String.format("https://api.spiget.org/v2/resources/%s/versions/%s/download", id, versionId);
               return this.downloadFile(downloadUrl, destination);
            }
         } catch (Exception var6) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to download from Spigot", (Throwable)var6);
            return false;
         }
      });
   }

   private String getVersionId(String resourceId, String versionName) {
      try {
         URL url = new URL(String.format("https://api.spiget.org/v2/resources/%s/versions?size=10&sort=-releaseDate", resourceId));
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         connection.setRequestProperty("User-Agent", "AdvancedHunt/Updater");
         if (connection.getResponseCode() == 200) {
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());

            for (JsonElement element : JsonParser.parseReader(reader).getAsJsonArray()) {
               JsonObject versionObj = element.getAsJsonObject();
               if (versionObj.get("name").getAsString().equals(versionName)) {
                  return versionObj.get("id").getAsString();
               }
            }
         }
      } catch (Exception var10) {
         Bukkit.getLogger().severe("Failed to get version ID from Spigot: " + var10.getMessage());
      }

      return null;
   }

   private boolean downloadFile(String urlString, File destination) {
      try {
         URL url = new URL(urlString);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestProperty("User-Agent", "AdvancedHunt/Updater");
         InputStream in = connection.getInputStream();

         boolean var6;
         try {
            Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            var6 = true;
         } catch (Throwable var9) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (in != null) {
            in.close();
         }

         return var6;
      } catch (IOException var10) {
         Bukkit.getLogger().severe("Failed to download file from Spigot: " + var10.getMessage());
         return false;
      }
   }

   @Override
   public String getName() {
      return "SpigotMC";
   }
}
