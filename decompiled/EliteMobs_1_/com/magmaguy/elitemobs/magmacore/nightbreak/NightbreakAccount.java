package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakAccount {
   private static final String BASE_URL = "https://nightbreak.io";
   private static final Object TEST_BASE_URL_LOCK = new Object();
   private static volatile String scopedTestBaseUrl;
   private static final AtomicInteger PRODUCTION_ORIGIN_SELECTIONS = new AtomicInteger();
   private static final String CONFIG_FOLDER_NAME = "MagmaCore";
   private static final String CONFIG_FILE_NAME = "nightbreak.yml";
   private static volatile NightbreakAccount instance;
   private String token;
   private static volatile File configFilePath = null;
   private static volatile long lastFileMtime = -1L;
   private static final List<NightbreakAccount.TokenChangeListenerRegistrationImpl> tokenChangeListeners = new CopyOnWriteArrayList<>();
   private static final int CONNECT_TIMEOUT_MS = 10000;
   private static final int READ_TIMEOUT_MS = 10000;
   private static final int MAX_PUBLIC_JSON_BYTES = 786432;
   private static final long MAX_PLUGIN_DOWNLOAD_BYTES = 268435456L;
   private static volatile boolean suppressAuthFailureLogs = false;
   private static volatile int lastAuthFailureStatus = 0;
   private static volatile String lastAuthFailureBody = null;
   private static volatile long networkFailureWindowStart = 0L;
   private static volatile int networkFailuresInWindow = 0;
   private static volatile String lastNetworkFailureMessage = null;
   private static final long NETWORK_FAILURE_WINDOW_MS = 300000L;

   private NightbreakAccount(String token) {
      this.token = token;
      instance = this;
   }

   public static NightbreakAccount getInstance() {
      ensureFresh();
      return instance;
   }

   public static synchronized NightbreakAccount initialize(JavaPlugin plugin) {
      File configFile = getConfigFile(plugin);
      String previousToken = currentToken();
      configFilePath = configFile;
      if (!configFile.exists()) {
         instance = null;
         lastFileMtime = -1L;
         if (previousToken != null) {
            resetAuthFailureSuppression();
            fireTokenChanged();
         }

         Logger.info("No account token found. Use /nightbreaklogin <token> to register your token.");
         return null;
      } else {
         loadTokenFromFile(configFile);
         lastFileMtime = configFile.lastModified();
         if (!Objects.equals(previousToken, currentToken())) {
            resetAuthFailureSuppression();
            fireTokenChanged();
         }

         if (instance != null) {
            Logger.info("Account token loaded successfully!");
         } else {
            Logger.info("No account token configured. Use /nightbreaklogin <token> to register your token.");
         }

         return instance;
      }
   }

   private static synchronized void ensureFresh() {
      File f = configFilePath;
      if (f != null) {
         String previousToken = currentToken();
         if (!f.exists()) {
            if (instance != null) {
               instance = null;
               lastFileMtime = -1L;
               resetAuthFailureSuppression();
               fireTokenChanged();
            }
         } else {
            long mtime = f.lastModified();
            if (mtime != lastFileMtime) {
               loadTokenFromFile(f);
               lastFileMtime = mtime;
               if (!Objects.equals(previousToken, currentToken())) {
                  resetAuthFailureSuppression();
                  fireTokenChanged();
               }
            }
         }
      }
   }

   private static void loadTokenFromFile(File configFile) {
      FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
      String token = config.getString("token");
      if (token != null) {
         token = token.trim();
      }

      if (token != null && !token.isEmpty() && !token.equals("YOUR_TOKEN_HERE")) {
         instance = new NightbreakAccount(token);
      } else {
         instance = null;
      }
   }

   public static void addTokenChangeListener(Runnable listener) {
      registerTokenChangeListener(listener);
   }

   public static NightbreakAccount.TokenChangeListenerRegistration registerTokenChangeListener(Runnable listener) {
      Objects.requireNonNull(listener, "listener");
      NightbreakAccount.TokenChangeListenerRegistrationImpl registration = new NightbreakAccount.TokenChangeListenerRegistrationImpl(listener);
      tokenChangeListeners.add(registration);
      return registration;
   }

   private static void fireTokenChanged() {
      for (NightbreakAccount.TokenChangeListenerRegistrationImpl registration : tokenChangeListeners) {
         if (!registration.closed.get()) {
            try {
               registration.listener.run();
            } catch (Throwable var3) {
               Logger.warn("Account token-change listener threw: " + var3.getMessage());
            }
         }
      }
   }

   private static String currentToken() {
      return instance == null ? null : instance.token;
   }

   public static synchronized NightbreakAccount registerToken(JavaPlugin plugin, String token) {
      String normalizedToken = token == null ? "" : token.trim();
      if (!normalizedToken.isEmpty() && !normalizedToken.equals("YOUR_TOKEN_HERE")) {
         File configFile = getConfigFile(plugin);
         configFilePath = configFile;
         long previousDiskMtime = configFile.exists() ? configFile.lastModified() : -1L;
         if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
         }

         FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
         config.options()
            .setHeader(
               List.of(
                  "MagmaCore Shared Configuration",
                  "",
                  "This folder is a common config folder for plugins created by MagmaGuy, including:",
                  "- EliteMobs",
                  "- BetterStructures",
                  "- FreeMinecraftModels",
                  "- BetterFood",
                  "- ResourcePackManager",
                  "- ExtractionCraft",
                  "- EternalTD",
                  "- MegaBlock Survivors",
                  "- And others!",
                  "",
                  "This folder can be deleted without causing too many problems,",
                  "but you may need to reconfigure shared settings like your account token.",
                  "",
                  "Get your token at: https://nightbreak.io/account"
               )
            );
         config.set("token", normalizedToken);
         File temporaryConfig = null;

         label105: {
            Object monotonicMtime;
            try {
               temporaryConfig = File.createTempFile(".nightbreak-", ".yml.tmp", configFile.getParentFile());
               config.save(temporaryConfig);

               try {
                  Files.move(temporaryConfig.toPath(), configFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
               } catch (AtomicMoveNotSupportedException var14) {
                  Files.move(temporaryConfig.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
               }
               break label105;
            } catch (IOException var15) {
               Logger.warn("Failed to save account token: " + var15.getMessage());
               monotonicMtime = null;
            } finally {
               if (temporaryConfig != null && temporaryConfig.exists()) {
                  temporaryConfig.delete();
               }
            }

            return (NightbreakAccount)monotonicMtime;
         }

         String previousToken = currentToken();
         instance = new NightbreakAccount(normalizedToken);
         long monotonicMtime = Math.max(System.currentTimeMillis(), Math.max(previousDiskMtime, lastFileMtime) + 1L);
         if (!configFile.setLastModified(monotonicMtime)) {
            Logger.warn("Could not advance the account-token file timestamp; other loaded plugins may observe this credential change late.");
         }

         lastFileMtime = configFile.lastModified();
         resetAuthFailureSuppression();
         if (!Objects.equals(previousToken, currentToken())) {
            fireTokenChanged();
         }

         return instance;
      } else {
         if (plugin != null) {
            plugin.getLogger().warning("Cannot register an empty or placeholder account token.");
         }

         return null;
      }
   }

   public static synchronized boolean clearToken(JavaPlugin plugin) {
      File configFile = getConfigFile(plugin);
      configFilePath = configFile;
      if (configFile.exists() && !configFile.delete()) {
         Logger.warn("Failed to remove the shared account token configuration.");
         return false;
      } else {
         boolean tokenChanged = instance != null;
         instance = null;
         lastFileMtime = -1L;
         resetAuthFailureSuppression();
         if (tokenChanged) {
            fireTokenChanged();
         }

         return true;
      }
   }

   private static File getConfigFile(JavaPlugin plugin) {
      File pluginsFolder = plugin.getDataFolder().getParentFile();
      File magmaCoreFolder = new File(pluginsFolder, "MagmaCore");
      return new File(magmaCoreFolder, "nightbreak.yml");
   }

   public static boolean hasToken() {
      ensureFresh();
      return instance != null && instance.token != null && !instance.token.isEmpty();
   }

   public NightbreakAccount.VersionInfo getVersion(String slug) {
      try {
         String url = baseUrl() + "/server/dlc/" + slug + "/version";
         String response = this.httpGet(url, false);
         return response == null ? null : parseVersionInfo(response);
      } catch (Exception var4) {
         Logger.warn("Error getting version for DLC '" + slug + "': " + var4.getMessage());
         return null;
      }
   }

   public Map<String, NightbreakAccount.VersionInfo> getAllVersions() {
      try {
         String url = baseUrl() + "/api/dlc/versions";
         String response = this.httpGet(url, false);
         return (Map<String, NightbreakAccount.VersionInfo>)(response == null ? new HashMap<>() : parseAllVersionsResponse(response));
      } catch (Exception var3) {
         Logger.warn("Error getting all DLC versions: " + var3.getMessage());
         return new HashMap<>();
      }
   }

   public NightbreakAccount.AccessInfo checkAccess(String slug) {
      if (!hasToken()) {
         Logger.warn("Cannot check access: No account token registered. Use /nightbreaklogin <token> first.");
         return null;
      } else {
         try {
            String url = baseUrl() + "/server/dlc/" + slug + "/access";
            String response = this.httpGet(url, true);
            return response == null ? null : parseAccessInfo(response);
         } catch (Exception var4) {
            Logger.warn("Error checking access for DLC '" + slug + "': " + var4.getMessage());
            return null;
         }
      }
   }

   public boolean download(String slug, File destinationFile) {
      return this.download(slug, destinationFile, null);
   }

   public boolean download(String slug, File destinationFile, String version) {
      if (!hasToken()) {
         Logger.warn("Cannot download: No account token registered. Use /nightbreaklogin <token> first.");
         return false;
      } else {
         try {
            String url = baseUrl() + "/server/dlc/" + slug + "/download";
            if (version != null) {
               url = url + "?version=" + version;
            }

            return this.httpDownload(url, destinationFile);
         } catch (Exception var5) {
            Logger.warn("Error downloading DLC '" + slug + "': " + var5.getMessage());
            return false;
         }
      }
   }

   public boolean download(String slug, File destinationFile, String version, NightbreakAccount.DownloadProgressCallback progressCallback) {
      if (!hasToken()) {
         Logger.warn("Cannot download: No account token registered. Use /nightbreaklogin <token> first.");
         return false;
      } else {
         try {
            String url = baseUrl() + "/server/dlc/" + slug + "/download";
            if (version != null) {
               url = url + "?version=" + version;
            }

            return this.httpDownloadWithProgress(url, destinationFile, progressCallback);
         } catch (Exception var6) {
            Logger.warn("Error downloading DLC '" + slug + "': " + var6.getMessage());
            return false;
         }
      }
   }

   public NightbreakAccount.VersionInfo getPluginVersion(String slug) {
      return getPublicPluginVersion(slug);
   }

   public static NightbreakAccount.VersionInfo getPublicPluginVersion(String slug) {
      return getPublicPluginVersion(slug, true);
   }

   public static NightbreakAccount.VersionInfo getPublicPluginVersion(String slug, boolean logFailures) {
      try {
         String url = baseUrl() + "/server/plugins/" + encodePathSegment(slug) + "/version";
         String response = httpGetPublic(url, logFailures);
         return response == null ? null : parseVersionInfo(response);
      } catch (Exception var4) {
         if (logFailures) {
            Logger.warn("Error getting plugin version for '" + slug + "': " + var4.getMessage());
         }

         return null;
      }
   }

   public static NightbreakAccount.VersionInfo getPublicDlcVersion(String slug, boolean logFailures) {
      try {
         String url = baseUrl() + "/server/dlc/" + encodePathSegment(slug) + "/version";
         String response = httpGetPublic(url, logFailures);
         return response == null ? null : parseVersionInfo(response);
      } catch (Exception var4) {
         if (logFailures) {
            Logger.warn("Error getting DLC version for '" + slug + "': " + var4.getMessage());
         }

         return null;
      }
   }

   public static List<NightbreakAccount.PluginChangelog> getPublicPluginChangelogs(String slug, boolean logFailures) {
      return getPublicChangelogs("/server/plugins/", slug, logFailures);
   }

   public static List<NightbreakAccount.PluginChangelog> getPublicDlcChangelogs(String slug, boolean logFailures) {
      return getPublicChangelogs("/server/dlc/", slug, logFailures);
   }

   private static List<NightbreakAccount.PluginChangelog> getPublicChangelogs(String prefix, String slug, boolean logFailures) {
      return fetchPublicChangelogs(prefix, slug, logFailures).releases();
   }

   static NightbreakAccount.ChangelogFetch fetchPluginChangelogs(String slug) {
      return fetchPublicChangelogs("/server/plugins/", slug, false);
   }

   static NightbreakAccount.ChangelogFetch fetchDlcChangelogs(String slug) {
      return fetchPublicChangelogs("/server/dlc/", slug, false);
   }

   private static NightbreakAccount.ChangelogFetch fetchPublicChangelogs(String prefix, String slug, boolean logFailures) {
      try {
         String url = baseUrl() + prefix + encodePathSegment(slug) + "/changelogs?limit=100";
         NightbreakAccount.PublicResponse response = httpGetPublicResponse(url, logFailures);
         if (response.body() == null) {
            return new NightbreakAccount.ChangelogFetch(List.of(), response.status() != 404 && response.status() != 410);
         } else {
            JsonElement root = JsonParser.parseString(response.body());
            JsonArray releases;
            if (root.isJsonArray()) {
               releases = root.getAsJsonArray();
            } else if (root.isJsonObject() && root.getAsJsonObject().has("releases")) {
               releases = root.getAsJsonObject().getAsJsonArray("releases");
            } else {
               if (!root.isJsonObject() || !root.getAsJsonObject().has("changelogs")) {
                  return new NightbreakAccount.ChangelogFetch(List.of(), false);
               }

               releases = root.getAsJsonObject().getAsJsonArray("changelogs");
            }

            List<NightbreakAccount.PluginChangelog> result = new ArrayList<>();

            for (JsonElement element : releases) {
               if (element.isJsonObject() && result.size() < 100) {
                  JsonObject object = element.getAsJsonObject();
                  String version = jsonString(object, "version");
                  String changelog = jsonString(object, "changelog");
                  if (changelog == null) {
                     changelog = jsonString(object, "body");
                  }

                  if (version != null && !version.isBlank()) {
                     String boundedChangelog = changelog == null ? "" : changelog.trim();
                     if (boundedChangelog.length() > 12000) {
                        boundedChangelog = boundedChangelog.substring(0, 12000);
                     }

                     result.add(new NightbreakAccount.PluginChangelog(version.trim(), boundedChangelog, jsonString(object, "releasedAt")));
                  }
               }
            }

            Collections.reverse(result);
            return new NightbreakAccount.ChangelogFetch(List.copyOf(result), false);
         }
      } catch (Exception var14) {
         if (logFailures) {
            Logger.warn("Error getting changelogs for '" + slug + "': " + var14.getMessage());
         }

         return new NightbreakAccount.ChangelogFetch(List.of(), false);
      }
   }

   private static String jsonString(JsonObject object, String key) {
      JsonElement value = object.get(key);
      return value != null && !value.isJsonNull() && value.isJsonPrimitive() ? value.getAsString() : null;
   }

   public NightbreakAccount.AccessInfo checkPluginAccess(String slug) {
      if (!hasToken()) {
         Logger.warn("Cannot check plugin update access: No account token registered. Use /nightbreaklogin <token> first.");
         return null;
      } else {
         try {
            String url = baseUrl() + "/server/plugins/" + encodePathSegment(slug) + "/access";
            String response = this.httpGet(url, true);
            return response == null ? null : parseAccessInfo(response);
         } catch (Exception var4) {
            Logger.warn("Error checking plugin update access for '" + slug + "': " + var4.getMessage());
            return null;
         }
      }
   }

   public boolean downloadPlugin(String slug, File destinationFile, NightbreakAccount.DownloadProgressCallback progressCallback) {
      return this.downloadPluginUpdate(slug, destinationFile, progressCallback).success;
   }

   public NightbreakAccount.PluginDownloadResult downloadPluginUpdate(
      String slug, File destinationFile, NightbreakAccount.DownloadProgressCallback progressCallback
   ) {
      return this.downloadPluginUpdate(slug, destinationFile, progressCallback, () -> true);
   }

   NightbreakAccount.PluginDownloadResult downloadPluginUpdate(
      String slug, File destinationFile, NightbreakAccount.DownloadProgressCallback progressCallback, BooleanSupplier operationCurrent
   ) {
      if (!hasToken()) {
         Logger.warn("Cannot download plugin update: No account token registered. Use /nightbreaklogin <token> first.");
         return new NightbreakAccount.PluginDownloadResult(false, 0, "NO_TOKEN", "No account token registered.", null, null);
      } else {
         try {
            String url = baseUrl() + "/server/plugins/" + encodePathSegment(slug) + "/download";
            return this.httpDownloadWithProgressResult(url, destinationFile, progressCallback, operationCurrent);
         } catch (Exception var6) {
            Logger.warn("Error downloading plugin update '" + slug + "': " + var6.getMessage());
            return new NightbreakAccount.PluginDownloadResult(false, 0, "DOWNLOAD_ERROR", var6.getMessage(), null, null);
         }
      }
   }

   static NightbreakAccount.ScopedTestBaseUrl useLoopbackBaseUrlForTests(URI baseUri) {
      if (baseUri == null) {
         throw new IllegalArgumentException("Test base URI cannot be null.");
      } else {
         String scheme = baseUri.getScheme();
         if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Test base URI must use HTTP or HTTPS.");
         } else {
            String host = baseUri.getHost();
            if (host != null && !host.isBlank()) {
               String normalizedHost = host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
               if (!isLoopbackTestHost(normalizedHost)) {
                  throw new IllegalArgumentException("Test base URI must be loopback-only.");
               } else if (baseUri.getRawUserInfo() == null && baseUri.getRawQuery() == null && baseUri.getRawFragment() == null) {
                  String path = baseUri.getRawPath();
                  if (path != null && !path.isBlank() && !"/".equals(path)) {
                     throw new IllegalArgumentException("Test base URI cannot contain an API path.");
                  } else {
                     String normalized = baseUri.toString();

                     while (normalized.endsWith("/")) {
                        normalized = normalized.substring(0, normalized.length() - 1);
                     }

                     synchronized (TEST_BASE_URL_LOCK) {
                        if (scopedTestBaseUrl != null) {
                           throw new IllegalStateException("A Nightbreak test base URI is already active.");
                        }

                        scopedTestBaseUrl = normalized;
                     }

                     return new NightbreakAccount.ScopedTestBaseUrl(normalized);
                  }
               } else {
                  throw new IllegalArgumentException("Test base URI cannot contain user info, a query, or a fragment.");
               }
            } else {
               throw new IllegalArgumentException("Test base URI must have a loopback host.");
            }
         }
      }
   }

   private static String baseUrl() {
      String testBaseUrl = scopedTestBaseUrl;
      if (testBaseUrl != null) {
         return testBaseUrl;
      } else {
         PRODUCTION_ORIGIN_SELECTIONS.incrementAndGet();
         return "https://nightbreak.io";
      }
   }

   static int productionOriginSelectionsForTests() {
      return PRODUCTION_ORIGIN_SELECTIONS.get();
   }

   static void resetForTests() {
      synchronized (TEST_BASE_URL_LOCK) {
         if (scopedTestBaseUrl != null) {
            throw new IllegalStateException("Close the Nightbreak test base URI scope before resetting state.");
         }
      }

      instance = null;
      configFilePath = null;
      lastFileMtime = -1L;
      tokenChangeListeners.clear();
      PRODUCTION_ORIGIN_SELECTIONS.set(0);
      resetAuthFailureSuppression();
   }

   private static boolean isLoopbackTestHost(String host) {
      if (!"localhost".equalsIgnoreCase(host) && !"::1".equals(host) && !"0:0:0:0:0:0:0:1".equals(host)) {
         String[] octets = host.split("\\.", -1);
         if (octets.length == 4 && "127".equals(octets[0])) {
            for (String octet : octets) {
               if (octet.isBlank()) {
                  return false;
               }

               try {
                  int value = Integer.parseInt(octet);
                  if (value < 0 || value > 255) {
                     return false;
                  }
               } catch (NumberFormatException var7) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   private static String encodePathSegment(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
   }

   private static String httpGetPublic(String urlString) {
      return httpGetPublic(urlString, true);
   }

   private static String httpGetPublic(String urlString, boolean logFailures) {
      return httpGetPublicResponse(urlString, logFailures).body();
   }

   private static NightbreakAccount.PublicResponse httpGetPublicResponse(String urlString, boolean logFailures) {
      try {
         URL url = new URL(urlString);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         configureScopedTestRedirectPolicy(connection);
         connection.setRequestMethod("GET");
         connection.setRequestProperty("Accept", "application/json");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         int responseCode = connection.getResponseCode();
         if (responseCode == 200) {
            return new NightbreakAccount.PublicResponse(responseCode, readUtf8Bounded(connection.getInputStream(), 786432));
         } else {
            String errorResponse = null;
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
               errorResponse = readUtf8Bounded(errorStream, 65536);
            }

            if (logFailures) {
               logHttpError(urlString, responseCode, errorResponse);
            }

            return new NightbreakAccount.PublicResponse(responseCode, null);
         }
      } catch (IOException var7) {
         if (logFailures) {
            logNetworkFailure(urlString, var7.getMessage());
         }

         return new NightbreakAccount.PublicResponse(0, null);
      }
   }

   private static String readUtf8Bounded(InputStream inputStream, int maximumBytes) throws IOException {
      InputStream stream = inputStream;

      String var4;
      try {
         byte[] bytes = stream.readNBytes(maximumBytes + 1);
         if (bytes.length > maximumBytes) {
            throw new IOException("Nightbreak response exceeded " + maximumBytes + " bytes");
         }

         var4 = new String(bytes, StandardCharsets.UTF_8);
      } catch (Throwable var6) {
         if (inputStream != null) {
            try {
               stream.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (inputStream != null) {
         inputStream.close();
      }

      return var4;
   }

   private String httpGet(String urlString, boolean withAuth) {
      try {
         URL url = new URL(urlString);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         configureScopedTestRedirectPolicy(connection);
         connection.setRequestMethod("GET");
         connection.setRequestProperty("Accept", "application/json");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         if (withAuth && this.token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + this.token);
         }

         int responseCode = connection.getResponseCode();
         if (responseCode == 200) {
            Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8);
            StringBuilder response = new StringBuilder();

            while (scanner.hasNext()) {
               response.append(scanner.nextLine());
            }

            scanner.close();
            return response.toString();
         } else {
            String errorResponse = null;
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
               Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8);
               StringBuilder sb = new StringBuilder();

               while (scanner.hasNext()) {
                  sb.append(scanner.nextLine());
               }

               scanner.close();
               errorResponse = sb.toString();
            }

            logHttpError(urlString, responseCode, errorResponse);
            return null;
         }
      } catch (IOException var10) {
         logNetworkFailure(urlString, var10.getMessage());
         return null;
      }
   }

   private static void logHttpError(String urlString, int responseCode, String errorResponse) {
      if (responseCode != 401 && responseCode != 403) {
         if (errorResponse != null) {
            Logger.warn("Nightbreak API error (" + responseCode + ") for " + urlString + ": " + errorResponse);
         } else {
            Logger.warn("Nightbreak API error (" + responseCode + ") for " + urlString + " (no error body)");
         }
      } else {
         lastAuthFailureStatus = responseCode;
         lastAuthFailureBody = errorResponse;
         if (!suppressAuthFailureLogs) {
            suppressAuthFailureLogs = true;
            String body = errorResponse != null ? errorResponse : "(no body)";
            Logger.warn(
               "The account token was rejected ("
                  + responseCode
                  + ") for "
                  + urlString
                  + ": "
                  + body
                  + ". Get a new token at https://nightbreak.io/account/ and run /nightbreaklogin <token>, then try again. Further token errors will be hidden until the token changes."
            );
         }
      }
   }

   private static void logNetworkFailure(String urlString, String message) {
      long now = System.currentTimeMillis();
      synchronized (NightbreakAccount.class) {
         if (now - networkFailureWindowStart > 300000L) {
            if (networkFailuresInWindow > 1) {
               Logger.warn(
                  "Nightbreak: "
                     + (networkFailuresInWindow - 1)
                     + " more network failures suppressed in the previous window. Last error: "
                     + lastNetworkFailureMessage
               );
            }

            networkFailureWindowStart = now;
            networkFailuresInWindow = 1;
            lastNetworkFailureMessage = message;
            Logger.warn("Nightbreak unreachable for " + urlString + ": " + message + " — further network failures will be summarized once per 5 minutes.");
         } else {
            networkFailuresInWindow++;
            lastNetworkFailureMessage = message;
         }
      }
   }

   public static void resetAuthFailureSuppression() {
      suppressAuthFailureLogs = false;
      lastAuthFailureStatus = 0;
      lastAuthFailureBody = null;
   }

   public static boolean hasAuthFailure() {
      return suppressAuthFailureLogs;
   }

   public static int getLastAuthFailureStatus() {
      return lastAuthFailureStatus;
   }

   public static String getLastAuthFailureBody() {
      return lastAuthFailureBody;
   }

   private boolean httpDownload(String urlString, File destinationFile) {
      try {
         URL url = new URL(urlString);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         configureScopedTestRedirectPolicy(connection);
         connection.setRequestMethod("GET");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         if (this.token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + this.token);
         }

         int responseCode = connection.getResponseCode();
         if (responseCode == 200) {
            if (!destinationFile.getParentFile().exists()) {
               destinationFile.getParentFile().mkdirs();
            }

            try (
               InputStream in = connection.getInputStream();
               FileOutputStream out = new FileOutputStream(destinationFile);
            ) {
               byte[] buffer = new byte[8192];

               int bytesRead;
               while ((bytesRead = in.read(buffer)) != -1) {
                  out.write(buffer, 0, bytesRead);
               }
            } catch (IOException var14) {
               if (destinationFile.exists()) {
                  destinationFile.delete();
               }

               throw var14;
            }

            return true;
         } else {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
               Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8);
               StringBuilder errorResponse = new StringBuilder();

               while (scanner.hasNext()) {
                  errorResponse.append(scanner.nextLine());
               }

               scanner.close();
               Logger.warn("Download failed (" + responseCode + ") for " + urlString + ": " + errorResponse);
            } else {
               Logger.warn("Download failed (" + responseCode + ") for " + urlString + " (no error body)");
            }

            return false;
         }
      } catch (IOException var15) {
         if (destinationFile.exists()) {
            destinationFile.delete();
         }

         Logger.warn("Download failed for " + urlString + ": " + var15.getMessage());
         return false;
      }
   }

   private boolean httpDownloadWithProgress(String urlString, File destinationFile, NightbreakAccount.DownloadProgressCallback callback) {
      try {
         URL url = new URL(urlString);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         configureScopedTestRedirectPolicy(connection);
         connection.setRequestMethod("GET");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         if (this.token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + this.token);
         }

         int responseCode = connection.getResponseCode();
         if (responseCode == 200) {
            if (!destinationFile.getParentFile().exists()) {
               destinationFile.getParentFile().mkdirs();
            }

            long totalBytes = connection.getContentLengthLong();
            long bytesDownloaded = 0L;
            long lastProgressUpdate = 0L;

            try (
               InputStream in = connection.getInputStream();
               FileOutputStream out = new FileOutputStream(destinationFile);
            ) {
               byte[] buffer = new byte[8192];

               int bytesRead;
               while ((bytesRead = in.read(buffer)) != -1) {
                  out.write(buffer, 0, bytesRead);
                  bytesDownloaded += bytesRead;
                  if (callback != null && (bytesDownloaded - lastProgressUpdate >= 102400L || bytesRead == -1)) {
                     callback.onProgress(bytesDownloaded, totalBytes);
                     lastProgressUpdate = bytesDownloaded;
                  }
               }

               if (callback != null) {
                  callback.onProgress(bytesDownloaded, totalBytes);
               }
            } catch (IOException var21) {
               if (destinationFile.exists()) {
                  destinationFile.delete();
               }

               throw var21;
            }

            return true;
         } else {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
               Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8);
               StringBuilder errorResponse = new StringBuilder();

               while (scanner.hasNext()) {
                  errorResponse.append(scanner.nextLine());
               }

               scanner.close();
               Logger.warn("Download failed (" + responseCode + ") for " + urlString + ": " + errorResponse);
            } else {
               Logger.warn("Download failed (" + responseCode + ") for " + urlString + " (no error body)");
            }

            return false;
         }
      } catch (IOException var22) {
         if (destinationFile.exists()) {
            destinationFile.delete();
         }

         Logger.warn("Download failed for " + urlString + ": " + var22.getMessage());
         return false;
      }
   }

   private NightbreakAccount.PluginDownloadResult httpDownloadWithProgressResult(
      String urlString, File destinationFile, NightbreakAccount.DownloadProgressCallback callback, BooleanSupplier operationCurrent
   ) {
      try {
         if (!operationCurrent.getAsBoolean()) {
            throw new IOException("Plugin download cancelled.");
         } else {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            configureScopedTestRedirectPolicy(connection);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            if (this.token != null) {
               connection.setRequestProperty("Authorization", "Bearer " + this.token);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
               if (!destinationFile.getParentFile().exists()) {
                  destinationFile.getParentFile().mkdirs();
               }

               long totalBytes = connection.getContentLengthLong();
               if (totalBytes > 268435456L) {
                  if (destinationFile.exists()) {
                     destinationFile.delete();
                  }

                  return new NightbreakAccount.PluginDownloadResult(
                     false, responseCode, "DOWNLOAD_TOO_LARGE", "Plugin update exceeds the maximum download size.", null, null
                  );
               } else {
                  long bytesDownloaded = 0L;
                  long lastProgressUpdate = 0L;
                  boolean exceededLimit = false;

                  try (
                     InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(destinationFile);
                  ) {
                     byte[] buffer = new byte[8192];

                     int bytesRead;
                     while ((bytesRead = in.read(buffer)) != -1) {
                        if (bytesDownloaded + bytesRead > 268435456L) {
                           exceededLimit = true;
                           break;
                        }

                        if (!operationCurrent.getAsBoolean()) {
                           throw new IOException("Plugin download cancelled.");
                        }

                        out.write(buffer, 0, bytesRead);
                        bytesDownloaded += bytesRead;
                        if (callback != null && bytesDownloaded - lastProgressUpdate >= 102400L) {
                           callback.onProgress(bytesDownloaded, totalBytes);
                           lastProgressUpdate = bytesDownloaded;
                        }
                     }

                     if (!exceededLimit && callback != null) {
                        callback.onProgress(bytesDownloaded, totalBytes);
                     }
                  } catch (IOException var23) {
                     if (destinationFile.exists()) {
                        destinationFile.delete();
                     }

                     throw var23;
                  }

                  if (exceededLimit) {
                     if (destinationFile.exists()) {
                        destinationFile.delete();
                     }

                     return new NightbreakAccount.PluginDownloadResult(
                        false, responseCode, "DOWNLOAD_TOO_LARGE", "Plugin update exceeds the maximum download size.", null, null
                     );
                  } else {
                     return new NightbreakAccount.PluginDownloadResult(true, responseCode, null, null, null, null);
                  }
               }
            } else {
               String errorResponse = readErrorResponse(connection);
               if (responseCode == 401) {
                  logHttpError(urlString, responseCode, errorResponse);
               } else if (responseCode >= 500) {
                  Logger.warn(
                     "Nightbreak plugin update download failed (" + responseCode + ") for " + urlString + (errorResponse == null ? "" : ": " + errorResponse)
                  );
               }

               return NightbreakAccount.PluginDownloadResult.fromHttp(responseCode, errorResponse);
            }
         }
      } catch (IOException var24) {
         if (destinationFile.exists()) {
            destinationFile.delete();
         }

         logNetworkFailure(urlString, var24.getMessage());
         return new NightbreakAccount.PluginDownloadResult(false, 0, "NETWORK_ERROR", var24.getMessage(), null, null);
      }
   }

   private static String readErrorResponse(HttpURLConnection connection) throws IOException {
      InputStream errorStream = connection.getErrorStream();
      if (errorStream == null) {
         return null;
      } else {
         Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8);
         StringBuilder errorResponse = new StringBuilder();

         while (scanner.hasNext()) {
            errorResponse.append(scanner.nextLine());
         }

         scanner.close();
         return errorResponse.toString();
      }
   }

   private static void configureScopedTestRedirectPolicy(HttpURLConnection connection) {
      if (scopedTestBaseUrl != null) {
         connection.setInstanceFollowRedirects(false);
      }
   }

   private static NightbreakAccount.VersionInfo parseVersionInfo(String json) {
      return parseVersionInfo(JsonParser.parseString(json).getAsJsonObject());
   }

   private static NightbreakAccount.VersionInfo parseVersionInfo(JsonObject json) {
      NightbreakAccount.VersionInfo info = new NightbreakAccount.VersionInfo();
      info.slug = extractJsonString(json, "slug");
      info.version = extractJsonString(json, "version");
      if (info.version == null) {
         info.version = extractJsonString(json, "currentVersion");
      }

      info.versionInt = extractJsonInt(json, "versionInt");
      if (info.versionInt < 0) {
         info.versionInt = parseIntegerVersion(info.version);
      }

      info.fileSize = extractJsonLong(json, "fileSize");
      info.fileName = extractJsonString(json, "fileName");
      info.checksum = extractJsonString(json, "checksum");
      info.changelog = extractJsonString(json, "changelog");
      info.downloadPageUrl = extractJsonString(json, "downloadPageUrl");
      return info;
   }

   private static int parseIntegerVersion(String version) {
      if (version == null) {
         return -1;
      } else {
         String normalized = version.trim();
         if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
         }

         if (!normalized.matches("\\d+")) {
            return -1;
         } else {
            try {
               return Integer.parseInt(normalized);
            } catch (NumberFormatException var3) {
               return -1;
            }
         }
      }
   }

   private static NightbreakAccount.AccessInfo parseAccessInfo(String json) {
      return parseAccessInfo(JsonParser.parseString(json).getAsJsonObject());
   }

   private static NightbreakAccount.AccessInfo parseAccessInfo(JsonObject json) {
      NightbreakAccount.AccessInfo info = new NightbreakAccount.AccessInfo();
      info.slug = extractJsonString(json, "slug");
      info.hasAccess = extractJsonBoolean(json, "hasAccess");
      info.accessSource = extractJsonString(json, "accessSource");
      info.reason = extractJsonString(json, "reason");
      info.version = extractJsonString(json, "version");
      info.fileSize = extractJsonLong(json, "fileSize");
      info.requiredTier = extractJsonString(json, "requiredTier");
      info.patreonLink = extractJsonString(json, "patreonLink");
      info.itchLink = extractJsonString(json, "itchLink");
      info.error = extractJsonString(json, "error");
      info.message = extractJsonString(json, "message");
      return info;
   }

   private static String extractJsonString(JsonObject json, String key) {
      JsonElement value = json.get(key);
      if (value == null || value.isJsonNull()) {
         return null;
      } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
         return value.getAsString();
      } else {
         throw new IllegalArgumentException("Expected string for " + key);
      }
   }

   private static int extractJsonInt(JsonObject json, String key) {
      return Math.toIntExact(extractJsonLong(json, key));
   }

   private static long extractJsonLong(JsonObject json, String key) {
      JsonElement value = json.get(key);
      if (value == null || value.isJsonNull()) {
         return -1L;
      } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
         return value.getAsBigDecimal().longValueExact();
      } else {
         throw new IllegalArgumentException("Expected number for " + key);
      }
   }

   private static boolean extractJsonBoolean(JsonObject json, String key) {
      JsonElement value = json.get(key);
      if (value == null || value.isJsonNull()) {
         return false;
      } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
         return value.getAsBoolean();
      } else {
         throw new IllegalArgumentException("Expected boolean for " + key);
      }
   }

   private static Map<String, NightbreakAccount.VersionInfo> parseAllVersionsResponse(String json) {
      Map<String, NightbreakAccount.VersionInfo> versions = new HashMap<>();
      JsonArray entries = JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("versions");
      if (entries == null) {
         throw new IllegalArgumentException("Missing versions array");
      } else {
         for (JsonElement entry : entries) {
            NightbreakAccount.VersionInfo info = parseVersionInfo(entry.getAsJsonObject());
            if (info.slug != null) {
               versions.put(info.slug, info);
            }
         }

         return versions;
      }
   }

   @Generated
   public String getToken() {
      return this.token;
   }

   public static class AccessInfo {
      public String slug;
      public boolean hasAccess;
      public String accessSource;
      public String reason;
      public String version;
      public long fileSize;
      public String requiredTier;
      public String patreonLink;
      public String itchLink;
      public String error;
      public String message;

      public boolean isError() {
         return this.error != null && !this.error.isEmpty();
      }

      @Override
      public String toString() {
         return this.isError()
            ? "AccessInfo{error='" + this.error + "', message='" + this.message + "'}"
            : "AccessInfo{slug='"
               + this.slug
               + "', hasAccess="
               + this.hasAccess
               + ", accessSource='"
               + this.accessSource
               + "', reason='"
               + this.reason
               + "', version='"
               + this.version
               + "', fileSize="
               + this.fileSize
               + ", patreonLink='"
               + this.patreonLink
               + "', itchLink='"
               + this.itchLink
               + "'}";
      }
   }

   record ChangelogFetch(List<NightbreakAccount.PluginChangelog> releases, boolean serviceUnavailable) {
   }

   public interface DownloadProgressCallback {
      void onProgress(long var1, long var3);
   }

   public record PluginChangelog(String version, String changelog, String releasedAt) {
   }

   public static class PluginDownloadResult {
      public final boolean success;
      public final int responseCode;
      public final String error;
      public final String message;
      public final String reason;
      public final String requiredTier;

      public PluginDownloadResult(boolean success, int responseCode, String error, String message, String reason, String requiredTier) {
         this.success = success;
         this.responseCode = responseCode;
         this.error = error;
         this.message = message;
         this.reason = reason;
         this.requiredTier = requiredTier;
      }

      private static NightbreakAccount.PluginDownloadResult fromHttp(int responseCode, String json) {
         try {
            JsonObject body = json == null ? new JsonObject() : JsonParser.parseString(json).getAsJsonObject();
            return new NightbreakAccount.PluginDownloadResult(
               false,
               responseCode,
               NightbreakAccount.extractJsonString(body, "error"),
               NightbreakAccount.extractJsonString(body, "message"),
               NightbreakAccount.extractJsonString(body, "reason"),
               NightbreakAccount.extractJsonString(body, "requiredTier")
            );
         } catch (RuntimeException var3) {
            return new NightbreakAccount.PluginDownloadResult(
               false, responseCode, "INVALID_RESPONSE", "Nightbreak returned an invalid error response.", null, null
            );
         }
      }

      public String displayDetail() {
         if (this.reason != null && !this.reason.isBlank()) {
            return this.reason;
         } else if (this.message != null && !this.message.isBlank()) {
            return this.message;
         } else if (this.error != null && !this.error.isBlank()) {
            return this.error;
         } else {
            return this.responseCode > 0 ? "Nightbreak returned HTTP " + this.responseCode + "." : null;
         }
      }
   }

   private record PublicResponse(int status, String body) {
   }

   static final class ScopedTestBaseUrl implements AutoCloseable {
      private final String expectedBaseUrl;
      private boolean closed;

      private ScopedTestBaseUrl(String expectedBaseUrl) {
         this.expectedBaseUrl = expectedBaseUrl;
      }

      @Override
      public void close() {
         synchronized (NightbreakAccount.TEST_BASE_URL_LOCK) {
            if (!this.closed) {
               if (!this.expectedBaseUrl.equals(NightbreakAccount.scopedTestBaseUrl)) {
                  throw new IllegalStateException("Nightbreak test base URI scope changed unexpectedly.");
               } else {
                  NightbreakAccount.scopedTestBaseUrl = null;
                  this.closed = true;
               }
            }
         }
      }
   }

   public interface TokenChangeListenerRegistration extends AutoCloseable {
      @Override
      void close();
   }

   private static final class TokenChangeListenerRegistrationImpl implements NightbreakAccount.TokenChangeListenerRegistration {
      private final Runnable listener;
      private final AtomicBoolean closed = new AtomicBoolean();

      private TokenChangeListenerRegistrationImpl(Runnable listener) {
         this.listener = listener;
      }

      @Override
      public void close() {
         if (this.closed.compareAndSet(false, true)) {
            NightbreakAccount.tokenChangeListeners.remove(this);
         }
      }
   }

   public static class VersionInfo {
      public String slug;
      public String version;
      public int versionInt;
      public long fileSize;
      public String fileName;
      public String checksum;
      public String changelog;
      public String downloadPageUrl;

      @Override
      public String toString() {
         return "VersionInfo{slug='"
            + this.slug
            + "', version='"
            + this.version
            + "', versionInt="
            + this.versionInt
            + ", fileSize="
            + this.fileSize
            + ", fileName='"
            + this.fileName
            + "', checksum='"
            + this.checksum
            + "'}";
      }
   }
}
