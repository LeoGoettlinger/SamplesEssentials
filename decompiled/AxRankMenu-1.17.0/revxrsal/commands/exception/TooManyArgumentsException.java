package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.ExecutableCommand;

@ThrowableFromCommand
public class TooManyArgumentsException extends RuntimeException {
   @NotNull
   private final ExecutableCommand command;
   @NotNull
   private final ArgumentStack arguments;

   @NotNull
   public ExecutableCommand getCommand() {
      return this.command;
   }

   @NotNull
   public ArgumentStack getArguments() {
      return this.arguments;
   }

   public TooManyArgumentsException(@NotNull ExecutableCommand command, @NotNull ArgumentStack arguments) {
      if (command == null) {
         throw new NullPointerException("command is marked non-null but is null");
      } else if (arguments == null) {
         throw new NullPointerException("arguments is marked non-null but is null");
      } else {
         this.command = command;
         this.arguments = arguments;
      }
   }
}
