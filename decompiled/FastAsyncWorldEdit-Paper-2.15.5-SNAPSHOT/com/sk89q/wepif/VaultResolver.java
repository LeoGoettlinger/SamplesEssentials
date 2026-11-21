package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultResolver implements PermissionsResolver {
   public static Permission perms = null;
   private final Server server;

   public static PermissionsResolver factory(Server server, YAMLProcessor config) {
      if (server.getPluginManager().getPlugin("Vault") == null) {
         return null;
      } else {
         RegisteredServiceProvider<Permission> rsp = server.getServicesManager().getRegistration(Permission.class);
         if (rsp == null) {
            return null;
         } else {
            perms = (Permission)rsp.getProvider();
            return perms == null ? null : new VaultResolver(server);
         }
      }
   }

   public VaultResolver(Server server) {
      this.server = server;
   }

   @Override
   public void load() {
   }

   @Override
   public String getDetectionMessage() {
      return "Vault detected! Using Vault for permissions";
   }

   @Override
   public boolean hasPermission(String name, String permission) {
      return this.hasPermission(this.server.getOfflinePlayer(name), permission);
   }

   @Override
   public boolean hasPermission(String worldName, String name, String permission) {
      return this.hasPermission(worldName, this.server.getOfflinePlayer(name), permission);
   }

   @Override
   public boolean inGroup(String player, String group) {
      return this.inGroup(this.server.getOfflinePlayer(player), group);
   }

   @Override
   public String[] getGroups(String player) {
      return this.getGroups(this.server.getOfflinePlayer(player));
   }

   @Override
   public boolean hasPermission(OfflinePlayer player, String permission) {
      Player onlinePlayer = player.getPlayer();
      return onlinePlayer == null ? perms.playerHas(null, player, permission) : perms.playerHas(onlinePlayer.getWorld().getName(), player, permission);
   }

   @Override
   public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
      return perms.playerHas(worldName, player, permission);
   }

   @Override
   public boolean inGroup(OfflinePlayer player, String group) {
      Player onlinePlayer = player.getPlayer();
      return onlinePlayer == null ? perms.playerInGroup(null, player, group) : perms.playerInGroup(onlinePlayer, group);
   }

   @Override
   public String[] getGroups(OfflinePlayer player) {
      Player onlinePlayer = player.getPlayer();
      return onlinePlayer == null ? perms.getPlayerGroups(null, player) : perms.getPlayerGroups(onlinePlayer);
   }
}
