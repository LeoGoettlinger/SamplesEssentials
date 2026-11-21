package vrx.economy.vRXClans.libs.cloud.internal;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.Command;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;

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
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
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
