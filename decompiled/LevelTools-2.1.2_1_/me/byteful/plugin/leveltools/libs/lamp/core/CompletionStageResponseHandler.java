package me.byteful.plugin.leveltools.libs.lamp.core;

import java.util.concurrent.CompletionStage;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import me.byteful.plugin.leveltools.libs.lamp.process.ResponseHandler;
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
