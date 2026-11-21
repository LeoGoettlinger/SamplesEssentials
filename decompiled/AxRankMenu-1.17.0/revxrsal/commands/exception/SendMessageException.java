package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

public class SendMessageException extends SendableException {
   private final Object[] arguments;

   public SendMessageException() {
      this.arguments = new Object[0];
   }

   public SendMessageException(String message, Object... arguments) {
      super(message);
      this.arguments = arguments;
   }

   @Override
   public void sendTo(@NotNull CommandActor actor) {
      if (!this.getMessage().isEmpty()) {
         actor.replyLocalized(this.getMessage(), this.arguments);
      }
   }
}
