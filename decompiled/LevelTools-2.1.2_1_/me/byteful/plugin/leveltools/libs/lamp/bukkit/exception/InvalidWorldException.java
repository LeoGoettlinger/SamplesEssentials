package me.byteful.plugin.leveltools.libs.lamp.bukkit.exception;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import me.byteful.plugin.leveltools.libs.lamp.exception.InvalidValueException;
import org.jetbrains.annotations.NotNull;

public class InvalidWorldException extends InvalidValueException {
   public InvalidWorldException(@NotNull CommandParameter parameter, @NotNull String input) {
      super(parameter, input);
   }
}
