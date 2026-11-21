package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.CommandHandler;
import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axtrade.libs.lamp.process.ResponseHandler;
import java.util.concurrent.CompletionStage;
import org.jetbrains.annotations.NotNull;

final class CompletionStageResponseHandler implements ResponseHandler<CompletionStage<Object>> {
   private final CommandHandler handler;
   private final ResponseHandler<Object> delegate;

   public CompletionStageResponseHandler(CommandHandler handler, ResponseHandler<Object> delegate) {
      this.handler = handler;
      this.delegate = delegate;
   }

   public void handleResponse(CompletionStage<Object> response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
      response.whenComplete((value, exception) -> {
         if (exception != null) {
            this.handler.getExceptionHandler().handleException(exception, actor);
         } else {
            try {
               this.delegate.handleResponse(value, actor, command);
            } catch (Throwable var6) {
               this.handler.getExceptionHandler().handleException(var6, actor);
            }
         }
      });
   }
}
