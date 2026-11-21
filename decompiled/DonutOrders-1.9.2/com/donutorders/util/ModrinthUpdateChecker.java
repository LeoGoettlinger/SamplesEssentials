package com.donutorders.util;

import com.donutorders.DonutOrders;
import com.donutorders.scheduler.FoliaScheduler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ModrinthUpdateChecker {
   private static final String PROJECT_SLUG = "donut-orders";
   private static final String MODRINTH_URL = "https://modrinth.com/plugin/donut-orders";
   private static final String VERSIONS_ENDPOINT = "https://api.modrinth.com/v2/project/donut-orders/version?version_type=release&limit=20";

   private ModrinthUpdateChecker() {
   }

   public static void checkAsync(DonutOrders plugin) {
      if (plugin.getConfig().getBoolean("update-check.enabled", true)) {
         String currentVersion = plugin.getDescription().getVersion();
         FoliaScheduler.runAsync(
            () -> {
               try {
                  String latestVersion = fetchLatestVersion(plugin);
                  if (latestVersion == null) {
                     return;
                  }

                  int comparison = compareVersions(latestVersion, currentVersion);
                  FoliaScheduler.runGlobal(
                     () -> {
                        if (comparison > 0) {
                           plugin.setUpdateAvailable(latestVersion, "https://modrinth.com/plugin/donut-orders");
                           plugin.getLogger()
                              .warning(
                                 "[DonutOrders] A new version is available: "
                                    + latestVersion
                                    + " (running "
                                    + currentVersion
                                    + "). Download: https://modrinth.com/plugin/donut-orders"
                              );
                        } else {
                           plugin.clearUpdateAvailable();
                           plugin.getLogger().info("[DonutOrders] Running the latest version (" + currentVersion + ").");
                        }
                     }
                  );
               } catch (Exception var4) {
                  plugin.getLogger().log(Level.FINE, "[DonutOrders] Could not check Modrinth for updates.", (Throwable)var4);
               }
            }
         );
      }
   }

   private static String fetchLatestVersion(DonutOrders plugin) throws Exception {
      HttpURLConnection connection = (HttpURLConnection)URI.create("https://api.modrinth.com/v2/project/donut-orders/version?version_type=release&limit=20")
         .toURL()
         .openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      connection.setRequestProperty("User-Agent", "DonutOrders/" + plugin.getDescription().getVersion() + " (https://modrinth.com/plugin/donut-orders)");
      int status = connection.getResponseCode();
      if (status != 200) {
         plugin.getLogger().fine("[DonutOrders] Modrinth update check returned HTTP " + status + ".");
         return null;
      } else {
         StringBuilder body = new StringBuilder();

         String line;
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
               body.append(line);
            }
         }

         return pickNewestVersionNumber(body.toString());
      }
   }

   static String pickNewestVersionNumber(String json) {
      List<ModrinthUpdateChecker.VersionEntry> entries = parseVersionEntries(json);
      if (entries.isEmpty()) {
         return null;
      } else {
         entries.sort((a, b) -> b.datePublished.compareTo(a.datePublished));
         return entries.get(0).versionNumber;
      }
   }

   static List<ModrinthUpdateChecker.VersionEntry> parseVersionEntries(String json) {
      List<ModrinthUpdateChecker.VersionEntry> entries = new ArrayList<>();
      if (json != null && !json.isBlank()) {
         Pattern number = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");
         Pattern date = Pattern.compile("\"date_published\"\\s*:\\s*\"([^\"]+)\"");
         Matcher numMatcher = number.matcher(json);

         while (numMatcher.find()) {
            String object = enclosingObject(json, numMatcher.start());
            if (object != null) {
               Matcher versionInObject = number.matcher(object);
               Matcher dateInObject = date.matcher(object);
               if (versionInObject.find() && dateInObject.find()) {
                  entries.add(new ModrinthUpdateChecker.VersionEntry(versionInObject.group(1), dateInObject.group(1)));
               }
            }
         }

         return entries;
      } else {
         return entries;
      }
   }

   static String enclosingObject(String json, int index) {
      int depth = 0;
      int start = -1;

      for (int i = index; i >= 0; i--) {
         char c = json.charAt(i);
         if (c == '}') {
            depth++;
         } else if (c == '{') {
            if (depth == 0) {
               start = i;
               break;
            }

            depth--;
         }
      }

      if (start < 0) {
         return null;
      } else {
         depth = 0;

         for (int ix = start; ix < json.length(); ix++) {
            char c = json.charAt(ix);
            if (c == '{') {
               depth++;
            } else if (c == '}') {
               if (--depth == 0) {
                  return json.substring(start, ix + 1);
               }
            }
         }

         return null;
      }
   }

   static int compareVersions(String a, String b) {
      String[] left = normalizeVersion(a).split("\\.");
      String[] right = normalizeVersion(b).split("\\.");
      int length = Math.max(left.length, right.length);

      for (int i = 0; i < length; i++) {
         int leftPart = i < left.length ? parseVersionPart(left[i]) : 0;
         int rightPart = i < right.length ? parseVersionPart(right[i]) : 0;
         if (leftPart != rightPart) {
            return Integer.compare(leftPart, rightPart);
         }
      }

      return 0;
   }

   static String normalizeVersion(String version) {
      if (version == null) {
         return "0";
      } else {
         String trimmed = version.trim();
         int plus = trimmed.indexOf(43);
         if (plus >= 0) {
            trimmed = trimmed.substring(0, plus);
         }

         int hyphen = trimmed.indexOf(45);
         if (hyphen > 0) {
            trimmed = trimmed.substring(0, hyphen);
         }

         return trimmed;
      }
   }

   private static int parseVersionPart(String part) {
      int end = 0;

      while (end < part.length() && Character.isDigit(part.charAt(end))) {
         end++;
      }

      return end == 0 ? 0 : Integer.parseInt(part.substring(0, end));
   }

   static final class VersionEntry {
      final String versionNumber;
      final String datePublished;

      VersionEntry(String versionNumber, String datePublished) {
         this.versionNumber = versionNumber;
         this.datePublished = datePublished == null ? "" : datePublished;
      }
   }
}
