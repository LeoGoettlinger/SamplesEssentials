package de.oliver.cloud.help;

import de.oliver.cloud.CommandManager;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface HelpHandlerFactory<C> {
   @NonNull
   static <C> HelpHandlerFactory<C> standard(@NonNull final CommandManager<C> commandManager) {
      return new StandardHelpHandlerFactory<>(commandManager);
   }

   @NonNull
   HelpHandler<C> createHelpHandler(@NonNull CommandPredicate<C> filter);
}
