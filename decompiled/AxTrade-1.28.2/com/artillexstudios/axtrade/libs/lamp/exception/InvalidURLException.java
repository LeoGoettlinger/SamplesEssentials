package com.artillexstudios.axtrade.libs.lamp.exception;

import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

public class InvalidURLException extends InvalidValueException {
   public InvalidURLException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
