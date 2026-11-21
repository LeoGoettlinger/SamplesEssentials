package de.theredend2000.advancedhunt.util.updater.source;

import com.google.gson.JsonArray;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class ModrinthSource implements UpdateSource {
   private static final String API_URL = "https://api.modrinth.com/v2/project/%s/version";
   private static final String LOADER = detectLoader();

   private static String detectLoader() {
      if (isClassPresent("io.papermc.paper.threadedregions.RegionizedServer")) {
         return "folia";
      } else if (isClassPresent("org.purpurmc.purpur.PurpurConfig")) {
         return "purpur";
      } else if (isClassPresent("io.papermc.paper.configuration.Configuration")) {
         return "paper";
      } else if (isClassPresent("com.destroystokyo.paper.PaperConfig")) {
         return "paper";
      } else {
         return isClassPresent("org.spigotmc.SpigotConfig") ? "spigot" : "bukkit";
      }
   }

   private static boolean isClassPresent(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      } catch (Throwable var3) {
         return false;
      }
   }

   @Override
   public CompletableFuture<UpdateInfo> getLatestUpdate(String id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            String loadersParam = URLEncoder.encode("[\"" + LOADER + "\"]", StandardCharsets.UTF_8.name());
            String mcVersion = this.getServerMinecraftVersion();
            UpdateInfo result = this.fetchLatestRelease(id, loadersParam, URLEncoder.encode("[\"" + mcVersion + "\"]", StandardCharsets.UTF_8.name()));
            if (result == null) {
               result = this.fetchLatestRelease(id, loadersParam, null);
            }

            return result;
         } catch (Exception var5) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to check Modrinth updates", (Throwable)var5);
            return null;
         }
      });
   }

   private UpdateInfo fetchLatestRelease(String id, String loadersParam, String gameVersionsParam) throws Exception {
      StringBuilder urlBuilder = new StringBuilder(String.format("https://api.modrinth.com/v2/project/%s/version", id))
         .append("?loaders=")
         .append(loadersParam);
      if (gameVersionsParam != null) {
         urlBuilder.append("&game_versions=").append(gameVersionsParam);
      }

      HttpURLConnection connection = (HttpURLConnection)new URL(urlBuilder.toString()).openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("User-Agent", "AdvancedHunt/Updater");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(10000);
      if (connection.getResponseCode() != 200) {
         return null;
      } else {
         for (JsonElement element : JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonArray()) {
            JsonObject versionObj = element.getAsJsonObject();
            String versionType = versionObj.get("version_type").getAsString();
            if (versionType.equalsIgnoreCase("release") && this.isCompatibleLoader(versionObj)) {
               String sourceVersion = versionObj.get("version_number").getAsString();
               String version = this.cleanVersion(sourceVersion);
               Instant publishedAt = null;
               if (versionObj.has("date_published") && !versionObj.get("date_published").isJsonNull()) {
                  publishedAt = Instant.parse(versionObj.get("date_published").getAsString());
               }

               return new UpdateInfo(version, sourceVersion, publishedAt);
            }
         }

         return null;
      }
   }

   private boolean isCompatibleLoader(JsonObject versionObj) {
      if (!versionObj.has("loaders")) {
         return true;
      } else {
         String detected = LOADER;

         for (JsonElement loader : versionObj.getAsJsonArray("loaders")) {
            if (isLoaderCompatibleWith(loader.getAsString().toLowerCase(), detected)) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean isLoaderCompatibleWith(String candidate, String detected) {
      switch (detected) {
         case "folia":
            return candidate.equals("folia")
               || candidate.equals("purpur")
               || candidate.equals("paper")
               || candidate.equals("spigot")
               || candidate.equals("bukkit");
         case "purpur":
            return candidate.equals("purpur") || candidate.equals("paper") || candidate.equals("spigot") || candidate.equals("bukkit");
         case "paper":
            return candidate.equals("paper") || candidate.equals("spigot") || candidate.equals("bukkit");
         case "spigot":
            return candidate.equals("spigot") || candidate.equals("bukkit");
         default:
            return candidate.equals("bukkit");
      }
   }

   private String getServerMinecraftVersion() {
      String bukkit = Bukkit.getBukkitVersion();
      int dash = bukkit.indexOf(45);
      return dash > 0 ? bukkit.substring(0, dash) : bukkit;
   }

   private String cleanVersion(String version) {
      return version.toLowerCase().startsWith("v") && version.length() > 1 && Character.isDigit(version.charAt(1)) ? version.substring(1) : version;
   }

   @Override
   public CompletableFuture<Boolean> downloadPlugin(String id, UpdateInfo update, File destination) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            String detectedLoader = LOADER;
            String loadersParam = URLEncoder.encode("[\"" + detectedLoader + "\"]", StandardCharsets.UTF_8.name());
            URL url = new URL(String.format("https://api.modrinth.com/v2/project/%s/version", id) + "?loaders=" + loadersParam);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "AdvancedHunt/Updater");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            if (connection.getResponseCode() == 200) {
               for (JsonElement element : JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonArray()) {
                  JsonObject versionObj = element.getAsJsonObject();
                  String sourceVersion = versionObj.get("version_number").getAsString();
                  if (sourceVersion.equals(update.sourceVersion()) || this.cleanVersion(sourceVersion).equals(update.version())) {
                     if (!this.isCompatibleLoader(versionObj)) {
                        Bukkit.getLogger().warning("[Modrinth] Version " + sourceVersion + " does not target a Bukkit-compatible platform. Aborting download.");
                        return false;
                     }

                     JsonArray files = versionObj.getAsJsonArray("files");
                     String downloadUrl = this.selectPrimaryFile(files);
                     if (downloadUrl != null) {
                        return this.downloadFile(downloadUrl, destination);
                     }
                  }
               }
            }
         } catch (Exception var15) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to download from Modrinth", (Throwable)var15);
         }

         return false;
      });
   }

   private String selectPrimaryFile(JsonArray files) {
      if (files != null && files.size() != 0) {
         for (JsonElement file : files) {
            JsonObject fileObj = file.getAsJsonObject();
            if (fileObj.has("primary") && fileObj.get("primary").getAsBoolean()) {
               return fileObj.get("url").getAsString();
            }
         }

         return files.get(0).getAsJsonObject().get("url").getAsString();
      } else {
         return null;
      }
   }

   private boolean downloadFile(String urlString, File destination) {
      try {
         URL url = new URL(urlString);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(60000);
         connection.setInstanceFollowRedirects(true);
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
         Bukkit.getLogger().severe("Failed to download file from Modrinth: " + var10.getMessage());
         return false;
      }
   }

   @Override
   public String getName() {
      return "Modrinth";
   }
}
