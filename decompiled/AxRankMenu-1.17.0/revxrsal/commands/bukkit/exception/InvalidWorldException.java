package revxrsal.commands.bukkit.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

public class InvalidWorldException extends InvalidValueException {
   public InvalidWorldException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
