package com.earth2me.essentials.perm;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public final class PermissionsDefaults {
   private PermissionsDefaults() {
   }

   public static void registerAllBackDefaults() {
      for (World world : Bukkit.getWorlds()) {
         registerBackDefaultFor(world);
      }
   }

   public static void registerBackDefaultFor(final World w) {
      String permName = "essentials.back.into." + w.getName();
      Permission p = Bukkit.getPluginManager().getPermission(permName);
      if (p == null) {
         p = new Permission(permName, "Allows access to /back when the destination location is within world " + w.getName(), PermissionDefault.TRUE);
         Bukkit.getPluginManager().addPermission(p);
      }
   }

   public static void registerAllHatDefaults() {
      PluginManager pluginManager = Bukkit.getPluginManager();
      Permission hatPerm = pluginManager.getPermission("essentials.hat.prevent-type.*");
      if (hatPerm == null) {
         Builder<String, Boolean> children = ImmutableMap.builder();

         for (Material mat : Material.values()) {
            String matPerm = "essentials.hat.prevent-type." + mat.name().toLowerCase();
            children.put(matPerm, true);
            pluginManager.addPermission(new Permission(matPerm, "Prevent using " + mat + " as a type of hat.", PermissionDefault.FALSE));
         }

         pluginManager.addPermission(new Permission("essentials.hat.prevent-type.*", "Prevent all types of hats", PermissionDefault.FALSE, children.build()));
      }
   }
}
