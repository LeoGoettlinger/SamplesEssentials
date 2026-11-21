package com.artillexstudios.axtrade.libs.lamp.exception;

import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import org.jetbrains.annotations.NotNull;

@ThrowableFromCommand
public abstract class SendableException extends RuntimeException {
   public SendableException() {
      this("");
   }

   public SendableException(String message) {
      super(message);
   }

   public abstract void sendTo(@NotNull CommandActor var1);
}
