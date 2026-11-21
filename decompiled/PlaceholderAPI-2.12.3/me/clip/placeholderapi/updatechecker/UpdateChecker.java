package me.clip.placeholderapi.updatechecker;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import javax.net.ssl.HttpsURLConnection;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.scheduler.scheduling.schedulers.TaskScheduler;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateChecker implements Listener {
   private static final String MODRINTH_URL = "https://api.modrinth.com/v2/project/lKEzGugV/version";
   private static final int RESOURCE_ID = 6245;
   private final PlaceholderAPIPlugin plugin;
   private final TaskScheduler scheduler;
   private final String pluginVersion;
   private String modrinthVersion;
   private boolean updateAvailable;

   public UpdateChecker(PlaceholderAPIPlugin plugin) {
      this.plugin = plugin;
      this.scheduler = plugin.getScheduler();
      this.pluginVersion = plugin.getDescription().getVersion();
   }

   public boolean hasUpdateAvailable() {
      return this.updateAvailable;
   }

   public String getModrinthVersion() {
      return this.modrinthVersion;
   }

   public void fetch() {
      this.scheduler.runTaskAsynchronously(() -> {
         try {
            HttpsURLConnection con = (HttpsURLConnection)new URL("https://api.modrinth.com/v2/project/lKEzGugV/version").openConnection();
            con.setRequestMethod("GET");
            JsonElement json = (JsonElement)new Gson().fromJson(new BufferedReader(new InputStreamReader(con.getInputStream())), JsonElement.class);
            this.modrinthVersion = json.getAsJsonArray().get(0).getAsJsonObject().get("version_number").getAsString();
         } catch (Exception var3) {
            this.plugin.getLogger().info("Failed to check for updates on modrinth.");
            return;
         }

         if (this.modrinthVersion != null && !this.modrinthVersion.isEmpty()) {
            this.updateAvailable = this.modrinthIsNewer();
            if (this.updateAvailable) {
               this.scheduler.runTask(() -> {
                  this.plugin.getLogger().info("An update for PlaceholderAPI (v" + this.getModrinthVersion() + ") is available at:");
                  this.plugin.getLogger().info("https://modrinth.com/plugin/placeholderapi");
                  Bukkit.getPluginManager().registerEvents(this, this.plugin);
               });
            }
         }
      });
   }

   private boolean modrinthIsNewer() {
      if (this.modrinthVersion != null && !this.modrinthVersion.isEmpty()) {
         int[] plV = this.toReadable(this.pluginVersion);
         int[] spV = this.toReadable(this.modrinthVersion);
         if (plV[0] < spV[0]) {
            return true;
         } else {
            return plV[1] < spV[1] ? true : plV[2] < spV[2];
         }
      } else {
         return false;
      }
   }

   private int[] toReadable(String version) {
      if (version.contains("-DEV")) {
         version = version.split("-DEV")[0];
      }

      return Arrays.stream(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onJoin(PlayerJoinEvent e) {
      if (e.getPlayer().hasPermission("placeholderapi.updatenotify")) {
         Msg.msg(
            e.getPlayer(),
            "&bAn update for &fPlaceholder&7API &e(&fPlaceholder&7API &fv" + this.getModrinthVersion() + "&e)",
            "&bis available at &ehttps://modrinth.com/plugin/placeholderapi"
         );
      }
   }
}
