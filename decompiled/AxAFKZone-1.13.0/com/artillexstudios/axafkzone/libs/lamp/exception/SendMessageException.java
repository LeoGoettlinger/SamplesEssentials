package com.artillexstudios.axafkzone.libs.lamp.exception;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;
import org.jetbrains.annotations.NotNull;

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
