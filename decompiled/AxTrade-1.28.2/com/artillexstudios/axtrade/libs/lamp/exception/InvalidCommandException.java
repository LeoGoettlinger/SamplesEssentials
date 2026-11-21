package com.artillexstudios.axtrade.libs.lamp.exception;

import com.artillexstudios.axtrade.libs.lamp.core.CommandPath;
import org.jetbrains.annotations.NotNull;

@ThrowableFromCommand
public class InvalidCommandException extends RuntimeException {
   @NotNull
   private final CommandPath path;
   @NotNull
   private final String input;

   @NotNull
   public CommandPath getPath() {
      return this.path;
   }

   @NotNull
   public String getInput() {
      return this.input;
   }

   public InvalidCommandException(@NotNull CommandPath path, @NotNull String input) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (input == null) {
         throw new NullPointerException("input is marked non-null but is null");
      } else {
         this.path = path;
         this.input = input;
      }
   }
}
