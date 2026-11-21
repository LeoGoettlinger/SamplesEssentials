package me.byteful.plugin.leveltools.libs.lamp.exception;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

public class InvalidUUIDException extends InvalidValueException {
   public InvalidUUIDException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
