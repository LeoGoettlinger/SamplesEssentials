package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;

public class InvalidURLException extends InvalidValueException {
   public InvalidURLException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
