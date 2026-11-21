package com.artillexstudios.axtrade.libs.lamp.command.trait;

import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.CommandPermission;
import com.artillexstudios.axtrade.libs.lamp.exception.NoPermissionException;
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
