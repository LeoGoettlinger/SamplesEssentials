package revxrsal.commands.bukkit.exception;

import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.ThrowableFromCommand;

@ThrowableFromCommand
public class MalformedEntitySelectorException extends RuntimeException {
   private final CommandActor actor;
   private final String input;
   private final String errorMessage;

   public CommandActor getActor() {
      return this.actor;
   }

   public String getInput() {
      return this.input;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public MalformedEntitySelectorException(final CommandActor actor, final String input, final String errorMessage) {
      this.actor = actor;
      this.input = input;
      this.errorMessage = errorMessage;
   }
}
