package dev.cwhead.GravesX.manager;

import com.ranull.graves.Graves;
import java.util.Collection;
import java.util.Objects;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PermissionManager {
   private final Graves plugin;

   public PermissionManager(Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
   }

   public boolean hasGrantedPermission(String permission, Player player) {
      this.plugin
         .debugMessage("[PermissionManager] hasGrantedPermission(String, Player) called. Player=" + this.nameOf(player) + " | Permission=" + permission, 4);
      if (player == null) {
         this.plugin.debugMessage("[PermissionManager] hasGrantedPermission(String, Player) failed: player is null. Returning false.", 4);
         return false;
      } else {
         boolean result = this.checkPermission(permission, player);
         this.plugin
            .debugMessage(
               "[PermissionManager] hasGrantedPermission(String, Player) result. Player="
                  + this.nameOf(player)
                  + " | Permission="
                  + permission
                  + " | Result="
                  + result,
               4
            );
         return result;
      }
   }

   @Deprecated
   public boolean hasGrantedPermission(String permission, OfflinePlayer offlinePlayer) {
      this.plugin
         .debugMessage(
            "[PermissionManager] hasGrantedPermission(String, OfflinePlayer) called. OfflinePlayer="
               + this.nameOf(offlinePlayer)
               + " | Permission="
               + permission,
            4
         );
      if (offlinePlayer == null) {
         this.plugin.debugMessage("[PermissionManager] hasGrantedPermission(String, OfflinePlayer) failed: offlinePlayer is null. Returning false.", 4);
         return false;
      } else {
         if (offlinePlayer.isOnline()) {
            Player online = offlinePlayer.getPlayer();
            this.plugin
               .debugMessage(
                  "[PermissionManager] OfflinePlayer is online. Attempting Player path. OfflinePlayer="
                     + this.nameOf(offlinePlayer)
                     + " | Player="
                     + this.nameOf(online),
                  4
               );
            if (online != null) {
               boolean result = this.checkPermission(permission, online);
               this.plugin
                  .debugMessage(
                     "[PermissionManager] hasGrantedPermission(String, OfflinePlayer) (online->player) result. OfflinePlayer="
                        + this.nameOf(offlinePlayer)
                        + " | Permission="
                        + permission
                        + " | Result="
                        + result,
                     4
                  );
               return result;
            }

            this.plugin
               .debugMessage(
                  "[PermissionManager] OfflinePlayer was marked online but getPlayer() returned null. Falling back to OfflinePlayer provider path.", 4
               );
         }

         boolean result = this.checkPermission(permission, offlinePlayer);
         this.plugin
            .debugMessage(
               "[PermissionManager] hasGrantedPermission(String, OfflinePlayer) result. OfflinePlayer="
                  + this.nameOf(offlinePlayer)
                  + " | Permission="
                  + permission
                  + " | Result="
                  + result,
               4
            );
         return result;
      }
   }

   public boolean hasGrantedPermissionOrOp(String permission, Player player) {
      this.plugin
         .debugMessage("[PermissionManager] hasGrantedPermissionOrOp(String, Player) called. Player=" + this.nameOf(player) + " | Permission=" + permission, 4);
      if (player == null) {
         this.plugin.debugMessage("[PermissionManager] hasGrantedPermissionOrOp(String, Player) failed: player is null. Returning false.", 4);
         return false;
      } else {
         boolean isOp = player.isOp();
         this.plugin.debugMessage("[PermissionManager] hasGrantedPermissionOrOp(String, Player) player.isOp()=" + isOp + " | Player=" + this.nameOf(player), 4);
         boolean result = isOp || this.hasGrantedPermission(permission, player);
         this.plugin
            .debugMessage(
               "[PermissionManager] hasGrantedPermissionOrOp(String, Player) result. Player="
                  + this.nameOf(player)
                  + " | Permission="
                  + permission
                  + " | Result="
                  + result,
               4
            );
         return result;
      }
   }

   public boolean hasAnyGrantedPermission(Player player, String... permissions) {
      this.plugin
         .debugMessage(
            "[PermissionManager] hasAnyGrantedPermission(Player, String...) called. Player="
               + this.nameOf(player)
               + " | PermissionsCount="
               + (permissions == null ? "null" : permissions.length),
            4
         );
      if (player == null) {
         this.plugin.debugMessage("[PermissionManager] hasAnyGrantedPermission(Player, String...) failed: player is null. Returning false.", 4);
         return false;
      } else if (permissions != null && permissions.length != 0) {
         for (String perm : permissions) {
            boolean granted = this.hasGrantedPermission(perm, player);
            this.plugin
               .debugMessage(
                  "[PermissionManager] hasAnyGrantedPermission(Player, String...) checked. Player="
                     + this.nameOf(player)
                     + " | Permission="
                     + perm
                     + " | Granted="
                     + granted,
                  4
               );
            if (granted) {
               this.plugin
                  .debugMessage("[PermissionManager] hasAnyGrantedPermission(Player, String...) result: true (matched). Player=" + this.nameOf(player), 4);
               return true;
            }
         }

         this.plugin
            .debugMessage("[PermissionManager] hasAnyGrantedPermission(Player, String...) result: false (no matches). Player=" + this.nameOf(player), 4);
         return false;
      } else {
         this.plugin.debugMessage("[PermissionManager] hasAnyGrantedPermission(Player, String...) failed: permissions is null/empty. Returning false.", 4);
         return false;
      }
   }

   public boolean hasAllGrantedPermissions(Player player, String... permissions) {
      this.plugin
         .debugMessage(
            "[PermissionManager] hasAllGrantedPermissions(Player, String...) called. Player="
               + this.nameOf(player)
               + " | PermissionsCount="
               + (permissions == null ? "null" : permissions.length),
            4
         );
      if (player == null) {
         this.plugin.debugMessage("[PermissionManager] hasAllGrantedPermissions(Player, String...) failed: player is null. Returning false.", 4);
         return false;
      } else if (permissions != null && permissions.length != 0) {
         for (String perm : permissions) {
            boolean granted = this.hasGrantedPermission(perm, player);
            this.plugin
               .debugMessage(
                  "[PermissionManager] hasAllGrantedPermissions(Player, String...) checked. Player="
                     + this.nameOf(player)
                     + " | Permission="
                     + perm
                     + " | Granted="
                     + granted,
                  4
               );
            if (!granted) {
               this.plugin
                  .debugMessage("[PermissionManager] hasAllGrantedPermissions(Player, String...) result: false (missing). Player=" + this.nameOf(player), 4);
               return false;
            }
         }

         this.plugin
            .debugMessage("[PermissionManager] hasAllGrantedPermissions(Player, String...) result: true (all granted). Player=" + this.nameOf(player), 4);
         return true;
      } else {
         this.plugin.debugMessage("[PermissionManager] hasAllGrantedPermissions(Player, String...) failed: permissions is null/empty. Returning false.", 4);
         return false;
      }
   }

   public boolean hasAnyGrantedPermission(Player player, Collection<String> permissions) {
      this.plugin
         .debugMessage(
            "[PermissionManager] hasAnyGrantedPermission(Player, Collection) called. Player="
               + this.nameOf(player)
               + " | PermissionsCount="
               + (permissions == null ? "null" : permissions.size()),
            4
         );
      if (player == null) {
         this.plugin.debugMessage("[PermissionManager] hasAnyGrantedPermission(Player, Collection) failed: player is null. Returning false.", 4);
         return false;
      } else if (permissions != null && !permissions.isEmpty()) {
         for (String perm : permissions) {
            boolean granted = this.hasGrantedPermission(perm, player);
            this.plugin
               .debugMessage(
                  "[PermissionManager] hasAnyGrantedPermission(Player, Collection) checked. Player="
                     + this.nameOf(player)
                     + " | Permission="
                     + perm
                     + " | Granted="
                     + granted,
                  4
               );
            if (granted) {
               this.plugin
                  .debugMessage("[PermissionManager] hasAnyGrantedPermission(Player, Collection) result: true (matched). Player=" + this.nameOf(player), 4);
               return true;
            }
         }

         this.plugin
            .debugMessage("[PermissionManager] hasAnyGrantedPermission(Player, Collection) result: false (no matches). Player=" + this.nameOf(player), 4);
         return false;
      } else {
         this.plugin.debugMessage("[PermissionManager] hasAnyGrantedPermission(Player, Collection) failed: permissions is null/empty. Returning false.", 4);
         return false;
      }
   }

   public boolean hasAllGrantedPermissions(Player player, Collection<String> permissions) {
      this.plugin
         .debugMessage(
            "[PermissionManager] hasAllGrantedPermissions(Player, Collection) called. Player="
               + this.nameOf(player)
               + " | PermissionsCount="
               + (permissions == null ? "null" : permissions.size()),
            4
         );
      if (player == null) {
         this.plugin.debugMessage("[PermissionManager] hasAllGrantedPermissions(Player, Collection) failed: player is null. Returning false.", 4);
         return false;
      } else if (permissions != null && !permissions.isEmpty()) {
         for (String perm : permissions) {
            boolean granted = this.hasGrantedPermission(perm, player);
            this.plugin
               .debugMessage(
                  "[PermissionManager] hasAllGrantedPermissions(Player, Collection) checked. Player="
                     + this.nameOf(player)
                     + " | Permission="
                     + perm
                     + " | Granted="
                     + granted,
                  4
               );
            if (!granted) {
               this.plugin
                  .debugMessage("[PermissionManager] hasAllGrantedPermissions(Player, Collection) result: false (missing). Player=" + this.nameOf(player), 4);
               return false;
            }
         }

         this.plugin
            .debugMessage("[PermissionManager] hasAllGrantedPermissions(Player, Collection) result: true (all granted). Player=" + this.nameOf(player), 4);
         return true;
      } else {
         this.plugin.debugMessage("[PermissionManager] hasAllGrantedPermissions(Player, Collection) failed: permissions is null/empty. Returning false.", 4);
         return false;
      }
   }

   public String getActivePermissionProviderName() {
      String provider;
      if (this.plugin.getIntegrationManager().hasLuckPermsHandler()) {
         provider = "LuckPerms";
      } else if (!this.plugin.getIntegrationManager().hasVault() && !this.plugin.getIntegrationManager().hasVaultPermProvider()) {
         provider = "Bukkit";
      } else {
         provider = "Vault";
      }

      this.plugin.debugMessage("[PermissionManager] getActivePermissionProviderName() -> " + provider, 4);
      return provider;
   }

   public boolean isUsingLuckPerms() {
      boolean value = this.plugin.getIntegrationManager().hasLuckPermsHandler();
      this.plugin.debugMessage("[PermissionManager] isUsingLuckPerms() -> " + value, 4);
      return value;
   }

   public boolean isUsingVaultPermissions() {
      boolean value = this.plugin.getIntegrationManager().hasVault() || this.plugin.getIntegrationManager().hasVaultPermProvider();
      this.plugin.debugMessage("[PermissionManager] isUsingVaultPermissions() -> " + value, 4);
      return value;
   }

   private boolean checkPermission(String permission, Player player) {
      this.plugin.debugMessage("[PermissionManager] checkPermission(String, Player) called. Player=" + this.nameOf(player) + " | Permission=" + permission, 4);
      String perm = this.normalizePermission(permission);
      if (perm == null) {
         this.plugin
            .debugMessage(
               "[PermissionManager] checkPermission(String, Player) failed: permission is null/blank. Player=" + this.nameOf(player) + " | Returning false.", 4
            );
         return false;
      } else {
         boolean hasPermission;
         String provider;
         if (this.isUsingLuckPerms()) {
            provider = "LuckPerms";
            hasPermission = this.plugin.getIntegrationManager().getLuckPermsHandler().hasPermission(player, perm);
            this.plugin.debugMessage("[LuckPerms] Player: " + player.getName() + " | Permission: " + perm + " | Has Permission: " + hasPermission, 4);
         } else if (this.isUsingVaultPermissions()) {
            provider = "Vault";
            hasPermission = this.plugin.getIntegrationManager().getVault().hasPermission(player, perm);
            this.plugin.debugMessage("[Vault] Player: " + player.getName() + " | Permission: " + perm + " | Has Permission: " + hasPermission, 4);
         } else {
            provider = "Bukkit";
            hasPermission = player.hasPermission(perm);
            this.plugin.debugMessage("[Bukkit] Player: " + player.getName() + " | Permission: " + perm + " | Has Permission: " + hasPermission, 4);
         }

         this.plugin
            .debugMessage(
               "[PermissionManager] checkPermission(String, Player) result. Player="
                  + this.nameOf(player)
                  + " | Permission="
                  + perm
                  + " | Provider="
                  + provider
                  + " | Result="
                  + hasPermission,
               4
            );
         return hasPermission;
      }
   }

   private boolean checkPermission(String permission, OfflinePlayer offlinePlayer) {
      this.plugin
         .debugMessage(
            "[PermissionManager] checkPermission(String, OfflinePlayer) called. OfflinePlayer=" + this.nameOf(offlinePlayer) + " | Permission=" + permission, 4
         );
      String perm = this.normalizePermission(permission);
      if (perm == null) {
         this.plugin
            .debugMessage(
               "[PermissionManager] checkPermission(String, OfflinePlayer) failed: permission is null/blank. OfflinePlayer="
                  + this.nameOf(offlinePlayer)
                  + " | Returning false.",
               4
            );
         return false;
      } else {
         boolean hasPermission;
         String provider;
         if (this.isUsingLuckPerms()) {
            provider = "LuckPerms";
            hasPermission = this.plugin.getIntegrationManager().getLuckPermsHandler().hasPermission(offlinePlayer, perm);
            this.plugin
               .debugMessage(
                  "[LuckPerms] Offline Player: " + this.safeName(offlinePlayer) + " | Permission: " + perm + " | Has Permission: " + hasPermission, 4
               );
         } else if (this.isUsingVaultPermissions()) {
            provider = "Vault";
            hasPermission = this.plugin.getIntegrationManager().getVault().hasPermission(offlinePlayer, perm);
            this.plugin
               .debugMessage("[Vault] Offline Player: " + this.safeName(offlinePlayer) + " | Permission: " + perm + " | Has Permission: " + hasPermission, 4);
         } else {
            provider = "Bukkit";
            hasPermission = false;
            this.plugin.debugMessage("[Bukkit] Failed to get offline player permission provider. Assuming player doesn't have permission.", 4);
            this.plugin
               .debugMessage("[Bukkit] Offline Player: " + this.safeName(offlinePlayer) + " | Permission: " + perm + " | Has Permission: " + hasPermission, 4);
         }

         this.plugin
            .debugMessage(
               "[PermissionManager] checkPermission(String, OfflinePlayer) result. OfflinePlayer="
                  + this.nameOf(offlinePlayer)
                  + " | Permission="
                  + perm
                  + " | Provider="
                  + provider
                  + " | Result="
                  + hasPermission,
               4
            );
         return hasPermission;
      }
   }

   private String normalizePermission(String permission) {
      if (permission == null) {
         this.plugin.debugMessage("[PermissionManager] normalizePermission(String) -> null (permission is null)", 4);
         return null;
      } else {
         String perm = permission.trim();
         if (perm.isEmpty()) {
            this.plugin.debugMessage("[PermissionManager] normalizePermission(String) -> null (permission is blank)", 4);
            return null;
         } else {
            this.plugin.debugMessage("[PermissionManager] normalizePermission(String) -> " + perm, 4);
            return perm;
         }
      }
   }

   private String nameOf(Player player) {
      return player == null ? "null" : player.getName() + " (" + player.getUniqueId() + ")";
   }

   private String nameOf(OfflinePlayer offlinePlayer) {
      return offlinePlayer == null ? "null" : this.safeName(offlinePlayer) + " (" + offlinePlayer.getUniqueId() + ")";
   }

   private String safeName(OfflinePlayer offlinePlayer) {
      if (offlinePlayer == null) {
         return "null";
      } else {
         String name = offlinePlayer.getName();
         return name == null ? "unknown" : name;
      }
   }
}
