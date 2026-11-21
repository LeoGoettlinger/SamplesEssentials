package com.artillexstudios.axafkzone.libs.lamp.bukkit.exception;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import com.artillexstudios.axafkzone.libs.lamp.exception.InvalidValueException;
import org.jetbrains.annotations.NotNull;

public class InvalidWorldException extends InvalidValueException {
   public InvalidWorldException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
