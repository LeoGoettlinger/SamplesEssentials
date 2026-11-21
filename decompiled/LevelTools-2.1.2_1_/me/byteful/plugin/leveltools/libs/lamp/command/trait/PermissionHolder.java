package me.byteful.plugin.leveltools.libs.lamp.command.trait;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandPermission;
import me.byteful.plugin.leveltools.libs.lamp.exception.NoPermissionException;
import org.jetbrains.annotations.NotNull;

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
