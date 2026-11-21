package com.artillexstudios.axafkzone.libs.lamp.exception;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

public class EnumNotFoundException extends InvalidValueException {
   public EnumNotFoundException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
