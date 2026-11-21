package me.byteful.plugin.leveltools.libs.lamp.bukkit;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandPermission;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

public final class BukkitCommandPermission implements CommandPermission {
   @NotNull
   private final Permission permission;

   @Override
   public boolean canExecute(@NotNull CommandActor actor) {
      return ((BukkitCommandActor)actor).getSender().hasPermission(this.permission);
   }

   @NotNull
   public Permission getPermission() {
      return this.permission;
   }

   @Override
   public String toString() {
      return "BukkitCommandPermission(permission=" + this.getPermission() + ")";
   }

   public BukkitCommandPermission(@NotNull final Permission permission) {
      if (permission == null) {
         throw new NullPointerException("permission is marked non-null but is null");
      } else {
         this.permission = permission;
      }
   }
}
