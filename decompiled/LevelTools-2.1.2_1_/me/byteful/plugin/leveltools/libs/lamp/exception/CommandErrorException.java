package me.byteful.plugin.leveltools.libs.lamp.exception;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import org.jetbrains.annotations.NotNull;

public class CommandErrorException extends SendableException {
   private final Object[] arguments;

   public CommandErrorException() {
      this.arguments = new Object[0];
   }

   public CommandErrorException(String message, Object... arguments) {
      super(message);
      this.arguments = arguments;
   }

   @Override
   public void sendTo(@NotNull CommandActor actor) {
      if (!this.getMessage().isEmpty()) {
         actor.errorLocalized(this.getMessage(), this.arguments);
      }
   }
}
