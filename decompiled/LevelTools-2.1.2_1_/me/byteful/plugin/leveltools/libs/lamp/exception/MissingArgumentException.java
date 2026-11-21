package me.byteful.plugin.leveltools.libs.lamp.exception;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;

@ThrowableFromCommand
public class MissingArgumentException extends RuntimeException {
   @NotNull
   private final CommandParameter parameter;

   @NotNull
   public ExecutableCommand getCommand() {
      return this.parameter.getDeclaringCommand();
   }

   @NotNull
   public CommandParameter getParameter() {
      return this.parameter;
   }

   public MissingArgumentException(@NotNull CommandParameter parameter) {
      if (parameter == null) {
         throw new NullPointerException("parameter is marked non-null but is null");
      } else {
         this.parameter = parameter;
      }
   }
}
