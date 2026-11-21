package me.byteful.plugin.leveltools.libs.lamp.exception;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

public class InvalidNumberException extends InvalidValueException {
   public InvalidNumberException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
