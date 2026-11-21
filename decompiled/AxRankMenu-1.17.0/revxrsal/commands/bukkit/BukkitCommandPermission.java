package revxrsal.commands.bukkit;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;

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
