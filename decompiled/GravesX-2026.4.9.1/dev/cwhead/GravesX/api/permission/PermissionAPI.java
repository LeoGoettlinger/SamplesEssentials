package dev.cwhead.GravesX.api.permission;

import com.ranull.graves.Graves;
import java.util.Collection;
import java.util.Objects;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PermissionAPI {
   private final Graves plugin;

   public PermissionAPI(@NotNull Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      plugin.debugMessage("[PermissionAPI] PermissionAPI initialized.", 4);
   }

   public boolean hasGrantedPermission(@NotNull String permission, @NotNull Player player) {
      this.plugin.debugMessage("[PermissionAPI] hasGrantedPermission(String, Player) called. Player=" + player.getName() + " | Permission=" + permission, 4);
      boolean result = this.plugin.getPermissionManager().hasGrantedPermission(permission, player);
      this.plugin
         .debugMessage(
            "[PermissionAPI] hasGrantedPermission(String, Player) result. Player=" + player.getName() + " | Permission=" + permission + " | Result=" + result,
            4
         );
      return result;
   }

   public boolean hasGrantedPermission(@NotNull String permission, @NotNull OfflinePlayer offlinePlayer) {
      this.plugin
         .debugMessage(
            "[PermissionAPI] hasGrantedPermission(String, OfflinePlayer) called. OfflinePlayer=" + this.nameOf(offlinePlayer) + " | Permission=" + permission,
            4
         );
      boolean result = this.plugin.getPermissionManager().hasGrantedPermission(permission, offlinePlayer);
      this.plugin
         .debugMessage(
            "[PermissionAPI] hasGrantedPermission(String, OfflinePlayer) result. OfflinePlayer="
               + this.nameOf(offlinePlayer)
               + " | Permission="
               + permission
               + " | Result="
               + result,
            4
         );
      return result;
   }

   public boolean hasGrantedPermissionOrOp(@NotNull String permission, @NotNull Player player) {
      this.plugin
         .debugMessage(
            "[PermissionAPI] hasGrantedPermissionOrOp(String, Player) called. Player="
               + player.getName()
               + " | Permission="
               + permission
               + " | isOp="
               + player.isOp(),
            4
         );
      boolean result = this.plugin.getPermissionManager().hasGrantedPermissionOrOp(permission, player);
      this.plugin
         .debugMessage(
            "[PermissionAPI] hasGrantedPermissionOrOp(String, Player) result. Player="
               + player.getName()
               + " | Permission="
               + permission
               + " | Result="
               + result,
            4
         );
      return result;
   }

   public boolean hasAnyGrantedPermission(@NotNull Player player, @NotNull String... permissions) {
      this.plugin
         .debugMessage(
            "[PermissionAPI] hasAnyGrantedPermission(Player, String...) called. Player="
               + player.getName()
               + " | PermissionsCount="
               + (permissions == null ? "null" : permissions.length),
            4
         );
      boolean result = this.plugin.getPermissionManager().hasAnyGrantedPermission(player, permissions);
      this.plugin.debugMessage("[PermissionAPI] hasAnyGrantedPermission(Player, String...) result. Player=" + player.getName() + " | Result=" + result, 4);
      return result;
   }

   public boolean hasAllGrantedPermissions(@NotNull Player player, @NotNull String... permissions) {
      this.plugin
         .debugMessage(
            "[PermissionAPI] hasAllGrantedPermissions(Player, String...) called. Player="
               + player.getName()
               + " | PermissionsCount="
               + (permissions == null ? "null" : permissions.length),
            4
         );
      boolean result = this.plugin.getPermissionManager().hasAllGrantedPermissions(player, permissions);
      this.plugin.debugMessage("[PermissionAPI] hasAllGrantedPermissions(Player, String...) result. Player=" + player.getName() + " | Result=" + result, 4);
      return result;
   }

   public boolean hasAnyGrantedPermission(@NotNull Player player, @NotNull Collection<String> permissions) {
      this.plugin
         .debugMessage(
            "[PermissionAPI] hasAnyGrantedPermission(Player, Collection) called. Player="
               + player.getName()
               + " | PermissionsCount="
               + (permissions.isEmpty() ? "null" : permissions.size()),
            4
         );
      boolean result = this.plugin.getPermissionManager().hasAnyGrantedPermission(player, permissions);
      this.plugin.debugMessage("[PermissionAPI] hasAnyGrantedPermission(Player, Collection) result. Player=" + player.getName() + " | Result=" + result, 4);
      return result;
   }

   public boolean hasAllGrantedPermissions(@NotNull Player player, @NotNull Collection<String> permissions) {
      this.plugin
         .debugMessage(
            "[PermissionAPI] hasAllGrantedPermissions(Player, Collection) called. Player="
               + player.getName()
               + " | PermissionsCount="
               + (permissions.isEmpty() ? "null" : permissions.size()),
            4
         );
      boolean result = this.plugin.getPermissionManager().hasAllGrantedPermissions(player, permissions);
      this.plugin.debugMessage("[PermissionAPI] hasAllGrantedPermissions(Player, Collection) result. Player=" + player.getName() + " | Result=" + result, 4);
      return result;
   }

   @NotNull
   public String getActivePermissionProviderName() {
      this.plugin.debugMessage("[PermissionAPI] getActivePermissionProviderName() called.", 4);
      String provider = this.plugin.getPermissionManager().getActivePermissionProviderName();
      this.plugin.debugMessage("[PermissionAPI] getActivePermissionProviderName() result -> " + provider, 4);
      return provider;
   }

   public boolean isUsingLuckPerms() {
      this.plugin.debugMessage("[PermissionAPI] isUsingLuckPerms() called.", 4);
      boolean value = this.plugin.getPermissionManager().isUsingLuckPerms();
      this.plugin.debugMessage("[PermissionAPI] isUsingLuckPerms() result -> " + value, 4);
      return value;
   }

   public boolean isUsingVaultPermissions() {
      this.plugin.debugMessage("[PermissionAPI] isUsingVaultPermissions() called.", 4);
      boolean value = this.plugin.getPermissionManager().isUsingVaultPermissions();
      this.plugin.debugMessage("[PermissionAPI] isUsingVaultPermissions() result -> " + value, 4);
      return value;
   }

   @NotNull
   private String nameOf(@NotNull OfflinePlayer offlinePlayer) {
      String name = offlinePlayer.getName();
      return name == null ? "unknown" : name;
   }
}
