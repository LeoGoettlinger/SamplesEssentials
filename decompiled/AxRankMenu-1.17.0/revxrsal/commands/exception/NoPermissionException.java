package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.trait.PermissionHolder;

@ThrowableFromCommand
public class NoPermissionException extends RuntimeException {
   @Nullable
   private final PermissionHolder command;
   @NotNull
   private final CommandPermission permission;

   @Nullable
   public PermissionHolder getCommand() {
      return this.command;
   }

   @NotNull
   public CommandPermission getPermission() {
      return this.permission;
   }

   public NoPermissionException(@Nullable PermissionHolder command, @NotNull CommandPermission permission) {
      if (permission == null) {
         throw new NullPointerException("permission is marked non-null but is null");
      } else {
         this.command = command;
         this.permission = permission;
      }
   }
}
