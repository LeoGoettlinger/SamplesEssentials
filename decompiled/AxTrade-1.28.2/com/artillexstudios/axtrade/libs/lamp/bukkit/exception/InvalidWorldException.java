package com.artillexstudios.axtrade.libs.lamp.bukkit.exception;

import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.exception.InvalidValueException;
import org.jetbrains.annotations.NotNull;

public class InvalidWorldException extends InvalidValueException {
   public InvalidWorldException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
