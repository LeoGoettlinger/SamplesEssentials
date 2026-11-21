package com.sk89q.wepif;

import org.bukkit.OfflinePlayer;

public interface PermissionsProvider {
   boolean hasPermission(String name, String permission);

   boolean hasPermission(String worldName, String name, String permission);

   boolean inGroup(String player, String group);

   String[] getGroups(String player);

   boolean hasPermission(OfflinePlayer player, String permission);

   boolean hasPermission(String worldName, OfflinePlayer player, String permission);

   boolean inGroup(OfflinePlayer player, String group);

   String[] getGroups(OfflinePlayer player);
}
