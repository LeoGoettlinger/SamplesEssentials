package revxrsal.commands.core;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ResponseHandler;

final class SupplierResponseHandler implements ResponseHandler<Supplier<Object>> {
   private final ResponseHandler<Object> delegate;

   public SupplierResponseHandler(ResponseHandler<Object> delegate) {
      this.delegate = delegate;
   }

   public void handleResponse(Supplier<Object> response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
      try {
         this.delegate.handleResponse(response.get(), actor, command);
      } catch (Throwable var5) {
         command.getCommandHandler().getExceptionHandler().handleException(var5, actor);
      }
   }
}
