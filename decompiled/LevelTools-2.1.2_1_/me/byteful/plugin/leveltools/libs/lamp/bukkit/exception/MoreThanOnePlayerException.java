package me.byteful.plugin.leveltools.libs.lamp.bukkit.exception;

import me.byteful.plugin.leveltools.libs.lamp.exception.ThrowableFromCommand;

@ThrowableFromCommand
public class MoreThanOnePlayerException extends RuntimeException {
   private final String input;

   public String getInput() {
      return this.input;
   }

   public MoreThanOnePlayerException(final String input) {
      this.input = input;
   }
}
