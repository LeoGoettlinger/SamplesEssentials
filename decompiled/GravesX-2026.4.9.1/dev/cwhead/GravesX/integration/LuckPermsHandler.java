package dev.cwhead.GravesX.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsHandler {
   private final LuckPerms luckPerms;

   public LuckPermsHandler() {
      RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
      if (provider != null) {
         this.luckPerms = (LuckPerms)provider.getProvider();
      } else {
         throw new IllegalStateException("LuckPerms is not available on this server.");
      }
   }

   public boolean hasPermission(OfflinePlayer offlinePlayer, String permissionNode) {
      User user = this.luckPerms.getUserManager().getUser(offlinePlayer.getUniqueId());
      return user != null && user.getCachedData().getPermissionData().checkPermission(permissionNode).asBoolean();
   }

   public boolean hasPermission(Player player, String permissionNode) {
      User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());
      return user != null && user.getCachedData().getPermissionData().checkPermission(permissionNode).asBoolean();
   }

   public boolean grantPermission(OfflinePlayer player, String permissionNode) {
      User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());
      if (user != null) {
         Node node = Node.builder(permissionNode).build();
         user.data().add(node);
         this.luckPerms.getUserManager().saveUser(user);
         return true;
      } else {
         return false;
      }
   }

   public boolean revokePermission(OfflinePlayer player, String permissionNode) {
      User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());
      if (user != null) {
         Node node = Node.builder(permissionNode).build();
         user.data().remove(node);
         this.luckPerms.getUserManager().saveUser(user);
         return true;
      } else {
         return false;
      }
   }

   public boolean isInGroup(OfflinePlayer player, String groupName) {
      User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());
      return user != null && user.getPrimaryGroup().equalsIgnoreCase(groupName);
   }
}
