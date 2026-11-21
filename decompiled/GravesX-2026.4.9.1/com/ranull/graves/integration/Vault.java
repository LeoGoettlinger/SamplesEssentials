package com.ranull.graves.integration;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Vault {
   private final Permission permission;

   @Deprecated
   public Vault(Permission permission) {
      this.permission = permission;
   }

   public boolean hasPermission(OfflinePlayer player, String permissionNode) {
      if (player != null && this.permission != null && permissionNode != null) {
         try {
            String world = null;
            Player online = player.getPlayer();
            if (online != null) {
               online.getWorld();
               world = online.getWorld().getName();
            }

            return this.permission.playerHas(world, player, permissionNode);
         } catch (NoSuchMethodError var5) {
            Player online = player.getPlayer();
            return online != null ? this.permission.has(online, permissionNode) : false;
         }
      } else {
         return false;
      }
   }
}
