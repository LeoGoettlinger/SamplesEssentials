package de.theredend2000.advancedhunt.lib.cloud.context;

import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
public final class StandardCommandContextFactory<C> implements CommandContextFactory<C> {
   private final CommandManager<C> commandManager;

   public StandardCommandContextFactory(@NonNull final CommandManager<C> commandManager) {
      this.commandManager = commandManager;
   }

   @NonNull
   @Override
   public CommandContext<C> create(final boolean suggestions, @NonNull final C sender) {
      return new CommandContext<>(suggestions, sender, this.commandManager);
   }
}
