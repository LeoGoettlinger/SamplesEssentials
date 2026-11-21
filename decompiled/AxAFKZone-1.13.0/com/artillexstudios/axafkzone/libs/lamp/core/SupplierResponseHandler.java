package com.artillexstudios.axafkzone.libs.lamp.core;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;
import com.artillexstudios.axafkzone.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axafkzone.libs.lamp.process.ResponseHandler;
import java.util.function.Supplier;
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
