package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;

public class InvalidUUIDException extends InvalidValueException {
   public InvalidUUIDException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
