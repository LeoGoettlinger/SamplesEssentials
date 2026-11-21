package me.byteful.plugin.leveltools.libs.lamp.core;

import java.util.function.Supplier;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import me.byteful.plugin.leveltools.libs.lamp.process.ResponseHandler;
import org.jetbrains.annotations.NotNull;

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
