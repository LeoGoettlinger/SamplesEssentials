package me.byteful.plugin.leveltools.libs.lamp.exception;

import me.byteful.plugin.leveltools.libs.lamp.command.ArgumentStack;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;

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
