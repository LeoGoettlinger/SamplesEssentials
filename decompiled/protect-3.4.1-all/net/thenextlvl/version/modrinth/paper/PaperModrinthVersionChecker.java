package net.thenextlvl.version.modrinth.paper;

import io.papermc.paper.ServerBuildInfo;
import java.util.Objects;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.thenextlvl.version.Version;
import net.thenextlvl.version.modrinth.ModrinthVersion;
import net.thenextlvl.version.modrinth.ModrinthVersionChecker;
import net.thenextlvl.version.plugin.PluginVersionChecker;
import org.bukkit.plugin.Plugin;

public abstract class PaperModrinthVersionChecker<V extends Version> extends ModrinthVersionChecker<V> implements PluginVersionChecker {
   private final V versionRunning;
   private final Plugin plugin;

   public PaperModrinthVersionChecker(Plugin plugin, String id) {
      super(id);
      this.plugin = plugin;
      this.versionRunning = Objects.requireNonNull(this.parseVersion(plugin.getPluginMeta().getVersion()), "Failed to parse plugin version running");
   }

   @Override
   public void checkVersion() {
      this.retrieveLatestSupportedVersion()
         .thenAccept(
            optional -> optional.ifPresentOrElse(
               this::printVersionInfo, () -> this.retrieveLatestVersion().thenAccept(this::printUnsupportedInfo).exceptionally(throwable -> {
                  this.plugin.getComponentLogger().warn("There are no compatible releases of this plugin for your server version");
                  return null;
               })
            )
         )
         .exceptionally(throwable -> {
            Throwable t = throwable.getCause() != null ? throwable.getCause() : throwable;
            this.plugin.getComponentLogger().error("Version check failed", t);
            return null;
         });
   }

   @Override
   public void checkLatestVersion() {
      this.retrieveLatestVersion().thenAccept(this::printVersionInfo).exceptionally(throwable -> {
         this.plugin.getComponentLogger().warn("There are no compatible releases for this plugin for your server version");
         return null;
      });
   }

   private void printUnsupportedInfo(V version) {
      ComponentLogger logger = this.plugin.getComponentLogger();
      ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();
      if (version.equals(this.versionRunning)) {
         logger.warn("{} seems to be unsupported by {} version {}", new Object[]{buildInfo.minecraftVersionId(), this.plugin.getName(), this.versionRunning});
      } else if (version.compareTo(this.versionRunning) > 0) {
         logger.warn("A new version for {} is available but {} seems to be unsupported", this.plugin.getName(), buildInfo.minecraftVersionId());
         logger.warn("You are running version {}, the latest version is {}", this.versionRunning, version);
         logger.warn("Update at https://modrinth.com/project/{}", this.getId());
         logger.warn("Do not test in production and always make backups before updating");
      } else {
         logger.warn("You are running a snapshot version of {}", this.plugin.getName());
      }
   }

   private void printVersionInfo(V version) {
      ComponentLogger logger = this.plugin.getComponentLogger();
      if (version.equals(this.versionRunning)) {
         logger.info("You are running the latest version of {}", this.plugin.getName());
      } else if (version.compareTo(this.versionRunning) > 0) {
         logger.warn("An update for {} is available", this.plugin.getName());
         logger.warn("You are running version {}, the latest version is {}", this.versionRunning, version);
         logger.warn(
            "Update at https://modrinth.com/project/{}/?version={}&loader={}#download",
            new Object[]{this.getId(), this.plugin.getServer().getMinecraftVersion(), this.getLoader()}
         );
         logger.warn("Do not test in production and always make backups before updating");
      } else {
         logger.warn("You are running a snapshot version of {}", this.plugin.getName());
      }
   }

   public String getLoader() {
      return "paper";
   }

   @Override
   protected String getVersionQuery() {
      return "version?loaders=[\"" + this.getLoader() + "\"]&game_versions=[\"" + this.plugin.getServer().getMinecraftVersion() + "\"]";
   }

   public boolean isSupported(ModrinthVersion version) {
      return version.gameVersions().contains(this.plugin.getServer().getMinecraftVersion()) && version.loaders().contains(this.getLoader());
   }

   @Override
   public V getVersionRunning() {
      return this.versionRunning;
   }

   @Override
   public Plugin getPlugin() {
      return this.plugin;
   }
}
