package vrx.economy.vRXClans.libs.cloud.context;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.CommandManager;

@API(
   status = API.Status.INTERNAL,
   consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
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
