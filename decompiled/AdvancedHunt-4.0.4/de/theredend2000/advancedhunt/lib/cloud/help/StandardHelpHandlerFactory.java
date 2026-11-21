package de.theredend2000.advancedhunt.lib.cloud.help;

import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
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
