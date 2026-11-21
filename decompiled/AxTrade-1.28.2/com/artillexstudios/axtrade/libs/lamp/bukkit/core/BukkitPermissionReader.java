package com.artillexstudios.axtrade.libs.lamp.bukkit.core;

import com.artillexstudios.axtrade.libs.lamp.bukkit.BukkitCommandPermission;
import com.artillexstudios.axtrade.libs.lamp.command.CommandPermission;
import com.artillexstudios.axtrade.libs.lamp.command.trait.CommandAnnotationHolder;
import com.artillexstudios.axtrade.libs.lamp.process.PermissionReader;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum BukkitPermissionReader implements PermissionReader {
   INSTANCE;

   @Nullable
   @Override
   public CommandPermission getPermission(@NotNull CommandAnnotationHolder command) {
      com.artillexstudios.axtrade.libs.lamp.bukkit.annotation.CommandPermission permissionAnn = command.getAnnotation(
         com.artillexstudios.axtrade.libs.lamp.bukkit.annotation.CommandPermission.class
      );
      return permissionAnn == null ? null : new BukkitCommandPermission(new Permission(permissionAnn.value(), permissionAnn.defaultAccess()));
   }
}
