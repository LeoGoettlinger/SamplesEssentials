package vrx.economy.vRXClans.libs.cloud.help;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.CommandManager;

@API(
   status = API.Status.STABLE
)
public interface HelpHandlerFactory<C> {
   @NonNull
   static <C> HelpHandlerFactory<C> standard(@NonNull final CommandManager<C> commandManager) {
      return new StandardHelpHandlerFactory<>(commandManager);
   }

   @NonNull
   HelpHandler<C> createHelpHandler(@NonNull CommandPredicate<C> filter);
}
