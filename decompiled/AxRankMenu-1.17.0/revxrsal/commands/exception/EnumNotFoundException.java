package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;

public class EnumNotFoundException extends InvalidValueException {
   public EnumNotFoundException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
