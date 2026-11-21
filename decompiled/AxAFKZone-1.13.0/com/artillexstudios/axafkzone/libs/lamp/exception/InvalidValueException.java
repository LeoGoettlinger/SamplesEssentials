package com.artillexstudios.axafkzone.libs.lamp.exception;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

@ThrowableFromCommand
public abstract class InvalidValueException extends RuntimeException {
   @NotNull
   private final CommandParameter parameter;
   @NotNull
   private final String input;

   @NotNull
   public CommandParameter getParameter() {
      return this.parameter;
   }

   @NotNull
   public String getInput() {
      return this.input;
   }

   public InvalidValueException(@NotNull CommandParameter parameter, @NotNull String input) {
      if (parameter == null) {
         throw new NullPointerException("parameter is marked non-null but is null");
      } else if (input == null) {
         throw new NullPointerException("input is marked non-null but is null");
      } else {
         this.parameter = parameter;
         this.input = input;
      }
   }
}
