package com.artillexstudios.axafkzone.libs.lamp.bukkit.core;

import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandPermission;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandPermission;
import com.artillexstudios.axafkzone.libs.lamp.command.trait.CommandAnnotationHolder;
import com.artillexstudios.axafkzone.libs.lamp.process.PermissionReader;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum BukkitPermissionReader implements PermissionReader {
   INSTANCE;

   @Nullable
   @Override
   public CommandPermission getPermission(@NotNull CommandAnnotationHolder command) {
      com.artillexstudios.axafkzone.libs.lamp.bukkit.annotation.CommandPermission permissionAnn = command.getAnnotation(
         com.artillexstudios.axafkzone.libs.lamp.bukkit.annotation.CommandPermission.class
      );
      return permissionAnn == null ? null : new BukkitCommandPermission(new Permission(permissionAnn.value(), permissionAnn.defaultAccess()));
   }
}
