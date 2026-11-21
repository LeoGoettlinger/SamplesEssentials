package com.artillexstudios.axtrade.libs.lamp.bukkit.exception;

import com.artillexstudios.axtrade.libs.lamp.exception.ThrowableFromCommand;

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
