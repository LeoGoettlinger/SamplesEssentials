package de.oliver.cloud.context;

import de.oliver.cloud.CommandManager;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.*"}
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
