package com.artillexstudios.axtrade.libs.lamp.bukkit.exception;

import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.exception.InvalidValueException;
import org.jetbrains.annotations.NotNull;

public class InvalidPlayerException extends InvalidValueException {
   public InvalidPlayerException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
