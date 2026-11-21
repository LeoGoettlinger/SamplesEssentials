package xyz.lychee.lagfixer.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.TextComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.event.ClickEvent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.event.HoverEvent;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class UpdaterManager extends AbstractManager implements Listener {
   private static UpdaterManager instance;
   private final UpdaterManager.VersionComparator comparator = new UpdaterManager.VersionComparator();
   private final Gson gson = new Gson();
   private int compared = 0;
   private int difference = 0;
   private int behind = -1;
   private String latestVersion = "";
   private String currentVersion = "";
   private BukkitTask task;
   private boolean updater;

   public UpdaterManager(LagFixer plugin) {
      super(plugin);
      instance = this;
   }

   @EventHandler
   public void onClick(PlayerJoinEvent e) {
      if (e.getPlayer().hasPermission("lagfixer.updater") && this.compared < 0 && this.updater) {
         SupportManager.getInstance()
            .getFork()
            .runLater(
               true,
               () -> {
                  Component text = ((TextComponent)MessageUtils.colors(
                           e.getPlayer(),
                           "LagFixer needs update, latest version: &f%s\n &8- &ehttps://modrinth.com/plugin/lagfixer/version/%s\n"
                              .formatted(this.latestVersion, this.currentVersion)
                        )
                        .hoverEvent(HoverEvent.showText((Component)MessageUtils.colors(e.getPlayer(), "Click to open url!"))))
                     .clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/lagfixer/version/" + this.latestVersion));
                  this.getPlugin().getAudiences().player(e.getPlayer()).sendMessage(text);
               },
               3L,
               TimeUnit.SECONDS
            );
      }
   }

   @Override
   public void load() throws IOException {
      this.updater = this.getPlugin().getConfig().getBoolean("main.updater");
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.spigotmc.org/legacy/update.php")).GET().build();
      this.task = SupportManager.getInstance()
         .getFork()
         .runTimer(
            true,
            () -> {
               this.currentVersion = this.getPlugin().getDescription().getVersion().split(" ")[0].trim();
               SupportManager.getInstance()
                  .getClient()
                  .sendAsync(request, BodyHandlers.ofString())
                  .thenAccept(
                     response -> {
                        Type listType = (new TypeToken<ArrayList<UpdaterManager.ModrinthVersion>>() {}).getType();
                        List<UpdaterManager.ModrinthVersion> versions = (List<UpdaterManager.ModrinthVersion>)this.gson.fromJson(response.body(), listType);
                        versions.removeIf(version -> version.getVersion_number().matches(".*[^0-9.].*"));
                        if (!versions.isEmpty()) {
                           versions.sort((v1, v2) -> this.comparator.compare(v2.getVersion_number(), v1.getVersion_number()));
                           UpdaterManager.ModrinthVersion latest = versions.getFirst();
                           this.latestVersion = latest.getVersion_number();
                           this.behind = this.calculateBuildsBehind(versions);
                        } else {
                           this.latestVersion = this.getPlugin().getDescription().getVersion();
                           this.behind = 0;
                        }

                        this.difference = this.comparator.difference(this.currentVersion, this.latestVersion);
                        this.compared = this.comparator.compare(this.currentVersion, this.latestVersion);
                        if (this.difference >= 0 && this.difference < 2 || this.behind > 5) {
                           this.updater = true;
                        }

                        if (this.updater && this.compared < 0) {
                           this.getPlugin()
                              .getLogger()
                              .info(
                                 String.format(
                                    "\n&8∘₊✧────────────────────────────────✧₊∘\n&c&lLagFixer needs an update!\n&fVersion: &e&n%s&r -> &e&n%s&r\n&ahttps://modrinth.com/plugin/lagfixer/version/%s\n\n&6⚠ &7Updating this plugin is crucial! &6⚠\n&8∘₊✧────────────────────────────────✧₊∘\n",
                                    this.currentVersion,
                                    this.latestVersion,
                                    this.latestVersion
                                 )
                              );
                        }
                     }
                  );
            },
            1L,
            30L,
            TimeUnit.MINUTES
         );
      Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
   }

   private int calculateBuildsBehind(List<UpdaterManager.ModrinthVersion> versions) {
      if (this.currentVersion.equals(this.latestVersion)) {
         return 0;
      } else {
         int currentIndex = -1;
         int latestIndex = -1;

         for (int i = 0; i < versions.size(); i++) {
            UpdaterManager.ModrinthVersion version = versions.get(i);
            if (version.getVersion_number().equals(this.currentVersion)) {
               currentIndex = i;
            }

            if (version.getVersion_number().equals(this.latestVersion)) {
               latestIndex = i;
            }
         }

         return currentIndex != -1 && latestIndex != -1 ? currentIndex - latestIndex : -1;
      }
   }

   @Override
   public void disable() {
      HandlerList.unregisterAll(this);
      if (this.task != null && !this.task.isCancelled()) {
         this.task.cancel();
      }
   }

   @Override
   public boolean isEnabled() {
      return true;
   }

   @Generated
   public UpdaterManager.VersionComparator getComparator() {
      return this.comparator;
   }

   @Generated
   public Gson getGson() {
      return this.gson;
   }

   @Generated
   public int getCompared() {
      return this.compared;
   }

   @Generated
   public int getDifference() {
      return this.difference;
   }

   @Generated
   public int getBehind() {
      return this.behind;
   }

   @Generated
   public String getLatestVersion() {
      return this.latestVersion;
   }

   @Generated
   public String getCurrentVersion() {
      return this.currentVersion;
   }

   @Generated
   public BukkitTask getTask() {
      return this.task;
   }

   @Generated
   public boolean isUpdater() {
      return this.updater;
   }

   @Generated
   public static UpdaterManager getInstance() {
      return instance;
   }

   public static class Dependency {
      public String version_id;
      public String project_id;
      public String file_name;
      public String dependency_type;

      @Generated
      public String getVersion_id() {
         return this.version_id;
      }

      @Generated
      public String getProject_id() {
         return this.project_id;
      }

      @Generated
      public String getFile_name() {
         return this.file_name;
      }

      @Generated
      public String getDependency_type() {
         return this.dependency_type;
      }

      @Generated
      public void setVersion_id(String version_id) {
         this.version_id = version_id;
      }

      @Generated
      public void setProject_id(String project_id) {
         this.project_id = project_id;
      }

      @Generated
      public void setFile_name(String file_name) {
         this.file_name = file_name;
      }

      @Generated
      public void setDependency_type(String dependency_type) {
         this.dependency_type = dependency_type;
      }
   }

   public static class ModrinthVersion {
      private List<String> game_versions;
      private List<String> loaders;
      private String id;
      private String project_id;
      private String author_id;
      private boolean featured;
      private String name;
      private String version_number;
      private String changelog;
      private String changelog_url;
      private String date_published;
      private int downloads;
      private String version_type;
      private String status;
      private String requested_status;
      private List<UpdaterManager.VersionFile> files;
      private List<UpdaterManager.Dependency> dependencies;

      @Generated
      public List<String> getGame_versions() {
         return this.game_versions;
      }

      @Generated
      public List<String> getLoaders() {
         return this.loaders;
      }

      @Generated
      public String getId() {
         return this.id;
      }

      @Generated
      public String getProject_id() {
         return this.project_id;
      }

      @Generated
      public String getAuthor_id() {
         return this.author_id;
      }

      @Generated
      public boolean isFeatured() {
         return this.featured;
      }

      @Generated
      public String getName() {
         return this.name;
      }

      @Generated
      public String getVersion_number() {
         return this.version_number;
      }

      @Generated
      public String getChangelog() {
         return this.changelog;
      }

      @Generated
      public String getChangelog_url() {
         return this.changelog_url;
      }

      @Generated
      public String getDate_published() {
         return this.date_published;
      }

      @Generated
      public int getDownloads() {
         return this.downloads;
      }

      @Generated
      public String getVersion_type() {
         return this.version_type;
      }

      @Generated
      public String getStatus() {
         return this.status;
      }

      @Generated
      public String getRequested_status() {
         return this.requested_status;
      }

      @Generated
      public List<UpdaterManager.VersionFile> getFiles() {
         return this.files;
      }

      @Generated
      public List<UpdaterManager.Dependency> getDependencies() {
         return this.dependencies;
      }

      @Generated
      public void setGame_versions(List<String> game_versions) {
         this.game_versions = game_versions;
      }

      @Generated
      public void setLoaders(List<String> loaders) {
         this.loaders = loaders;
      }

      @Generated
      public void setId(String id) {
         this.id = id;
      }

      @Generated
      public void setProject_id(String project_id) {
         this.project_id = project_id;
      }

      @Generated
      public void setAuthor_id(String author_id) {
         this.author_id = author_id;
      }

      @Generated
      public void setFeatured(boolean featured) {
         this.featured = featured;
      }

      @Generated
      public void setName(String name) {
         this.name = name;
      }

      @Generated
      public void setVersion_number(String version_number) {
         this.version_number = version_number;
      }

      @Generated
      public void setChangelog(String changelog) {
         this.changelog = changelog;
      }

      @Generated
      public void setChangelog_url(String changelog_url) {
         this.changelog_url = changelog_url;
      }

      @Generated
      public void setDate_published(String date_published) {
         this.date_published = date_published;
      }

      @Generated
      public void setDownloads(int downloads) {
         this.downloads = downloads;
      }

      @Generated
      public void setVersion_type(String version_type) {
         this.version_type = version_type;
      }

      @Generated
      public void setStatus(String status) {
         this.status = status;
      }

      @Generated
      public void setRequested_status(String requested_status) {
         this.requested_status = requested_status;
      }

      @Generated
      public void setFiles(List<UpdaterManager.VersionFile> files) {
         this.files = files;
      }

      @Generated
      public void setDependencies(List<UpdaterManager.Dependency> dependencies) {
         this.dependencies = dependencies;
      }
   }

   public static class VersionComparator implements Comparator<String> {
      public int compare(String current, String latest) {
         String[] parts1 = current.split("\\.");
         String[] parts2 = latest.split("\\.");
         int length = Math.max(parts1.length, parts2.length);

         for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) {
               return Integer.compare(num1, num2);
            }
         }

         return 0;
      }

      public int difference(String current, String latest) {
         String[] parts1 = current.split("\\.");
         String[] parts2 = latest.split("\\.");
         int length = Math.max(parts1.length, parts2.length);

         for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 < num2) {
               return i;
            }
         }

         return -1;
      }
   }

   public static class VersionFile {
      private Map<String, String> hashes;
      private String url;
      private String filename;
      private boolean primary;
      private int size;
      private String file_type;

      @Generated
      public Map<String, String> getHashes() {
         return this.hashes;
      }

      @Generated
      public String getUrl() {
         return this.url;
      }

      @Generated
      public String getFilename() {
         return this.filename;
      }

      @Generated
      public boolean isPrimary() {
         return this.primary;
      }

      @Generated
      public int getSize() {
         return this.size;
      }

      @Generated
      public String getFile_type() {
         return this.file_type;
      }

      @Generated
      public void setHashes(Map<String, String> hashes) {
         this.hashes = hashes;
      }

      @Generated
      public void setUrl(String url) {
         this.url = url;
      }

      @Generated
      public void setFilename(String filename) {
         this.filename = filename;
      }

      @Generated
      public void setPrimary(boolean primary) {
         this.primary = primary;
      }

      @Generated
      public void setSize(int size) {
         this.size = size;
      }

      @Generated
      public void setFile_type(String file_type) {
         this.file_type = file_type;
      }
   }
}
