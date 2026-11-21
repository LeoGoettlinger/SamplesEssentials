package revxrsal.commands.core;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ResponseHandler;

final class OptionalResponseHandler implements ResponseHandler<Optional<Object>> {
   private final ResponseHandler<Object> delegate;

   public OptionalResponseHandler(ResponseHandler<Object> delegate) {
      this.delegate = delegate;
   }

   public void handleResponse(Optional<Object> response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
      response.ifPresent(v -> this.delegate.handleResponse(v, actor, command));
   }
}
