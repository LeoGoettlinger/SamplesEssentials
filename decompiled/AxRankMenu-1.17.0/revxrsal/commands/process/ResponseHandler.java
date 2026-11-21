package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

public interface ResponseHandler<T> {
   void handleResponse(T var1, @NotNull CommandActor var2, @NotNull ExecutableCommand var3);

   static void reply(Object response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
      actor.reply(String.valueOf(response));
   }
}
