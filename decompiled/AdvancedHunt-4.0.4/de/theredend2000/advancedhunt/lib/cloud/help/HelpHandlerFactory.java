package de.theredend2000.advancedhunt.lib.cloud.help;

import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

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
