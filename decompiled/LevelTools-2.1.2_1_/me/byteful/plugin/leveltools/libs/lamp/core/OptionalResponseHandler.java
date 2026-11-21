package me.byteful.plugin.leveltools.libs.lamp.core;

import java.util.Optional;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import me.byteful.plugin.leveltools.libs.lamp.process.ResponseHandler;
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
