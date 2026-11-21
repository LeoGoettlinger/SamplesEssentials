package de.theredend2000.advancedhunt.lib.cloud.execution;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface CommandExecutionHandler<C> {
   @API(
      status = API.Status.STABLE
   )
   @NonNull
   static <C> CommandExecutionHandler<C> noOpCommandExecutionHandler() {
      return (CommandExecutionHandler<C>)NullCommandExecutionHandler.INSTANCE;
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   static <C> CommandExecutionHandler<C> delegatingExecutionHandler(final List<CommandExecutionHandler<C>> handlers) {
      return new MulticastDelegateFutureCommandExecutionHandler<>(handlers);
   }

   void execute(@NonNull CommandContext<C> commandContext);

   @API(
      status = API.Status.STABLE
   )
   default CompletableFuture<Void> executeFuture(@NonNull CommandContext<C> commandContext) {
      CompletableFuture<Void> future = new CompletableFuture<>();

      try {
         this.execute(commandContext);
         future.complete(null);
      } catch (Throwable var4) {
         future.completeExceptionally(var4);
      }

      return future;
   }

   @FunctionalInterface
   @API(
      status = API.Status.STABLE
   )
   public interface FutureCommandExecutionHandler<C> extends CommandExecutionHandler<C> {
      @Override
      default void execute(@NonNull CommandContext<C> commandContext) {
         throw new UnsupportedOperationException("execute should not be called on FutureCommandExecutionHandlers, call executeFuture instead.");
      }

      @Override
      CompletableFuture<Void> executeFuture(@NonNull CommandContext<C> commandContext);
   }
}
