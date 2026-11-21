package me.byteful.plugin.leveltools.libs.lamp.bukkit.core;

import me.byteful.plugin.leveltools.libs.lamp.bukkit.BukkitCommandPermission;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandPermission;
import me.byteful.plugin.leveltools.libs.lamp.command.trait.CommandAnnotationHolder;
import me.byteful.plugin.leveltools.libs.lamp.process.PermissionReader;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum BukkitPermissionReader implements PermissionReader {
   INSTANCE;

   @Nullable
   @Override
   public CommandPermission getPermission(@NotNull CommandAnnotationHolder command) {
      me.byteful.plugin.leveltools.libs.lamp.bukkit.annotation.CommandPermission permissionAnn = command.getAnnotation(
         me.byteful.plugin.leveltools.libs.lamp.bukkit.annotation.CommandPermission.class
      );
      return permissionAnn == null ? null : new BukkitCommandPermission(new Permission(permissionAnn.value(), permissionAnn.defaultAccess()));
   }
}
