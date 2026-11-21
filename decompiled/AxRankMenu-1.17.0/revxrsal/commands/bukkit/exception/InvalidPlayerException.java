package revxrsal.commands.bukkit.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

public class InvalidPlayerException extends InvalidValueException {
   public InvalidPlayerException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
