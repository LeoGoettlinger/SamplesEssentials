package revxrsal.commands.command.trait;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.exception.NoPermissionException;

public interface PermissionHolder {
   @NotNull
   CommandPermission getPermission();

   default boolean hasPermission(@NotNull CommandActor actor) {
      return this.getPermission().canExecute(actor);
   }

   default void checkPermission(@NotNull CommandActor actor) {
      if (!this.getPermission().canExecute(actor)) {
         throw new NoPermissionException(this, this.getPermission());
      }
   }
}
