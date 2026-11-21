package de.theredend2000.advancedhunt.util.updater.source;

import de.theredend2000.advancedhunt.util.updater.UpdateInfo;
import de.theredend2000.advancedhunt.util.updater.UpdateSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bukkit.Bukkit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BukkitSource implements UpdateSource {
   private static final String RSS_URL = "https://dev.bukkit.org/projects/%s/files.rss";

   @Override
   public CompletableFuture<UpdateInfo> getLatestUpdate(String id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            URL url = new URL(String.format("https://dev.bukkit.org/projects/%s/files.rss", id));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "AdvancedHunt/Updater");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            if (connection.getResponseCode() == 200) {
               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
               factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
               factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
               DocumentBuilder builder = factory.newDocumentBuilder();
               Document doc = builder.parse(connection.getInputStream());
               NodeList items = doc.getElementsByTagName("item");

               for (int i = 0; i < items.getLength(); i++) {
                  Element item = (Element)items.item(i);
                  String title = item.getElementsByTagName("title").item(0).getTextContent();
                  if (!this.isUnstable(title)) {
                     Instant publishedAt = null;
                     NodeList pubDates = item.getElementsByTagName("pubDate");
                     if (pubDates.getLength() > 0) {
                        String pubDate = pubDates.item(0).getTextContent();
                        publishedAt = ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                     }

                     return new UpdateInfo(this.cleanVersion(title), title, publishedAt);
                  }
               }
            }
         } catch (Exception var14) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to check Bukkit updates", (Throwable)var14);
         }

         return null;
      });
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
            URL url = new URL(String.format("https://dev.bukkit.org/projects/%s/files.rss", id));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "AdvancedHunt/Updater");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            if (connection.getResponseCode() == 200) {
               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
               factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
               factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
               DocumentBuilder builder = factory.newDocumentBuilder();
               Document doc = builder.parse(connection.getInputStream());
               NodeList items = doc.getElementsByTagName("item");

               for (int i = 0; i < items.getLength(); i++) {
                  Element item = (Element)items.item(i);
                  String title = item.getElementsByTagName("title").item(0).getTextContent();
                  if (title.equals(update.sourceVersion())) {
                     String link = item.getElementsByTagName("link").item(0).getTextContent();
                     return this.downloadFile(link + "/download", destination);
                  }
               }
            }
         } catch (Exception var14) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to download from Bukkit", (Throwable)var14);
         }

         return false;
      });
   }

   private boolean downloadFile(String urlString, File destination) {
      try {
         URL url = new URL(urlString);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestProperty("User-Agent", "AdvancedHunt/Updater");
         connection.setInstanceFollowRedirects(true);
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(60000);
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
         Bukkit.getLogger().severe("Failed to download file from Bukkit: " + var10.getMessage());
         return false;
      }
   }

   @Override
   public String getName() {
      return "Bukkit";
   }
}
