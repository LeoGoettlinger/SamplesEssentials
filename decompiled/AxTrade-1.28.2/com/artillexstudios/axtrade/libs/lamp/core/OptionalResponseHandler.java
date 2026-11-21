package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axtrade.libs.lamp.process.ResponseHandler;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

final class OptionalResponseHandler implements ResponseHandler<Optional<Object>> {
   private final ResponseHandler<Object> delegate;

   public OptionalResponseHandler(ResponseHandler<Object> delegate) {
      this.delegate = delegate;
   }

   public void handleResponse(Optional<Object> response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
      response.ifPresent(v -> this.delegate.handleResponse(v, actor, command));
   }
}
