package net.milkbowl.vault.permission;

import java.util.logging.Logger;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/** @deprecated */
public abstract class Permission {
   protected static final Logger log = Logger.getLogger("Minecraft");
   protected Plugin plugin = null;

   public abstract String getName();

   public abstract boolean isEnabled();

   public abstract boolean hasSuperPermsCompat();

   @Deprecated
   public boolean has(final String world, final String player, final String permission) {
      return world == null ? this.playerHas((String)null, player, permission) : this.playerHas(world, player, permission);
   }

   @Deprecated
   public boolean has(final World world, final String player, final String permission) {
      return world == null ? this.playerHas((String)null, player, permission) : this.playerHas(world.getName(), player, permission);
   }

   public boolean has(final CommandSender sender, final String permission) {
      return sender.hasPermission(permission);
   }

   public boolean has(final Player player, final String permission) {
      return player.hasPermission(permission);
   }

   @Deprecated
   public abstract boolean playerHas(String world, String player, String permission);

   @Deprecated
   public boolean playerHas(final World world, final String player, final String permission) {
      return world == null ? this.playerHas((String)null, player, permission) : this.playerHas(world.getName(), player, permission);
   }

   public boolean playerHas(final String world, final OfflinePlayer player, final String permission) {
      return world == null ? this.has((String)null, player.getName(), permission) : this.has(world, player.getName(), permission);
   }

   public boolean playerHas(final Player player, final String permission) {
      return this.has(player, permission);
   }

   @Deprecated
   public abstract boolean playerAdd(String world, String player, String permission);

   @Deprecated
   public boolean playerAdd(final World world, final String player, final String permission) {
      return world == null ? this.playerAdd((String)null, player, permission) : this.playerAdd(world.getName(), player, permission);
   }

   public boolean playerAdd(final String world, final OfflinePlayer player, final String permission) {
      return world == null ? this.playerAdd((String)null, player.getName(), permission) : this.playerAdd(world, player.getName(), permission);
   }

   public boolean playerAdd(final Player player, final String permission) {
      return this.playerAdd(player.getWorld().getName(), player, permission);
   }

   public boolean playerAddTransient(final OfflinePlayer player, final String permission) throws UnsupportedOperationException {
      if (player.isOnline()) {
         return this.playerAddTransient((Player)player, permission);
      } else {
         throw new UnsupportedOperationException(this.getName() + " does not support offline player transient permissions!");
      }
   }

   public boolean playerAddTransient(final Player player, final String permission) {
      for (PermissionAttachmentInfo paInfo : player.getEffectivePermissions()) {
         if (paInfo.getAttachment() != null && paInfo.getAttachment().getPlugin().equals(this.plugin)) {
            paInfo.getAttachment().setPermission(permission, true);
            return true;
         }
      }

      PermissionAttachment attach = player.addAttachment(this.plugin);
      attach.setPermission(permission, true);
      return true;
   }

   public boolean playerAddTransient(final String worldName, final OfflinePlayer player, final String permission) {
      return this.playerAddTransient(player, permission);
   }

   public boolean playerAddTransient(final String worldName, final Player player, final String permission) {
      return this.playerAddTransient(player, permission);
   }

   public boolean playerRemoveTransient(final String worldName, final OfflinePlayer player, final String permission) {
      return this.playerRemoveTransient(player, permission);
   }

   public boolean playerRemoveTransient(final String worldName, final Player player, final String permission) {
      return this.playerRemoveTransient((OfflinePlayer)player, permission);
   }

   @Deprecated
   public abstract boolean playerRemove(String world, String player, String permission);

   public boolean playerRemove(final String world, final OfflinePlayer player, final String permission) {
      return world == null ? this.playerRemove((String)null, player.getName(), permission) : this.playerRemove(world, player.getName(), permission);
   }

   @Deprecated
   public boolean playerRemove(final World world, final String player, final String permission) {
      return world == null ? this.playerRemove((String)null, player, permission) : this.playerRemove(world.getName(), player, permission);
   }

   public boolean playerRemove(final Player player, final String permission) {
      return this.playerRemove(player.getWorld().getName(), player, permission);
   }

   public boolean playerRemoveTransient(final OfflinePlayer player, final String permission) {
      return player.isOnline() ? this.playerRemoveTransient((Player)player, permission) : false;
   }

   public boolean playerRemoveTransient(final Player player, final String permission) {
      for (PermissionAttachmentInfo paInfo : player.getEffectivePermissions()) {
         if (paInfo.getAttachment() != null && paInfo.getAttachment().getPlugin().equals(this.plugin)) {
            paInfo.getAttachment().unsetPermission(permission);
            return true;
         }
      }

      return false;
   }

   public abstract boolean groupHas(String world, String group, String permission);

   public boolean groupHas(final World world, final String group, final String permission) {
      return world == null ? this.groupHas((String)null, group, permission) : this.groupHas(world.getName(), group, permission);
   }

   public abstract boolean groupAdd(String world, String group, String permission);

   public boolean groupAdd(final World world, final String group, final String permission) {
      return world == null ? this.groupAdd((String)null, group, permission) : this.groupAdd(world.getName(), group, permission);
   }

   public abstract boolean groupRemove(String world, String group, String permission);

   public boolean groupRemove(final World world, final String group, final String permission) {
      return world == null ? this.groupRemove((String)null, group, permission) : this.groupRemove(world.getName(), group, permission);
   }

   @Deprecated
   public abstract boolean playerInGroup(String world, String player, String group);

   @Deprecated
   public boolean playerInGroup(final World world, final String player, final String group) {
      return world == null ? this.playerInGroup((String)null, player, group) : this.playerInGroup(world.getName(), player, group);
   }

   public boolean playerInGroup(final String world, final OfflinePlayer player, final String group) {
      return world == null ? this.playerInGroup((String)null, player.getName(), group) : this.playerInGroup(world, player.getName(), group);
   }

   public boolean playerInGroup(final Player player, final String group) {
      return this.playerInGroup(player.getWorld().getName(), player, group);
   }

   @Deprecated
   public abstract boolean playerAddGroup(String world, String player, String group);

   @Deprecated
   public boolean playerAddGroup(final World world, final String player, final String group) {
      return world == null ? this.playerAddGroup((String)null, player, group) : this.playerAddGroup(world.getName(), player, group);
   }

   public boolean playerAddGroup(final String world, final OfflinePlayer player, final String group) {
      return world == null ? this.playerAddGroup((String)null, player.getName(), group) : this.playerAddGroup(world, player.getName(), group);
   }

   public boolean playerAddGroup(final Player player, final String group) {
      return this.playerAddGroup(player.getWorld().getName(), player, group);
   }

   @Deprecated
   public abstract boolean playerRemoveGroup(String world, String player, String group);

   @Deprecated
   public boolean playerRemoveGroup(final World world, final String player, final String group) {
      return world == null ? this.playerRemoveGroup((String)null, player, group) : this.playerRemoveGroup(world.getName(), player, group);
   }

   public boolean playerRemoveGroup(final String world, final OfflinePlayer player, final String group) {
      return world == null ? this.playerRemoveGroup((String)null, player.getName(), group) : this.playerRemoveGroup(world, player.getName(), group);
   }

   public boolean playerRemoveGroup(final Player player, final String group) {
      return this.playerRemoveGroup(player.getWorld().getName(), player, group);
   }

   @Deprecated
   public abstract String[] getPlayerGroups(String world, String player);

   @Deprecated
   public String[] getPlayerGroups(final World world, final String player) {
      return world == null ? this.getPlayerGroups((String)null, player) : this.getPlayerGroups(world.getName(), player);
   }

   public String[] getPlayerGroups(final String world, final OfflinePlayer player) {
      return this.getPlayerGroups(world, player.getName());
   }

   public String[] getPlayerGroups(final Player player) {
      return this.getPlayerGroups(player.getWorld().getName(), player);
   }

   @Deprecated
   public abstract String getPrimaryGroup(String world, String player);

   @Deprecated
   public String getPrimaryGroup(final World world, final String player) {
      return world == null ? this.getPrimaryGroup((String)null, player) : this.getPrimaryGroup(world.getName(), player);
   }

   public String getPrimaryGroup(final String world, final OfflinePlayer player) {
      return this.getPrimaryGroup(world, player.getName());
   }

   public String getPrimaryGroup(final Player player) {
      return this.getPrimaryGroup(player.getWorld().getName(), player);
   }

   public abstract String[] getGroups();

   public abstract boolean hasGroupSupport();
}
