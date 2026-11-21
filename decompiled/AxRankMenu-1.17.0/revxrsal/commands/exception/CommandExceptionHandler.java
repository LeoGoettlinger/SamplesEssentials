package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

public interface CommandExceptionHandler {
   void handleException(@NotNull Throwable var1, @NotNull CommandActor var2);
}
