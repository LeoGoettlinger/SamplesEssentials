package com.earth2me.essentials.perm.impl;

import java.util.Arrays;
import java.util.List;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractVaultHandler extends SuperpermsHandler {
   protected static Permission perms = null;
   protected static Chat chat = null;

   private boolean setupProviders() {
      try {
         Class.forName("net.milkbowl.vault.permission.Permission");
         Class.forName("net.milkbowl.vault.chat.Chat");
      } catch (ClassNotFoundException var3) {
         return false;
      }

      RegisteredServiceProvider<Permission> permsProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
      RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
      if (permsProvider != null && chatProvider != null) {
         perms = (Permission)permsProvider.getProvider();
         chat = (Chat)chatProvider.getProvider();
         return perms != null && chat != null;
      } else {
         return false;
      }
   }

   @Override
   public String getGroup(final OfflinePlayer base) {
      return base.isOnline() ? perms.getPrimaryGroup(base.getPlayer()) : perms.getPrimaryGroup(null, base);
   }

   @Override
   public List<String> getGroups(final OfflinePlayer base) {
      return base.isOnline() ? Arrays.asList(perms.getPlayerGroups(base.getPlayer())) : Arrays.asList(perms.getPlayerGroups(null, base));
   }

   @Override
   public List<String> getGroups() {
      return Arrays.asList(perms.getGroups());
   }

   @Override
   public boolean addToGroup(OfflinePlayer base, String group) {
      return perms.playerAddGroup(null, base, group);
   }

   @Override
   public boolean removeFromGroup(OfflinePlayer base, String group) {
      return perms.playerRemoveGroup(null, base, group);
   }

   @Override
   public boolean inGroup(final Player base, final String group) {
      return perms.playerInGroup(base, group);
   }

   @Override
   public String getPrefix(final Player base) {
      String playerPrefix = chat.getPlayerPrefix(base);
      if (playerPrefix != null) {
         return playerPrefix;
      } else {
         String playerGroup = perms.getPrimaryGroup(base);
         return playerGroup != null ? chat.getGroupPrefix(base.getWorld().getName(), playerGroup) : null;
      }
   }

   @Override
   public String getSuffix(final Player base) {
      String playerSuffix = chat.getPlayerSuffix(base);
      if (playerSuffix != null) {
         return playerSuffix;
      } else {
         String playerGroup = perms.getPrimaryGroup(base);
         return playerGroup != null ? chat.getGroupSuffix(base.getWorld().getName(), playerGroup) : null;
      }
   }

   @Override
   public String getBackendName() {
      String reportedPlugin = perms.getName();
      String classPlugin = JavaPlugin.getProvidingPlugin(perms.getClass()).getName();
      return reportedPlugin.equals(classPlugin) ? reportedPlugin : reportedPlugin + " (" + classPlugin + ")";
   }

   boolean canLoad() {
      if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         try {
            return this.setupProviders();
         } catch (Throwable var2) {
            return false;
         }
      }
   }
}
