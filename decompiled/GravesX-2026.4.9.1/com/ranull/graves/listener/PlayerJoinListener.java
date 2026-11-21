package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.util.StringUtil;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
   private final Graves plugin;

   public PlayerJoinListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (this.shouldCheckForUpdates(player)) {
         this.plugin.getSchedulerManager().runTaskAsynchronously(() -> this.notifyPlayerIfOutdated(player));
      }
   }

   private boolean shouldCheckForUpdates(Player player) {
      return this.plugin.getConfig().getBoolean("settings.update.check")
         && this.plugin.getPermissionManager().hasGrantedPermission("graves.update.notify", player);
   }

   private void notifyPlayerIfOutdated(Player player) {
      String latestVersion = this.plugin.getLatestVersion();
      String installedVersion = this.plugin.getDescription().getVersion();
      String prefix = this.plugin.getConfigManager().getConfigSection("message.prefix", player).getString("message.prefix");

      try {
         int comparisonResult = this.compareVersions(installedVersion, latestVersion);
         if (comparisonResult < 0) {
            List<String> stringList = this.plugin
               .getConfigManager()
               .getConfigSection("message.grave-plugin-version-outdated", player)
               .getStringList("message.grave-plugin-version-outdated");
            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               for (String message : stringList) {
                  String toConvert = StringUtil.parseString(prefix + message, player, this.plugin);
                  String newString = MiniMessage.parseString(toConvert);
                  player.sendMessage(newString);
               }
            } else {
               for (String message : stringList) {
                  player.sendMessage(StringUtil.parseString(prefix + message, player, this.plugin));
               }
            }
         } else if (comparisonResult > 0) {
            List<String> stringList = this.plugin
               .getConfigManager()
               .getConfigSection("message.grave-plugin-version-development", player)
               .getStringList("message.grave-plugin-version-development");
            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               for (String message : stringList) {
                  String toConvert = StringUtil.parseString(prefix + message.replace("%public-version", this.plugin.getLatestVersion()), player, this.plugin);
                  String newString = MiniMessage.parseString(toConvert);
                  player.sendMessage(newString);
               }
            } else {
               for (String message : stringList) {
                  player.sendMessage(StringUtil.parseString(prefix + message.replace("%public-version", this.plugin.getLatestVersion()), player, this.plugin));
               }
            }
         } else {
            String string = this.plugin
               .getConfigManager()
               .getConfigSection("message.grave-plugin-version-latest", player)
               .getString("message.grave-plugin-version-latest");
            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               String toConvert = StringUtil.parseString(prefix + string, player, this.plugin);
               String newString = MiniMessage.parseString(toConvert);
               player.sendMessage(newString);
            } else {
               player.sendMessage(StringUtil.parseString(prefix + string, player, this.plugin));
            }
         }
      } catch (NumberFormatException var11) {
         List<String> stringList = this.plugin
            .getConfigManager()
            .getConfigSection("message.grave-plugin-version-outdated", player)
            .getStringList("message.grave-plugin-version-outdated");
         if (this.plugin.getIntegrationManager().hasMiniMessage()) {
            for (String message : stringList) {
               String toConvert = StringUtil.parseString(prefix + message, player, this.plugin);
               String newString = MiniMessage.parseString(toConvert);
               player.sendMessage(newString);
            }
         } else {
            for (String message : stringList) {
               player.sendMessage(StringUtil.parseString(prefix + message, player, this.plugin));
            }
         }
      }
   }

   private int compareVersions(String version1, String version2) {
      String[] levels1 = version1.split("\\.");
      String[] levels2 = version2.split("\\.");
      int length = Math.max(levels1.length, levels2.length);

      for (int i = 0; i < length; i++) {
         int v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
         int v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
         if (v1 < v2) {
            return -1;
         }

         if (v1 > v2) {
            return 1;
         }
      }

      return 0;
   }

   private void sendOutdatedVersionMessage(Player player, double currentVersion, double latestVersion) {
      player.sendMessage(
         ChatColor.RED
            + "☠"
            + ChatColor.DARK_GRAY
            + " » "
            + ChatColor.RESET
            + "Outdated version detected "
            + currentVersion
            + ", latest version is "
            + latestVersion
            + ", https://www.spigotmc.org/resources/"
            + this.plugin.getSpigotID()
            + "/"
      );
   }

   private void sendDevelopmentVersionMessage(Player player, double currentVersion) {
      player.sendMessage(
         ChatColor.RED
            + "☠"
            + ChatColor.DARK_GRAY
            + " » "
            + ChatColor.RESET
            + "Development version detected "
            + currentVersion
            + ", Report any bugs to https://discord.ranull.com/"
      );
   }

   private void sendOutdatedVersionMessage(Player player, String currentVersion, String latestVersion) {
      player.sendMessage(
         ChatColor.RED
            + "☠"
            + ChatColor.DARK_GRAY
            + " » "
            + ChatColor.RESET
            + "Outdated version detected "
            + currentVersion
            + ", latest version is "
            + latestVersion
            + ", https://www.spigotmc.org/resources/"
            + this.plugin.getSpigotID()
            + "/"
      );
   }

   private boolean isDifferentVersion(String currentVersion, String latestVersion) {
      return !currentVersion.equalsIgnoreCase(latestVersion) && !latestVersion.equalsIgnoreCase("4.9");
   }
}
