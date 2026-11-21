package de.oliver.cloud.help;

import de.oliver.cloud.CommandManager;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.*"}
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
