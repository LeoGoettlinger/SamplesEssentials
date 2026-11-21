package de.theredend2000.advancedhunt.lib.cloud;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContextFactory;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import de.theredend2000.advancedhunt.lib.cloud.exception.handling.ExceptionController;
import de.theredend2000.advancedhunt.lib.cloud.execution.CommandExecutor;
import de.theredend2000.advancedhunt.lib.cloud.execution.CommandResult;
import de.theredend2000.advancedhunt.lib.cloud.execution.ExecutionCoordinator;
import de.theredend2000.advancedhunt.lib.cloud.services.State;
import de.theredend2000.advancedhunt.lib.cloud.util.CompletableFutures;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

final class StandardCommandExecutor<C> implements CommandExecutor<C> {
   private final CommandManager<C> commandManager;
   private final ExecutionCoordinator<C> executionCoordinator;
   private final CommandContextFactory<C> commandContextFactory;

   StandardCommandExecutor(
      @NonNull final CommandManager<C> commandManager,
      @NonNull final ExecutionCoordinator<C> executionCoordinator,
      @NonNull final CommandContextFactory<C> commandContextFactory
   ) {
      this.commandManager = commandManager;
      this.executionCoordinator = executionCoordinator;
      this.commandContextFactory = commandContextFactory;
   }

   @NonNull
   @Override
   public CompletableFuture<CommandResult<C>> executeCommand(
      @NonNull final C commandSender, @NonNull final String input, @NonNull final Consumer<CommandContext<C>> contextConsumer
   ) {
      CommandContext<C> context = this.commandContextFactory.create(false, commandSender);
      contextConsumer.accept(context);
      CommandInput commandInput = CommandInput.of(input);
      return this.executeCommand(context, commandInput).whenComplete((result, throwable) -> {
         if (throwable != null) {
            try {
               this.commandManager.exceptionController().handleException(context, ExceptionController.unwrapCompletionException(throwable));
            } catch (RuntimeException var5x) {
               throw var5x;
            } catch (Throwable var6) {
               throw new CompletionException(var6);
            }
         }
      });
   }

   @NonNull
   private CompletableFuture<CommandResult<C>> executeCommand(@NonNull final CommandContext<C> context, @NonNull final CommandInput commandInput) {
      context.store("__raw_input__", commandInput.copy());

      try {
         if (this.commandManager.preprocessContext(context, commandInput) == State.ACCEPTED) {
            return this.executionCoordinator().coordinateExecution(this.commandManager.commandTree(), context, commandInput);
         }
      } catch (Exception var4) {
         return CompletableFutures.failedFuture(var4);
      }

      return CompletableFuture.completedFuture(null);
   }

   @NonNull
   @Override
   public ExecutionCoordinator<C> executionCoordinator() {
      return this.executionCoordinator;
   }
}
