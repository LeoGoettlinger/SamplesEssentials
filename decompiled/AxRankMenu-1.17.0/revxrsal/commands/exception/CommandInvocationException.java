package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ExecutableCommand;

public class CommandInvocationException extends RuntimeException {
   @NotNull
   private final ExecutableCommand command;
   @NotNull
   private final Throwable cause;

   @NotNull
   public ExecutableCommand getCommand() {
      return this.command;
   }

   @NotNull
   @Override
   public Throwable getCause() {
      return this.cause;
   }

   public CommandInvocationException(@NotNull ExecutableCommand command, @NotNull Throwable cause) {
      if (command == null) {
         throw new NullPointerException("command is marked non-null but is null");
      } else if (cause == null) {
         throw new NullPointerException("cause is marked non-null but is null");
      } else {
         this.command = command;
         this.cause = cause;
      }
   }
}
