package com.artillexstudios.axafkzone.libs.lamp.bukkit.exception;

import com.artillexstudios.axafkzone.libs.lamp.exception.ThrowableFromCommand;

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
