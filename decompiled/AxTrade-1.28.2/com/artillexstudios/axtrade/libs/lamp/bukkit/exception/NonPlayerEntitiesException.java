package com.artillexstudios.axtrade.libs.lamp.bukkit.exception;

import com.artillexstudios.axtrade.libs.lamp.exception.ThrowableFromCommand;

@ThrowableFromCommand
public class NonPlayerEntitiesException extends RuntimeException {
   private final String input;

   public String getInput() {
      return this.input;
   }

   public NonPlayerEntitiesException(final String input) {
      this.input = input;
   }
}
