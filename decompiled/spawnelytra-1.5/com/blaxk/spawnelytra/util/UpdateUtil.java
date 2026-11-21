package com.blaxk.spawnelytra.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import org.bukkit.plugin.Plugin;

public enum UpdateUtil {
   private static final String MODRINTH_PROJECT_ID = "Egw2R8Fj";
   private static final String VERSION_ENDPOINT = "https://api.modrinth.com/v2/project/Egw2R8Fj/version";
   private static final String USER_AGENT = "SpawnElytra-AutoUpdater";
   private static final int CONNECT_TIMEOUT = 5000;
   private static final int READ_TIMEOUT = 10000;
   private static final int DOWNLOAD_TIMEOUT = 30000;

   public static boolean downloadAndInstallUpdate(Plugin plugin, String versionNumber) throws IOException {
      if (plugin != null && versionNumber != null && !versionNumber.isEmpty()) {
         JsonObject versionInfo = fetchVersionInfo(versionNumber);
         JsonArray files = versionInfo.getAsJsonArray("files");
         if (files != null && !files.isEmpty()) {
            JsonObject targetFileInfo = selectPrimaryFile(files);
            String downloadUrl = targetFileInfo.get("url").getAsString();
            String fileName = targetFileInfo.get("filename").getAsString();
            String sanitizedFileName = sanitizeFileName(fileName);
            if (sanitizedFileName == null) {
               throw new IOException("Invalid or unsafe filename from Modrinth: " + fileName);
            } else {
               Path updateFolder = plugin.getDataFolder().getParentFile().toPath().resolve("update");
               Files.createDirectories(updateFolder);
               Path updateRoot = updateFolder.toAbsolutePath().normalize();
               Path targetFile = updateRoot.resolve(sanitizedFileName).normalize();
               Path tempFile = updateRoot.resolve(sanitizedFileName + ".tmp").normalize();
               if (targetFile.startsWith(updateRoot) && tempFile.startsWith(updateRoot)) {
                  downloadFile(downloadUrl, tempFile);
                  Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                  return true;
               } else {
                  throw new IOException("Resolved update path escapes the update directory: " + sanitizedFileName);
               }
            }
         } else {
            throw new IOException("No files found for version " + versionNumber);
         }
      } else {
         throw new IllegalArgumentException("Plugin and version number must not be null or empty");
      }
   }

   private static JsonObject selectPrimaryFile(JsonArray files) {
      for (JsonElement fileElement : files) {
         JsonObject file = fileElement.getAsJsonObject();
         JsonElement primary = file.get("primary");
         if (primary != null && primary.getAsBoolean()) {
            return file;
         }
      }

      return files.get(0).getAsJsonObject();
   }

   private static JsonObject fetchVersionInfo(String versionNumber) throws IOException {
      HttpURLConnection conn = openConnection("https://api.modrinth.com/v2/project/Egw2R8Fj/version", 10000);

      JsonObject var7;
      try {
         requireOk(conn);

         try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) {
               throw new IOException("Unexpected response from Modrinth");
            }

            Iterator var4 = root.getAsJsonArray().iterator();

            JsonObject obj;
            do {
               if (!var4.hasNext()) {
                  throw new IOException("Version " + versionNumber + " not found on Modrinth");
               }

               JsonElement elem = (JsonElement)var4.next();
               obj = elem.getAsJsonObject();
            } while (!obj.get("version_number").getAsString().equals(versionNumber));

            var7 = obj;
         }
      } finally {
         conn.disconnect();
      }

      return var7;
   }

   private static void downloadFile(String fileUrl, Path destination) throws IOException {
      HttpURLConnection conn = openConnection(fileUrl, 30000);

      try {
         requireOk(conn);

         try (InputStream in = conn.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
         }
      } finally {
         conn.disconnect();
      }
   }

   private static HttpURLConnection openConnection(String url, int readTimeout) throws IOException {
      HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("User-Agent", "SpawnElytra-AutoUpdater");
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(readTimeout);
      return conn;
   }

   private static void requireOk(HttpURLConnection conn) throws IOException {
      int status = conn.getResponseCode();
      if (200 != status) {
         throw new IOException("HTTP " + status + " " + conn.getResponseMessage());
      }
   }

   private static String sanitizeFileName(String fileName) {
      if (fileName != null && !fileName.isEmpty()) {
         String sanitized = fileName.replace('\\', '/');
         int lastSlash = sanitized.lastIndexOf(47);
         if (lastSlash >= 0) {
            sanitized = sanitized.substring(lastSlash + 1);
         }

         sanitized = sanitized.replace("..", "").replace("\u0000", "");
         return !sanitized.matches("^[a-zA-Z0-9._-]+\\.jar$") ? null : sanitized;
      } else {
         return null;
      }
   }
}
