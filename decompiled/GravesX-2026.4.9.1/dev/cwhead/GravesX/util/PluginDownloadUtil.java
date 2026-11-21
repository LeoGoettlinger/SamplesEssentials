package dev.cwhead.GravesX.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class PluginDownloadUtil {
   private static final int CONNECT_TIMEOUT_MS = 10000;
   private static final int READ_TIMEOUT_MS = 30000;
   private static final int BUFFER_SIZE = 8192;
   private static final String USER_AGENT = "GravesX/1 PluginDownloadUtil (+spiget)";
   private static final OutputStream DISCARD = new OutputStream() {
      @Override
      public void write(int b) {
      }

      @Override
      public void write(@NotNull byte[] b, int off, int len) {
      }

      @Override
      public void write(@NotNull byte[] b) {
      }
   };

   private PluginDownloadUtil() {
   }

   public static void downloadAndReplacePlugin(long pluginId, String pluginName, String pluginsFolder, CommandSender commandSender) throws IOException {
      downloadAndReplacePlugin(String.valueOf(pluginId), pluginName, pluginsFolder, commandSender);
   }

   public static void downloadAndReplacePlugin(String pluginId, String pluginName, String pluginsFolder, CommandSender commandSender) throws IOException {
      String downloadUrl = "https://api.spiget.org/v2/resources/" + pluginId + "/download";
      File pluginsDir = new File(pluginsFolder);
      File pluginJar = new File(pluginsDir, pluginName + ".jar");
      Files.createDirectories(pluginsDir.toPath());
      sendMessageToExecutor(commandSender, ChatColor.GREEN + "Downloading plugin: " + pluginName + "...");
      File tempFile = downloadFile(downloadUrl, commandSender);
      if (tempFile != null) {
         if (pluginJar.exists()) {
            sendMessageToExecutor(commandSender, ChatColor.GREEN + "Replacing existing plugin: " + pluginJar.getName());
            Files.delete(pluginJar.toPath());
         } else {
            sendMessageToExecutor(commandSender, ChatColor.GREEN + "Adding new plugin: " + pluginJar.getName());
         }

         Files.move(tempFile.toPath(), pluginJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
         sendMessageToExecutor(
            commandSender,
            ChatColor.GREEN + "Plugin " + pluginName + " successfully updated in \"" + pluginsFolder + "\". Restart the server in order to take effect."
         );
      }
   }

   private static File downloadFile(String fileUrl, CommandSender commandSender) throws IOException {
      HttpURLConnection connection = null;

      File tempFile;
      try {
         connection = open(fileUrl);
         int responseCode = connection.getResponseCode();
         if (responseCode / 100 == 2) {
            tempFile = File.createTempFile("plugin-", ".jar");
            tempFile.deleteOnExit();

            try (
               InputStream inputStream = connection.getInputStream();
               FileOutputStream outputStream = new FileOutputStream(tempFile);
            ) {
               byte[] buffer = new byte[8192];

               int bytesRead;
               while ((bytesRead = inputStream.read(buffer)) != -1) {
                  outputStream.write(buffer, 0, bytesRead);
               }
            }

            return tempFile;
         }

         sendMessageToExecutor(commandSender, ChatColor.RED + "Failed to download file. HTTP Response Code: " + responseCode);

         try (InputStream es = connection.getErrorStream()) {
            if (es != null) {
               es.transferTo(DISCARD);
            }
         } catch (IOException var22) {
         }

         tempFile = null;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }

      return tempFile;
   }

   private static HttpURLConnection open(String urlStr) throws IOException {
      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      setup(conn);
      int status = conn.getResponseCode();

      for (int redirects = 0; isRedirect(status) && redirects < 5; redirects++) {
         String loc = conn.getHeaderField("Location");
         conn.disconnect();
         if (loc == null || loc.isBlank()) {
            throw new ProtocolException("Redirect without Location header");
         }

         URL newUrl = new URL(url, loc);
         conn = (HttpURLConnection)newUrl.openConnection();
         setup(conn);
         status = conn.getResponseCode();
      }

      return conn;
   }

   private static void setup(HttpURLConnection conn) throws ProtocolException {
      conn.setInstanceFollowRedirects(false);
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(30000);
      conn.setUseCaches(false);
      conn.setRequestProperty("User-Agent", "GravesX/1 PluginDownloadUtil (+spiget)");
      conn.setRequestProperty("Accept", "application/java-archive, application/octet-stream");
   }

   private static boolean isRedirect(int code) {
      return code == 301 || code == 302 || code == 303 || code == 307 || code == 308;
   }

   private static void sendMessageToExecutor(CommandSender commandSender, String message) {
      if (commandSender != null && message != null) {
         commandSender.sendMessage(message);
      }
   }
}
