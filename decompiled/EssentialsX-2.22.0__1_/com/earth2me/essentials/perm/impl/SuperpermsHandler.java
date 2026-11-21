package com.earth2me.essentials.perm.impl;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.perm.IPermissionsHandler;
import com.earth2me.essentials.utils.TriState;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class SuperpermsHandler implements IPermissionsHandler {
   protected boolean emulateWildcards() {
      return true;
   }

   @Override
   public boolean addToGroup(OfflinePlayer base, String group) {
      return false;
   }

   @Override
   public boolean removeFromGroup(OfflinePlayer base, String group) {
      return false;
   }

   @Override
   public String getGroup(final OfflinePlayer base) {
      return null;
   }

   @Override
   public List<String> getGroups(final OfflinePlayer base) {
      return null;
   }

   @Override
   public List<String> getGroups() {
      return null;
   }

   @Override
   public boolean canBuild(final Player base, final String group) {
      return this.hasPermission(base, "essentials.build");
   }

   @Override
   public boolean inGroup(final Player base, final String group) {
      return this.hasPermission(base, "group." + group);
   }

   private boolean isDeniedToOps(final String node) {
      Permission perm = Bukkit.getServer().getPluginManager().getPermission(node);
      return perm != null && !perm.getDefault().getValue(true);
   }

   @Override
   public boolean hasPermission(final Player base, String node) {
      if (!this.emulateWildcards()) {
         return base.hasPermission(node);
      } else {
         String permCheck;
         for (permCheck = node; !base.isPermissionSet(permCheck) && !this.isDeniedToOps(node); permCheck = node + ".*") {
            int index = node.lastIndexOf(46);
            if (index < 1) {
               return base.hasPermission("*");
            }

            node = node.substring(0, index);
         }

         return base.hasPermission(permCheck);
      }
   }

   @Override
   public boolean isPermissionSet(final Player base, final String node) {
      return base.isPermissionSet(node);
   }

   @Override
   public boolean isOfflinePermissionSet(UUID uuid, String node) {
      return false;
   }

   @Override
   public TriState isPermissionSetExact(Player base, String node) {
      for (PermissionAttachmentInfo perm : base.getEffectivePermissions()) {
         if (perm.getPermission().equalsIgnoreCase(node)) {
            return perm.getValue() ? TriState.TRUE : TriState.FALSE;
         }
      }

      return TriState.UNSET;
   }

   @Override
   public String getPrefix(final Player base) {
      return null;
   }

   @Override
   public String getSuffix(final Player base) {
      return null;
   }

   @Override
   public void registerContext(final String context, final Function<User, Iterable<String>> calculator, final Supplier<Iterable<String>> suggestions) {
   }

   @Override
   public void unregisterContexts() {
   }

   @Override
   public String getBackendName() {
      return this.getEnabledPermsPlugin();
   }

   @Override
   public boolean tryProvider(Essentials ess) {
      return this.getEnabledPermsPlugin() != null;
   }

   public String getEnabledPermsPlugin() {
      String enabledPermsPlugin = null;
      List<String> specialCasePlugins = Arrays.asList(
         "PermissionsEx", "GroupManager", "SimplyPerms", "Privileges", "bPermissions", "zPermissions", "PermissionsBukkit", "DroxPerms", "xPerms", "LuckPerms"
      );

      for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
         if (specialCasePlugins.contains(plugin.getName())) {
            enabledPermsPlugin = plugin.getName();
            break;
         }
      }

      return enabledPermsPlugin;
   }
}
