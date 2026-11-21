package vrx.economy.vRXClans.libs.cloud.help;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.CommandManager;

@API(
   status = API.Status.INTERNAL,
   consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
)
final class StandardHelpHandlerFactory<C> implements HelpHandlerFactory<C> {
   private final CommandManager<C> commandManager;

   StandardHelpHandlerFactory(@NonNull final CommandManager<C> commandManager) {
      this.commandManager = commandManager;
   }

   @NonNull
   @Override
   public HelpHandler<C> createHelpHandler(@NonNull final CommandPredicate<C> filter) {
      return new StandardHelpHandler<>(this.commandManager, filter);
   }
}
