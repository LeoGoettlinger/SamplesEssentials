package com.artillexstudios.axtrade.libs.lamp.exception;

import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

public class InvalidNumberException extends InvalidValueException {
   public InvalidNumberException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
