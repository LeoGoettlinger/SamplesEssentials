package revxrsal.commands.bukkit.exception;

import revxrsal.commands.exception.ThrowableFromCommand;

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
