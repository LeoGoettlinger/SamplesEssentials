package org.sethomegui.Managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;

public class HomeManager {
   private final SetHomeGUI plugin;
   public static final int UNLIMITED = -1;
   public static final String PERMISSION_UNLIMITED = "sethome.maxhomes.unlimited";
   private static final String PERMISSION_PREFIX = "sethome.maxhomes.";

   public HomeManager(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   public YamlDocument getPlayerFile(UUID uuid) {
      return this.plugin.getStorageManager().get(uuid);
   }

   public void deletePlayerData(UUID uuid) {
      this.plugin.getStorageManager().delete(uuid);
   }

   public void saveHome(UUID uuid, String homeName, Location loc) {
      YamlDocument config = this.getPlayerFile(uuid);
      if (config != null) {
         List<String> homeList = config.getStringList("homes");
         if (homeList == null) {
            homeList = new ArrayList<>();
         }

         if (!homeList.contains(homeName)) {
            homeList.add(homeName);
         }

         config.set("homes", homeList);
         config.set(homeName + ".world", loc.getWorld().getName());
         config.set(homeName + ".x", loc.getX());
         config.set(homeName + ".y", loc.getY());
         config.set(homeName + ".z", loc.getZ());
         config.set(homeName + ".yaw", (double)loc.getYaw());
         config.set(homeName + ".pitch", (double)loc.getPitch());

         try {
            config.save();
         } catch (IOException var8) {
            String saveErrorMsg = this.plugin.getMainConfig().getString("messages.system-errors.save-error", "Could not save home data for UUID %uuid%");
            this.plugin.getLogger().severe(saveErrorMsg.replace("%uuid%", uuid.toString()));
            var8.printStackTrace();
         }
      }
   }

   public int getPlayerMaxHomes(Player player) {
      if (player.hasPermission("sethome.maxhomes.unlimited")) {
         return -1;
      } else {
         int maxFromPermission = Integer.MIN_VALUE;

         for (PermissionAttachmentInfo attachment : player.getEffectivePermissions()) {
            if (attachment.getValue()) {
               String permission = attachment.getPermission().toLowerCase(Locale.ROOT);
               if (permission.startsWith("sethome.maxhomes.")) {
                  String suffix = permission.substring("sethome.maxhomes.".length());

                  try {
                     int val = Integer.parseInt(suffix);
                     if (val < 0) {
                        return -1;
                     }

                     if (val > maxFromPermission) {
                        maxFromPermission = val;
                     }
                  } catch (NumberFormatException var8) {
                  }
               }
            }
         }

         return maxFromPermission != Integer.MIN_VALUE ? maxFromPermission : this.plugin.getMainConfig().getInt("default-max-homes", 3);
      }
   }

   public int getHomeCount(UUID uuid) {
      YamlDocument config = this.getPlayerFile(uuid);
      if (config == null) {
         return 0;
      } else {
         List<String> homeList = config.getStringList("homes");
         return homeList != null ? homeList.size() : 0;
      }
   }
}
