package github.nighter.smartspawner.updates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import github.nighter.smartspawner.Scheduler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker implements Listener {
   private final JavaPlugin plugin;
   private final String projectId = "9tQwxSFr";
   private static final int MAX_NOTIFIABLE_MAJOR_VERSION = 1;
   private boolean updateAvailable = false;
   private final String currentVersion;
   private String latestVersion = "";
   private String downloadUrl = "";
   private static final String RESET = "\u001b[0m";
   private static final String BOLD = "\u001b[1m";
   private static final String DIM = "\u001b[2m";
   private static final String GREEN = "\u001b[92m";
   private static final String YELLOW = "\u001b[33m";
   private static final String CYAN = "\u001b[96m";
   private final Map<UUID, LocalDate> notifiedPlayers = new HashMap<>();

   public UpdateChecker(JavaPlugin plugin) {
      this.plugin = plugin;
      this.currentVersion = plugin.getPluginMeta().getVersion();
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
      this.checkForUpdates().thenAccept(hasUpdate -> {
         if (hasUpdate) {
            this.displayConsoleUpdateMessage();
         }
      }).exceptionally(ex -> {
         plugin.getLogger().warning("Failed to check for updates: " + ex.getMessage());
         return null;
      });
   }

   private void displayConsoleUpdateMessage() {
      String line = "\u001b[2m-----------------------------------------------------\u001b[0m";
      this.plugin.getLogger().info(line);
      this.plugin.getLogger().info("\u001b[1m  SmartSpawner - Update Available\u001b[0m");
      this.plugin.getLogger().info(line);
      this.plugin.getLogger().info("  Current version  :  \u001b[33m" + this.currentVersion + "\u001b[0m");
      this.plugin.getLogger().info("  Latest version   :  \u001b[92m" + this.latestVersion + "\u001b[0m");
      this.plugin.getLogger().info("  Download         :  \u001b[96mhttps://modrinth.com/plugin/9tQwxSFr/version/" + this.latestVersion + "\u001b[0m");
      this.plugin.getLogger().info(line);
   }

   private boolean isNotifiableRelease(String versionNumber) {
      return new Version(versionNumber).getMajor() <= 1;
   }

   public CompletableFuture<Boolean> checkForUpdates() {
      return CompletableFuture.supplyAsync(() -> {
         try {
            HttpURLConnection connection = (HttpURLConnection)URI.create("https://api.modrinth.com/v2/project/9tQwxSFr/version").toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "SmartSpawner-UpdateChecker/1.0");
            if (connection.getResponseCode() != 200) {
               this.plugin.getLogger().warning("Update check failed with HTTP " + connection.getResponseCode());
               return false;
            } else {
               BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

               String response;
               try {
                  response = reader.lines().collect(Collectors.joining("\n"));
               } catch (Throwable var11) {
                  try {
                     reader.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }

                  throw var11;
               }

               reader.close();
               JsonArray versions = JsonParser.parseString(response).getAsJsonArray();
               if (versions.isEmpty()) {
                  return false;
               } else {
                  JsonObject latestVersionObj = null;

                  for (JsonElement element : versions) {
                     JsonObject version = element.getAsJsonObject();
                     if ("release".equals(version.get("version_type").getAsString()) && this.isNotifiableRelease(version.get("version_number").getAsString())) {
                        if (latestVersionObj == null) {
                           latestVersionObj = version;
                        } else {
                           String currentDate = latestVersionObj.get("date_published").getAsString();
                           String newDate = version.get("date_published").getAsString();
                           if (newDate.compareTo(currentDate) > 0) {
                              latestVersionObj = version;
                           }
                        }
                     }
                  }

                  if (latestVersionObj == null) {
                     return false;
                  } else {
                     this.latestVersion = latestVersionObj.get("version_number").getAsString();
                     this.downloadUrl = "https://modrinth.com/plugin/9tQwxSFr/version/" + this.latestVersion;
                     this.updateAvailable = new Version(this.latestVersion).compareTo(new Version(this.currentVersion)) > 0;
                     return this.updateAvailable;
                  }
               }
            }
         } catch (Exception var12) {
            this.plugin.getLogger().warning("Error checking for updates: " + var12.getMessage());
            return false;
         }
      });
   }

   private void sendUpdateNotification(Player player) {
      if (this.updateAvailable && player.hasPermission("smartspawner.admin")) {
         TextColor purple = TextColor.fromHexString("#9b72cf");
         TextColor green = TextColor.fromHexString("#57d98e");
         TextColor yellow = TextColor.fromHexString("#f0c857");
         TextColor white = TextColor.fromHexString("#e6e6e6");
         TextColor gray = TextColor.fromHexString("#555577");
         Component line = Component.text("----------------------------------------").color(gray);
         Component header = ((TextComponent)Component.text("SmartSpawner").color(purple)).append(Component.text(" - Update Available").color(white));
         Component versions = ((TextComponent)((TextComponent)((TextComponent)Component.text("  Current: ").color(white))
                  .append(Component.text(this.currentVersion).color(yellow)))
               .append(Component.text("  ->  Latest: ").color(white)))
            .append(Component.text(this.latestVersion).color(green));
         Component download = ((TextComponent)((TextComponent)Component.text("  [ Click here to download v" + this.latestVersion + " ]").color(purple))
               .clickEvent(ClickEvent.openUrl(this.downloadUrl)))
            .hoverEvent(HoverEvent.showText(Component.text("Open download page on Modrinth").color(white)));
         player.sendMessage(Component.empty());
         player.sendMessage(line);
         player.sendMessage(header);
         player.sendMessage(versions);
         player.sendMessage(download);
         player.sendMessage(line);
         player.sendMessage(Component.empty());
         player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 1.2F);
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (player.isOp()) {
         UUID playerId = player.getUniqueId();
         LocalDate today = LocalDate.now();
         this.notifiedPlayers.entrySet().removeIf(entry -> entry.getValue().isBefore(today));
         if (!this.notifiedPlayers.containsKey(playerId)) {
            if (this.updateAvailable) {
               Scheduler.runTaskLater(() -> {
                  this.sendUpdateNotification(player);
                  this.notifiedPlayers.put(playerId, today);
               }, 40L);
            } else {
               this.checkForUpdates().thenAccept(hasUpdate -> {
                  if (hasUpdate) {
                     Scheduler.runTask(() -> {
                        this.sendUpdateNotification(player);
                        this.notifiedPlayers.put(playerId, today);
                     });
                  }
               });
            }
         }
      }
   }
}
