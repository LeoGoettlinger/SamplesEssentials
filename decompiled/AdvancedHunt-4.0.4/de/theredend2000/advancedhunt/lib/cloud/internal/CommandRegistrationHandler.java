package de.theredend2000.advancedhunt.lib.cloud.internal;

import de.theredend2000.advancedhunt.lib.cloud.Command;
import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface CommandRegistrationHandler<C> {
   @NonNull
   static <C> CommandRegistrationHandler<C> nullCommandRegistrationHandler() {
      return new CommandRegistrationHandler.NullCommandRegistrationHandler<>();
   }

   boolean registerCommand(@NonNull Command<C> command);

   @API(
      status = API.Status.STABLE
   )
   default void unregisterRootCommand(@NonNull final CommandComponent<C> rootCommand) {
   }

   @API(
      status = API.Status.INTERNAL,
      consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
   )
   public static final class NullCommandRegistrationHandler<C> implements CommandRegistrationHandler<C> {
      private NullCommandRegistrationHandler() {
      }

      @Override
      public boolean registerCommand(@NonNull final Command<C> command) {
         return true;
      }

      @Override
      public void unregisterRootCommand(@NonNull final CommandComponent<C> rootCommand) {
      }
   }
}
