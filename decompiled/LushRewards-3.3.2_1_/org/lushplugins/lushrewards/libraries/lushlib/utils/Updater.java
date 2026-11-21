package org.lushplugins.lushrewards.libraries.lushlib.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;

public class Updater {
   private static final ScheduledExecutorService updateExecutor = Executors.newScheduledThreadPool(1);
   private static final HashSet<Updater> updaters = new HashSet<>();
   private final String pluginName;
   private final String modrinthProjectId;
   private final String currentVersion;
   private final String jarName;
   private final String permission;
   private final String downloadCommand;
   private final File updateFolder;
   private String updateMessage = "&#ffe27aA new &#e0c01b%plugin_name% &#ffe27aupdate is now available, type &#e0c01b'%download_command%' &#ffe27ato download it!";
   private boolean enabled;
   private String latestVersion;
   private String downloadUrl;
   private boolean updateAvailable = false;
   private boolean ready = false;
   private boolean alreadyDownloaded = false;

   public Updater(JavaPlugin plugin, String modrinthProjectId, String permission, String downloadCommand) {
      this.pluginName = plugin.getName();
      this.modrinthProjectId = modrinthProjectId;
      String currentVersion = plugin.getDescription().getVersion();
      this.currentVersion = currentVersion.contains("-") ? currentVersion.split("-")[0] : currentVersion;
      this.jarName = plugin.getDescription().getName();
      this.permission = permission;
      this.downloadCommand = downloadCommand;
      this.updateFolder = new File(plugin.getDataFolder().getParentFile(), Bukkit.getUpdateFolder());
      updateExecutor.scheduleAtFixedRate(() -> {
         try {
            this.check();
         } catch (Exception var2x) {
            LushLogger.getLogger().info("Unable to check for update: " + var2x.getMessage());
         }
      }, 2L, 600L, TimeUnit.SECONDS);
      updaters.add(this);
   }

   public void queueCheck() {
      updateExecutor.schedule(() -> {
         try {
            this.check();
         } catch (Exception var2) {
            LushLogger.getLogger().info("Unable to check for update: " + var2.getMessage());
         }
      }, 0L, TimeUnit.SECONDS);
   }

   private void check() throws IOException {
      if (this.enabled) {
         URL url = new URL("https://api.modrinth.com/v2/project/" + this.modrinthProjectId + "/version?featured=true");
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.addRequestProperty("User-Agent", this.jarName + "/" + this.currentVersion);
         if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Response code was " + connection.getResponseCode());
         } else {
            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            String bukkitVersion = Bukkit.getBukkitVersion();
            JsonArray versionsJson;
            if (!bukkitVersion.contains("1.16") && !bukkitVersion.contains("1.17")) {
               versionsJson = JsonParser.parseReader(reader).getAsJsonArray();
            } else {
               versionsJson = new JsonParser().parse(reader).getAsJsonArray();
            }

            JsonObject currVersionJson = versionsJson.get(0).getAsJsonObject();
            this.latestVersion = currVersionJson.get("version_number").getAsString();
            this.downloadUrl = currVersionJson.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
            if (this.latestVersion.contains("-")) {
               this.latestVersion = this.latestVersion.split("-")[0];
            }

            if (this.latestVersion.isEmpty()) {
               throw new IllegalStateException("Latest version is empty!");
            } else {
               String[] parts = this.latestVersion.split("\\.");
               String[] currParts = this.currentVersion.split("\\.");
               int i = 0;

               for (String part : parts) {
                  if (i >= currParts.length) {
                     break;
                  }

                  int newVersion = Integer.parseInt(part);
                  int currVersion = Integer.parseInt(currParts[i]);
                  if (newVersion > currVersion) {
                     if (i == 0) {
                        this.updateAvailable = true;
                        break;
                     }

                     int newVersionLast = Integer.parseInt(parts[i - 1]);
                     int currVersionLast = Integer.parseInt(currParts[i - 1]);
                     if (newVersionLast >= currVersionLast) {
                        this.updateAvailable = true;
                        break;
                     }
                  } else if (newVersion < currVersion) {
                     break;
                  }

                  i++;
               }

               if (this.updateAvailable && !this.ready) {
                  LushLogger.getLogger().info("An update is available! (" + this.latestVersion + ") Do /" + this.downloadCommand + " to download it!");
               } else if (!this.ready) {
                  LushLogger.getLogger().info("You are up to date! (" + this.latestVersion + ")");
               }

               this.ready = true;
            }
         }
      }
   }

   public boolean isUpdateAvailable() {
      return this.enabled && this.updateAvailable;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isAlreadyDownloaded() {
      return this.alreadyDownloaded;
   }

   public CompletableFuture<Boolean> downloadUpdate() {
      if (!this.isEnabled()) {
         LushLogger.getLogger().warning("Updater is disabled");
         return CompletableFuture.completedFuture(false);
      } else if (!this.isUpdateAvailable()) {
         LushLogger.getLogger().warning("No update is available!");
         return CompletableFuture.completedFuture(false);
      } else if (this.isAlreadyDownloaded()) {
         LushLogger.getLogger().warning("The update has already been downloaded!");
         return CompletableFuture.completedFuture(false);
      } else {
         CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
         updateExecutor.schedule(() -> {
            try {
               URL url = new URL(this.downloadUrl);
               HttpURLConnection connection = (HttpURLConnection)url.openConnection();
               connection.addRequestProperty("User-Agent", this.jarName + "/" + this.currentVersion);
               connection.setInstanceFollowRedirects(true);
               HttpURLConnection.setFollowRedirects(true);
               if (connection.getResponseCode() != 200) {
                  throw new IllegalStateException("Response code was " + connection.getResponseCode());
               }

               ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
               File out = new File(this.getUpdateFolder(), this.jarName + "-" + this.latestVersion + ".jar");
               LushLogger.getLogger().info(out.getAbsolutePath());
               FileOutputStream fos = new FileOutputStream(out);
               fos.getChannel().transferFrom(rbc, 0L, Long.MAX_VALUE);
               fos.close();
               this.updateAvailable = false;
               this.alreadyDownloaded = true;
               completableFuture.complete(true);
            } catch (Exception var7) {
               var7.printStackTrace();
               completableFuture.complete(false);
            }
         }, 0L, TimeUnit.SECONDS);
         return completableFuture;
      }
   }

   public void shutdown() {
      updateExecutor.shutdown();
      updaters.remove(this);
   }

   public String getPluginName() {
      return this.pluginName;
   }

   public String getModrinthProjectId() {
      return this.modrinthProjectId;
   }

   public String getPermission() {
      return this.permission;
   }

   public String getDownloadCommand() {
      return this.downloadCommand;
   }

   public File getUpdateFolder() {
      if (!this.updateFolder.exists()) {
         this.updateFolder.mkdir();
      }

      return this.updateFolder;
   }

   public String getUpdateMessage() {
      return this.updateMessage;
   }

   public Updater setUpdateMessage(String updateMessage) {
      this.updateMessage = updateMessage;
      return this;
   }

   public static HashSet<Updater> getUpdaters() {
      return updaters;
   }

   public static class UpdaterListener implements Listener {
      @EventHandler
      public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         Updater.updaters
            .forEach(
               updater -> {
                  if (player.hasPermission(updater.getPermission()) && updater.isUpdateAvailable() && !updater.isAlreadyDownloaded()) {
                     Updater.updateExecutor
                        .schedule(
                           () -> {
                              String message = updater.getUpdateMessage()
                                 .replace("%modrinth_slug%", updater.getModrinthProjectId())
                                 .replace("%plugin_name%", updater.getPluginName())
                                 .replace("%download_command%", updater.getDownloadCommand());
                              ChatColorHandler.sendMessage(player, message);
                           },
                           2L,
                           TimeUnit.SECONDS
                        );
                  }
               }
            );
      }
   }
}
