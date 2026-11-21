package me.byteful.plugin.leveltools.libs.lamp.process;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;

public interface ResponseHandler<T> {
   void handleResponse(T var1, @NotNull CommandActor var2, @NotNull ExecutableCommand var3);

   static void reply(Object response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
      actor.reply(String.valueOf(response));
   }
}
