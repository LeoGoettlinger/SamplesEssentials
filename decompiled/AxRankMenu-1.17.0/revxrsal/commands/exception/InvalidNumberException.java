package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;

public class InvalidNumberException extends InvalidValueException {
   public InvalidNumberException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
